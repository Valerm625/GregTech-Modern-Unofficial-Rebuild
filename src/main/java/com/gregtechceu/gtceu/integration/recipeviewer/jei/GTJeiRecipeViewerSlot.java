package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerSlotSemantics;
import net.minecraft.world.item.ItemStack;

public final class GTJeiRecipeViewerSlot extends RecipeViewerSlotWidget<GTJeiRecipeViewerSlot> implements IngredientProvider<Object>, RecipeViewerSlotSemantics {
   private EntryList<?> ingredients = ItemStackList.of(ItemStack.EMPTY);
   private RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
   private float chance = 1.0F;
   private boolean perTick;
   private boolean lateBound;

   public GTJeiRecipeViewerSlot() {
      this.size(18, 18);
      this.tooltipAutoUpdate(true);
   }

   public GTJeiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
      this.recipeSlotRole = recipeSlotRole;
      return this;
   }

   public <T> GTJeiRecipeViewerSlot value(EntryList<T> ingredients) {
      this.ingredients = ingredients == null ? ItemStackList.of(ItemStack.EMPTY) : ingredients;
      return this;
   }

   public GTJeiRecipeViewerSlot chance(float chance) {
      this.chance = chance;
      return this;
   }

   public GTJeiRecipeViewerSlot lateBound(boolean lateBound) {
      this.lateBound = lateBound;
      return this;
   }

   public boolean isLateBound() {
      return this.lateBound;
   }

   @Override
   @SuppressWarnings("unchecked")
   public EntryList<Object> getIngredients() {
      return (EntryList<Object>)this.ingredients;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Class<Object> ingredientClass() {
      return (Class<Object>)this.ingredients.getType();
   }

   @Override
   public RecipeSlotRole getRecipeRole() {
      return this.recipeSlotRole;
   }

   public float chance() {
      return this.chance;
   }

   @Override
   public void setPerTick(boolean perTick) {
      this.perTick = perTick;
   }

   @Override
   public boolean isPerTick() {
      return this.perTick;
   }
}
