package com.gregtechceu.gtceu.core.mixins.jeict;

import com.gregtechceu.gtceu.integration.recipeviewer.jei.jeict.GTJeictRecipeSemantics;
import com.lhy.jeict.api.PatternEncodingDraft;
import com.lhy.jeict.api.PatternEncodingMode;
import com.lhy.jeict.recipe_tree.RecipeTreeOutputViewModel;
import com.lhy.jeict.recipe_tree.RecipeTreeRecipeViewModel;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {PatternEncodingDraft.class},
   remap = false
)
public abstract class PatternEncodingDraftMixin {
   @Inject(
      method = {"fromRecipe"},
      at = {@At("RETURN")}
   )
   private static void gtceu$filterUnsafePatternContents(
      RecipeTreeRecipeViewModel recipe,
      PatternEncodingMode mode,
      boolean substituteItems,
      boolean substituteFluids,
      CallbackInfoReturnable<PatternEncodingDraft> cir
   ) {
      if (GTJeictRecipeSemantics.isTracked(recipe.recipeId())) {
         PatternEncodingDraft draft = (PatternEncodingDraft)cir.getReturnValue();
         List<RecipeTreeOutputViewModel> outputs = new ArrayList<>(recipe.outputs());
         outputs.sort((left, right) -> Boolean.compare(right.primary(), left.primary()));
         boolean stochasticPrimary = outputs.stream().filter(RecipeTreeOutputViewModel::primary).findFirst().map(output -> output.chance() < 1.0).orElse(true);
         if (!GTJeictRecipeSemantics.isUnsafeForProcessingPattern(recipe.recipeId()) && !stochasticPrimary) {
            int comparableOutputs = Math.min(outputs.size(), draft.outputs().size());

            for (int index = comparableOutputs - 1; index >= 0; index--) {
               if (outputs.get(index).chance() < 1.0) {
                  draft.outputs().remove(index);
               }
            }
         } else {
            draft.clear();
         }
      }
   }
}
