package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreProcessingRecipeWidget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GTOreProcessingJeiCategory extends SizedModularUIRecipeCategory<GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper> {
   public static final RecipeType<GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper> RECIPE_TYPE = new RecipeType(
      GTCEu.id("ore_processing_diagram"), GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper.class
   );
   private final IDrawable icon;

   public GTOreProcessingJeiCategory(IJeiHelpers helpers) {
      super(v -> new OreProcessingRecipeWidget(v.material), v -> v.material.getResourceLocation(), 176, 166);
      this.icon = helpers.getGuiHelper().createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
   }

   public static void registerRecipes(IRecipeRegistration registry) {
      registry.addRecipes(
         RECIPE_TYPE,
         GTRegistries.MATERIALS
            .stream()
            .filter(material -> material.hasProperty(PropertyKey.ORE) && !material.hasFlag(MaterialFlags.NO_ORE_PROCESSING_TAB))
            .map(GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper::new)
            .toList()
      );
   }

   public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(GTMachines.MACERATOR[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.ORE_WASHER[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.THERMAL_CENTRIFUGE[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.CENTRIFUGE[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.CHEMICAL_BATH[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.ELECTROMAGNETIC_SEPARATOR[1].asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTMachines.SIFTER[1].asStack(), new RecipeType[]{RECIPE_TYPE});
   }

   @NotNull
   public RecipeType<GTOreProcessingJeiCategory.GTOreProcessingInfoWrapper> getRecipeType() {
      return RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable("gtceu.jei.ore_processing_diagram");
   }

   @NotNull
   public IDrawable getIcon() {
      return this.icon;
   }

   public static record GTOreProcessingInfoWrapper(Material material) {
   }
}
