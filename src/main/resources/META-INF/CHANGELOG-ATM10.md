# ATM10 maintenance-fork changelog

This file describes changes specific to the unofficial ATM10 fork. For upstream
history, use the GregTech CEu: Modern repository and its changelog.

## 8.0.1-atm10.5

- Restored the Micro Universe Orb's ATM9 recipe-modifier semantics exactly:
  both recipe maps use the native parallel-hatch and non-perfect-overclock
  modifiers. Removed the later collector-only balance bridge and replaced its
  class-loading gate with a source gate that rejects that divergence.
- Embedded the ATM10 GregStar production chain, custom recipe maps, materials,
  multiblocks, infinity cells, mining-dimension layers, Occultism miner routes,
  and cross-mod compatibility resources in the GTCEu replacement JAR. The
  original local KubeJS copies are kept only in the instance backup directory
  to prevent duplicate registration.
- Kept ATM10's original `gregstar` chapter in place (chapter ID
  `3202C575456F57D2`) and migrated only its 13 invalid references across 11
  `kubejs:*` item IDs to canonical `gtceu:*` IDs. Task IDs, dependencies,
  text, coordinates, layout, and recipe IDs are unchanged; no replacement
  quest chapter is packaged. The old IDs remain hidden save-compatibility
  aliases, while AllTheTweaks-owned star IDs remain untouched.
- Reused ATM10's existing GregStar task mapping for the absent ATM9
  `exchangers:end_steel_exchanger`: the final Star Forge route now consumes
  `8x enderio:vibrant_capacitor_bank` without fabricated NBT. The independent
  `enderio:vibrant_photovoltaic_module` exchanger-core mapping remains outside
  the final recipe.
- Quarantined the previously generated duplicate GregStar chapter and its two
  language overlays, and fixed the Rhino block-scope redeclarations in the
  cross-mod and Occultism post-reload audits.
- Closed the first-production gap for Trinaquadalloy, bounded Star Matter
  Fusion startup to `600,000,000 EU`, bounded ordinary infinity-fluid-cell
  inputs to `4,000,000 mB`, and added static reachability checks for those
  constraints. Added the 176 CWU/t multi-HPCA operating note and made the three
  established quest-facing cell IDs visible, reversible GT Canner sample units
  backed by `1,000 mB` of their corresponding fluids.
- Replaced the earlier authored cross-mod balance with a 118-recipe ATM9
  migration: 15 AllTheModium, 52 AE-family (including the three original
  Universal Press items, exact textures and 17 Inscriber recipes), 26
  Productive Bees, two Rock Breaker, 22 surviving AllTheOres and one Railcraft
  route. ATM9 chances, tier boosts, durations, voltages and yields are retained.
- Removed the 22 custom Mekanism Rotary recipes and helper script because ATM9
  contains no such bridge; Mekanism's native owner recipes remain authoritative.
- Restored Occultism's 94-entry fixed ATM9 miner table and removed the invented
  mass/temperature tiering, Eldritch expansion and custom ritual.
- Matched relocated GT veins to AllTheModium 3.0.1's four actual mining strata,
  including a corrected stone ceiling of Y=247, and updated the internal ranges
  of veined, dike, and cuboid generators.
- Added per-script optional-mod dependency filtering and post-reload audits for
  GregStar, the 118 cross-mod manifest and the static 94-recipe Occultism table.
- Restricted ATM10-only activation to the AllTheModium and AllTheTweaks marker
  pair. Optional domains inside the shared cross-mod script remain independently
  guarded, so one absent addon cannot suppress unrelated compatibility routes.
- Added an ATM10-only vanilla-recipe restoration scope. It restores furnace,
  crafting, smelting, and compression recipes while leaving GT machine
  processing routes intact; the configured/effective removal sets are exposed
  through `RecipeRemoval` and documented in `docs/ATM10-VANILLA-RECIPE-RESTORATION.md`.
- Made `all_parallels` optional with a `[1, 1, 1]` default in the GT recipe
  codec. This fixes KubeJS-created recipes that omit the internal parallel
  metadata and prevents the recipe reload failures seen in ATM10.
- Moved the Dragon Survival Curios guard into an optional, non-fatal GTCEu
  Mixin and restored the official Dragon Survival JAR in ATM10. The prior
  locally modified JAR remains outside the active mods directory with its
  provenance recorded in `docs/ATM10-DRAGON-SURVIVAL-MIGRATION.md`.
- Traced the Dragon Survival `2.0.63` startup failure to external Dragonborn Lib
  `1.2.2`, whose Mixin still targeted `generateSkinTextures(DragonEntity)`.
  ATM10 now stages official Dragonborn Lib `1.2.3`, which declares Dragon
  Survival `[2.0.63,)` and targets the new
  `generateSkinTextures(Player, DragonStateHandler)` signature; `1.2.2` is
  retained only as a disabled backup. A replacement-client restart remains a
  release gate.
- Folded the three legacy AllTheModium item registry IDs into GTCEu through a
  reflection-only optional adapter, allowing the standalone migration shim to
  be removed without breaking older saves.
- Removed the candidate Modern Industrialization energy bridge and its tests;
  ATM9 does not include Modern Industrialization, so the bridge had no source
  under the ATM9-only migration policy.
- Added an optional batch-pattern compatibility adapter for JEI Crafting Tree
  `0.0.2` with AE2 Utility `1.6.0` or later; ATM10 validation targets AE2
  Utility `1.7.6`.
- Preserved GT-aware marginal output probabilities (including the built-in
  `OR`, `AND`, `FIRST`, `XOR`, and `NONE` logics) and reusable catalyst inputs
  in recipe-tree snapshots instead of treating every displayed slot as
  deterministic and consumed.
- Prevented unsafe AE2 processing-pattern drafts for probabilistic consumed
  inputs, probabilistic primary outputs, item/fluid tick I/O, random or
  ranged-count item/fluid I/O, and recipes with `ingredientActions`.
  Probabilistic secondary outputs are omitted rather than encoded as
  guaranteed products.
- Added missing probability information to fluid-slot tooltips.
- Diagnosed the ATM10 client crash caused by Collapsible Groups `1.3.0`
  implementing the JEI `19.43` `IElement` contract without `tick()`. The
  replacement metadata now accepts the addon only at `1.4.2` or later, keeps it
  external and optional, and adds a generated-JAR packaging/metadata gate.
- Preserved ordinary JEI item stack counts, leaving count suppression only for
  ranged-count overlays. Replaced narrow split bands and substring clipping with
  non-overlapping full-width rows and complete-text scaling for chance, amount,
  and per-tick annotations.
- Restored the ATM9-style native JEI/EMI multiblock preview. EMI exposes only
  the controller output and adds no material list, workstation, recipe-tree,
  or automatic-construction semantics.
- Added source-level JEI Crafting Tree safety and EMI multiblock integration
  guards to the Gradle `check` lifecycle. The 2026-08-06 dirty-tree full
  pre-release run passed all 44 tasks; a final clean-commit rerun remains
  required.
- Added `ModularUITestHandlerMixin` and the red-to-green
  `verifyModularUiConfigSafety` probe. The redirect is limited to
  `enableTestOverlays()` and returns false when that optional diagnostic reads
  NeoForge config too early and receives `IllegalStateException`; unrelated
  failures still propagate, preventing a secondary error-screen crash from
  hiding the original mod-loading failure.
- Synchronized the missing Jade configuration labels for GT diode and
  ME-grid information providers in English and Simplified Chinese, and added
  a build gate that keeps every configurable GT Jade component aligned with
  both language resources.
- Added the full Apache-2.0 text, exact binary/source hashes, nested JarJar
  metadata verification, and corresponding-source archive for EvalEx `3.6.0`,
  which is embedded as a second-level dependency inside ModularUI.
- Completed the 2026-08-06 dirty-tree forced GameTest run with 155/155 tests
  passing. This is pre-release evidence only; the final clean commit still
  requires its own check, GameTest, reproducible build, and client validation.

## 8.0.1-atm10.4

- Removed stale optional Mixin declarations whose configuration files are not
  present, preventing conditional loader failures in public installations.
- Restricted metadata to the tested Minecraft 1.21.1 target.
- Raised the declared NeoForge minimum to the exact release build baseline,
  `21.1.234`.
- Hardened release provenance so a release build rejects malformed or
  mismatched commit IDs and false clean-tree declarations.
- Isolated source-archive provenance from ambient CI variables belonging to a
  parent or unrelated repository.
- Added GameTest execution to release workflows and removed inherited
  workflows that could contact upstream infrastructure, inherit secrets, or
  force-push branches.
- Pinned every third-party GitHub Action to an audited immutable commit.
- Removed inherited PR label enforcement that depended on an upstream-only
  label taxonomy.
- Removed Maven credentials from CI and blocked remote publication while the
  fork still uses a temporary namespace or lacks its own HTTPS project URL.
- Changed release attachment automation to a manual, exact-tag, draft-only
  workflow so failed gates cannot leave an empty public Release.
- Added Applied Flux and Modern Industrialization to the GameTest runtime so
  release tests load their optional compatibility entrypoints.
- Completed that runtime with Applied Flux's Glodium dependency and the
  KubeJS/Rhino bundle. The KubeJS recipe-data GameTest now uses a real recipe
  event and accepts the canonical NBT byte representation of boolean `true`.
- Made an explicitly requested GameTest task always start the test server,
  preventing stale local task outputs from being mistaken for a fresh pass.
- Repaired the fluid-detector GameTest fixture so it uses deterministic stored
  fluid and the cover's actual 20-tick update phase, then restored that check
  from optional to required.
- Made the complete source archive rebuildable from an extracted, Git-free
  directory by regenerating its pinned third-party source companions exactly
  once. The public release workflow now exercises that self-rebuild path.
- Replaced upstream-owned support, issue, and contribution routing with
  fork-local metadata.
- Added complete file-level license notices, full license texts, pinned binary
  and source hashes, and corresponding sources for embedded libraries.
- Added CC0 and OFL texts to the standalone license bundle and the applicable
  OFL notice to binary metadata.
- Replaced the upstream-remote-dependent Spotless ratchet with the pinned
  upstream base commit and a source-archive-safe full-tree fallback.
- Normalized source and license archive permissions while preserving the Unix
  executable bit on `gradlew`.
- Normalized packaged text resources to fixed LF (and batch files to CRLF) so
  Windows and Linux checkouts do not change release archive bytes.
- Removed two stale imports exposed by the source-archive full-tree formatting
  gate.


## 8.0.1-atm10.3

- Restored the `GregStarRecipeModifiers` Java compatibility entrypoint used by
  ATM10's mixed collector/reactor Micro Universe Orb startup definition.
- Deferred access to GT recipe modifiers until recipe evaluation so KubeJS can
  load the compatibility class safely before GTCEu material registration.
- Restricted parallel-hatch processing and electric overclocking to collector
  recipes so reactor recipes retain their authored output rate and duration.
- Added a release-JAR regression gate for Java entrypoints referenced by the
  supported ATM10 KubeJS integration, including an isolated early-class-loading
  guard, preventing a source-migration omission from reaching another release
  candidate.

## 8.0.1-atm10.2

- Moved optional Applied Flux part-capability setup onto the GTCEu mod event
  bus, avoiding an early entrypoint-class mixin crash during cold start.
- Added explicit, widget-derived dimensions to every ModularUI-backed GT JEI
  category required by JEI 19.39 and later.
- Added a GT-owned JEI recipe slot and a guarded ModularUI factory fallback for
  the pinned upstream snapshot's unfinished JEI-only branch. EMI and REI keep
  their original priority and behavior.
- Added static regressions covering all GT JEI category implementations,
  embedded-screen lifecycle, slot defaults, renderer/tooltip de-duplication,
  and legacy slot-name lookup.
- Replaced the bidirectional Modern Industrialization bridge from
  `8.0.1-atm10.1` with a GT-to-MI export-only bridge. The direct MI-to-GT
  capability, adapter, and ledger were removed; tier mapping, live conversion
  ratio validation, conservation, and rate-limit regression tests remain.
- Hardened release provenance and publication: reproducible archives, a pinned
  Gradle wrapper checksum, verified ModularUI binary and sources, a complete
  source ZIP, release checksums, fork-owned Maven gates, and checksummed GitHub
  Release assets.

## 8.0.1-atm10.1

Base: GregTech CEu: Modern `1.21` revision
`8201bf3847792e40b0dd75a6952e76c4c72e3cbf`.

### Consolidated

- Replaced scattered local GT patch JARs with source-level changes in one
  `gtceu` distribution.
- The locally designed JEI multiblock builder from this development line was
  later removed in favor of the ATM9-style native structure preview.
- Preserved the `gtceu` mod ID for existing worlds, addons, KubeJS scripts, and
  recipe identifiers.

### Multiblock recipe viewer

- Uses the native ATM9-style structure preview in JEI and EMI.
- Does not add automatic construction, material-source adapters, protection or
  rollback systems, variable-size build controls, or recipe-tree semantics.

### GT-owned fixes

- Preserved required recipe data when recipes are generated through KubeJS.
- Expanded fusion fluid-input presentation where the recipe requires more than
  two inputs.
- Corrected distillery output-ingredient selection.
- Corrected client-to-server synchronization for the parallel hatch control.
- Restored item-bus change notification for count-only slot updates.
- Synchronized JourneyMap ore-marker options with persisted JourneyMap state.

### Compatibility policy

- Keeps optional integrations in explicit adapters and avoids mandatory class
  loading when the target mod is absent.
- Restores Applied Flux accessor input and adaptive AE2-to-GT output using the
  target machine's accepted voltage, amperage, capacity, and the network's
  actually available energy.
- Adds a tier-aware bidirectional Modern Industrialization bridge when the
  configured ratios form the exact ledger `1 MI EU = 4 GT EU`; unsafe or
  non-integral configurations disable the direct bridge.
- Does not change the global Modern Industrialization energy ratio as a shortcut
  for tier compatibility.
- Treats cross-mod material and chemical conversion as a conservation problem;
  conversions must be explicit and tested for duplication loops.
- Reconciles exceptional AE2 rollback insertions before using player escrow, so
  an unknown third-party storage outcome cannot duplicate construction items.

### Publication boundary

- Restored the ATM9 GregStar structure layouts and source artwork required by
  the migration. Their sources and SHA-256 values are pinned in
  `tools/atm9-gregstar-migration-manifest.json`. These All Rights Reserved
  resources may not be publicly distributed without authorization from the
  All The Mods team.
- Excluded the local debug bridge, credentials, quick-launch scripts, test
  worlds, logs, recordings, screenshots, and machine-local configuration.
- Excluded unrelated third-party-mod patches from the GT fork.

### Distribution note

This JAR replaces the official GTCEu JAR and cannot be installed beside it. The
release is unofficial and is not supported by the GregTechCEu Team or the All
The Mods Team.
