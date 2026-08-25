package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class OreProcessingRecipeWidget extends ParentWidget<OreProcessingRecipeWidget> {
   public static final int WIDTH = 176;
   public static final int HEIGHT = 166;
   protected static final IntImmutableList ITEM_INPUT_LOCATIONS = IntImmutableList.of(
      new int[]{3, 3, 23, 3, 3, 24, 23, 71, 50, 80, 24, 25, 97, 71, 70, 80, 114, 48, 133, 71, 3, 123, 41, 145, 102, 145, 24, 48, 155, 71, 101, 25}
   );
   protected static final IntImmutableList ITEM_OUTPUT_LOCATIONS = IntImmutableList.of(
      new int[]{
         46,
         3,
         3,
         47,
         3,
         65,
         23,
         92,
         23,
         110,
         50,
         101,
         50,
         119,
         64,
         25,
         82,
         25,
         97,
         92,
         97,
         110,
         70,
         101,
         70,
         119,
         137,
         47,
         155,
         47,
         133,
         92,
         133,
         110,
         3,
         105,
         3,
         145,
         23,
         145,
         63,
         145,
         84,
         145,
         124,
         145,
         64,
         48,
         82,
         48,
         155,
         92,
         155,
         110,
         155,
         128,
         119,
         3,
         137,
         3,
         155,
         3,
         119,
         21,
         137,
         21,
         155,
         21
      }
   );
   protected static final IntImmutableList FLUID_LOCATIONS = IntImmutableList.of(new int[]{42, 25, 42, 48});
   protected static final IntSet FINAL_OUTPUT_INDICES = IntSet.of(
      new int[]{0, 4, 8, 10, 12, 16, 20, 22, 24, 28, 30, 32, 40, 44, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66}
   );

   public OreProcessingRecipeWidget(Material material) {
      this.size(176, 166);
      this.setRecipe(new GTOreByProduct(material));
   }

   public void setRecipe(GTOreByProduct recipeWrapper) {
      boolean hasSifter = recipeWrapper.hasSifter();
      this.child((IWidget)GTGuiTextures.OREBY_BASE.asWidget().size(176, 166));
      if (recipeWrapper.hasDirectSmelt()) {
         this.child((IWidget)GTGuiTextures.OREBY_SMELT.asWidget().size(176, 166));
      }

      if (recipeWrapper.hasChemBath()) {
         this.child((IWidget)GTGuiTextures.OREBY_CHEM.asWidget().size(176, 166));
      }

      if (recipeWrapper.hasSeparator()) {
         this.child((IWidget)GTGuiTextures.OREBY_SEP.asWidget().size(176, 166));
      }

      if (hasSifter) {
         this.child((IWidget)GTGuiTextures.OREBY_SIFT.asWidget().size(176, 166));
      }

      List<ItemEntryList> itemInputs = recipeWrapper.itemInputs;
      ParentWidget<?> itemStackGroup = (ParentWidget<?>)new ParentWidget().sizeRel(1.0F);

      for (int i = 0; i < ITEM_INPUT_LOCATIONS.size(); i += 2) {
         itemStackGroup.child(
            ((RecipeViewerSlotWidget)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
                     .recipeSlotRole(RecipeSlotRole.INPUT)
                     .pos(ITEM_INPUT_LOCATIONS.getInt(i), ITEM_INPUT_LOCATIONS.getInt(i + 1)))
                  .tooltipBuilder(recipeWrapper.getTooltip(i / 2)))
               .value((EntryList)itemInputs.get(i / 2))
               .background(new IDrawable[]{(IDrawable)(i == 0 ? GuiTextures.SLOT_ITEM : IDrawable.NONE)})
         );
      }

      NonNullList<ItemStack> itemOutputs = recipeWrapper.itemOutputs;

      for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
         int slotIndex = i / 2;
         Content chance = recipeWrapper.getChance(i / 2 + itemInputs.size());
         IDrawable overlay = null;
         if (chance != null) {
            overlay = new ContentOverlay(chance, false);
         }

         if (!((ItemStack)itemOutputs.get(slotIndex)).isEmpty()) {
            itemStackGroup.child(
               ((RecipeViewerSlotWidget)((RecipeViewerSlotWidget)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
                           .pos(ITEM_OUTPUT_LOCATIONS.getInt(i), ITEM_OUTPUT_LOCATIONS.getInt(i + 1)))
                        .recipeSlotRole(RecipeSlotRole.OUTPUT)
                        .tooltip(recipeWrapper.getTooltip(slotIndex + itemInputs.size())))
                     .overlay(new IDrawable[]{overlay}))
                  .value((ItemStack)itemOutputs.get(i / 2))
            );
         }
      }

      List<FluidEntryList> fluidInputs = recipeWrapper.fluidInputs;
      ParentWidget<?> fluidStackGroup = (ParentWidget<?>)new ParentWidget().sizeRel(1.0F);

      for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
         int slotIndexx = i / 2;
         if (!fluidInputs.get(slotIndexx).isEmpty()) {
            fluidStackGroup.child(
               ((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
                     .recipeSlotRole(RecipeSlotRole.INPUT)
                     .pos(FLUID_LOCATIONS.getInt(i), FLUID_LOCATIONS.getInt(i + 1)))
                  .value((EntryList)fluidInputs.get(slotIndexx))
            );
         }
      }

      this.child(itemStackGroup);
      this.child(fluidStackGroup);
   }
}
