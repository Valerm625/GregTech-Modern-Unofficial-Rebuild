# Third-party notices

This notice covers the source tree and the components embedded by the release
build. It supplements, and does not replace, the license text stored in
`LICENSE` and license files shipped beside individual resources.

## GregTech CEu: Modern

- Project: <https://github.com/GregTechCEu/GregTech-Modern/>
- Upstream base: `8201bf3847792e40b0dd75a6952e76c4c72e3cbf`
- License: GNU Lesser General Public License v3.0
- License text: `LICENSE`, also packaged as
  `META-INF/LICENSE_GTCEU_LGPL-3.0.txt`

This fork is a modified version. See `UPSTREAM.md` and `CHANGELOG-ATM10.md` for
provenance and a description of modifications.

## License-text bundle

The complete source archive contains `third_party_licenses/`. The binary and
sources JARs contain the same directory under
`META-INF/third_party_licenses/`. It includes:

- GNU GPL v2 for files offered under GPL-2.0-or-later;
- GNU LGPL v2.1 for files offered under LGPL-2.1-or-later;
- GNU LGPL v2.1 for the external Dragonborn Lib compatibility target;
- GNU GPL v3 and GNU LGPL v3 for LGPL-3.0-only works;
- Mozilla Public License 2.0 for Registrate;
- the MIT notice for Configuration; and
- Apache License 2.0 for EvalEx.

The license bundle ZIP additionally copies `docs/LICENSE` as
`Documentation-CC0-1.0.txt` and the font license as
`Font-OFL-1.1.txt`. The font text is also packaged in the main JAR beside the
font resources and under `META-INF/third_party_licenses/`.

Each file retains its own license. The repository-level `LICENSE` does not
override the file-specific terms below, and the presence of an embedded
library does not relicense that library.

## Source-derived components retained from upstream GTCEu

### ConnectedTexturesMod

- Project: <https://github.com/Chisel-Team/ConnectedTexturesMod/>
- Exact source anchor:
  <https://github.com/Chisel-Team/ConnectedTexturesMod/tree/19a58b080ff2d4fec4fd44ffdb426fc078ce853d>
- License: GPL-2.0-or-later
- License text:
  `third_party_licenses/ConnectedTexturesMod-GPL-2.0-or-later.txt`

The following adapted files retain their ConnectedTexturesMod notices:

- `src/main/java/com/gregtechceu/gtceu/client/model/ctm/ConnectionCheck.java`
- `src/main/java/com/gregtechceu/gtceu/client/model/ctm/ISubmap.java`
- `src/main/java/com/gregtechceu/gtceu/client/model/ctm/OctagonalOrientation.java`
- `src/main/java/com/gregtechceu/gtceu/client/model/ctm/Submap.java`
- `src/main/java/com/gregtechceu/gtceu/client/model/ctm/TextureConnections.java`

Each file identifies its corresponding upstream path in Javadoc.

### CodeChickenLib

- Project: <https://github.com/TheCBProject/CodeChickenLib/>
- Exact source anchor:
  <https://github.com/TheCBProject/CodeChickenLib/tree/3b43c7fe055aaae997038910c78f4761fdee90cc>
- Copyright: Copyright (c) 2018 covers1624
- License: LGPL-2.1-or-later
- License text:
  `third_party_licenses/CodeChickenLib-LGPL-2.1-or-later.txt`

Adapted files:

- `src/main/java/com/gregtechceu/gtceu/client/util/quad/transformers/InterpolationHelper.java`
  from `src/main/java/codechicken/lib/math/InterpHelper.java`
- `src/main/java/com/gregtechceu/gtceu/client/util/quad/transformers/QuadCornerKicker.java`
- `src/main/java/com/gregtechceu/gtceu/client/util/quad/transformers/QuadReInterpolator.java`
- `src/main/java/com/gregtechceu/gtceu/client/util/quad/transformers/QuadTinter.java`

The final three files correspond to the same names under
`src/main/java/codechicken/lib/model/pipeline/transformers/`.

### Applied Energistics 2

- Project: <https://github.com/AppliedEnergistics/Applied-Energistics-2/>
- Exact source anchor:
  <https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/374469e6c9ea42c8e2ee56a97d9cc26f9fed35c6>
- Copyright: (c) 2013 - 2020 AlgorithmX2 et al
- License: LGPL-3.0-only
- License texts: `third_party_licenses/LGPL-3.0-only.txt` and
  `third_party_licenses/GPL-3.0-only.txt`

Adapted files:

- `src/main/java/com/gregtechceu/gtceu/client/util/ExtendedBlockModelRotation.java`
  from `src/main/java/appeng/api/orientation/BlockOrientation.java`
- `src/main/java/com/gregtechceu/gtceu/client/util/VariantRotationHelpers.java`
  from `src/main/java/appeng/hooks/BlockstateDefinitionHook.java`

## Libraries embedded by the inherited Jar-in-Jar build

| Component | Build version | Project | License | Binary SHA-256 |
| --- | --- | --- | --- | --- |
| Registrate | `MC1.21-1.3.0+67` | <https://github.com/tterrag1098/Registrate> | MPL-2.0 | `510f4041c41739f1d8ea8850ab8364d3e3a5fada8529beed5d9f479e2523db52` |
| Configuration | `3.1.1-neoforge` | <https://github.com/Toma1O6/Configuration> | MIT | `907d031a236b51bc2d790d6cafd857c1ff148320b5ea855f8e711f9b67c125c8` |
| ModularUI | `3.3.1-SNAPSHOT` (resolved as `3.3.1-20260718.084355-4`) | <https://github.com/brachy84/ModularUI-Modern> | LGPL-3.0-only | `0de8ae778722065f006028396e7a29eab19218c774774c7df51c6143bbb58eb1` |
| EvalEx (nested inside ModularUI) | `3.6.0` | <https://github.com/ezylang/EvalEx> | Apache-2.0 | `b80b549e22727f45b62c71efd1d03d46807b86f012cc3001f0d101965bf2dc2c` |

The exact resolved dependency graph produced for a release is authoritative.
Release maintainers must compare this table with the built JAR and update the
table whenever a bundled component or version changes. License notices already
contained in nested JARs must not be removed.

### Registrate source and license anchor

- Sources JAR:
  <https://mvn.devos.one/snapshots/com/tterrag/registrate/Registrate/MC1.21-1.3.0%2B67/Registrate-MC1.21-1.3.0%2B67-sources.jar>
- Sources SHA-256:
  `6a360539af5dcd9dd2bb6f63f52b44115d20d7d48e0db4f3072b5f73ca4628d7`
- MPL-2.0 text anchor:
  <https://github.com/tterrag1098/Registrate/blob/80807a93c679acb5e99cc34e0950e6f1976ac984/LICENSE>
- Packaged license:
  `third_party_licenses/Registrate-MPL-2.0.txt`

The Maven sources JAR is the exact corresponding-source anchor for the
embedded binary and is copied into
`third_party_sources/registrate/` in the complete source ZIP. It is generated
delomboked source; the Git commit above anchors the unmodified MPL text, not a
claim that its tree is byte-identical to the generated sources JAR.

### Configuration source and license anchor

- Common sources JAR:
  <https://repo.repsy.io/mvn/toma/public/dev/toma/configuration/configuration-1.21.1/3.1.1-common/configuration-1.21.1-3.1.1-common-sources.jar>
- Common sources SHA-256:
  `19ee14cf87ebae71b6742218cc1b5038007b38e92bc2b8d519e2d3ba37605413`
- NeoForge sources JAR:
  <https://repo.repsy.io/mvn/toma/public/dev/toma/configuration/configuration-1.21.1/3.1.1-neoforge/configuration-1.21.1-3.1.1-neoforge-sources.jar>
- NeoForge sources SHA-256:
  `2bb99843ff79ef8f3840ad9fbb36f86f18e0cf21b212a8a05ed2ded5c6929311`
- Packaged license: `third_party_licenses/Configuration-MIT.txt`

Both sources JARs are required because the NeoForge archive contains only the
platform-specific portion. The complete source ZIP copies them into
`third_party_sources/configuration/`.

### ModularUI snapshot reproducibility anchor

The ModularUI Maven repository currently publishes `3.3.1` only as a mutable
`SNAPSHOT`; it does not expose an immutable release coordinate. The timestamped
artifact is stored below the `3.3.1-SNAPSHOT` directory and therefore also
cannot be requested as a normal immutable Maven version. For this fork's
audited dependency resolution on 2026-08-03, Gradle resolved:

- Maven repository: <https://maven.gtceu.com/>
- Snapshot identity: `3.3.1-20260718.084355-4`
- Binary JAR: `modularui-mc1.21.1-3.3.1-20260718.084355-4.jar`
- Binary JAR SHA-256:
  `0de8ae778722065f006028396e7a29eab19218c774774c7df51c6143bbb58eb1`
- Sources JAR: `modularui-mc1.21.1-3.3.1-20260718.084355-4-sources.jar`
- Sources JAR SHA-256:
  `6bad38c38437d20a43051e81747bf477b8faec1299b8388fdd68821896fd97fa`
- Corresponding source revision:
  <https://github.com/brachy84/ModularUI-Modern/commit/ec883c644bcad03080e6192dc59a10767fea6662>
- Publishing workflow run:
  <https://github.com/brachy84/ModularUI-Modern/actions/runs/29637835394>

A release build must resolve both the same binary hash and the same sources
hash or stop for an explicit dependency review. The complete source ZIP embeds
the verified sources JAR at
`third_party_sources/modularui/modularui-mc1.21.1-3.3.1-20260718.084355-4-sources.jar`,
with `LICENSE-LGPL-3.0.txt` and a copy of this notice beside it. If upstream
replaces the snapshot, update the source revision, timestamped identity, both
hashes, and this notice together; the `3.3.1-SNAPSHOT` declaration alone is
not a reproducibility guarantee.

### EvalEx nested dependency and source anchor

ModularUI embeds EvalEx as a second-level Jar-in-Jar dependency. The audited
runtime path is `META-INF/jarjar/EvalEx-3.6.0.jar`; the release build verifies
that ModularUI's JarJar metadata identifies exactly `com.ezylang:EvalEx:3.6.0`
at that path and that its binary SHA-256 is the value in the table above.

- Project and version tag: <https://github.com/ezylang/EvalEx/tree/3.6.0>
- Exact source revision:
  <https://github.com/ezylang/EvalEx/tree/a85e85e19b2407f6097ecbd8f7b272636db9d9cf>
- Copyright: Copyright 2012-2023 by Udo Klimaschewski
- Binary JAR:
  <https://repo1.maven.org/maven2/com/ezylang/EvalEx/3.6.0/EvalEx-3.6.0.jar>
- Binary JAR SHA-256:
  `b80b549e22727f45b62c71efd1d03d46807b86f012cc3001f0d101965bf2dc2c`
- Sources JAR:
  <https://repo1.maven.org/maven2/com/ezylang/EvalEx/3.6.0/EvalEx-3.6.0-sources.jar>
- Sources JAR SHA-256:
  `076f14e90affb50f61475686971ef02281727fed1c9c70889b46b07213907c4a`
- License: Apache-2.0
- Packaged license:
  `third_party_licenses/EvalEx-Apache-2.0.txt`

The upstream binary and sources JARs do not contain a standalone LICENSE or
NOTICE file. This distribution therefore supplies the unmodified license text
from the tagged upstream source. The complete source ZIP copies the verified
sources JAR, license, and this notice into `third_party_sources/evalex/`.

### Configuration MIT notice

This notice is also stored verbatim at
`third_party_licenses/Configuration-MIT.txt`.

Copyright (c) Toma.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Documentation and fonts

- The upstream documentation tree under `docs/` is licensed CC0-1.0. Its full
  legal text is in `docs/LICENSE`.
- Bitmap font resources under
  `src/main/resources/assets/gtceu/textures/font/` retain the SIL Open Font
  License 1.1. Its full text remains at
  `src/main/resources/assets/gtceu/textures/font/LICENSE.txt` and is packaged
  beside the resources.

## Translation compatibility resources

This fork adds independently authored Simplified Chinese strings for GTCEu and
for the embedded ModularUI user interface. The ModularUI namespace override is
translation data only; ModularUI remains an LGPL-3.0 work credited above.

## Credited assets

The upstream asset credits in `README.md` are incorporated by reference and
must remain present in redistributed source. Inclusion in the credits does not
imply endorsement of this fork.

The same attribution list ships inside the binary and sources JARs as
`META-INF/CREDITS.md`, so a redistributed build carries its credits with it.

Redistribution of the credited textures, fonts, and recovered resources rests on
the publisher's rights determination recorded in
`docs/ASSET_RIGHTS_ATTESTATION.md`: those works are open source and allow
derivative works with attribution. Each credited work keeps its own terms; if a
rights holder disagrees with that determination, the affected asset is removed
or replaced.

## External compatibility target not redistributed

### DS Dragonborn Lib

- Project: <https://www.curseforge.com/minecraft/mc-mods/ds-dragonborn-lib>
- Author: ZoldLeo
- Tested version: `1.2.3` (CurseForge file `8568270`)
- Declared license: GNU Lesser General Public License version 2.1
- Binary SHA-256:
  `DCCC9BFCBD74902F13AE81286ED0CC32FF370F5CDE376996E6D23821E35927D2`
- Upstream `LICENSE.txt` SHA-256:
  `04F3955AE6E65E7432ED002129ECE7DA848542A73D18DB29616CC89E7A914CB9`
- Packaged normalized license copy:
  `third_party_licenses/Dragonborn-Lib-LGPL-2.1.txt`
- Packaged normalized license SHA-256:
  `20C17D8B8C48A600800DFD14F95D5CB9FF47066A9641DDEAB48DC54AEC96E331`

Dragonborn Lib is an optional external runtime dependency. Its JAR, classes,
and source are not embedded in or redistributed with this GTCEu release. The
license text is included only so the tested compatibility baseline is
unambiguous; this does not change the license of GTCEu or any other component.

## All The Mods 9 migration sources

The current local migration candidate contains ATM9 GregStar structure layouts
and 16 byte-identical source resources. Their ATM9 paths, destination paths,
and SHA-256 values are enumerated in
`tools/atm9-gregstar-migration-manifest.json`. All The Mods packs state All
Rights Reserved terms for this material. Inclusion in a local test candidate
does not grant redistribution rights: public distribution requires explicit
authorization from the All The Mods team, and that authorization must be
obtained and documented before release.

The release must not contain the local Minecraft debug bridge, access tokens,
quick-launch scripts, test worlds, logs, or captured media.
