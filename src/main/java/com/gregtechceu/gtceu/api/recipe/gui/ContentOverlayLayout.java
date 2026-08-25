package com.gregtechceu.gtceu.api.recipe.gui;

import java.util.EnumMap;
import java.util.Map;

public final class ContentOverlayLayout {
   private ContentOverlayLayout() {
   }

   public static Map<ContentOverlayLayout.Label, ContentOverlayLayout.Band> layout(
      int width, int height, boolean badge, boolean chance, boolean amount, boolean tick
   ) {
      int safeWidth = Math.max(width, 1);
      int safeHeight = Math.max(height, 1);
      int activeLabels = (badge ? 1 : 0) + (chance ? 1 : 0) + (amount ? 1 : 0) + (tick ? 1 : 0);
      int rowHeight = Math.max(1, Math.min(7, safeHeight / Math.max(3, activeLabels)));
      Map<ContentOverlayLayout.Label, ContentOverlayLayout.Band> result = new EnumMap<>(ContentOverlayLayout.Label.class);
      int top = 0;
      if (badge) {
         result.put(ContentOverlayLayout.Label.BADGE, new ContentOverlayLayout.Band(0, top, safeWidth, rowHeight));
         top += rowHeight;
      }

      if (chance) {
         result.put(ContentOverlayLayout.Label.CHANCE, new ContentOverlayLayout.Band(0, top, safeWidth, rowHeight));
      }

      int bottom = safeHeight;
      if (amount) {
         bottom = safeHeight - rowHeight;
         result.put(ContentOverlayLayout.Label.AMOUNT, new ContentOverlayLayout.Band(0, bottom, safeWidth, rowHeight));
      }

      if (tick) {
         bottom -= rowHeight;
         result.put(ContentOverlayLayout.Label.TICK, new ContentOverlayLayout.Band(0, bottom, safeWidth, rowHeight));
      }

      return Map.copyOf(result);
   }

   public static float fitTextScale(ContentOverlayLayout.Band band, int textWidth, int fontHeight, int margin, float maximumScale) {
      if (band != null && !band.isEmpty() && textWidth > 0 && fontHeight > 0 && !(maximumScale <= 0.0F)) {
         float availableWidth = (float)Math.max(0, band.width() - margin * 2);
         float widthScale = availableWidth / (float)textWidth;
         float heightScale = (float)band.height() / (float)fontHeight;
         return Math.max(0.0F, Math.min(maximumScale, Math.min(widthScale, heightScale)));
      } else {
         return 0.0F;
      }
   }

   public static record Band(int x, int y, int width, int height) {
      public boolean isEmpty() {
         return this.width <= 0 || this.height <= 0;
      }

      public boolean intersects(ContentOverlayLayout.Band other) {
         return this.x < other.x + other.width && other.x < this.x + this.width && this.y < other.y + other.height && other.y < this.y + this.height;
      }

      public boolean contains(int slotWidth, int slotHeight) {
         return this.x >= 0 && this.y >= 0 && this.width >= 0 && this.height >= 0 && this.x + this.width <= slotWidth && this.y + this.height <= slotHeight;
      }
   }

   public static enum Label {
      BADGE,
      CHANCE,
      AMOUNT,
      TICK;
   }
}
