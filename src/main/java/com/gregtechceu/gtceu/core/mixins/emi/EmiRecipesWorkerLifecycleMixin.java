package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.EmiRecipeBakeLifecycle;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"dev.emi.emi.registry.EmiRecipes$Worker"},
   remap = false
)
public abstract class EmiRecipesWorkerLifecycleMixin {
   @Inject(
      method = {"run"},
      at = {@At("HEAD")}
   )
   private void gtceu$beginRecipeBake(CallbackInfo ci) {
      EmiRecipeBakeLifecycle.beginWorker();
   }

   @ModifyArg(
      method = {"run"},
      at = @At(
         value = "INVOKE",
         target = "Ldev/emi/emi/registry/EmiRecipes$Manager;<init>(Ljava/util/List;Ljava/util/Map;Ljava/util/List;Z)V"
      ),
      index = 2
   )
   private List<?> gtceu$makeRecipeBakeCancellationAware(List<?> recipes) {
      return EmiRecipeBakeLifecycle.cancellationAware(recipes);
   }

   @Inject(
      method = {"run"},
      at = {@At(
         value = "FIELD",
         target = "Ldev/emi/emi/registry/EmiRecipes;activeWorker:Ldev/emi/emi/registry/EmiRecipes$Worker;",
         opcode = 178
      )},
      cancellable = true
   )
   private void gtceu$discardCompletedObsoleteBake(CallbackInfo ci) {
      if (EmiRecipeBakeLifecycle.isWorkerObsolete()) {
         EmiRecipeBakeLifecycle.clearActiveWorkerIfCurrent(this);
         EmiRecipeBakeLifecycle.endWorker();
         ci.cancel();
      }
   }

   @Redirect(
      method = {"run"},
      at = @At(
         value = "FIELD",
         target = "Ldev/emi/emi/registry/EmiRecipes;manager:Ldev/emi/emi/api/recipe/EmiRecipeManager;",
         opcode = 179
      )
   )
   private void gtceu$publishOnlyTheCurrentBake(EmiRecipeManager manager) {
      EmiRecipeBakeLifecycle.publishManagerIfCurrent(this, manager);
   }

   @Inject(
      method = {"run"},
      at = {@At(
         value = "INVOKE",
         target = "Ldev/emi/emi/registry/EmiRecipes;setWorker(Ldev/emi/emi/registry/EmiRecipes$Worker;)V"
      )},
      cancellable = true
   )
   private void gtceu$clearOnlyTheCompletingWorker(CallbackInfo ci) {
      boolean handled = EmiRecipeBakeLifecycle.clearActiveWorkerIfCurrent(this);
      EmiRecipeBakeLifecycle.endWorker();
      if (handled) {
         ci.cancel();
      }
   }
}
