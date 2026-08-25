package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import java.util.List;
import java.util.function.Supplier;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CenteredJeiIngredientRenderer<T> implements IIngredientRenderer<T> {
   private final IIngredientRenderer<T> delegate;
   private final int width;
   private final int height;
   private final Supplier<ContentOverlay> contentOverlaySupplier;

   public CenteredJeiIngredientRenderer(IIngredientRenderer<T> delegate, int width, int height, Supplier<ContentOverlay> contentOverlaySupplier) {
      this.delegate = delegate;
      this.width = width;
      this.height = height;
      this.contentOverlaySupplier = contentOverlaySupplier;
   }

   public void render(GuiGraphics graphics, T ingredient) {
      int x;
      int y;
      ContentOverlay contentOverlay;
      T displayIngredient;
      label34: {
         x = Math.max(0, (this.width - this.delegate.getWidth()) / 2);
         y = Math.max(0, (this.height - this.delegate.getHeight()) / 2);
         contentOverlay = this.contentOverlaySupplier.get();
         displayIngredient = ingredient;
         if (ingredient instanceof ItemStack stack && !stack.isEmpty() && stack.getCount() != 1) {
            int delegateCount = JeiIngredientDisplayPolicy.delegateItemCount(stack.getCount(), contentOverlay != null && contentOverlay.hasItemRangeAmount());
            if (delegateCount != stack.getCount()) {
               ItemStack displayStack = stack.copy();
               displayStack.setCount(delegateCount);
               displayIngredient = (T)displayStack;
            }
            break label34;
         }

         if (ingredient instanceof FluidStack fluid) {
            FluidStack full = fluid.copy();
            full.setAmount(1000);
            displayIngredient = (T)full;
         }
      }

      this.delegate.render(graphics, displayIngredient, x, y);
      if (contentOverlay != null) {
         contentOverlay.drawChance(graphics, 0.0F, 0.0F, this.width, this.height);
         contentOverlay.drawRangeAmount(graphics, 0.0F, 0.0F, this.width, this.height);
         contentOverlay.drawFluidAmount(graphics, 0.0F, 0.0F, this.width, this.height);
         if (contentOverlay.perTick()) {
            contentOverlay.drawTick(graphics, 0.0F, 0.0F, this.width, this.height);
         }
      }
   }

   public List<Component> getTooltip(T ingredient, TooltipFlag flag) {
      return this.delegate.getTooltip(ingredient, flag);
   }

   public void getTooltip(ITooltipBuilder builder, T ingredient, TooltipFlag flag) {
      this.delegate.getTooltip(builder, ingredient, flag);
   }

   public Font getFontRenderer(Minecraft minecraft, T ingredient) {
      return this.delegate.getFontRenderer(minecraft, ingredient);
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}
