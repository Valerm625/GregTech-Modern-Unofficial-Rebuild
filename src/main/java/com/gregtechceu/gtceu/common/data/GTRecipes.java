package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.MaterialInfoLoader;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeAddition;
import com.gregtechceu.gtceu.data.recipe.configurable.RecipeRemoval;
import com.gregtechceu.gtceu.data.recipe.generated.DecompositionRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.MaterialRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.PartsRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.PipeRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.PolarizingRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.RecyclingRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.generated.WireCombiningHandler;
import com.gregtechceu.gtceu.data.recipe.generated.WireRecipeHandler;
import com.gregtechceu.gtceu.data.recipe.misc.AirScrubberRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.AssemblerRecipeLoader;
import com.gregtechceu.gtceu.data.recipe.misc.AssemblyLineLoader;
import com.gregtechceu.gtceu.data.recipe.misc.BatteryRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.CircuitRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.ComponentRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.CraftingRecipeLoader;
import com.gregtechceu.gtceu.data.recipe.misc.CustomToolRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.DecorationRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.FuelRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.FusionLoader;
import com.gregtechceu.gtceu.data.recipe.misc.GCYMRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.MachineRecipeLoader;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityMachineRecipeLoader;
import com.gregtechceu.gtceu.data.recipe.misc.MiscRecipeLoader;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.StoneMachineRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.VanillaStandardRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.ChemistryRecipes;
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTRecipes {
   public static final Set<ResourceLocation> RECIPE_FILTERS = new ObjectOpenHashSet();

   public static void recipeAddition(final RecipeOutput originalConsumer) {
      try (ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope ignored = ConfigHolder.INSTANCE.recipes.beginAtm10VanillaRecipeRestoration()) {
         RecipeOutput consumer = new RecipeOutput() {
            @NotNull
            public Builder advancement() {
               return originalConsumer.advancement();
            }

            public void accept(
               @NotNull ResourceLocation id, @NotNull Recipe<?> recipe, @Nullable AdvancementHolder advancement, @NotNull ICondition... conditions
            ) {
               if (!GTRecipes.RECIPE_FILTERS.contains(id)) {
                  originalConsumer.accept(id, recipe, advancement, conditions);
               }
            }
         };
         ItemMaterialData.reinitializeMaterialData();
         MaterialInfoLoader.init();

         for (Material material : GTRegistries.MATERIALS) {
            if (!material.hasFlag(MaterialFlags.DISABLE_MATERIAL_RECIPES)) {
               DecompositionRecipeHandler.run(consumer, material);
               MaterialRecipeHandler.run(consumer, material);
               OreRecipeHandler.run(consumer, material);
               PartsRecipeHandler.run(consumer, material);
               PipeRecipeHandler.run(consumer, material);
               PolarizingRecipeHandler.run(consumer, material);
               RecyclingRecipeHandler.run(consumer, material);
               ToolRecipeHandler.run(consumer, material);
               WireCombiningHandler.run(consumer, material);
               WireRecipeHandler.run(consumer, material);
            }
         }

         CustomToolRecipes.init(consumer);
         AirScrubberRecipes.init(consumer);
         ChemistryRecipes.init(consumer);
         MetaTileEntityMachineRecipeLoader.init(consumer);
         MiscRecipeLoader.init(consumer);
         VanillaStandardRecipes.init(consumer);
         WoodMachineRecipes.init(consumer);
         StoneMachineRecipes.init(consumer);
         CraftingRecipeLoader.init(consumer);
         FuelRecipes.init(consumer);
         FusionLoader.init(consumer);
         MachineRecipeLoader.init(consumer);
         AssemblerRecipeLoader.init(consumer);
         AssemblyLineLoader.init(consumer);
         BatteryRecipes.init(consumer);
         DecorationRecipes.init(consumer);
         CircuitRecipes.init(consumer);
         ComponentRecipes.init(consumer);
         MetaTileEntityLoader.init(consumer);
         GCYMRecipes.init(consumer);
         RecipeAddition.init(consumer);
         AddonFinder.getAddonList().forEach(addon -> addon.addRecipes(consumer));
         if (!GTCEu.Mods.isKubeJSLoaded() || !GTRecipes.KJSCallWrapper.recipeEventHasListeners()) {
            RecyclingRecipes.init(consumer);
            ItemMaterialData.resolveItemMaterialInfos(consumer);
         }
      }
   }

   public static void recipeRemoval(Consumer<ResourceLocation> registry) {
      try (ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope ignored = ConfigHolder.INSTANCE.recipes.beginAtm10VanillaRecipeRestoration()) {
         Consumer<ResourceLocation> actualConsumer = registry.andThen(RECIPE_FILTERS::add);
         RECIPE_FILTERS.clear();
         RecipeRemoval.init(actualConsumer);
         AddonFinder.getAddonList().forEach(addon -> addon.removeRecipes(actualConsumer));
      }
   }

   private static class KJSCallWrapper {
      private static boolean recipeEventHasListeners() {
         return ServerEvents.RECIPES.hasListeners();
      }
   }
}
