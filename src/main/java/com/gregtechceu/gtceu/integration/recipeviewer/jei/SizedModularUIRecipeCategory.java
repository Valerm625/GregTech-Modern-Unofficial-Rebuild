package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.widget.WidgetTree;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class SizedModularUIRecipeCategory<T> extends ModularUIRecipeCategory<T> {
   private final int width;
   private final int height;
   private final LoadingCache<T, ModularScreen> modularScreenCache;

   protected SizedModularUIRecipeCategory(
      final Function<T, IWidget> widgetFactory, final Function<T, ResourceLocation> recipeIdGetter, final int width, final int height
   ) {
      super(widgetFactory, recipeIdGetter);
      if (width > 0 && height > 0) {
         this.width = width;
         this.height = height;
         this.modularScreenCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10L, TimeUnit.SECONDS)
            .maximumSize(10L)
            .build(new CacheLoader<T, ModularScreen>() {
               public ModularScreen load(T recipe) {
                  IWidget widget = widgetFactory.apply(recipe);
                  ResourceLocation recipeId = recipeIdGetter.apply(recipe);
                  ModularPanel<?> panel = (ModularPanel<?>)new SizedModularUIRecipeCategory.EmbeddedRecipePanel(recipeId.toString()).size(width, height);
                  panel.child(widget);
                  return ModularScreen.createEmbed(recipeId.getNamespace(), panel, width, height);
               }
            });
      } else {
         throw new IllegalArgumentException("JEI recipe category dimensions must be positive");
      }
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   protected ModularScreen getModularScreen(T recipe) {
      return (ModularScreen)this.modularScreenCache.getUnchecked(recipe);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
      ModularScreen screen = this.getModularScreen(recipe);
      MutableInt slotIndex = new MutableInt(0);
      WidgetTree.foreachChildBFS(screen.getMainPanel(), widget -> {
         if (widget instanceof IngredientProvider<?> provider) {
            if (provider instanceof GTJeiRecipeViewerSlot jeiSlot && jeiSlot.isLateBound()) {
               return true;
            }

            GTJeiSlotRegistration.addSlot(builder, provider, mapToRole(provider.getRecipeRole()), slotIndex.getAndIncrement());
            return true;
         } else {
            return true;
         }
      }, true);
   }

   public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
      builder.addGuiEventListener(new SizedModularUIRecipeCategory.ModularUIGuiEventListener(recipe));
      builder.addWidget(new SizedModularUIRecipeCategory.UIForegroundRenderWidget(recipe));
   }

   public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
      ModularScreen screen = this.getModularScreen(recipe);
      RecipeScreenRenderingUtil.drawScreenBackground(
         graphics, screen, (int)mouseX, (int)mouseY, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)
      );
   }

   public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      tooltip.clear();
   }

   public static RecipeIngredientRole mapToRole(RecipeSlotRole slotRole) {
      return switch (slotRole) {
         case INPUT -> RecipeIngredientRole.INPUT;
         case OUTPUT -> RecipeIngredientRole.OUTPUT;
         case CATALYST -> RecipeIngredientRole.CATALYST;
         case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
         default -> throw new MatchException(null, null);
      };
   }

   private static final class EmbeddedRecipePanel extends ModularPanel<SizedModularUIRecipeCategory.EmbeddedRecipePanel> {
      private EmbeddedRecipePanel(String name) {
         super(name);
      }

      public boolean shouldAnimate() {
         return false;
      }
   }

   public final class ModularUIGuiEventListener implements IJeiGuiEventListener {
      private final T recipe;

      private ModularUIGuiEventListener(T recipe) {
         this.recipe = recipe;
      }

      public ScreenRectangle getArea() {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).getMainRectangle();
      }

      public void mouseMoved(double mouseX, double mouseY) {
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).mousePressed(button);
      }

      public boolean mouseReleased(double mouseX, double mouseY, int button) {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).mouseReleased(button);
      }

      public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).mouseDragged(button, dragX, dragY);
      }

      public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).mouseScrolled(deltaX, deltaY);
      }

      public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
         return SizedModularUIRecipeCategory.this.getModularScreen(this.recipe).keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public final class UIForegroundRenderWidget implements IRecipeWidget {
      private final T recipe;

      private UIForegroundRenderWidget(T recipe) {
         this.recipe = recipe;
      }

      public ScreenPosition getPosition() {
         return new ScreenPosition(0, 0);
      }

      public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
         ModularScreen screen = SizedModularUIRecipeCategory.this.getModularScreen(this.recipe);
         RecipeScreenRenderingUtil.drawScreenForeground(
            graphics, screen, (int)mouseX, (int)mouseY, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)
         );
      }
   }
}
