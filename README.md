# 格雷科技现代版（非官方重制版）

Unofficial, community-maintained GregTech Modern source for Minecraft 1.21.1
and NeoForge. This project preserves the `gtceu` mod id so existing worlds and
modpacks can recognize it.

This is a recovered maintenance tree, not an official GregTech CEu release
and not a claim that the upstream project reviewed or endorsed these changes.
The source combines the pinned public NeoForge baseline with a manifest-tracked
recovery layer. Some files are reconstructed from verified artifacts; review
the provenance records before relying on a particular implementation.

## Compatibility

- Minecraft: `1.21.1`
- Loader: NeoForge `21.1.x` (compiled against `21.1.234`)
- Fork version: `8.0.1-unofficial`
- Mod id: `gtceu`
- Upstream baseline: [`8201bf3`](https://github.com/GregTechCEu/GregTech-Modern/commit/8201bf3847792e40b0dd75a6952e76c4c72e3cbf)
- Recovered fork revision: `6dd4c6f490c7799620cd7e07c49c8cb368e0ddf9`

Install this build in place of the matching GTCEu JAR. Do not install two
providers of the `gtceu` mod id at the same time.

## Build

Use JDK 21. The build intentionally disables automatic JDK downloads.

```text
./gradlew --no-daemon clean compileJava compileTestJava jar sourcesJar test
```

On Windows, use `gradlew.bat` with the same task list. The build verifies the
pinned EMI artifact when it is resolved; CI supplies that artifact explicitly
and checks its size and SHA-256 before compiling.

CI keeps ModularUI pinned to `3.3.1-20260718.084355-4`. Its Maven snapshot
index is checked into `.github/dependencies/modularui-maven-metadata.xml`,
because the remote `maven-metadata.xml` advances when upstream publishes newer
snapshots. CI verifies the local index and all five downloaded, timestamped
artifacts before Gradle uses the temporary Maven repository. Do not refresh the
index to the upstream latest build without auditing the dependency change.
Run `python3 tools/test_pinned_modularui.py` to check this provisioning step
without downloading dependencies (Python 3 and Bash are required).

The same temporary repository mirrors the existing Iris and ModernFix versions
from their pinned Modrinth CDN URLs to avoid hosted-runner artifact resolution failures
through Modrinth Maven. `.github/dependencies/modrinth-artifacts.json` records the
exact versions, original POM bytes, sizes and hashes; this does not upgrade or
remove either integration. `python3 tools/test_pinned_modrinth.py` exercises
successful provisioning and fail-closed handling of corrupt or missing files.

The ordinary Gradle `test` task currently compiles the test source set and may
discover no JUnit suites. NeoForge GameTest fixtures are separate and require
an explicitly provisioned game-test run; a successful compile is not evidence
that every fixture has executed in a game.

The isolated GameTest server has been executed from `run/gametest` with the
recovered GTCEu tree and its pinned development dependencies. The current
verification run completed all `160/160` required GameTests and shut down
normally. This validates the server-side fixtures only; it does not replace a
user-side ATM10 client or save migration test.

## Provenance and maintenance

- [`SOURCE_PROVENANCE.md`](SOURCE_PROVENANCE.md) records the pinned upstream
  snapshot.
- [`FORK_PROVENANCE.properties`](FORK_PROVENANCE.properties) records the
  recovered fork revision and dirty state.
- [`SOURCE_LINEAGE.csv`](SOURCE_LINEAGE.csv) maps target classes to baseline and
  recovery sources and their hashes.
- [`SOURCE_ASSEMBLY.md`](SOURCE_ASSEMBLY.md),
  [`SOURCE_ASSEMBLY_PLAN.csv`](SOURCE_ASSEMBLY_PLAN.csv), and
  [`SOURCE_BUILD_PLAN.csv`](SOURCE_BUILD_PLAN.csv) describe the deterministic
  assembly that produced this tree.
- [`docs/RECOVERY_STATUS.md`](docs/RECOVERY_STATUS.md) lists known review
  boundaries and remaining evidence work.

Keep upstream-only fixes in the baseline lineage, keep recovered ATM10 changes
traceable in the lineage manifest, and preserve all file-level notices. Do not
commit local modpack directories, generated JARs, dependency caches, or
credentials.

## Licensing

The core remains under the file-specific licenses carried by GTCEu and its
adapted components. The repository-level notice is in [`NOTICE.md`](NOTICE.md);
the complete license texts and third-party notices are retained under
`LICENSE`, `license_bundle/`, and `src/main/resources/META-INF/`.

## Credited Works

The upstream asset credits are retained here because several bundled textures
originated outside the GTCEu source tree:

- Most textures are originally from [Gregtech: Refreshed](https://modrinth.com/resourcepack/gregtech-refreshed) by @ULSTICK, with consistency edits and additions by @Ghostipedia.
- Some textures are originally from the [ZedTech GTCEu Resourcepack](https://github.com/brachy84/zedtech-ceu), with community changes.
- New material item textures are by @TTFTCUTS and @Rosethorns.
- Wooden Forms, World Accelerators, and the Extreme Combustion Engine are from the [GregTech: New Horizons Modpack](https://www.curseforge.com/minecraft/modpacks/gt-new-horizons).
- Primitive Water Pump is from the [IMPACT: GREGTECH EDITION Modpack](https://gt-impact.github.io/#/).
- Ender Fluid Link Cover, Auto-Maintenance Hatch, Optical Fiber, and Data Bank textures are from [TecTech](https://github.com/Technus/TecTech).
- Steam Grinder is from [GregTech++](https://www.curseforge.com/minecraft/mc-mods/gregtech-gt-gtplusplus).
- Certificate of Not Being a Noob Anymore is from [Crops++](https://www.curseforge.com/minecraft/mc-mods/berries).

These credits do not add a license or permission beyond the terms supplied by
the original authors. See [`THIRD_PARTY_NOTICES.md`](src/main/resources/META-INF/THIRD_PARTY_NOTICES.md)
and [`docs/ATM9_MIGRATION_EXCLUSIONS.md`](docs/ATM9_MIGRATION_EXCLUSIONS.md)
for the separate review boundary covering excluded ATM9 migration material.

The project is unofficial. GregTech, GTCEu, Minecraft, NeoForge, and related
names and marks remain with their respective owners. Nothing here implies
sponsorship, endorsement, or support by an upstream maintainer.

The embedded ModularUI dependency is resolved from the `3.3.1-SNAPSHOT`
coordinate but is accepted only when it matches the timestamped binary and
corresponding-source hashes recorded in the third-party notice. CI supplies
those exact artifacts through a temporary Maven repository so a future
snapshot replacement cannot silently change the build.

## AI-assisted recovery

The recovery layer includes an AI-use disclosure in
`src/main/resources/META-INF/AI_DISCLOSURE.md`. All changes require human
review before release or downstream redistribution; see [`AI_POLICY.md`](AI_POLICY.md).

## Support

Use the issue tracker on this repository for fork-specific problems. Include
the fork version, Minecraft/NeoForge versions, the complete log, and the
relevant provenance hashes when reporting a reproducible issue.
