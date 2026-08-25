# Source provenance

This repository is an assembled GTCEu maintenance tree. It uses the following
public GregTech Modern NeoForge snapshot as its baseline:

- Repository: <https://github.com/GregTechCEu/GregTech-Modern>
- Loader target: NeoForge 1.21.1
- Pinned commit: [`8201bf3847792e40b0dd75a6952e76c4c72e3cbf`](https://github.com/GregTechCEu/GregTech-Modern/commit/8201bf3847792e40b0dd75a6952e76c4c72e3cbf)
- Upstream commit message: `fix uppercase ids being used in reslocs (#5232)`
- Baseline snapshot digest: `ab08fb243e2e9f58cf5e3e51cbdd98415ed627278bae9601f14ee217b4228586`

The baseline digest was computed over the pinned source snapshot after
normalizing text line endings. The baseline deliberately omitted upstream
collaboration-only files such as `docs/**`, `.github/**`, the dev-container
definition, `.git-blame-ignore-revs`, `CHANGELOG.md`, and `CODEOWNERS`, while
retaining all required build inputs.

This assembled tree is not itself byte-identical to the upstream snapshot.
The recovered maintenance sources, their hashes, and the resulting source
operations are recorded in [`SOURCE_LINEAGE.csv`](SOURCE_LINEAGE.csv),
[`SOURCE_ASSEMBLY.md`](SOURCE_ASSEMBLY.md), and
[`SOURCE_ASSEMBLY_PLAN.csv`](SOURCE_ASSEMBLY_PLAN.csv). The fork revision and
dirty-state provenance are recorded in
[`FORK_PROVENANCE.properties`](FORK_PROVENANCE.properties).

The repository also includes a small set of publication-audit supplements:
`docs/LICENSE`, `docs/ATM9_MIGRATION_EXCLUSIONS.md`, and the files under
`tools/`. These documents record licensing and recovery boundaries; they are
not part of the upstream source snapshot or the recovered Java/resource
assembly.

Keep upstream work attributable to GregTechCEu and preserve all file-level
copyright and license notices. This project is an unofficial maintenance fork,
not an official upstream release or source archive.
