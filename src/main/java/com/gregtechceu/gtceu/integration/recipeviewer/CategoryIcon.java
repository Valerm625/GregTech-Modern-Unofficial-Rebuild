package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJEIPlugin;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CategoryIcon {
   private final ResourceLocation texture;
   private final ItemStack stack;
   private Object wrappedValue;

   public CategoryIcon(ResourceLocation texture) {
      this.texture = texture;
      this.stack = null;
   }

   public CategoryIcon(ItemStack stack) {
      this.texture = null;
      this.stack = stack.copy();
   }

   public Object get() {
      if (this.wrappedValue == null && GTCEu.isClientSide()) {
         if (GTCEu.Mods.isEMILoaded()) {
            this.wrappedValue = this.texture != null
               ? CategoryIcon.EmiCallWrapper.getRenderable(this.texture)
               : CategoryIcon.EmiCallWrapper.getRenderable(this.stack);
         } else if (GTCEu.Mods.isJEILoaded() && GTJEIPlugin.getRuntime() != null) {
            this.wrappedValue = this.texture != null
               ? CategoryIcon.JeiCallWrapper.getRenderable(this.texture)
               : CategoryIcon.JeiCallWrapper.getRenderable(this.stack);
         }

         return this.wrappedValue;
      } else {
         return this.wrappedValue;
      }
   }

   public IDrawable getJeiDrawable(IGuiHelper guiHelper) {
      return this.texture != null
         ? CategoryIcon.JeiCallWrapper.getRenderable(guiHelper, this.texture)
         : CategoryIcon.JeiCallWrapper.getRenderable(guiHelper, this.stack);
   }

   private static class EmiCallWrapper {
      public static EmiRenderable getRenderable(ResourceLocation location) {
         return new EmiTexture(location, 0, 0, 16, 16, 16, 16, 16, 16);
      }

      public static EmiRenderable getRenderable(ItemStack stack) {
         return EmiStack.of(stack);
      }
   }

   private static class JeiCallWrapper {
      public static IDrawable getRenderable(ResourceLocation location) {
         return getRenderable(GTJEIPlugin.getRuntime().getJeiHelpers().getGuiHelper(), location);
      }

      public static IDrawable getRenderable(IGuiHelper guiHelper, ResourceLocation location) {
         return guiHelper.drawableBuilder(location, 0, 0, 16, 16).setTextureSize(16, 16).build();
      }

      public static IDrawable getRenderable(ItemStack stack) {
         return getRenderable(GTJEIPlugin.getRuntime().getJeiHelpers().getGuiHelper(), stack);
      }

      public static IDrawable getRenderable(IGuiHelper guiHelper, ItemStack stack) {
         return guiHelper.createDrawableItemStack(stack);
      }
   }
}
