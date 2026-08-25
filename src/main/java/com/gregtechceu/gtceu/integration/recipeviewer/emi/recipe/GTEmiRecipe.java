package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerChanceSemantics;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class GTEmiRecipe extends ModularUIEmiRecipe {
   final EmiRecipeCategory category;
   final GTRecipe recipe;

   public GTEmiRecipe(GTRecipe recipe, EmiRecipeCategory category) {
      super(recipe.getId(), () -> new GTRecipeViewerWidget(recipe));
      this.category = category;
      this.recipe = recipe;
      this.calculateSize();
   }

   public EmiRecipeCategory getCategory() {
      return this.category;
   }

   public List<EmiIngredient> getInputs() {
      List<EmiIngredient> ingredients = new ObjectArrayList();
      List<Content> items = this.recipe.getInputContents(ItemRecipeCapability.CAP);
      List<Content> fluids = this.recipe.getInputContents(FluidRecipeCapability.CAP);

      for (Content itemContent : items) {
         float chance = RecipeViewerChanceSemantics.effectiveChance(this.recipe, ItemRecipeCapability.CAP, IO.IN, false, itemContent);
         ItemEntryList mapped = ItemRecipeCapability.mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));
         if (!mapped.isEmpty()) {
            ingredients.add(EmiStackConverter.ITEM.convertTo(mapped, chance));
         }
      }

      for (Content fluidContent : fluids) {
         float chance = RecipeViewerChanceSemantics.effectiveChance(this.recipe, FluidRecipeCapability.CAP, IO.IN, false, fluidContent);
         FluidEntryList mapped = FluidRecipeCapability.mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));
         if (!mapped.isEmpty()) {
            ingredients.add(EmiStackConverter.FLUID.convertTo(mapped, chance));
         }
      }

      return ingredients;
   }

   public List<EmiStack> getOutputs() {
      List<EmiStack> outputs = new ObjectArrayList();
      List<Content> items = this.recipe.getOutputContents(ItemRecipeCapability.CAP);
      List<Content> fluids = this.recipe.getOutputContents(FluidRecipeCapability.CAP);

      for (Content itemContent : items) {
         float chance = RecipeViewerChanceSemantics.effectiveChance(this.recipe, ItemRecipeCapability.CAP, IO.OUT, false, itemContent);
         ItemEntryList mapped = ItemRecipeCapability.mapIngredientToEntryList(ItemRecipeCapability.CAP.of(itemContent.content()));
         List<ItemStack> stacks = mapped.getStacks();
         if (!stacks.isEmpty()) {
            outputs.add(EmiStack.of(stacks.get(0)).setChance(chance));
         }
      }

      for (Content fluidContent : fluids) {
         float chance = RecipeViewerChanceSemantics.effectiveChance(this.recipe, FluidRecipeCapability.CAP, IO.OUT, false, fluidContent);
         FluidEntryList mapped = FluidRecipeCapability.mapIngredientToEntryList(FluidRecipeCapability.CAP.of(fluidContent.content()));
         List<FluidStack> stacks = mapped.getStacks();
         if (!stacks.isEmpty()) {
            FluidStack fluid = stacks.get(0);
            outputs.add(EmiStack.of(fluid.getFluid(), (long)fluid.getAmount()).setChance(chance));
         }
      }

      return outputs;
   }
}
