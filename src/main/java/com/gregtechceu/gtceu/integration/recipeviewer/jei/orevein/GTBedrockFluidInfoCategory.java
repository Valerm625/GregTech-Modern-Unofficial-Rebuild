package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.SizedModularUIRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;
import java.util.Objects;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GTBedrockFluidInfoCategory extends SizedModularUIRecipeCategory<GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper> {
   public static final RecipeType<GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper> RECIPE_TYPE = new RecipeType(
      GTCEu.id("bedrock_fluid_diagram"), GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper.class
   );
   private final IDrawable icon;

   public GTBedrockFluidInfoCategory(IJeiHelpers helpers) {
      super(
         v -> new OreVeinRecipeWidget(v.fluid),
         v -> Objects.requireNonNull(Minecraft.getInstance().level).registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID).getKey(v.fluid),
         120,
         140
      );
      this.icon = helpers.getGuiHelper().createDrawableItemStack(GTMaterials.Oil.getBucket().getDefaultInstance());
   }

   public static void registerRecipes(IRecipeRegistration registry) {
      registry.addRecipes(
         RECIPE_TYPE,
         Objects.requireNonNull(Minecraft.getInstance().level)
            .registryAccess()
            .registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID)
            .stream()
            .map(GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper::new)
            .toList()
      );
   }

   public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), new RecipeType[]{RECIPE_TYPE});
   }

   @NotNull
   public RecipeType<GTBedrockFluidInfoCategory.BedrockFluidInfoWrapper> getRecipeType() {
      return RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
   }

   @NotNull
   public IDrawable getIcon() {
      return this.icon;
   }

   public static record BedrockFluidInfoWrapper(BedrockFluidDefinition fluid) {
   }
}
