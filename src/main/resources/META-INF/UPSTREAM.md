# Upstream provenance

This project is an unofficial modified distribution of
[GregTech CEu: Modern](https://github.com/GregTechCEu/GregTech-Modern/).

## Fork baseline

| Field | Value |
| --- | --- |
| Upstream repository | `https://github.com/GregTechCEu/GregTech-Modern.git` |
| Upstream branch | `1.21` |
| Base revision | `8201bf3847792e40b0dd75a6952e76c4c72e3cbf` |
| Fork release | `8.0.1-atm10.5` |
| Minecraft target | `1.21.1` |
| Loader | NeoForge |
| Preserved mod ID | `gtceu` |

The base revision is recorded in the release JAR manifest. Each later release
must update this document, the manifest value, and the changelog together.

## Relationship to upstream

The GregTechCEu Team remains the author and maintainer of the upstream project.
ATM10 maintenance-fork contributors are responsible only for the changes made
after the base revision. The fork name, version suffix, metadata, and release
notes must always make this distinction visible.

This fork's public source repository is
<https://github.com/Valerm625/GregTech-Modern-Unofficial-Rebuild>. The URL is exposed in
the fork's Gradle metadata and is the support channel for fork-specific issues.
The upstream URL remains a provenance link, not a support channel for this
fork. Fork-specific defects must not be filed in the upstream issue tracker
unless an independent reproducer proves the defect also exists in an unmodified
upstream build.

## Compatibility identity

The mod ID remains `gtceu` solely for save, addon, recipe, and script
compatibility. Consequently, the fork and an official GTCEu JAR are mutually
exclusive at runtime. A published binary must clearly state that it replaces,
not supplements, the official JAR.

The Gradle `maven_group` is deliberately changed to
`dev.atm10maintenance.gtceu`; fork binaries must never be uploaded under the
upstream Maven namespace without explicit authorization.

Inherited upstream publication workflows have been replaced with one fork-owned
build workflow. Push, pull-request, and manual runs compile and test the source
tree only; they do not upload artifacts, create releases, or use repository
secrets. Remote Maven upload is absent by default and requires an explicit
opt-in plus a fork-owned repository URL. The fork does not carry the upstream
CurseForge or Modrinth project IDs. A binary release remains gated on the
resource-rights review, corresponding-source checks, and real client/GameTest
validation described in `docs/RECOVERY_STATUS.md`.

## Updating the fork

For each upstream refresh:

1. Fetch the upstream `1.21` branch without rewriting the published fork
   history.
2. Record the new base revision before applying maintenance changes.
3. Reapply GT-owned fixes directly to GT source and keep optional third-party
   integrations behind explicit adapters and dependency checks.
4. Reject copied ATM9 All Rights Reserved scripts, text, textures, recipes, and
   world-generation assets unless written redistribution permission is present.
5. Build from a clean checkout and test both with and without optional mods.
6. Update `CHANGELOG-ATM10.md`, `THIRD_PARTY_NOTICES.md`, the JAR manifest, and
   this file.
7. Publish the complete corresponding source and reproducible build files with
   the binary.

## License continuity

GTCEu-derived code and modifications remain under LGPL-3.0. Upstream copyright,
license, authorship, and asset credits are preserved. Files with a more specific
license retain that license, including the CTM, CodeChickenLib, and AE2-derived
files enumerated in `THIRD_PARTY_NOTICES.md`, CC0 documentation, and the SIL
Open Font License notice stored beside the bitmap font resources.
