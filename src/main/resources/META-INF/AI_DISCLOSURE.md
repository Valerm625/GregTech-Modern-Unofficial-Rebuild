# AI assistance disclosure

Development of the ATM10 maintenance fork has used **OpenAI Codex** since
2026-07-30.

## Scope of assistance

Codex assisted with:

- inventorying local patches, source trees, runtime logs, and integration
  boundaries;
- comparing the Minecraft 1.21.1 port with upstream and older reference code;
- proposing and drafting Java, mixin, resource, localization, build-metadata,
  and documentation changes;
- auditing and restoring the ATM9-style native JEI/EMI multiblock viewer path;
- identifying dependency, licensing, and redistribution risks;
- running local compilation and disposable-world verification workflows and
  summarizing their results.

AI output is not accepted as proof of correctness. Before any public release, a
human maintainer must review and understand every changed source file, inspect
the final dependency and asset set, reproduce the build, and approve the test
results. Any unreviewed generated change must be removed or held back from the
release.

## Defect-fix pass (2026-10-25)

An additional pass used **DeepSeek Harness (deepseek-flash)** to audit the 103
files that `SOURCE_LINEAGE.csv` marks as recovered or reconstructed, and to fix
the defects it reported. Scope of that pass:

- reproducing the `runData` language-provider crash (`Duplicate translation key
  block.gtceu.creosote`) from a local build and fixing its registration order;
- comparing reconstructed files against unmodified sibling implementations in
  the same tree instead of assuming upstream intent;
- drafting the fixes for machine client-sync, block-pattern bounds, chance-roll
  counts, recipe codec validation, `GTRecipe` identity, reload staging, the
  maintenance tooltip range, the metadata encoding of the Chinese mod name, and
  the build-time ModularUI pin opt-out;
- running `compileJava`, `compileTestJava`, `test`, `runData` and
  `runGameTestServer` (160/160) on the exact tree and recording the results in
  the pull request.

The human review requirement above is unchanged and applies to these changes as
well; this section only records which tool produced the draft.

## Media

No AI-generated art, textures, icons, audio, video, or other media is included.
The upstream `AI_POLICY.md` remains in force and is incorporated into the
release process.

## Non-endorsement

Use of Codex or DeepSeek Harness does not imply review or endorsement by OpenAI,
DeepSeek, the GregTechCEu Team, or the All The Mods Team. Responsibility for the
published fork and its claims remains with its human publisher.
