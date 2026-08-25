package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.screen.RichTooltip;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class RecipeViewerChanceSemantics {
   private RecipeViewerChanceSemantics() {
   }

   public static float effectiveChance(GTRecipe recipe, RecipeCapability<?> capability, IO io, boolean perTick, Content target) {
      if (recipe == null) {
         return 0.0F;
      } else {
         int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
         return effectiveChance(recipe, capability, io, perTick, target, recipe.getType().getChanceFunction(), recipeTier, recipeTier + recipe.ocLevel);
      }
   }

   public static float effectiveChance(GTRecipe recipe, RecipeCapability<?> capability, IO io, boolean perTick, Content target, int recipeTier, int chanceTier) {
      return recipe == null ? 0.0F : effectiveChance(recipe, capability, io, perTick, target, recipe.getType().getChanceFunction(), recipeTier, chanceTier);
   }

   public static float effectiveChance(
      GTRecipe recipe,
      RecipeCapability<?> capability,
      IO io,
      boolean perTick,
      Content target,
      ChanceBoostFunction boostFunction,
      int recipeTier,
      int chanceTier
   ) {
      if (recipe != null && capability != null && target != null) {
         int targetMax = target.maxChance();
         if (targetMax <= 0) {
            return 0.0F;
         } else if (target.chance() >= targetMax) {
            return 1.0F;
         } else {
            List<Content> chancedContents = contents(recipe, io, perTick)
               .getOrDefault(capability, List.of())
               .stream()
               .filter(RecipeViewerChanceSemantics::isRolledContent)
               .toList();
            int targetIndex = identityIndexOf(chancedContents, target);
            if (targetIndex < 0) {
               return boostedProbability(target, boostFunction, recipeTier, chanceTier);
            } else {
               ChanceLogic logic = recipe.getChanceLogicForCapability(capability, io, perTick);
               if (logic == ChanceLogic.NONE) {
                  return 0.0F;
               } else {
                  List<Float> probabilities = new ArrayList<>(chancedContents.size());

                  for (Content content : chancedContents) {
                     probabilities.add(boostedProbability(content, boostFunction, recipeTier, chanceTier));
                  }

                  if (logic == ChanceLogic.AND) {
                     double jointChance = 1.0;

                     for (float probability : probabilities) {
                        jointChance *= (double)probability;
                     }

                     return clampChance(jointChance);
                  } else if (logic == ChanceLogic.FIRST) {
                     double marginalChance = (double)probabilities.get(targetIndex).floatValue();

                     for (int index = 0; index < targetIndex; index++) {
                        marginalChance *= 1.0 - (double)probabilities.get(index).floatValue();
                     }

                     return clampChance(marginalChance);
                  } else {
                     return logic == ChanceLogic.XOR
                        ? xorMarginalChance(chancedContents, targetIndex, boostFunction, recipeTier, chanceTier)
                        : probabilities.get(targetIndex);
                  }
               }
            }
         }
      } else {
         return 0.0F;
      }
   }

   static Content displayContent(Content original, float probability) {
      int maxChance = original.maxChance();
      if (maxChance <= 0) {
         return original;
      } else {
         int displayChance = Math.round(clampChance((double)probability) * (float)maxChance);
         displayChance = Math.max(0, Math.min(maxChance, displayChance));
         return displayChance == original.chance()
            ? original
            : Content.fromSerialized(original.content(), displayChance, maxChance, original.tierChanceBoost());
      }
   }

   static void addChanceTooltips(
      RichTooltip tooltip,
      GTRecipe recipe,
      RecipeCapability<?> capability,
      IO io,
      boolean perTick,
      Content content,
      ChanceBoostFunction boostFunction,
      int recipeTier,
      int chanceTier
   ) {
      ChanceLogic logic = recipe.getChanceLogicForCapability(capability, io, perTick);
      Content.addChanceTooltips(tooltip, content, logic);
      if (content.maxChance() > 0 && content.chance() < content.maxChance() && content.tierChanceBoost() != 0 && boostFunction != ChanceBoostFunction.NONE) {
         float bonusPerTier = 100.0F * (float)content.tierChanceBoost() / (float)content.maxChance();
         String bonusKey = bonusPerTier < 0.0F ? "gtceu.gui.content.chance_tier_boost_minus" : "gtceu.gui.content.chance_tier_boost_plus";
         tooltip.addLine(FormattingUtil.formatPercentage2Places(bonusKey, Math.abs(bonusPerTier)));
         float baseChance = normalizedChance(content);
         float effectiveChance = effectiveChance(recipe, capability, io, perTick, content, boostFunction, recipeTier, chanceTier);
         if (!(Math.abs(effectiveChance - baseChance) <= 1.0E-6F)) {
            float effectivePercent = effectiveChance * 100.0F;
            if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
               tooltip.addLine(
                  Component.translatable(
                        "gtceu.gui.content.chance_boosted_logic", new Object[]{FormattingUtil.formatNumber2Places(effectivePercent), logic.getTranslation()}
                     )
                     .withStyle(ChatFormatting.YELLOW)
               );
            } else {
               tooltip.addLine(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_boosted", effectivePercent));
            }
         }
      }
   }

   private static Map<RecipeCapability<?>, List<Content>> contents(GTRecipe recipe, IO io, boolean perTick) {
      if (io == IO.OUT) {
         return perTick ? recipe.tickOutputs : recipe.outputs;
      } else {
         return perTick ? recipe.tickInputs : recipe.inputs;
      }
   }

   private static int identityIndexOf(List<Content> contents, Content target) {
      for (int index = 0; index < contents.size(); index++) {
         if (contents.get(index) == target) {
            return index;
         }
      }

      return contents.indexOf(target);
   }

   private static float xorMarginalChance(List<Content> contents, int targetIndex, ChanceBoostFunction boostFunction, int recipeTier, int chanceTier) {
      int maximum = ChanceLogic.getMaxChancedValue();
      List<Content> normalized = ChanceLogic.normalizeXorChances(contents, boostFunction, recipeTier, chanceTier);
      if (normalized.isEmpty()) {
         return 0.0F;
      } else {
         int cumulativeWeight = 0;
         double earlierFailureChance = 1.0;

         for (int index = 0; index < normalized.size(); index++) {
            cumulativeWeight = Math.min(maximum, cumulativeWeight + normalized.get(index).chance());
            double conditionalChance = (double)cumulativeWeight / (double)maximum;
            conditionalChance = Math.max(0.0, Math.min(1.0, conditionalChance));
            if (index == targetIndex) {
               return clampChance(earlierFailureChance * conditionalChance);
            }

            earlierFailureChance *= 1.0 - conditionalChance;
         }

         return 0.0F;
      }
   }

   private static float normalizedChance(Content content) {
      return content.maxChance() <= 0 ? 0.0F : clampChance((double)content.chance() / (double)content.maxChance());
   }

   private static boolean isRolledContent(Content content) {
      return content.maxChance() > 0 && content.chance() < content.maxChance() && (content.chance() > 0 || content.tierChanceBoost() > 0);
   }

   private static float boostedProbability(Content content, ChanceBoostFunction boostFunction, int recipeTier, int chanceTier) {
      return content.maxChance() <= 0 ? 0.0F : clampChance((double)boostedChance(content, boostFunction, recipeTier, chanceTier) / (double)content.maxChance());
   }

   private static int boostedChance(Content content, ChanceBoostFunction boostFunction, int recipeTier, int chanceTier) {
      if (content.maxChance() <= 0) {
         return 0;
      } else {
         ChanceBoostFunction function = boostFunction == null ? ChanceBoostFunction.NONE : boostFunction;
         int chance = function.getBoostedChance(content, recipeTier, chanceTier);
         return Math.max(0, Math.min(content.maxChance(), chance));
      }
   }

   private static float clampChance(double chance) {
      return (float)Math.max(0.0, Math.min(1.0, chance));
   }
}
