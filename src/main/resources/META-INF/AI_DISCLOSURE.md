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

## Media

No AI-generated art, textures, icons, audio, video, or other media is included.
The upstream `AI_POLICY.md` remains in force and is incorporated into the
release process.

## Non-endorsement

Use of Codex does not imply review or endorsement by OpenAI, the GregTechCEu
Team, or the All The Mods Team. Responsibility for the published fork and its
claims remains with its human publisher.
