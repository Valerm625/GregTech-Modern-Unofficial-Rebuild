package com.gregtechceu.gtceu.core.mixins.jeict;

import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJeiSlotMetadata;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.jeict.GTJeictRecipeSemantics;
import com.lhy.jeict.jei.JeiCraftingTreePlugin;
import com.lhy.jeict.jei.RecipeTreeJeiLookup;
import com.lhy.jeict.recipe_tree.RecipeTreeInputViewModel;
import com.lhy.jeict.recipe_tree.RecipeTreeOutputViewModel;
import com.lhy.jeict.recipe_tree.RecipeTreeRecipeViewModel;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {RecipeTreeJeiLookup.class},
   remap = false
)
public abstract class RecipeTreeJeiLookupMixin {
   @Shadow
   private static void appendInputs(
      List<RecipeTreeInputViewModel> inputs,
      List<IRecipeSlotView> slots,
      @Nullable IIngredientManager ingredientManager,
      boolean consumed,
      boolean preserveSlotIndex
   ) {
      throw new AssertionError();
   }

   @Inject(
      method = {"createSnapshot"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void gtceu$restoreRecipeSemantics(
      IRecipeSlotsView recipeSlots,
      @Nullable ResourceLocation recipeId,
      Component title,
      @Nullable Component subtitle,
      @Nullable IDrawable subtitleIcon,
      @Nullable ITypedIngredient<?> preferredOutput,
      boolean preserveCraftingGrid,
      CallbackInfoReturnable<RecipeTreeRecipeViewModel> cir
   ) {
      if (GTJeictRecipeSemantics.isTracked(recipeId)) {
         RecipeTreeRecipeViewModel original = (RecipeTreeRecipeViewModel)cir.getReturnValue();
         List<RecipeTreeInputViewModel> inputs = new ArrayList<>(original.inputs());
         IJeiRuntime runtime = JeiCraftingTreePlugin.getJeiRuntime();
         appendInputs(inputs, recipeSlots.getSlotViews(RecipeIngredientRole.CATALYST), runtime == null ? null : runtime.getIngredientManager(), false, false);
         List<RecipeTreeOutputViewModel> outputs = withGtOutputChances(original.outputs(), recipeSlots);
         cir.setReturnValue(
            new RecipeTreeRecipeViewModel(
               original.primaryOutputIngredient(),
               original.primaryOutput(),
               original.primaryOutputAmount(),
               outputs,
               original.title(),
               original.subtitle(),
               original.subtitleIcon(),
               original.recipeId(),
               inputs
            )
         );
      }
   }

   private static List<RecipeTreeOutputViewModel> withGtOutputChances(List<RecipeTreeOutputViewModel> original, IRecipeSlotsView recipeSlots) {
      List<RecipeTreeOutputViewModel> corrected = new ArrayList<>(original.size());
      int outputIndex = 0;

      for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)) {
         if (hasDisplayedIngredient(slot) && outputIndex < original.size()) {
            RecipeTreeOutputViewModel output = original.get(outputIndex++);
            double chance = GTJeiSlotMetadata.decode(slot).map(GTJeiSlotMetadata::chance).map(Float::doubleValue).orElse(output.chance());
            corrected.add(new RecipeTreeOutputViewModel(output.ingredient(), output.itemStack(), output.amount(), chance, output.primary()));
         }
      }

      while (outputIndex < original.size()) {
         corrected.add(original.get(outputIndex++));
      }

      return corrected;
   }

   private static boolean hasDisplayedIngredient(IRecipeSlotView slot) {
      return slot.getDisplayedIngredient().isPresent() || slot.getAllIngredients().findAny().isPresent();
   }
}
