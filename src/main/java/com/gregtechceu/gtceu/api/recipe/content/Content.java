package com.gregtechceu.gtceu.api.recipe.content;

import brachy.modularui.screen.RichTooltip;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

public record Content(Object content, int chance, int maxChance, int tierChanceBoost) {
   public Content(Object content, int chance, int maxChance) {
      this(content, chance, maxChance, 0);
   }

   public Content(Object content, int chance, int maxChance, int tierChanceBoost) {
      this.content = content;
      this.chance = chance;
      this.maxChance = maxChance;
      this.tierChanceBoost = this.fixBoost(tierChanceBoost);
   }

   public static Content fromSerialized(Object content, int chance, int maxChance, int tierChanceBoost) {
      return new Content(content, chance, maxChance, restoreBoostInput(tierChanceBoost, maxChance));
   }

   public static <T> Codec<Content> codec(RecipeCapability<T> capability) {
      return RecordCodecBuilder.create(
         instance -> instance.group(
                  capability.serializer.codec().fieldOf("content").forGetter(val -> capability.of(val.content)),
                  ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("chance", ChanceLogic.getMaxChancedValue()).forGetter(Content::chance),
                  ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("maxChance", ChanceLogic.getMaxChancedValue()).forGetter(Content::maxChance),
                  // JSON carries the boost in the 1/10000 scale, while the record stores it scaled to
                  // maxChance (see fixBoost), so the codec converts in both directions instead of
                  // letting fromSerialized() undo the normalization on decode only.
                  Codec.INT.optionalFieldOf("tierChanceBoost", 0)
                        .forGetter(val -> restoreBoostInput(val.tierChanceBoost(), val.maxChance()))
               )
               .apply(instance, Content::new)
      );
   }

   public Content copy(RecipeCapability<?> capability) {
      return fromSerialized(capability.copyContent(this.content), this.chance, this.maxChance, this.tierChanceBoost);
   }

   public Content copy(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
      return modifier != ContentModifier.IDENTITY && this.chance >= this.maxChance
         ? fromSerialized(capability.copyContent(this.content, modifier), this.chance, this.maxChance, this.tierChanceBoost)
         : this.copy(capability);
   }

   public Content copyChanced(RecipeCapability<?> capability, @NotNull ContentModifier modifier) {
      return modifier == ContentModifier.IDENTITY
         ? this.copy(capability)
         : fromSerialized(capability.copyContent(this.content, modifier), this.chance, this.maxChance, this.tierChanceBoost);
   }

   public boolean isChanced() {
      return this.chance > 0 && this.chance < this.maxChance;
   }

   private int fixBoost(int chanceBoost) {
      return fixBoost(chanceBoost, this.maxChance);
   }

   private static int fixBoost(int chanceBoost, int maxChance) {
      float error = (float)ChanceLogic.getMaxChancedValue() / (float)maxChance;
      int fixed = Math.round((float)Math.abs(chanceBoost) / error);
      return chanceBoost < 0 ? -fixed : fixed;
   }

   private static int restoreBoostInput(int fixedBoost, int maxChance) {
      if (fixedBoost == 0 || maxChance == ChanceLogic.getMaxChancedValue()) {
         return fixedBoost;
      } else if (maxChance <= 0) {
         return fixedBoost;
      } else {
         long target = Math.abs((long)fixedBoost);
         float error = (float)ChanceLogic.getMaxChancedValue() / (float)maxChance;
         long restored = Math.round((double)target * (double)error);
         restored = Math.max(0L, Math.min(2147483647L, restored));
         long refixed = Math.abs((long)fixBoost((int)restored, maxChance));

         while (refixed < target && restored < 2147483647L) {
            refixed = Math.abs((long)fixBoost((int)(++restored), maxChance));
         }

         while (refixed > target && restored > 0L) {
            refixed = Math.abs((long)fixBoost((int)(--restored), maxChance));
         }

         int result = (int)restored;
         return fixedBoost < 0 ? -result : result;
      }
   }

   public static void addChanceTooltips(RichTooltip tooltip, Content content, ChanceLogic logic) {
      // Compare against this entry's own scale: the body below formats the percentage as
      // chance / maxChance, so the guard has to use the same denominator as isChanced() does.
      if (content.chance() < content.maxChance()) {
         if (content.chance() == 0) {
            tooltip.addLine(Component.translatable("gtceu.gui.content.chance_nc"));
         } else {
            float baseChanceFloat = 100.0F * (float)content.chance() / (float)content.maxChance();
            if (logic != ChanceLogic.NONE && logic != ChanceLogic.OR) {
               tooltip.addLine(
                  Component.translatable(
                        "gtceu.gui.content.chance_no_boost_logic", new Object[]{FormattingUtil.formatNumber2Places(baseChanceFloat), logic.getTranslation()}
                     )
                     .withStyle(ChatFormatting.YELLOW)
               );
            } else {
               tooltip.addLine(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_no_boost", baseChanceFloat));
            }
         }
      }
   }
}
