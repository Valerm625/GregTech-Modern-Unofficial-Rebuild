package com.gregtechceu.gtceu.core.mixins.dragonsurvival;

import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.CuriosClientConfig;

@Mixin(
   targets = {"by.dragonsurvivalteam.dragonsurvival.compat.curios.CuriosButtonHandler"},
   remap = false
)
public abstract class DragonSurvivalCuriosConfigMixin {
   @Inject(
      method = {"handleCurios"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void gtceu$skipUntilCuriosConfigIsReady(Post event, CallbackInfo callback) {
      try {
         CuriosClientConfig.CLIENT.enableButton.get();
      } catch (IllegalStateException var3) {
         callback.cancel();
      }
   }
}
