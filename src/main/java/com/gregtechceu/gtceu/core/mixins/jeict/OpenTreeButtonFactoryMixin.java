package com.gregtechceu.gtceu.core.mixins.jeict;

import com.lhy.jeict.jei.OpenTreeButtonFactory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {OpenTreeButtonFactory.class},
   remap = false
)
public abstract class OpenTreeButtonFactoryMixin {
   @Inject(
      method = {"createButtonController"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      expect = 1,
      allow = 1,
      remap = false
   )
   private <T> void gtceu$removeRecipeTreeButton(IRecipeLayoutDrawable<T> recipeLayout, CallbackInfoReturnable<IIconButtonController> cir) {
      cir.setReturnValue(null);
   }
}
