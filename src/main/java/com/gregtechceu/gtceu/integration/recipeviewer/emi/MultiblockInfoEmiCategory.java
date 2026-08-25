package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import brachy.modularui.integration.emi.EmiRecipeViewerSlot;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MultiblockInfoEmiCategory extends EmiRecipeCategory {
   public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

   private MultiblockInfoEmiCategory() {
      super(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
   }

   public static void registerDisplays(EmiRegistry registry) {
      GTRegistries.MACHINES
         .stream()
         .filter(MultiblockMachineDefinition.class::isInstance)
         .map(MultiblockMachineDefinition.class::cast)
         .filter(MultiblockMachineDefinition::isRenderXEIPreview)
         .filter(definition -> definition.getStructurePatterns().containsKey("main"))
         .map(MultiblockInfoEmiCategory.MultiblockInfoEmiWrapper::new)
         .forEach(registry::addRecipe);
   }

   public Component getName() {
      return Component.translatable("gtceu.jei.multiblock_info");
   }

   public static class MultiblockInfoEmiWrapper extends ModularUIEmiRecipe {
      private final MultiblockMachineDefinition definition;

      public MultiblockInfoEmiWrapper(MultiblockMachineDefinition definition) {
         super(
            definition.getId().withPrefix("multiblock_info/"),
            200,
            180,
            () -> new MultiblockPreviewWidget(
                  definition, null, 200, 180, ignored -> new EmiRecipeViewerSlot(), MultiblockPreviewWidget.CandidateInteractionMode.RECIPE_VIEWER_READ_ONLY
               )
         );
         this.definition = definition;
      }

      public EmiRecipeCategory getCategory() {
         return MultiblockInfoEmiCategory.CATEGORY;
      }

      @Nullable
      public ResourceLocation getId() {
         return this.definition.getId().withPrefix("multiblock_info/");
      }

      public List<EmiIngredient> getInputs() {
         return List.of();
      }

      public List<EmiStack> getOutputs() {
         return List.of(EmiStack.of(this.definition.getBlock()));
      }

      public boolean supportsRecipeTree() {
         return false;
      }

      public boolean hideCraftable() {
         return true;
      }
   }
}
