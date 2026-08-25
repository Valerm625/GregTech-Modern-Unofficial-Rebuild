package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class MultiblockLang {
   public static void init(RegistrateLangProvider provider) {
      initReadOnlyRecipeViewerLang(provider);
      provider.add("gtceu.pattern_error.mismatch_coils", "Mismatched coils: %s vs %s at (%d, %d, %d)");
      provider.add("gtceu.pattern_error.mismatch_filters", "Mismatched filters: %s vs %s at (%d, %d, %d)");
      provider.add("gtceu.predicate_error.active_transformer.missing_io", "Missing hatches: IN - %s | OUT - %s");
      provider.add("gtceu.predicate_error.cleanroom.too_small", "Cleanroom must be at least 5x5x4.");
      provider.add("gtceu.predicate_error.cleanroom.not_centered", "Cleanroom controller must be centered.");
      provider.add("gtceu.predicate_error.distillery.unexpected_hatch", "Fluid hatch at unexpected position (%d, %d, %d).");
      provider.add("gtceu.predicate_error.distillery.missing_outputs", "Missing fluid output hatches.");
      provider.add("gtceu.predicate_error.power_substation.missing_batteries", "Missing batteries.");
      provider.add("gtceu.predicate_error.databank.missing_maintenance", "Missing maintenance hatch.");
      provider.add("gtceu.predicate_error.object_holder.direction", "Object holder must face opposite the controller.");
      provider.add("gtceu.predicate_error.research.missing_computation", "Missing computation hatch.");
      provider.add("gtceu.predicate_error.research.missing_object_holder", "Missing object holder hatch.");
      provider.add("gtceu.predicate_error.charcoal.walls", "Incorrect wall block at (%d, %d, %d).");
      provider.add("gtceu.predicate_error.charcoal.logs", "Must be completely filled with logs.");
      provider.add("gtceu.predicate_error.steam.missing_steam_hatch", "Missing steam hatch");
      provider.add("gtceu.pattern_predicate.blocks", "Error at X: %d, Y: %d, Z: %d");
   }

   private static void initReadOnlyRecipeViewerLang(RegistrateLangProvider provider) {
      provider.add("jei.gtceu.multiblock.title", "GregTech Multiblock Structure");
      provider.add("jei.gtceu.multiblock.recipe", "Recipe");
      provider.add("jei.gtceu.multiblock.materials_page", "Materials %s-%s/%s · page %s/%s");
      provider.add("jei.gtceu.multiblock.material_page_changed", "Material page %s/%s");
      provider.add("jei.gtceu.multiblock.material_count", "Exact required: %s");
      provider.add("jei.gtceu.multiblock.role.required", "Required");
      provider.add("jei.gtceu.multiblock.role.optional", "Optional");
      provider.add("jei.gtceu.multiblock.role.replaceable", "Replaceable");
      provider.add("jei.gtceu.multiblock.all_layers", "All layers");
      provider.add("jei.gtceu.multiblock.layer", "Layer %s/%s");
      provider.add("jei.gtceu.multiblock.all_layers_compact", "All");
      provider.add("jei.gtceu.multiblock.layer_compact", "L %s/%s");
      provider.add("jei.gtceu.multiblock.final_dimensions", "Final XxYxZ: %sx%sx%s");
      provider.add("jei.gtceu.multiblock.final_dimensions_compact", "XYZ %s×%s×%s");
      provider.add("jei.gtceu.multiblock.clearance.required", "Required empty/operating-clearance cells: %s");
      provider.add("jei.gtceu.multiblock.dimension.axis_compact", "%s %s [%s-%s]");
      provider.add("jei.gtceu.multiblock.dimension.axis_fixed_compact", "%s %s · fixed");
      provider.add("jei.gtceu.multiblock.dimension.axis", "Preview %s axis: %s (adjustable %s..%s)");
      provider.add("jei.gtceu.multiblock.dimension.axis_fixed", "Preview %s axis: %s (fixed)");
      provider.add("jei.gtceu.multiblock.dimension.axis_selected", "%s-axis size selected");
      provider.add("jei.gtceu.multiblock.dimension.axis_changed", "%s-axis size changed to %s");
      provider.add("jei.gtceu.multiblock.dimension.rejected", "That dimension combination is invalid");
      provider.add("jei.gtceu.multiblock.render_failed", "3D rendering failed; the material list is still available");
      provider.add("jei.gtceu.multiblock.click_block", "Click a structure block to inspect its name and candidates");
      provider.add("jei.gtceu.multiblock.selection_hint", "Left-click a structure block; use the candidate arrow buttons to switch it");
      provider.add("jei.gtceu.multiblock.air", "Air (leave empty)");
      provider.add("jei.gtceu.multiblock.candidates", "%s candidates");
      provider.add("jei.gtceu.multiblock.consistency.heating_coils", "Uniform heating-coil group");
      provider.add("jei.gtceu.multiblock.consistency.cleanroom_filters", "Uniform cleanroom-filter group");
      provider.add("jei.gtceu.multiblock.consistency.summary", "Group %s / %s choices");
      provider.add("jei.gtceu.multiblock.consistency.members", "All %s positions in this group must use the same block");
      provider.add("jei.gtceu.multiblock.consistency.changed", "All positions in the uniform group were changed together");
      provider.add("jei.gtceu.multiblock.candidate_list", "Replacement candidates (%s):");
      provider.add("jei.gtceu.multiblock.candidate_more", "...and %s more");
      provider.add("jei.gtceu.multiblock.candidate_changed", "Candidate block changed");
      provider.add("jei.gtceu.multiblock.candidate_rejected", "That candidate violates a structure count limit");
   }
}
