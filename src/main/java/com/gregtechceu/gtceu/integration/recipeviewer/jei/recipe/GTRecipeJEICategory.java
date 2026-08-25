package com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe;

import brachy.modularui.api.widget.IWidget;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.gui.JeiRecipeTypeCustomLayouts;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.SizedModularUIRecipeCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.jeict.GTJeictRecipeSemantics;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTRecipeJEICategory<T extends Recipe<?>> extends SizedModularUIRecipeCategory<T> {
   public static final Function<GTRecipeCategory, RecipeType<GTRecipe>> TYPES = Util.memoize(c -> new RecipeType<>(c.registryKey, GTRecipe.class));
   @Nullable
   private final GTRecipeCategory category;
   @Nullable
   private final IDrawable icon;

   /** Restores the Neo mother extension constructor used by third-party JEI categories. */
   protected GTRecipeJEICategory(Function<T, IWidget> wrapperFunction, Function<T, ResourceLocation> recipeIdGetter) {
      this(wrapperFunction, recipeIdGetter, 176, 166);
   }

   /** Sized extension point for rebuilt addons that need a non-default recipe canvas. */
   protected GTRecipeJEICategory(
      Function<T, IWidget> wrapperFunction, Function<T, ResourceLocation> recipeIdGetter, int width, int height
   ) {
      super(wrapperFunction, recipeIdGetter, width, height);
      this.category = null;
      this.icon = null;
   }

   public GTRecipeJEICategory(IJeiHelpers helpers, GTRecipeCategory category) {
      super(
         recipe -> new GTRecipeViewerWidget(requireGTRecipe(recipe)),
         recipe -> requireGTRecipe(recipe).getId(),
         JeiRecipeTypeCustomLayouts.categoryWidth(category),
         JeiRecipeTypeCustomLayouts.categoryHeight(category)
      );
      this.category = category;
      this.icon = category.getIcon().getJeiDrawable(helpers.getGuiHelper());
   }

   @NotNull
   @SuppressWarnings("unchecked")
   public RecipeType<T> getRecipeType() {
      return (RecipeType<T>)(RecipeType<?>)TYPES.apply(this.requireTargetCategory());
   }

   @NotNull
   public Component getTitle() {
      return Component.translatable(this.requireTargetCategory().getLanguageKey());
   }

   @NotNull
   public IDrawable getIcon() {
      if (this.icon == null) {
         throw new IllegalStateException("Legacy GTRecipeJEICategory subclasses must override getIcon()");
      }
      return this.icon;
   }

   @Nullable
   public ResourceLocation getRegistryName(T recipe) {
      return recipe instanceof GTRecipe gtRecipe ? this.getRegistryName(gtRecipe) : null;
   }

   @Nullable
   public ResourceLocation getRegistryName(GTRecipe recipe) {
      GTJeictRecipeSemantics.remember(recipe);
      return recipe.getId();
   }

   private GTRecipeCategory requireTargetCategory() {
      if (this.category == null) {
         throw new IllegalStateException("Legacy GTRecipeJEICategory subclasses must override this category method");
      }
      return this.category;
   }

   private static GTRecipe requireGTRecipe(Recipe<?> recipe) {
      if (recipe instanceof GTRecipe gtRecipe) return gtRecipe;
      throw new IllegalArgumentException("The built-in GT recipe category only accepts GTRecipe instances");
   }

   public static void registerRecipes(IRecipeRegistration registration) {
      List<GTRecipeCategory> subCategories = new ArrayList<>();

      for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
         if (category.shouldRegisterDisplays()) {
            GTRecipeType type = category.getRecipeType();
            if (category == type.getCategory()) {
               type.buildRepresentativeRecipes();
               List wrapped = List.copyOf(type.getRecipesInCategory(category));
               registration.addRecipes(TYPES.apply(category), wrapped);
            } else {
               subCategories.add(category);
            }
         }
      }

      for (GTRecipeCategory subCategory : subCategories) {
         if (subCategory.shouldRegisterDisplays()) {
            GTRecipeType type = subCategory.getRecipeType();
            List<GTRecipe> wrapped = List.copyOf(type.getRecipesInCategory(subCategory));
            registration.addRecipes(TYPES.apply(subCategory), wrapped);
         }
      }
   }

   public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      for (MachineDefinition machine : GTRegistries.MACHINES) {
         for (GTRecipeType type : machine.getRecipeTypes()) {
            for (GTRecipeCategory category : type.getCategories()) {
               if (category.isXEIVisible() || GTCEu.isDev()) {
                  registration.addRecipeCatalyst(machine.asStack(), new RecipeType[]{machineType(category)});
               }
            }
         }
      }
   }

   public static RecipeType<?> machineType(GTRecipeCategory category) {
      return category == GTRecipeTypes.FURNACE_RECIPES.getCategory() ? RecipeTypes.SMELTING : TYPES.apply(category);
   }
}
