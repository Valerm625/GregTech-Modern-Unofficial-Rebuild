# mui-patch

Build-time post-processing for the ModularUI jar embedded through `jarJar`.

`PatchModularUI` adds the missing `ModConfigSpec.isLoaded()` guard to
`brachy.modularui.ModularUIConfig#enableTestOverlays()` and
`brachy.modularui.test.TestHandler#onOpenScreen(...)`. Without it, a Minecraft
1.21.1 client crashes while initializing the title screen with
`IllegalStateException: Cannot get config value before config is loaded.`

The Gradle wiring lives in `gradle/scripts/mui-patch.gradle`; the rationale,
the resulting bytecode and the maintenance rules are documented in
[`docs/mui-test-overlay-patch.md`](../../../docs/mui-test-overlay-patch.md).

Standalone usage (any jar that embeds `META-INF/jarjar/modularui-*.jar`):

```text
java -cp asm-9.9.1.jar:asm-tree-9.9.1.jar:classes \
     com.gregtechceu.buildtools.muipatch.PatchModularUI patch <jar> [--output <jar>] \
     [--version-suffix gtpatch | --no-version-bump]

java -cp ... com.gregtechceu.buildtools.muipatch.PatchModularUI verify <jar>
```

Both commands are idempotent and fail closed when the expected classes or
methods are missing.
