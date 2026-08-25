package com.gregtechceu.gtceu.core.mixins.jei;

import brachy.modularui.ModularUI.Mods;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.GTJeiRecipeViewerSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {RecipeViewerSlotWidget.class},
   remap = false
)
public abstract class RecipeViewerSlotWidgetFactoryMixin {
   @Inject(
      method = {"create()Lbrachy/modularui/integration/recipeviewer/RecipeViewerSlotWidget;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      expect = 1,
      allow = 1,
      remap = false
   )
   private static void gtceu$createImplementedJeiSlot(CallbackInfoReturnable<RecipeViewerSlotWidget<?>> cir) {
      if (Mods.JEI.isLoaded() && !Mods.EMI.isLoaded() && !Mods.REI.isLoaded()) {
         cir.setReturnValue(new GTJeiRecipeViewerSlot());
      }
   }
}
