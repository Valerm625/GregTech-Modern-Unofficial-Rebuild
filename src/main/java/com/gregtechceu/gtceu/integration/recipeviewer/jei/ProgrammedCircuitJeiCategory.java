package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.ProgrammedCircuitRecipeWidget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProgrammedCircuitJeiCategory extends SizedModularUIRecipeCategory<ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper> {
   public static final RecipeType<ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper> RECIPE_TYPE = new RecipeType(
      GTCEu.id("programmed_circuit"), ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper.class
   );
   private final IDrawable icon;

   public ProgrammedCircuitJeiCategory(IJeiHelpers helpers) {
      super($ -> new ProgrammedCircuitRecipeWidget(), $ -> GTCEu.id("programmed_circuit"), 150, 80);
      this.icon = helpers.getGuiHelper().createDrawableItemStack(GTItems.PROGRAMMED_CIRCUIT.asStack());
   }

   @NotNull
   public RecipeType<ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper> getRecipeType() {
      return RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable("gtceu.jei.programmed_circuit");
   }

   @Nullable
   public IDrawable getIcon() {
      return this.icon;
   }

   public static class GTProgrammedCircuitWrapper {
   }
}
