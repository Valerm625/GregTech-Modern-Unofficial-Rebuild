package com.gregtechceu.gtceu.core.mixins.client;

import brachy.modularui.ModularUIConfig;
import brachy.modularui.test.TestHandler;
import com.gregtechceu.gtceu.client.util.ModularUIConfigSafety;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {TestHandler.class},
   remap = false
)
public abstract class ModularUITestHandlerMixin {
   @Redirect(
      method = {"onOpenScreen"},
      at = @At(
         value = "INVOKE",
         target = "Lbrachy/modularui/ModularUIConfig;enableTestOverlays()Z"
      )
   )
   private static boolean gtceu$readTestOverlayConfigAfterItLoads() {
      return ModularUIConfigSafety.readBooleanOrFalse(ModularUIConfig::enableTestOverlays);
   }
}
