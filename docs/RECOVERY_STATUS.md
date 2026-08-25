# Recovery status

This repository contains an evidence-backed maintenance recovery of the custom
GTCEu layer identified as `8.0.1-atm10.5`. It is suitable for review,
maintenance, and compilation, but it is not an official upstream source archive
and is not claimed to reproduce every class byte-for-byte.

## Evidence summary

- Target artifact SHA-256: `D922311A96FA607BC15FD5E8056A11D84440FD72897099BCB05690E36185DD19`
- Target artifact size: `17,347,036` bytes
- Upstream baseline: `8201bf3847792e40b0dd75a6952e76c4c72e3cbf`
- Recorded fork revision: `6dd4c6f490c7799620cd7e07c49c8cb368e0ddf9`
- Recorded build state: `Fork-Dirty: true`
- Recovered Java files: `1,714`
- Target and reconstructed class entries: `2,314 / 2,314`
- Strict source assembly: `43` adds, `1,671` replacements, `1` deletion, and
  `51` hash-verified build/input operations

## Source lineage

The Java sources are not all decompiler output. The path-and-hash authority is
[`SOURCE_LINEAGE.csv`](../SOURCE_LINEAGE.csv):

| Action group | Files | Meaning |
| --- | ---: | --- |
| `replace_with_neo_source` | 1,418 | Exact source from the pinned NeoForge mother tree |
| `use_verified_mft_original` | 189 | Original deleted-tree files recovered through MFT evidence |
| Migration/reconstruction review actions | 107 | Fork deltas, target-only reconstruction, or intentional maintenance changes |

The assembly and build plans in the repository root capture the exact source
operation set. [`FORK_PROVENANCE.properties`](../FORK_PROVENANCE.properties)
records the recovery revision, dirty state, and upstream revision. Neither
record makes this fork an official GTCEu source release.

## Verification boundary

Source assembly, `compileJava`, `compileTestJava`, `jar`, `sourcesJar`, and the
ordinary Gradle `test` task have succeeded on the recovery tree. The ordinary
test task discovers no JUnit suites. Focused NeoForge GameTest sources compile,
but a fresh `runGameTestServer` execution is still required before behavior
coverage can be claimed.

The compatibility work includes a concrete bridge for legacy and boosted
`ChanceLogic` implementations and unambiguous four- and five-argument
`chancedOutput` behavior. Focused transaction tests also cover recipe-index
rollback after a failed reload. These tests are source-level regression
fixtures, not proof of a complete in-game validation run.

## Public binary boundary

The source artifact comparison still records 60 unresolved non-class
differences: 53 target-only paths and seven common paths whose content differs
after text normalization. These are review entries, not a blanket claim that
all material is restricted. Importing resources from the target JAR is disabled
until every path has documented source, license, and redistribution permission.

Accordingly, this repository publishes source for collaboration. A public
binary release remains gated on resource-rights review, corresponding-source
checks, and real client/GameTest validation of the exact build.
