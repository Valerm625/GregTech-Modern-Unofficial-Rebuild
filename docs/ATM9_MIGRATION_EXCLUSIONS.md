# ATM9 Migration Exclusion

The recovered ATM10 artifact contained a small GregStar migration candidate
derived from a local All the Mods 9 installation. The candidate is **not** part
of this public source repository. Its scripts, structure definitions, language
files, recipes, and textures are excluded pending written redistribution
permission from the relevant rights holders.

`tools/atm9-gregstar-migration-manifest.json` is a hash-only evidence record.
It names the local source paths, the proposed destination paths, and the
SHA-256 values used during the recovery review; it does not redistribute any
of the source files. The record is intentionally insufficient to reconstruct
the excluded assets without the original local installation.

This boundary is separate from the GTCEu core source. Do not copy the excluded
files into a release, a pull request, a generated resource directory, or a
binary artifact unless a maintainer has recorded explicit permission and
updated the rights review.
