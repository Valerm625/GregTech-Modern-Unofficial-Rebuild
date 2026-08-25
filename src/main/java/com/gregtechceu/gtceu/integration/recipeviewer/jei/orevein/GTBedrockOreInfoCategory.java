package com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.SizedModularUIRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;
import java.util.Objects;
import lombok.Generated;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class GTBedrockOreInfoCategory extends SizedModularUIRecipeCategory<GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper> {
   public static final RecipeType<GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper> RECIPE_TYPE = new RecipeType(
      GTCEu.id("bedrock_ore_diagram"), GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper.class
   );
   private final IDrawable icon;

   public GTBedrockOreInfoCategory(IJeiHelpers helpers) {
      super(
         v -> new OreVeinRecipeWidget(v.bedrockOre),
         v -> Objects.requireNonNull(Minecraft.getInstance().level).registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_ORE).getKey(v.bedrockOre),
         120,
         140
      );
      this.icon = helpers.getGuiHelper().createDrawableItemStack(Items.RAW_IRON.getDefaultInstance());
   }

   public static void registerRecipes(IRecipeRegistration registry) {
      registry.addRecipes(
         RECIPE_TYPE,
         Objects.requireNonNull(Minecraft.getInstance().level)
            .registryAccess()
            .registryOrThrow(GTRegistries.Keys.BEDROCK_ORE)
            .stream()
            .map(GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper::new)
            .toList()
      );
   }

   public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_HV.asStack(), new RecipeType[]{RECIPE_TYPE});
      registration.addRecipeCatalyst(GTItems.PROSPECTOR_LuV.asStack(), new RecipeType[]{RECIPE_TYPE});
   }

   @NotNull
   public RecipeType<GTBedrockOreInfoCategory.GTBedrockOreInfoWrapper> getRecipeType() {
      return RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable("gtceu.jei.bedrock_ore_diagram");
   }

   @Generated
   public IDrawable getIcon() {
      return this.icon;
   }

   public static record GTBedrockOreInfoWrapper(BedrockOreDefinition bedrockOre) {
   }
}
