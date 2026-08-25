package com.gregtechceu.gtceu.core.mixins.jeict;

import com.lhy.jeict.client.JeiRecipeTreeShortcutOverlay;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Post;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {JeiRecipeTreeShortcutOverlay.class},
   remap = false
)
public abstract class JeiRecipeTreeShortcutOverlayMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      expect = 1,
      allow = 1,
      remap = false
   )
   private static void gtceu$hideLowerLeftShortcut(Post event, CallbackInfo ci) {
      ci.cancel();
   }

   @Inject(
      method = {"handleMousePressed"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 1,
      expect = 1,
      allow = 1,
      remap = false
   )
   private static void gtceu$ignoreHiddenShortcut(Pre event, CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(false);
   }
}
