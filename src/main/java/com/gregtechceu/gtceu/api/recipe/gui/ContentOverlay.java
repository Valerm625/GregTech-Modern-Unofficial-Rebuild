package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IRangedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@OnlyIn(Dist.CLIENT)
public record ContentOverlay(Content content, boolean perTick) implements IDrawable {
   private static final int OVERLAY_Z = 400;
   private static final int TEXT_MARGIN = 1;
   private static final int FONT_HEIGHT = 9;
   private static final float MAX_TEXT_SCALE = 0.5F;

   public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
      this.drawChance(context.getGraphics(), (float)x, (float)y, width, height);
      this.drawRangeAmount(context.getGraphics(), (float)x, (float)y, width, height);
      this.drawFluidAmount(context.getGraphics(), (float)x, (float)y, width, height);
      if (this.perTick) {
         this.drawTick(context.getGraphics(), (float)x, (float)y, width, height);
      }
   }

   public void drawRangeAmount(GuiGraphics graphics, float x, float y, int width, int height) {
      IntProviderIngredient ingredient = this.getItemRangeIngredient();
      if (ingredient != null) {
         int min = ingredient.getCountProvider().getMinValue();
         int max = ingredient.getCountProvider().getMaxValue();
         String s = String.format("%s-%s", min, max);
         int color = 16777215;
         if (s.length() > 5) {
            s = "X-Y";
            color = ChatFormatting.GOLD.getColor();
         }

         this.drawText(graphics, x, y, width, height, ContentOverlayLayout.Label.AMOUNT, s, color, true);
      }
   }

   public void drawFluidAmount(GuiGraphics graphics, float x, float y, int width, int height) {
      if (this.content.content() instanceof SizedFluidIngredient ingredient) {
         String s;
         int color;
         if (ingredient.ingredient() instanceof IRangedIngredient) {
            color = ChatFormatting.GOLD.getColor();
            s = "X-Y";
         } else {
            int amount = ingredient.amount();
            color = 16777215;
            s = FormattingUtil.formatBuckets((long)amount);
            Font fontRenderer = Minecraft.getInstance().font;
            if (fontRenderer.width(s) > 32) {
               s = FormattingUtil.formatNumberReadable((double)amount, true, FormattingUtil.DECIMAL_FORMAT_1F, "B");
            }

            if (fontRenderer.width(s) > 32) {
               s = FormattingUtil.formatNumberReadable((double)amount, true, FormattingUtil.DECIMAL_FORMAT_0F, "B");
            }
         }

         this.drawText(graphics, x, y, width, height, ContentOverlayLayout.Label.AMOUNT, s, color, true);
      }
   }

   public void drawChance(GuiGraphics graphics, float x, float y, int width, int height) {
      if (this.content.chance() != this.content.maxChance()) {
         if (this.content.chance() == 0) {
            this.drawText(graphics, x, y, width, height, ContentOverlayLayout.Label.BADGE, "∞", 16733525, false);
         } else {
            float chanceFloat = 1.0F * (float)this.content.chance() / (float)this.content.maxChance();
            String percent = FormattingUtil.formatNumber2Places(100.0F * chanceFloat);
            String s = percent + "%";
            int color = GradientUtil.toRGB(Mth.lerp(chanceFloat, 29.0F, 167.0F), 100.0F, 50.0F);
            this.drawText(graphics, x, y, width, height, ContentOverlayLayout.Label.CHANCE, s, color, true);
         }
      }
   }

   public void drawTick(GuiGraphics graphics, float x, float y, int width, int height) {
      RenderSystem.disableDepthTest();
      String s = Component.translatable("gtceu.gui.content.tips.per_tick_short").getString();
      this.drawText(graphics, x, y, width, height, ContentOverlayLayout.Label.TICK, s, 16776960, false);
   }

   public boolean hasItemRangeAmount() {
      return this.getItemRangeIngredient() != null;
   }

   private boolean hasAmountOverlay() {
      return this.hasItemRangeAmount() || this.content.content() instanceof SizedFluidIngredient;
   }

   private IntProviderIngredient getItemRangeIngredient() {
      Object ranged = this.content.content();
      if (ranged instanceof IntProviderIngredient) {
         return (IntProviderIngredient)ranged;
      } else {
         if (this.content.content() instanceof SizedIngredient ingredient) {
            ICustomIngredient var6 = ingredient.ingredient().getCustomIngredient();
            if (var6 instanceof IntProviderIngredient) {
               return (IntProviderIngredient)var6;
            }
         }

         return null;
      }
   }

   private void drawText(
      GuiGraphics graphics, float x, float y, int width, int height, ContentOverlayLayout.Label label, String text, int color, boolean rightAligned
   ) {
      Map<ContentOverlayLayout.Label, ContentOverlayLayout.Band> bands = ContentOverlayLayout.layout(
         width,
         height,
         this.content.chance() == 0,
         this.content.chance() != 0 && this.content.chance() != this.content.maxChance(),
         this.hasAmountOverlay(),
         this.perTick
      );
      ContentOverlayLayout.Band band = bands.get(label);
      if (band != null && !band.isEmpty() && text != null && !text.isEmpty()) {
         Font font = Minecraft.getInstance().font;
         int textWidth = font.width(text);
         float scale = ContentOverlayLayout.fitTextScale(band, textWidth, 9, 1, 0.5F);
         if (!(scale <= 0.0F)) {
            float logicalTextWidth = (float)textWidth * scale;
            float logicalTextHeight = 9.0F * scale;
            float logicalX = rightAligned ? x + (float)band.x() + (float)band.width() - 1.0F - logicalTextWidth : x + (float)band.x() + 1.0F;
            float logicalY = y + (float)band.y() + Math.max(0.0F, ((float)band.height() - logicalTextHeight) / 2.0F);
            int rawX = Math.round(logicalX / scale);
            int rawY = Math.round(logicalY / scale);
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 400.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, text, rawX, rawY, color, true);
            graphics.pose().popPose();
         }
      }
   }
}
