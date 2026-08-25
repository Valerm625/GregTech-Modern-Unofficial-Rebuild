package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.compat.atm10.Atm10Environment;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;

final class EmbeddedAtm10KubeJs {
   private static final String RESOURCE_ROOT = "gtceu_atm10_embedded/kubejs/";
   private static final String PACK_PREFIX = "gtceu_atm10_embedded_";
   private static final Set<String> COMPLETE_GREGSTAR_RECIPE_MODS = Set.of(
      "allthecompressed",
      "allthemodium",
      "allthetweaks",
      "appmek",
      "computercraft",
      "connectedglass",
      "enderio",
      "extendedae",
      "industrialforegoing",
      "ironfurnaces",
      "mekanism",
      "mysticalagradditions"
   );
   private static final Map<String, Set<String>> SCRIPT_REQUIRED_MODS = Map.ofEntries(
      Map.entry("codex_gregstar_audit.js", COMPLETE_GREGSTAR_RECIPE_MODS),
      Map.entry("codex_gregstar_recipes.js", COMPLETE_GREGSTAR_RECIPE_MODS),
      Map.entry("codex_gtceu_recipe_gap_recovery.js", Set.of("allthecompressed", "allthetweaks")),
      Map.entry("codex_gtceu_crossmod_compat.js", Set.of("allthemodium")),
      Map.entry("codex_gtceu_cobaltite_oxygen.js", Set.of("mekanism")),
      Map.entry("codex_occultism_dimensional_mineshaft.js", Set.of("allthemodium", "occultism", "occultism_kubejs")),
      Map.entry("codex_gregstar_infinity_cells.js", Set.of("appmek", "extendedae", "mekanism")),
      Map.entry("codex_gregstar_machines.js", Set.of("allthecompressed", "connectedglass"))
   );

   private EmbeddedAtm10KubeJs() {
   }

   static void load(ScriptManager manager) {
      if (isAtm10Environment() && !GTCEu.isDataGen()) {
         ScriptType type = manager.scriptType;
         String scriptDirectory = type.name;
         Path target = GTCEu.GTCEU_FOLDER.resolve("embedded-atm10").resolve(safeVersion()).resolve("gtceu_atm10_embedded_" + scriptDirectory);

         try {
            deleteTree(target);
            Files.createDirectories(target);
            int copied = copyResources(scriptDirectory, target);
            if (copied == 0) {
               GTCEu.LOGGER.error("No embedded ATM10 KubeJS scripts were discoverable for {}; GregStar content will not be registered", scriptDirectory);
               return;
            }

            manager.loadPackFromDirectory(target, scriptDirectory + "/", true);
            GTCEu.LOGGER.info("Loaded {} embedded ATM10 KubeJS scripts for {}", copied, scriptDirectory);
         } catch (Exception var5) {
            GTCEu.LOGGER.error("Failed to load embedded ATM10 KubeJS scripts for {}", scriptDirectory, var5);
         }
      }
   }

   private static boolean isAtm10Environment() {
      return GTCEu.Mods.isKubeJSLoaded() && Atm10Environment.isDetected();
   }

   private static String safeVersion() {
      String version = "unknown";

      try {
         version = ModList.get().getModContainerById("gtceu").map(container -> container.getModInfo().getVersion().toString()).orElse(version);
      } catch (RuntimeException var2) {
      }

      return version.replaceAll("[^A-Za-z0-9._-]", "_");
   }

   private static int copyResources(String scriptDirectory, Path target) throws IOException {
      String resourceDirectory = resourceDirectoryName(scriptDirectory);
      String prefix = "gtceu_atm10_embedded/kubejs/" + resourceDirectory + "/";
      Set<String> copied = new HashSet<>();
      ClassLoader loader = EmbeddedAtm10KubeJs.class.getClassLoader();
      Enumeration<URL> roots = loader.getResources(prefix);

      while (roots.hasMoreElements()) {
         URL root = roots.nextElement();
         if ("file".equals(root.getProtocol())) {
            Path source = Path.of(URI.create(root.toString()));
            copyDirectory(source, source, target, copied);
         } else if ("jar".equals(root.getProtocol())) {
            JarURLConnection connection = (JarURLConnection)root.openConnection();

            try (JarFile jar = connection.getJarFile()) {
               copyJarEntries(jar, prefix, target, copied);
            }
         }
      }

      if (copied.isEmpty()) {
         IModFileInfo modFileInfo = ModList.get().getModFileById("gtceu");
         if (modFileInfo != null) {
            Path resourcePath = modFileInfo.getFile().findResource(new String[]{"gtceu_atm10_embedded", "kubejs", resourceDirectory});
            if (Files.isDirectory(resourcePath)) {
               copyDirectory(resourcePath, resourcePath, target, copied);
            }

            if (copied.isEmpty()) {
               try {
                  Path modPath = modFileInfo.getFile().getFilePath();
                  if (modPath != null && Files.isRegularFile(modPath)) {
                     try (JarFile jar = new JarFile(modPath.toFile())) {
                        copyJarEntries(jar, prefix, target, copied);
                     }
                  } else if (modPath != null && Files.isDirectory(modPath)) {
                     Path resourceRoot = modPath.resolve(prefix);
                     if (Files.isDirectory(resourceRoot)) {
                        copyDirectory(resourceRoot, resourceRoot, target, copied);
                     }
                  }
               } catch (RuntimeException var19) {
                  GTCEu.LOGGER.debug("Unable to inspect the owning ATM10 mod file for embedded KubeJS", var19);
               }
            }
         }
      }

      if (copied.isEmpty()) {
         URL codeSource = EmbeddedAtm10KubeJs.class.getProtectionDomain().getCodeSource().getLocation();
         if ("file".equals(codeSource.getProtocol())) {
            Path source = Path.of(URI.create(codeSource.toString()));
            if (Files.isRegularFile(source)) {
               try (JarFile jar = new JarFile(source.toFile())) {
                  copyJarEntries(jar, prefix, target, copied);
               }
            } else {
               Path resourceRoot = source.resolve(prefix);
               if (Files.isDirectory(resourceRoot)) {
                  copyDirectory(resourceRoot, resourceRoot, target, copied);
               }
            }
         }
      }

      return copied.size();
   }

   static String resourceDirectoryName(String scriptDirectory) {
      switch (scriptDirectory) {
         case "startup":
         case "server":
         case "client":
            return scriptDirectory + "_scripts";
         default:
            throw new IllegalArgumentException("Unknown KubeJS script type: " + scriptDirectory);
      }
   }

   private static void copyDirectory(Path root, Path current, Path target, Set<String> copied) throws IOException {
      try (Stream<Path> stream = Files.walk(current)) {
         for (Path path : stream.filter(x$0 -> Files.isRegularFile(x$0)).toList()) {
            String relative = root.relativize(path).toString().replace('\\', '/');
            if ((relative.endsWith(".js") || relative.endsWith(".ts"))
               && isSafeRelativePath(relative)
               && hasRequiredModsForScript(relative, GTCEu::isModLoaded)) {
               Path destination = target.resolve(relative);
               Files.createDirectories(destination.getParent());
               Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
               copied.add(relative);
            }
         }
      }
   }

   private static void copyJarEntries(JarFile jar, String prefix, Path target, Set<String> copied) throws IOException {
      Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements()) {
         JarEntry entry = entries.nextElement();
         if (!entry.isDirectory() && entry.getName().startsWith(prefix)) {
            String relative = entry.getName().substring(prefix.length());
            if ((relative.endsWith(".js") || relative.endsWith(".ts"))
               && isSafeRelativePath(relative)
               && hasRequiredModsForScript(relative, GTCEu::isModLoaded)) {
               Path destination = target.resolve(relative);
               Files.createDirectories(destination.getParent());

               try (InputStream input = jar.getInputStream(entry)) {
                  Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
               }

               copied.add(relative);
            }
         }
      }
   }

   private static void deleteTree(Path target) throws IOException {
      if (Files.exists(target)) {
         try (Stream<Path> stream = Files.walk(target)) {
            for (Path path : stream.sorted((left, right) -> right.getNameCount() - left.getNameCount()).toList()) {
               Files.deleteIfExists(path);
            }
         }
      }
   }

   private static boolean isSafeRelativePath(String relative) {
      Path path = Path.of(relative);
      return !path.isAbsolute() && path.normalize().equals(path) && !relative.startsWith("../") && !relative.contains("/../");
   }

   static Set<String> requiredModsForScript(String relative) {
      int slash = Math.max(relative.lastIndexOf(47), relative.lastIndexOf(92));
      String fileName = slash >= 0 ? relative.substring(slash + 1) : relative;
      return SCRIPT_REQUIRED_MODS.getOrDefault(fileName, Set.of());
   }

   static boolean hasRequiredModsForScript(String relative, Predicate<String> isLoaded) {
      return requiredModsForScript(relative).stream().allMatch(isLoaded);
   }
}
