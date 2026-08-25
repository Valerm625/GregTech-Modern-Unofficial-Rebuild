package com.gregtechceu.gtceu.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJeiSlotRegistration;
import java.util.List;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ModularUIRecipeCategory.class})
public abstract class ModularUIJeiIngredientManagerMixin {
   @Redirect(
      method = {"addJEISlot"},
      at = @At(
         value = "INVOKE",
         target = "Lmezz/jei/api/runtime/IJeiRuntime;getIngredientManager()Lmezz/jei/api/runtime/IIngredientManager;"
      ),
      remap = false
   )
   private static IIngredientManager gtceu$getIngredientManager(IJeiRuntime runtime) {
      return GTJeiSlotRegistration.getIngredientManager(runtime);
   }

   @Redirect(
      method = {"addJEISlot"},
      at = @At(
         value = "INVOKE",
         target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;addIngredients(Lmezz/jei/api/ingredients/IIngredientType;Ljava/util/List;)Lmezz/jei/api/gui/builder/IIngredientAcceptor;"
      ),
      remap = false
   )
   private static IIngredientAcceptor gtceu$discardEmptyIngredients(IRecipeSlotBuilder slot, IIngredientType ingredientType, List ingredients) {
      return GTJeiSlotRegistration.addFilteredIngredients(slot, ingredientType, ingredients);
   }

   @Redirect(
      method = {"addJEISlot"},
      at = @At(
         value = "INVOKE",
         target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;setCustomRenderer(Lmezz/jei/api/ingredients/IIngredientType;Lmezz/jei/api/ingredients/IIngredientRenderer;)Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
      ),
      remap = false
   )
   private static IRecipeSlotBuilder gtceu$installCenteredJeiRenderer(
      IRecipeSlotBuilder slot,
      IIngredientType ingredientType,
      IIngredientRenderer transparentRenderer,
      IRecipeLayoutBuilder ignoredLayout,
      IngredientProvider provider,
      RecipeIngredientRole ignoredRole,
      int ignoredIndex
   ) {
      return GTJeiSlotRegistration.installRenderer(slot, ingredientType, transparentRenderer, provider);
   }
}
