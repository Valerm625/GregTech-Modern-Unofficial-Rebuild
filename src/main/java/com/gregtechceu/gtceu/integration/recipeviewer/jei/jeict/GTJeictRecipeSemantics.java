package com.gregtechceu.gtceu.integration.recipeviewer.jei.jeict;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;

public final class GTJeictRecipeSemantics {
   private static final Map<ResourceLocation, Boolean> UNSAFE_FOR_PROCESSING_PATTERN = new ConcurrentHashMap<>();

   private GTJeictRecipeSemantics() {
   }

   public static void remember(GTRecipe recipe) {
      ResourceLocation recipeId = recipe.getId();
      if (recipeId != null) {
         UNSAFE_FOR_PROCESSING_PATTERN.put(recipeId, isUnsafeForProcessingPattern(recipe));
      }
   }

   public static boolean isTracked(ResourceLocation recipeId) {
      return recipeId != null && UNSAFE_FOR_PROCESSING_PATTERN.containsKey(recipeId);
   }

   public static boolean isUnsafeForProcessingPattern(ResourceLocation recipeId) {
      return recipeId != null && UNSAFE_FOR_PROCESSING_PATTERN.getOrDefault(recipeId, false);
   }

   private static boolean isUnsafeForProcessingPattern(GTRecipe recipe) {
      return recipe.ingredientActions.isEmpty() && !hasMaterialTickIo(recipe) && !hasRangedMaterialIo(recipe)
         ? hasProbabilisticConsumedInput(recipe.inputs)
         : true;
   }

   private static boolean hasMaterialTickIo(GTRecipe recipe) {
      return recipe.tickInputs.containsKey(ItemRecipeCapability.CAP)
         || recipe.tickInputs.containsKey(FluidRecipeCapability.CAP)
         || recipe.tickOutputs.containsKey(ItemRecipeCapability.CAP)
         || recipe.tickOutputs.containsKey(FluidRecipeCapability.CAP);
   }

   private static boolean hasRangedMaterialIo(GTRecipe recipe) {
      return hasRangedItems(recipe.inputs.get(ItemRecipeCapability.CAP))
         || hasRangedItems(recipe.outputs.get(ItemRecipeCapability.CAP))
         || hasRangedFluids(recipe.inputs.get(FluidRecipeCapability.CAP))
         || hasRangedFluids(recipe.outputs.get(FluidRecipeCapability.CAP));
   }

   private static boolean hasRangedItems(List<Content> contents) {
      return contents == null
         ? false
         : contents.stream()
            .map(Content::content)
            .map(ItemRecipeCapability.CAP::of)
            .anyMatch(ingredient -> ingredient.ingredient().getCustomIngredient() instanceof IntProviderIngredient);
   }

   private static boolean hasRangedFluids(List<Content> contents) {
      return contents == null
         ? false
         : contents.stream()
            .map(Content::content)
            .map(FluidRecipeCapability.CAP::of)
            .anyMatch(ingredient -> ingredient.ingredient() instanceof IRangedIngredient);
   }

   private static boolean hasProbabilisticConsumedInput(Map<?, List<Content>> inputs) {
      for (List<Content> contents : inputs.values()) {
         for (Content content : contents) {
            if (content.chance() < content.maxChance() && (content.chance() > 0 || content.tierChanceBoost() > 0)) {
               return true;
            }
         }
      }

      return false;
   }
}
