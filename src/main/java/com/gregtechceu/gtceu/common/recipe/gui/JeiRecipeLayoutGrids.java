package com.gregtechceu.gtceu.common.recipe.gui;

import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;

public final class JeiRecipeLayoutGrids implements GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder {
   public static final GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder ASSEMBLY_ITEM_INPUT = new JeiRecipeLayoutGrids("ssss", "ssss", "ssss", "ssss");
   public static final GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder ASSEMBLY_FLUID_INPUT = new JeiRecipeLayoutGrids("s", "s", "s", "s");
   public static final GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder RESEARCH_ITEM_INPUT = new JeiRecipeLayoutGrids("ss");
   private final String[] rows;

   private JeiRecipeLayoutGrids(String... var1) {
      this.rows = (String[])var1.clone();
   }

   @Override
   public String[] buildGrid(GTRecipeTypeUILayout var1) {
      return (String[])this.rows.clone();
   }
}
