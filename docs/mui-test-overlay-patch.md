# ModularUI test-overlay patch

The packaged jar embeds ModularUI through the `jarJar` configuration. ModularUI 3.3.1 ships a
development test handler that reads its own config before NeoForge has loaded it, which crashes a
client on Minecraft 1.21.1 / NeoForge 21.1.x. Because that code is inside the embedded dependency
and not in this repository, the build post-processes the packaged jar and adds the missing guard.

## Symptom

`All the Mods 10 8.3` (NeoForge 21.1.259, 594 mods) dies at the title screen:

```text
Description: Initializing game

java.lang.IllegalStateException: Cannot get config value before config is loaded.
    at net.neoforged.neoforge.common.ModConfigSpec$ConfigValue.getRaw(ModConfigSpec.java:1235)
    at net.neoforged.neoforge.common.ModConfigSpec$BooleanValue.getAsBoolean(ModConfigSpec.java:1291)
    at brachy.modularui.ModularUIConfig.enableTestOverlays(ModularUIConfig.java:164)
    at brachy.modularui.test.TestHandler.onOpenScreen(TestHandler.java:138)
    at brachy.modularui.overlay.OverlayStack.onOpenScreen(OverlayStack.java:128)
    at brachy.modularui.screen.ClientScreenHandler.onGuiChanged(ClientScreenHandler.java:302)
    at net.minecraft.client.Minecraft.setScreen(Minecraft.java:1039)
    at net.minecraft.client.Minecraft.<init>(Minecraft.java:615)
```

The server-side or GameTest runs never hit this path, so only a client launch exposes it.

## Root cause

| Observation | Evidence |
| --- | --- |
| `TestHandler` is subscribed in **production** builds too | `TestHandler.java:47` carries `@EventBusSubscriber(modid = ModularUI.MOD_ID, value = Dist.CLIENT)` with no `isDev()` gate. Compare `ModularUI.java:68`, where `TestRegistration` *is* gated behind `ModularUI.isDev()`. |
| The handler reads a config value first | `TestHandler.java:138` `if (ModularUIConfig.enableTestOverlays())`, and `ModularUIConfig.java:163` returns `ENABLE_TEST_OVERLAYS.get()`. |
| That config is a `CLIENT` config registered during mod construction | `ModularUI.java:61` `modContainer.registerConfig(ModConfig.Type.CLIENT, ModularUIConfig.CONFIG, ModularUI.MOD_ID + ".toml")`. |
| On NeoForge 21.1.x the first screen opens before that spec is loaded | The failing run's `debug.log` only logs `Config file modularui.toml for modularui tracking`; there is no `Loaded TOML config file ... modularui.toml` and no `config/modularui.toml` is ever created, while configs registered around the same moment do load (for example `tombstone-client.toml`, registered 11 ms later, is created and loaded). On the same mod set under NeoForge 21.1.248 the spec is loaded first (`Loaded TOML config file ...\config\modularui.toml`) and the client starts. |

An unloaded `ModConfigSpec` value throws on purpose, so the handler cannot run that early.

Two consequences follow:

- Upgrading to a ModularUI *release* does not help. `brachy.modularui.test` is part of the main
  source set, so every artifact of the 3.3.1 line ships the same unconditional subscriber.
- Deleting `brachy/modularui/test/**` does not help either: `ModularUI`, `ModularUIClient`,
  `InventoryTypes` and `ModularUIEmiPlugin` reference those classes, so a missing class would
  surface as `NoClassDefFoundError`. The patch therefore edits bytes instead of removing code.

## The patch

`tools/mui-patch/src/main/java/com/gregtechceu/buildtools/muipatch/PatchModularUI.java` inserts the
missing guard at the entry of both access paths, so the behaviour is unchanged once the spec is
loaded (the shipped default of `enableTestOverlays` is `false` anyway):

```text
ModularUIConfig.enableTestOverlays()Z
   0: getstatic     ModularUIConfig.CONFIG:ModConfigSpec
   3: invokevirtual ModConfigSpec.isLoaded:()Z
   6: ifne          11          // loaded: run the original body
   9: iconst_0
  10: ireturn                   // not loaded: keep the documented default
  11: getstatic     ENABLE_TEST_OVERLAYS
  14: invokevirtual getAsBoolean:()Z

TestHandler.onOpenScreen(OpenScreenEvent)V
   0: getstatic     ModularUIConfig.CONFIG:ModConfigSpec
   3: invokevirtual ModConfigSpec.isLoaded:()Z
   6: ifne          10
   9: return
  10: <original body>
```

Both files are bytecode-edited with ASM. The inserted branch target uses a `same` stack map frame,
which is exactly the method's initial frame, so the JVM verifier accepts the result; every other
entry of the embedded ModularUI jar stays byte-identical.

## Build integration

`gradle/scripts/mui-patch.gradle` (applied from `build.gradle`) adds:

| Task | Purpose |
| --- | --- |
| `compileMuiPatchTool` | Compiles the tool with the project's Java 21 toolchain. ASM is resolved into the isolated `muiPatchTool` configuration and never reaches the mod. |
| `patchEmbeddedModularUI` | Runs after `jar` (which packages the `jarJar` output) and rewrites that jar in place. |
| `verifyEmbeddedModularUI` | Fails unless both guards are present and the jar-in-jar metadata matches the embedded ModularUI version. |

`assemble` and every `AbstractPublishToMaven` task depend on `patchEmbeddedModularUI`, so a shipped
artifact cannot silently lose the patch. CI runs `patchEmbeddedModularUI verifyEmbeddedModularUI`
explicitly.

Properties:

| Property | Default | Meaning |
| --- | --- | --- |
| `muiPatchSuffix` | `gtpatch` | Appended to the pinned ModularUI version inside the jar, e.g. `3.3.1-SNAPSHOT` -> `3.3.1-SNAPSHOT-gtpatch`. Pass `--no-version-bump` (tool flag) or keep the default to stay discoverable in `latest.log`. |

The patched jar also carries `META-INF/mui-patch.properties`, which records the patch id, the pinned
version, the pristine upstream SHA-256 and the patched SHA-256 of the embedded jar:

```text
patch=mui-test-overlays-config-guard
embeddedJar=META-INF/jarjar/modularui-mc1.21.1-3.3.1-SNAPSHOT-gtpatch.jar
modularui.baseVersion=3.3.1-SNAPSHOT
modularui.version=3.3.1-SNAPSHOT-gtpatch
modularui.sourceSha256=5eddfe0eedb04e3356438245f7b6684c15686684cabd4286c12d9e7e516dbf14
modularui.patchedSha256=156c2832e835433bc77f329313598c385cb219deda380415672394b6a4610e7b
```

The step is idempotent: re-running it on an already patched jar detects both guards, keeps the
recorded base version and rewrites no bytecode. Because the patch rewrites the output of `jar`,
that task is reported out of date on every build; this is expected and keeps the artifact
deterministic.

The renamed artifact version stays inside the jar-in-jar range from `metadata.json`
(`[3.3.1-SNAPSHOT,)`, verified with Maven's own `VersionRange`) and no other mod declares a
dependency on `modularui`, so renaming cannot break dependency resolution.

## Verifying a produced jar

```text
./gradlew --no-daemon verifyEmbeddedModularUI

# or against an arbitrary artifact
java -cp <asm-9.9.1.jar>:<asm-tree-9.9.1.jar>:build/mui-patch/classes \
     com.gregtechceu.buildtools.muipatch.PatchModularUI verify build/libs/gtceu-1.21.1-*.jar
```

The command fails closed: a missing class, a missing method, a changed method signature or
inconsistent jar-in-jar metadata all abort the build instead of shipping a jar that still crashes.

## Maintenance

- **Bumping ModularUI**: nothing to do. The tool patches whatever jar `jarJar` embeds and fails if
  the target methods disappear.
- **Upstream fixes the crash**: remove `apply from: ... mui-patch.gradle` from `build.gradle`,
  delete `tools/mui-patch` and `gradle/scripts/mui-patch.gradle`, and drop this document. Run
  `verifyEmbeddedModularUI` first: if it passes on an unpatched jar, the guard is already upstream.
- **Client smoke test**: `config/modularui.toml` must appear after the first client launch and the
  log must contain `Loaded TOML config file ...modularui.toml`.
