package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@FunctionalInterface
public interface CapabilityContentBuilder {
   CapabilityContentBuilder ITEM = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
      if (widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget) {
         ChanceBoostFunction boostFunction = recipeType == null ? recipe.getType().getChanceFunction() : recipeType.getChanceFunction();
         float chance = RecipeViewerChanceSemantics.effectiveChance(
            recipe, ItemRecipeCapability.CAP, io, perTick, content, boostFunction, recipeTier, chanceTier
         );
         Content displayContent = RecipeViewerChanceSemantics.displayContent(content, chance);
         SizedIngredient innerContent = ItemRecipeCapability.CAP.of(content.content());
         recipeViewerSlotWidget.value(ItemRecipeCapability.mapIngredientToEntryList(innerContent));
         recipeViewerSlotWidget.overlay(new IDrawable[]{new ContentOverlay(displayContent, perTick)});
         recipeViewerSlotWidget.chance(chance);
         if (recipeViewerSlotWidget instanceof RecipeViewerSlotSemantics semantics) {
            semantics.setPerTick(perTick);
         }

         if (io != IO.IN || content.chance() != 0 && !(innerContent.ingredient().getCustomIngredient() instanceof IntCircuitIngredient)) {
            if (io == IO.IN) {
               recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.INPUT);
            } else {
               recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.OUTPUT);
            }
         } else {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.CATALYST);
         }

         recipeViewerSlotWidget.tooltipBuilder(
            tooltip -> {
               RecipeViewerChanceSemantics.addChanceTooltips(
                  tooltip, recipe, ItemRecipeCapability.CAP, io, perTick, content, boostFunction, recipeTier, chanceTier
               );
               if (innerContent.ingredient().getCustomIngredient() instanceof IntProviderIngredient ingredient) {
                  IntProvider countProvider = ingredient.getCountProvider();
                  tooltip.add(
                     Component.translatable("gtceu.gui.content.count_range", new Object[]{countProvider.getMinValue(), countProvider.getMaxValue()})
                        .withStyle(ChatFormatting.GOLD)
                  );
               } else if (innerContent.ingredient().getCustomIngredient() instanceof IntProviderIngredient ingredient) {
                  IntProvider countProvider = ingredient.getCountProvider();
                  tooltip.add(
                     Component.translatable("gtceu.gui.content.count_range", new Object[]{countProvider.getMinValue(), countProvider.getMaxValue()})
                        .withStyle(ChatFormatting.GOLD)
                  );
               }

               if (perTick) {
                  tooltip.add(Component.translatable("gtceu.gui.content.per_tick"));
               }
            }
         );
      }
   };
   CapabilityContentBuilder FLUID = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
      if (widget instanceof RecipeViewerSlotWidget<?> recipeViewerSlotWidget) {
         ChanceBoostFunction boostFunction = recipeType == null ? recipe.getType().getChanceFunction() : recipeType.getChanceFunction();
         float chance = RecipeViewerChanceSemantics.effectiveChance(
            recipe, FluidRecipeCapability.CAP, io, perTick, content, boostFunction, recipeTier, chanceTier
         );
         Content displayContent = RecipeViewerChanceSemantics.displayContent(content, chance);
         SizedFluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content());
         recipeViewerSlotWidget.value(FluidRecipeCapability.mapIngredientToEntryList(ingredient));
         recipeViewerSlotWidget.overlay(new IDrawable[]{new ContentOverlay(displayContent, perTick)});
         recipeViewerSlotWidget.chance(chance);
         if (recipeViewerSlotWidget instanceof RecipeViewerSlotSemantics semantics) {
            semantics.setPerTick(perTick);
         }

         recipeViewerSlotWidget.tooltipBuilder(
            tooltip -> {
               RecipeViewerChanceSemantics.addChanceTooltips(
                  tooltip, recipe, FluidRecipeCapability.CAP, io, perTick, content, boostFunction, recipeTier, chanceTier
               );
               if (ingredient.ingredient() instanceof IRangedIngredient provider) {
                  IntProvider countProvider = provider.getCountProvider();
                  tooltip.addLine(
                     Component.translatable("gtceu.gui.content.fluid_range", new Object[]{countProvider.getMinValue(), countProvider.getMaxValue()})
                        .withStyle(ChatFormatting.GOLD)
                  );
               }

               if (perTick) {
                  tooltip.addLine(Component.translatable("gtceu.gui.content.per_tick"));
               }
            }
         );
         if (io == IO.IN && content.chance() == 0) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.CATALYST);
         } else if (io == IO.IN) {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.INPUT);
         } else {
            recipeViewerSlotWidget.recipeSlotRole(RecipeSlotRole.OUTPUT);
         }
      }
   };
   CapabilityContentBuilder COMPUTATION = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
      if (widget instanceof Flow flow) {
         IWidget existingCompTickWidget = WidgetTree.findFirstWithNameNullable(flow, "comp_tick");
         IWidget existingCompTotal = WidgetTree.findFirstWithNameNullable(flow, "comp_total");
         if (recipe.tickInputs.get(CWURecipeCapability.CAP) != null) {
            if (CWURecipeCapability.CAP.isTickSlot(0, IO.IN, recipe)) {
               int cwu = recipe.getTickInputContents(CWURecipeCapability.CAP).stream().map(Content::content).mapToInt(CWURecipeCapability.CAP::of).sum();
               ModularComponent text = Text.lang("gtceu.recipe.computation_per_tick", new Object[]{FormattingUtil.formatNumbers(cwu)});
               if (existingCompTickWidget != null) {
                  ((TextWidget)existingCompTickWidget).value(text);
               } else {
                  flow.child(text.asWidget().name("comp_tick"));
               }
            }

            if (recipe.data.getBoolean("duration_is_total_cwu")) {
               ModularComponent text = Text.lang("gtceu.recipe.total_computation", new Object[]{FormattingUtil.formatNumbers(recipe.duration)});
               if (existingCompTotal != null) {
                  ((TextWidget)existingCompTotal).value(text);
               } else {
                  flow.child(text.asWidget().name("comp_total"));
               }
            }
         }
      }
   };
   CapabilityContentBuilder EU = (widget, content, io, perTick, recipeType, recipe, chanceTier, recipeTier) -> {
      if (widget instanceof Flow flow) {
         EnergyStack eu = RecipeHelper.getRealEUt(recipe);
         byte minVoltageTier = GTUtil.getTierByVoltage(eu.voltage());
         float minAmperage = (float)eu.getTotalEU() / (float)GTValues.V[minVoltageTier];
         if (eu.voltage() > 0L) {
            IWidget maxEuWidget = WidgetTree.findFirstWithNameNullable(flow, "max_eu");
            IWidget euWidget = WidgetTree.findFirstWithNameNullable(flow, "eu");
            ModularComponent maxEu;
            if (recipe.data.getBoolean("duration_is_total_cwu") && recipe.tickInputs.containsKey(CWURecipeCapability.CAP)) {
               int minimumCWUt = Math.max(
                  recipe.tickInputs.get(CWURecipeCapability.CAP).stream().map(Content::content).mapToInt(CWURecipeCapability.CAP::of).sum(), 1
               );
               maxEu = Text.lang("gtceu.recipe.max_eu", new Object[]{FormattingUtil.formatNumbers(eu.getTotalEU() / (long)minimumCWUt)});
            } else {
               maxEu = Text.lang("gtceu.recipe.total", new Object[]{FormattingUtil.formatNumbers(eu.getTotalEU() * (long)recipe.duration)});
            }

            if (maxEuWidget != null) {
               ((TextWidget)maxEuWidget).value(maxEu);
            } else {
               flow.child(maxEu.asWidget().name("max_eu"));
            }

            ModularComponent euText = Text.lang(
                  io == IO.IN ? "gtceu.recipe.eu" : "gtceu.recipe.eu_inverted",
                  new Object[]{FormattingUtil.formatNumber2Places(minAmperage), GTValues.VN[minVoltageTier]}
               )
               .withStyle(ChatFormatting.UNDERLINE);
            RichTooltip tooltip = new RichTooltip();
            tooltip.addLine(Text.lang("gtceu.recipe.eu.total", new Object[]{FormattingUtil.formatNumbers(eu.getTotalEU())}).withStyle(ChatFormatting.UNDERLINE));
            if (euWidget != null) {
               ((TextWidget)euWidget).value(euText).tooltip(tooltip);
            } else {
               flow.child(((TextWidget)((TextWidget)euText.asWidget().tooltip(tooltip)).marginBottom(1)).name("eu"));
            }
         }
      }
   };

   void buildWidgetContent(IWidget var1, Content var2, IO var3, boolean var4, GTRecipeType var5, GTRecipe var6, int var7, int var8);
}
