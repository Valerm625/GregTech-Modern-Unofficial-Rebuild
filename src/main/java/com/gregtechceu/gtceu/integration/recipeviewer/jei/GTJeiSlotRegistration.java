package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class GTJeiSlotRegistration {
   private GTJeiSlotRegistration() {
   }

   public static IIngredientManager getIngredientManager(@Nullable IJeiRuntime runtime) {
      IJeiHelpers helpers = ModularUIJeiPlugin.jeiHelpers;
      if (helpers != null) {
         return helpers.getIngredientManager();
      } else if (runtime != null) {
         return runtime.getIngredientManager();
      } else {
         throw new IllegalStateException("JEI ingredient manager is unavailable during recipe registration");
      }
   }

   public static void addSlot(IRecipeLayoutBuilder builder, IngredientProvider<?> provider, RecipeIngredientRole role, int index) {
      IIngredientManager manager = getIngredientManager(ModularUIJeiPlugin.getRuntime());
      Optional<? extends IIngredientType<?>> checkedType = manager.getIngredientTypeChecked(provider.ingredientClass());
      if (!checkedType.isEmpty()) {
         Area area = provider.getArea();
         IRecipeSlotBuilder slot = builder.addSlot(role, area.x, area.y);
         IIngredientType ingredientType = checkedType.get();
         addFilteredIngredients(slot, ingredientType, provider.getIngredients().getStacks());
         installRenderer(slot, ingredientType, transparentRenderer(area), provider);
         slot.setSlotName(GTJeiSlotMetadata.encodeSlotName(index, provider));
      }
   }

   public static IIngredientAcceptor addFilteredIngredients(IRecipeSlotBuilder slot, IIngredientType ingredientType, List ingredients) {
      List filtered = ingredients;

      for (Object ingredient : ingredients) {
         if (isEmptyIngredient(ingredient)) {
            filtered = new ArrayList(ingredients.size());

            for (Object candidate : ingredients) {
               if (!isEmptyIngredient(candidate)) {
                  filtered.add(candidate);
               }
            }
            break;
         }
      }

      return (IIngredientAcceptor)(filtered.isEmpty() ? slot : slot.addIngredients(ingredientType, filtered));
   }

   public static IRecipeSlotBuilder installRenderer(
      IRecipeSlotBuilder slot, IIngredientType ingredientType, IIngredientRenderer transparentRenderer, IngredientProvider<?> provider
   ) {
      if (provider instanceof GTJeiRecipeViewerSlot jeiSlot) {
         slot.addRichTooltipCallback((view, builder) -> appendRichTooltip(jeiSlot, builder));
         IIngredientManager manager = getIngredientManager(ModularUIJeiPlugin.getRuntime());
         IIngredientRenderer delegate = manager.getIngredientRenderer(ingredientType);
         if (!(jeiSlot.getOverlay() instanceof ContentOverlay value)) {
            Object var10000 = null;
         }

         hideOccupiedSlotHint(jeiSlot, provider);
         return slot.setCustomRenderer(
            ingredientType,
            new CenteredJeiIngredientRenderer(
               delegate,
               transparentRenderer.getWidth(),
               transparentRenderer.getHeight(),
               () -> jeiSlot.getOverlay() instanceof ContentOverlay valuex ? valuex : null
            )
         );
      } else {
         return slot.setCustomRenderer(ingredientType, transparentRenderer);
      }
   }

   private static IIngredientRenderer<Object> transparentRenderer(final Area area) {
      return new IIngredientRenderer<Object>() {
         public void render(GuiGraphics graphics, Object ingredient) {
         }

         public List<Component> getTooltip(Object ingredient, TooltipFlag flag) {
            return List.of();
         }

         public int getWidth() {
            return area.width;
         }

         public int getHeight() {
            return area.height;
         }
      };
   }

   private static boolean isEmptyIngredient(Object ingredient) {
      if (ingredient instanceof ItemStack itemStack && itemStack.isEmpty() || ingredient instanceof FluidStack fluidStack && fluidStack.isEmpty()) {
         return true;
      }

      return false;
   }

   private static void hideOccupiedSlotHint(GTJeiRecipeViewerSlot slot, IngredientProvider<?> provider) {
      boolean occupied = false;

      for (Object ingredient : provider.getIngredients().getStacks()) {
         if (!isEmptyIngredient(ingredient)) {
            occupied = true;
            break;
         }
      }

      if (occupied) {
         if (slot.getBackground() instanceof DrawableStack stack) {
            IDrawable[] layers = stack.drawables();
            if (layers.length == 2 && layers[0] != null) {
               slot.background(new IDrawable[]{layers[0]});
            }
         }
      }
   }

   private static void appendRichTooltip(GTJeiRecipeViewerSlot slot, ITooltipBuilder builder) {
      RichTooltip tooltip = slot.getTooltip();
      if (tooltip != null) {
         if (tooltip.autoUpdate()) {
            tooltip.markDirty();
         }

         tooltip.buildTooltip();
         if (tooltip.getRichText() instanceof RichText text) {
            HashSet<String> existing = new HashSet<>();
            builder.getLines().forEach(line -> existing.add(line.toString()));
            text.getAsText().stream().filter(line -> existing.add(line.toString())).forEach(builder.getLines()::add);
         }
      }
   }
}
