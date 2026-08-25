package com.gregtechceu.gtceu.core.mixins.jei;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJeiRecipeViewerSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({Widget.class})
public abstract class ModularUIJeiSlotTooltipMixin {
   @Redirect(
      method = {"drawOverlay"},
      at = @At(
         value = "INVOKE",
         target = "Lbrachy/modularui/api/drawable/IDrawable;drawAtZeroPadded(Lbrachy/modularui/screen/viewport/GuiContext;Lbrachy/modularui/widget/sizer/Area;Lbrachy/modularui/theme/WidgetTheme;)V"
      ),
      remap = false
   )
   private void gtceu$drawContentOverlayAfterJeiIngredient(IDrawable drawable, GuiContext context, Area area, WidgetTheme theme) {
      if (!((Object)this instanceof GTJeiRecipeViewerSlot) || !(drawable instanceof ContentOverlay)) {
         drawable.drawAtZeroPadded(context, area, theme);
      }
   }

   @Redirect(
      method = {"drawForeground"},
      at = @At(
         value = "INVOKE",
         target = "Lbrachy/modularui/screen/RichTooltip;draw(Lbrachy/modularui/screen/viewport/GuiContext;)V"
      ),
      remap = false
   )
   private void gtceu$avoidDuplicateJeiSlotTooltip(RichTooltip tooltip, GuiContext context) {
      if (!((Object)this instanceof GTJeiRecipeViewerSlot)) {
         tooltip.draw(context);
      }
   }
}
