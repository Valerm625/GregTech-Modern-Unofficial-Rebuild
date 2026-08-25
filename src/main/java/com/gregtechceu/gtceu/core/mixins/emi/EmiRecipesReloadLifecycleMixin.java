package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.EmiRecipeBakeLifecycle;
import dev.emi.emi.registry.EmiRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {EmiRecipes.class},
   remap = false
)
public abstract class EmiRecipesReloadLifecycleMixin {
   @Inject(
      method = {"clear"},
      at = {@At(
         value = "INVOKE",
         target = "Ldev/emi/emi/registry/EmiRecipes;setWorker(Ldev/emi/emi/registry/EmiRecipes$Worker;)V",
         shift = Shift.AFTER
      )}
   )
   private static void gtceu$invalidateObsoleteRecipeBakes(CallbackInfo ci) {
      EmiRecipeBakeLifecycle.invalidateWorkers();
   }
}
