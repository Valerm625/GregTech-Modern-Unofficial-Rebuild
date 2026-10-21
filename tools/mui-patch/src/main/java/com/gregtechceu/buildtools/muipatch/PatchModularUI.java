package com.gregtechceu.buildtools.muipatch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Post-processes a packaged GregTech jar so that the ModularUI build embedded in
 * {@code META-INF/jarjar/} cannot read its own configuration before NeoForge has loaded it.
 *
 * <p>ModularUI ships {@code brachy.modularui.test.TestHandler} in <em>every</em> artifact of the
 * 3.3.1 line (the class carries an unconditional {@code @EventBusSubscriber}; only
 * {@code TestRegistration} is gated behind {@code ModularUI.isDev()}). Its
 * {@code onOpenScreen} handler begins with {@code ModularUIConfig.enableTestOverlays()}, i.e. with
 * a {@code ModConfigSpec} read. On Minecraft 1.21.1 / NeoForge 21.1.x the client opens the title
 * screen before {@code config/modularui.toml} has been loaded, so that read throws
 * {@code IllegalStateException: Cannot get config value before config is loaded.} and the game
 * dies during "Initializing game".
 *
 * <p>This tool inserts the missing guard at the entry of both access paths. Once the spec is
 * loaded the original behaviour is restored, and the shipped default of the option is
 * {@code false} anyway:
 *
 * <pre>
 *   ModularUIConfig.enableTestOverlays()Z      -&gt; false while the spec is unloaded
 *   TestHandler.onOpenScreen(OpenScreenEvent)V -&gt; returns while the spec is unloaded
 * </pre>
 *
 * <p>CLI:
 * <pre>
 *   PatchModularUI patch &lt;jar&gt; [--output &lt;jar&gt;] [--version-suffix &lt;suffix&gt; | --no-version-bump]
 *   PatchModularUI verify &lt;jar&gt;
 * </pre>
 *
 * <p>Both commands are idempotent and safe to run on an already patched jar.
 */
public final class PatchModularUI {

    /** Identifier of this patch, recorded in {@link #MARKER_ENTRY}. */
    public static final String PATCH_ID = "mui-test-overlays-config-guard";
    /** Marker resource written into the packaged jar. */
    public static final String MARKER_ENTRY = "META-INF/mui-patch.properties";

    private static final String DEFAULT_VERSION_SUFFIX = "gtpatch";
    private static final String JARJAR_PREFIX = "META-INF/jarjar/";
    private static final String JARJAR_METADATA = JARJAR_PREFIX + "metadata.json";
    private static final String MUI_MODS_TOML = "META-INF/neoforge.mods.toml";
    private static final String MUI_MANIFEST = "META-INF/MANIFEST.MF";

    private static final String CONFIG_OWNER = "brachy/modularui/ModularUIConfig";
    private static final String CONFIG_CLASS = CONFIG_OWNER + ".class";
    private static final String CONFIG_FIELD = "CONFIG";
    private static final String CONFIG_METHOD = "enableTestOverlays";
    private static final String SPEC_OWNER = "net/neoforged/neoforge/common/ModConfigSpec";
    private static final String SPEC_TYPE = "L" + SPEC_OWNER + ";";

    private static final String TEST_HANDLER_OWNER = "brachy/modularui/test/TestHandler";
    private static final String TEST_HANDLER_CLASS = TEST_HANDLER_OWNER + ".class";
    private static final String TEST_HANDLER_METHOD = "onOpenScreen";
    private static final String TEST_HANDLER_DESC = "(Lbrachy/modularui/screen/event/OpenScreenEvent;)V";

    private static final String MOD_ENTRY_CLASS = "brachy/modularui/ModularUI.class";
    private static final String CONFIG_TYPE_OWNER = "net/neoforged/fml/config/ModConfig$Type";
    private static final String CONFIG_TYPE_NAME = "CLIENT";
    private static final String CONFIG_TYPE_REPLACEMENT = "STARTUP";
    private static final String REGISTER_CONFIG_DESC =
            "(Lnet/neoforged/fml/config/ModConfig$Type;Lnet/neoforged/fml/config/IConfigSpec;Ljava/lang/String;)V";

    private static final Pattern MUI_JAR = Pattern.compile("^" + Pattern.quote(JARJAR_PREFIX)
            + "(modularui-mc[^/]*?)-([^/]+)\\.jar$");
    private static final Pattern TOML_VERSION = Pattern.compile("(?m)^(\\s*version\\s*=\\s*\")([^\"]*)(\")");
    private static final Pattern ARTIFACT_VERSION =
            Pattern.compile("\"artifactVersion\"\\s*:\\s*\"([^\"]*)\"");

    private PatchModularUI() {}

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (Exception e) {
            System.err.println("[mui-patch] FAILED: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int run(String[] args) throws IOException {
        if (args.length == 0) {
            usage();
            return 2;
        }
        return switch (args[0]) {
            case "patch" -> patch(args);
            case "verify" -> verify(args);
            case "-h", "--help", "help" -> {
                usage();
                yield 0;
            }
            default -> {
                usage();
                yield 2;
            }
        };
    }

    private static void usage() {
        System.out.println("""
                PatchModularUI - patches the ModularUI jar embedded in a GregTech jar.

                  patch <jar> [--output <jar>] [--version-suffix <suffix>]
                              [--no-version-bump]
                      Rewrites <jar> in place, or writes a patched copy with --output.
                      The embedded ModularUI version becomes "<pinned>-<suffix>"
                      (default suffix: gtpatch) unless --no-version-bump is used.

                  verify <jar>
                      Exits non-zero unless both guards are present and the jar-in-jar
                      metadata matches the embedded ModularUI version.
                """);
    }

    // ------------------------------------------------------------------ patch

    private static int patch(String[] args) throws IOException {
        Path input = null;
        Path output = null;
        String suffix = DEFAULT_VERSION_SUFFIX;
        boolean bump = true;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--output" -> output = Paths.get(args[++i]);
                case "--version-suffix" -> suffix = args[++i];
                case "--no-version-bump" -> bump = false;
                default -> input = Paths.get(args[i]);
            }
        }
        if (input == null || !Files.isRegularFile(input)) {
            usage();
            return 2;
        }
        if (output == null) {
            output = input;
        }
        String log = patchJar(input.toAbsolutePath(), output.toAbsolutePath(), bump ? suffix : null);
        System.out.print(log);
        return 0;
    }

    private static int verify(String[] args) throws IOException {
        Path jar = args.length > 1 ? Paths.get(args[1]) : null;
        if (jar == null || !Files.isRegularFile(jar)) {
            usage();
            return 2;
        }
        List<String> problems = new ArrayList<>();
        Map<String, byte[]> outer = readZip(jar);
        String nestedName = findEmbeddedModularUI(outer);
        if (nestedName == null) {
            System.err.println("[mui-patch] verify FAILED: no modularui jar under " + JARJAR_PREFIX);
            return 1;
        }
        Map<String, byte[]> nested = readZip(nestedName, outer.get(nestedName));
        if (!hasConfigGuards(nested)) {
            problems.add("ModularUI still reads its config without an isLoaded() guard");
        }
        if (!isConfigPromoted(nested)) {
            problems.add("ModularUI still registers a CLIENT config instead of a STARTUP config");
        }
        String nestedVersion = readVersion(nested);
        String metadataVersion = readMetadataVersion(outer.get(JARJAR_METADATA), nestedName);
        if (nestedVersion != null && metadataVersion != null && !nestedVersion.equals(metadataVersion)) {
            problems.add("jar-in-jar metadata version " + metadataVersion
                    + " does not match embedded ModularUI version " + nestedVersion);
        }
        if (!outer.containsKey(MARKER_ENTRY)) {
            problems.add(MARKER_ENTRY + " is missing");
        }
        if (problems.isEmpty()) {
            System.out.println("[mui-patch] verify OK: " + jar.getFileName()
                    + " (embedded " + nestedName + ", version " + nestedVersion + ")");
            return 0;
        }
        for (String problem : problems) {
            System.err.println("[mui-patch] verify FAILED: " + problem);
        }
        return 1;
    }

    private static String patchJar(Path input, Path output, String versionSuffix) throws IOException {
        StringBuilder log = new StringBuilder();
        Map<String, byte[]> outer = readZip(input);
        String nestedName = findEmbeddedModularUI(outer);
        if (nestedName == null) {
            throw new IOException("no modularui jar under " + JARJAR_PREFIX + " in " + input.getFileName());
        }
        Map<String, byte[]> nested = readZip(nestedName, outer.get(nestedName));
        byte[] existingMarker = outer.get(MARKER_ENTRY);
        String originalVersion = readVersion(nested);
        String baseVersion = readMarkerValue(existingMarker, "modularui.baseVersion");
        if (baseVersion == null) {
            baseVersion = originalVersion;
        }
        byte[] originalNestedBytes = writeZip(nested);
        log.append("[mui-patch] embedded ").append(nestedName)
                .append(" version=").append(originalVersion)
                .append(" sha256=").append(sha256(originalNestedBytes)).append('\n');

        Map<String, byte[]> patched = new LinkedHashMap<>(nested);
        if (hasConfigGuards(nested)) {
            log.append("[mui-patch] config-loaded guards already present\n");
        } else {
            patched.put(CONFIG_CLASS, guard(requireClass(nested, CONFIG_CLASS), CONFIG_METHOD, "()Z"));
            patched.put(TEST_HANDLER_CLASS,
                    guard(requireClass(nested, TEST_HANDLER_CLASS), TEST_HANDLER_METHOD, TEST_HANDLER_DESC));
            log.append("[mui-patch] inserted config-loaded guard in ").append(CONFIG_CLASS).append(" and ")
                    .append(TEST_HANDLER_CLASS).append('\n');
        }
        if (!isConfigPromoted(nested)) {
            // Configs of type CLIENT are loaded only after the client already opened a screen on some
            // NeoForge 21.1.x builds, which is exactly the ordering ModularUI cannot survive. STARTUP
            // configs are loaded while the mod loading phase is still finishing, so every config read
            // the client performs afterwards sees loaded values.
            patched.put(MOD_ENTRY_CLASS, promoteConfigToStartup(requireClass(nested, MOD_ENTRY_CLASS)));
            log.append("[mui-patch] ModularUI config type CLIENT -> STARTUP\n");
        }

        String newVersion = originalVersion;
        String newName = nestedName;
        if (versionSuffix != null && !versionSuffix.isBlank() && baseVersion != null) {
            newVersion = withSuffix(baseVersion, versionSuffix);
            if (!newVersion.equals(originalVersion)) {
                rewriteVersionIdentifiers(patched, originalVersion, newVersion);
                newName = JARJAR_PREFIX + stripVersionSuffix(stripJarSuffix(nestedName).substring(JARJAR_PREFIX.length()),
                        originalVersion) + "-" + newVersion + ".jar";
                log.append("[mui-patch] embedded ModularUI version ").append(originalVersion)
                        .append(" -> ").append(newVersion).append('\n');
            }
        }

        byte[] patchedNestedBytes = writeZip(patched);
        // Keep the hash of the pristine upstream artifact across repeated runs.
        String sourceHash = readMarkerValue(outer.get(MARKER_ENTRY), "modularui.sourceSha256");
        if (sourceHash == null) {
            sourceHash = sha256(originalNestedBytes);
        }
        byte[] marker = String.join("\n",
                "# Generated by GTCEu's ModularUI post-processing step; do not edit.",
                "patch=" + PATCH_ID,
                "embeddedJar=" + newName,
                "modularui.baseVersion=" + baseVersion,
                "modularui.version=" + newVersion,
                "modularui.configType=" + CONFIG_TYPE_REPLACEMENT,
                "modularui.sourceSha256=" + sourceHash,
                "modularui.patchedSha256=" + sha256(patchedNestedBytes),
                "").getBytes(StandardCharsets.UTF_8);

        Map<String, byte[]> rewritten = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : outer.entrySet()) {
            String name = entry.getKey();
            if (name.equals(nestedName)) {
                rewritten.put(newName, patchedNestedBytes);
            } else if (name.equals(JARJAR_METADATA)) {
                rewritten.put(name, rewriteMetadata(entry.getValue(), nestedName, newName, newVersion, log));
            } else {
                rewritten.put(name, entry.getValue());
            }
        }
        rewritten.put(MARKER_ENTRY, marker);

        Path temp = Files.createTempFile(output.getParent(), "mui-patch-", ".jar");
        try {
            writeZip(temp, rewritten);
            Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
        log.append("[mui-patch] wrote ").append(output).append('\n');
        return log.toString();
    }

    private static byte[] requireClass(Map<String, byte[]> nested, String name) throws IOException {
        byte[] data = nested.get(name);
        if (data == null) {
            throw new IOException("missing " + name + " in the embedded ModularUI jar");
        }
        return data;
    }

    /** Prepends {@code if (!ModularUIConfig.CONFIG.isLoaded()) return <default>;}. */
    private static byte[] guard(byte[] classBytes, String methodName, String methodDesc) {
        ClassNode classNode = new ClassNode();
        new ClassReader(classBytes).accept(classNode, 0);
        MethodNode target = findMethod(classNode, methodName, methodDesc);
        if (target == null) {
            throw new IllegalStateException("missing method " + classNode.name + "#" + methodName + methodDesc);
        }
        if ((target.access & Opcodes.ACC_STATIC) == 0) {
            throw new IllegalStateException("expected a static method: " + classNode.name + "#" + methodName);
        }
        boolean returnsBoolean = methodDesc.endsWith(")Z");
        if (!returnsBoolean && !methodDesc.endsWith(")V")) {
            throw new IllegalStateException("unsupported return type " + methodDesc);
        }

        LabelNode body = new LabelNode();
        InsnList bytecode = new InsnList();
        bytecode.add(new FieldInsnNode(Opcodes.GETSTATIC, CONFIG_OWNER, CONFIG_FIELD, SPEC_TYPE));
        bytecode.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, SPEC_OWNER, "isLoaded", "()Z", false));
        bytecode.add(new JumpInsnNode(Opcodes.IFNE, body));
        if (returnsBoolean) {
            bytecode.add(new InsnNode(Opcodes.ICONST_0));
            bytecode.add(new InsnNode(Opcodes.IRETURN));
        } else {
            bytecode.add(new InsnNode(Opcodes.RETURN));
        }
        bytecode.add(body);
        // The jump target starts in the method's initial frame: arguments in the locals,
        // nothing on the operand stack.
        bytecode.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        target.instructions.insert(bytecode);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }
        return null;
    }

    private static boolean hasConfigGuards(Map<String, byte[]> nested) {
        return missingGuard(nested.get(CONFIG_CLASS), CONFIG_METHOD, "()Z").isEmpty()
                && missingGuard(nested.get(TEST_HANDLER_CLASS), TEST_HANDLER_METHOD, TEST_HANDLER_DESC).isEmpty();
    }

    /** @return whether the ModularUI entrypoint already registers a STARTUP config. */
    private static boolean isConfigPromoted(Map<String, byte[]> nested) {
        byte[] classBytes = nested.get(MOD_ENTRY_CLASS);
        if (classBytes == null) {
            return false;
        }
        ClassNode classNode = new ClassNode();
        new ClassReader(classBytes).accept(classNode, 0);
        return configTypeAccesses(classNode).stream()
                .allMatch(field -> CONFIG_TYPE_REPLACEMENT.equals(field.name));
    }

    /**
     * Rewrites {@code ModConfig.Type.CLIENT} to {@code ModConfig.Type.STARTUP} in the constructor that
     * registers ModularUI's own config, so it is loaded before the client opens its first screen.
     */
    private static byte[] promoteConfigToStartup(byte[] classBytes) {
        ClassNode classNode = new ClassNode();
        new ClassReader(classBytes).accept(classNode, 0);
        List<FieldInsnNode> accesses = configTypeAccesses(classNode);
        if (accesses.isEmpty()) {
            throw new IllegalStateException("no ModConfig.Type access found in " + classNode.name);
        }
        for (FieldInsnNode access : accesses) {
            access.name = CONFIG_TYPE_REPLACEMENT;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static List<FieldInsnNode> configTypeAccesses(ClassNode classNode) {
        List<FieldInsnNode> accesses = new ArrayList<>();
        for (MethodNode method : classNode.methods) {
            boolean registersConfig = false;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && "registerConfig".equals(call.name)
                        && REGISTER_CONFIG_DESC.equals(call.desc)) {
                    registersConfig = true;
                }
            }
            if (!registersConfig) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field && CONFIG_TYPE_OWNER.equals(field.owner)
                        && (CONFIG_TYPE_NAME.equals(field.name) || CONFIG_TYPE_REPLACEMENT.equals(field.name))) {
                    accesses.add(field);
                }
            }
        }
        return accesses;
    }

    private static List<String> missingGuard(byte[] classBytes, String methodName, String methodDesc) {
        List<String> problems = new ArrayList<>();
        if (classBytes == null) {
            problems.add("missing " + methodName + " owner class");
            return problems;
        }
        ClassNode classNode = new ClassNode();
        new ClassReader(classBytes).accept(classNode, 0);
        MethodNode method = findMethod(classNode, methodName, methodDesc);
        if (method == null) {
            problems.add("missing method " + classNode.name + "#" + methodName + methodDesc);
            return problems;
        }
        List<AbstractInsnNode> real = new ArrayList<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null && real.size() < 4;
                insn = insn.getNext()) {
            if (insn.getOpcode() >= 0) {
                real.add(insn);
            }
        }
        if (real.size() < 3
                || real.get(0).getOpcode() != Opcodes.GETSTATIC
                || !(real.get(0) instanceof FieldInsnNode field
                        && CONFIG_OWNER.equals(field.owner) && CONFIG_FIELD.equals(field.name))
                || real.get(1).getOpcode() != Opcodes.INVOKEVIRTUAL
                || !(real.get(1) instanceof MethodInsnNode call && "isLoaded".equals(call.name))
                || real.get(2).getOpcode() != Opcodes.IFNE) {
            problems.add("method " + classNode.name + "#" + methodName + " has no config-loaded guard");
        }
        return problems;
    }

    // ------------------------------------------------------------- jar helpers

    private static String findEmbeddedModularUI(Map<String, byte[]> outer) {
        for (String name : outer.keySet()) {
            if (MUI_JAR.matcher(name).matches()) {
                return name;
            }
        }
        return null;
    }

    /** Reads a {@code key=value} line from the marker properties, or {@code null}. */
    private static String readMarkerValue(byte[] marker, String key) {
        if (marker == null) {
            return null;
        }
        for (String line : new String(marker, StandardCharsets.UTF_8).split("\\R")) {
            int separator = line.indexOf('=');
            if (separator > 0 && line.substring(0, separator).trim().equals(key)) {
                return line.substring(separator + 1).trim();
            }
        }
        return null;
    }

    private static Map<String, byte[]> readZip(Path path) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                entries.put(entry.getName(), readAll(zip.getInputStream(entry)));
            }
        }
        return entries;
    }

    private static Map<String, byte[]> readZip(String ignoredName, byte[] bytes) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // The ZipInputStream must stay open across entries, so read without closing it.
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.max(entry.getSize() > 0
                        ? (int) Math.min(entry.getSize(), 1 << 24)
                        : 1 << 14, 1 << 10));
                byte[] chunk = new byte[1 << 14];
                int read;
                while ((read = zis.read(chunk)) > 0) {
                    buffer.write(chunk, 0, read);
                }
                entries.put(entry.getName(), buffer.toByteArray());
                zis.closeEntry();
            }
        }
        return entries;
    }

    private static byte[] writeZip(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1 << 21);
        try (ZipOutputStream zos = new ZipOutputStream(buffer)) {
            zos.setLevel(6);
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                // Fixed timestamp keeps the rebuilt archive reproducible.
                zipEntry.setTime(1L);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
        return buffer.toByteArray();
    }

    private static void writeZip(Path path, Map<String, byte[]> entries) throws IOException {
        try (OutputStream out = Files.newOutputStream(path);
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {
            zos.setLevel(9);
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setTime(1L);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
    }

    private static byte[] readAll(InputStream stream) throws IOException {
        try (stream) {
            return stream.readAllBytes();
        }
    }

    // -------------------------------------------------------------- versions

    private static String readVersion(Map<String, byte[]> nested) {
        byte[] toml = nested.get(MUI_MODS_TOML);
        if (toml == null) {
            return null;
        }
        Matcher matcher = TOML_VERSION.matcher(new String(toml, StandardCharsets.UTF_8));
        return matcher.find() ? matcher.group(2) : null;
    }

    private static String readMetadataVersion(byte[] metadata, String nestedName) {
        if (metadata == null) {
            return null;
        }
        String json = new String(metadata, StandardCharsets.UTF_8);
        int pathIndex = json.indexOf('"' + nestedName + '"');
        if (pathIndex < 0) {
            return null;
        }
        int start = json.lastIndexOf("\"identifier\"", pathIndex);
        Matcher matcher = ARTIFACT_VERSION.matcher(json);
        matcher.region(Math.max(start, 0), pathIndex);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static void rewriteVersionIdentifiers(Map<String, byte[]> nested, String oldVersion, String newVersion) {
        byte[] toml = nested.get(MUI_MODS_TOML);
        if (toml != null) {
            String text = new String(toml, StandardCharsets.UTF_8);
            String updated = TOML_VERSION.matcher(text)
                    .replaceFirst("$1" + Matcher.quoteReplacement(newVersion) + "$3");
            if (!updated.equals(text)) {
                nested.put(MUI_MODS_TOML, updated.getBytes(StandardCharsets.UTF_8));
            }
        }
        byte[] manifest = nested.get(MUI_MANIFEST);
        if (manifest != null) {
            String text = new String(manifest, StandardCharsets.UTF_8);
            String updated = text.replace("Implementation-Version: " + oldVersion,
                    "Implementation-Version: " + newVersion);
            if (!updated.equals(text)) {
                nested.put(MUI_MANIFEST, updated.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static byte[] rewriteMetadata(byte[] metadata, String oldName, String newName, String newVersion,
            StringBuilder log) {
        if (metadata == null) {
            return null;
        }
        String json = new String(metadata, StandardCharsets.UTF_8);
        if (oldName.equals(newName)) {
            return metadata;
        }
        String updated = json.replace('"' + oldName + '"', '"' + newName + '"');
        int pathIndex = json.indexOf('"' + oldName + '"');
        if (pathIndex >= 0) {
            int start = json.lastIndexOf("\"identifier\"", pathIndex);
            Matcher matcher = ARTIFACT_VERSION.matcher(json);
            matcher.region(Math.max(start, 0), pathIndex);
            if (matcher.find()) {
                updated = updated.substring(0, matcher.start(1)) + newVersion + updated.substring(matcher.end(1));
            }
        }
        log.append("[mui-patch] jar-in-jar metadata updated: ").append(newName)
                .append(" artifactVersion=").append(newVersion).append('\n');
        return updated.getBytes(StandardCharsets.UTF_8);
    }

    private static String withSuffix(String version, String suffix) {
        String normalized = suffix.startsWith("-") ? suffix.substring(1) : suffix;
        return version.endsWith("-" + normalized) ? version : version + "-" + normalized;
    }

    /** Strips {@code -<version>} from a jar base name, tolerating a previous patch suffix. */
    private static String stripVersionSuffix(String baseName, String version) {
        if (version == null || baseName == null) {
            return baseName;
        }
        int marker = baseName.lastIndexOf('-' + version);
        if (marker > 0) {
            return baseName.substring(0, marker);
        }
        return baseName;
    }

    private static String stripJarSuffix(String name) {
        return name.endsWith(".jar") ? name.substring(0, name.length() - 4) : name;
    }

    private static String sha256(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                builder.append(Character.forDigit((value >> 4) & 0xF, 16));
                builder.append(Character.forDigit(value & 0xF, 16));
            }
            return builder.toString();
        } catch (Exception e) {
            return "unavailable";
        }
    }
}
