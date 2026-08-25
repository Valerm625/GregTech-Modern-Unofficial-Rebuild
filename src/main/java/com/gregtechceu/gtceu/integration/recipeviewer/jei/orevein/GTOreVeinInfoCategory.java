package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.SizedModularUIRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public class GTOreVeinInfoCategory extends SizedModularUIRecipeCategory<GTOreVeinInfoCategory.GTOreVeinInfoWrapper> {
   public static final RecipeType<GTOreVeinInfoCategory.GTOreVeinInfoWrapper> RECIPE_TYPE = new RecipeType(
      GTCEu.id("ore_vein_diagram"), GTOreVeinInfoCategory.GTOreVeinInfoWrapper.class
   );
   private final IDrawable icon;

   public GTOreVeinInfoCategory(IJeiHelpers helpers) {
      super(
         v -> new OreVeinRecipeWidget(v.oreDefinition),
         v -> Objects.requireNonNull(Minecraft.getInstance().level).registryAccess().registryOrThrow(GTRegistries.Keys.ORE_VEIN).getKey(v.oreDefinition),
         120,
         160
      );
      this.icon = helpers.getGuiHelper().createDrawableItemStack(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Iron));
   }

   public static void registerRecipes(IRecipeRegistration registry) {
      registry.addRecipes(
         RECIPE_TYPE,
         Minecraft.getInstance()
            .level
            .registryAccess()
            .registryOrThrow(GTRegistries.Keys.ORE_VEIN)
            .stream()
            .map(GTOreVeinInfoCategory.GTOreVeinInfoWrapper::new)
            .toList()
      );
   }

   public void setRecipe(IRecipeLayoutBuilder builder, GTOreVeinInfoCategory.GTOreVeinInfoWrapper wrapper, IFocusGroup focuses) {
      super.setRecipe(builder, wrapper, focuses);
      builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(OreVeinRecipeWidget.getContainedOresAndBlocks(wrapper.oreDefinition));
   }

   public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_LV.asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), new RecipeType[]{RECIPE_TYPE});
   }

   @NotNull
   public RecipeType<GTOreVeinInfoCategory.GTOreVeinInfoWrapper> getRecipeType() {
      return RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable("gtceu.jei.ore_vein_diagram");
   }

   @NotNull
   public IDrawable getIcon() {
      return this.icon;
   }

   public static record GTOreVeinInfoWrapper(GTOreDefinition oreDefinition) {
   }
}
