package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.EmiUsageRecipeSorter;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {EmiApi.class},
   remap = false
)
public abstract class EmiApiUsageLookupMixin {
   @Inject(
      method = {"pruneUses(Ljava/util/List;Ldev/emi/emi/api/stack/EmiIngredient;)Ljava/util/List;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void gtceu$cacheUsageSortKeys(List<EmiRecipe> recipes, EmiIngredient queried, CallbackInfoReturnable<List<EmiRecipe>> cir) {
      cir.setReturnValue(
         EmiUsageRecipeSorter.filterAndSort(recipes, recipe -> gtceu$matchesUsage(recipe, queried), recipe -> gtceu$getSmallestPresence(recipe, queried))
      );
   }

   private static boolean gtceu$matchesUsage(EmiRecipe recipe, EmiIngredient queried) {
      return recipe.getInputs().stream().anyMatch(input -> gtceu$containsAll(input, queried))
         || recipe.getCatalysts().stream().anyMatch(catalyst -> gtceu$containsAll(catalyst, queried));
   }

   private static int gtceu$getSmallestPresence(EmiRecipe recipe, EmiIngredient queried) {
      int queriedSize = queried.getEmiStacks().size();
      int smallestPresence = Integer.MAX_VALUE;

      for (EmiIngredient input : recipe.getInputs()) {
         if (gtceu$containsAll(input, queried)) {
            smallestPresence = Math.min(smallestPresence, input.getEmiStacks().size());
            if (smallestPresence <= queriedSize) {
               break;
            }
         }
      }

      return smallestPresence;
   }

   private static boolean gtceu$containsAll(EmiIngredient ingredient, EmiIngredient queried) {
      for (EmiStack queriedStack : queried.getEmiStacks()) {
         boolean found = false;

         for (EmiStack ingredientStack : ingredient.getEmiStacks()) {
            if (ingredientStack.isEqual(queriedStack)) {
               found = true;
               break;
            }
         }

         if (!found) {
            return false;
         }
      }

      return true;
   }
}
