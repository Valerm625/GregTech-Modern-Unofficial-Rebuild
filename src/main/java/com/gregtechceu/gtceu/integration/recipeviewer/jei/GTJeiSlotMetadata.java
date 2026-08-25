package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerSlotSemantics;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

public record GTJeiSlotMetadata(float chance, boolean perTick) {
   private static final String SLOT_PREFIX = "slot_";
   private static final String METADATA_SEPARATOR = "_gtceu_semantics_";
   private static final int CHANCE_SCALE = 1000000;

   public static String encodeSlotName(int index, IngredientProvider<?> provider) {
      int scaledChance;
      boolean var10000;
      label17: {
         float chance = Math.max(0.0F, Math.min(1.0F, provider.chance()));
         scaledChance = Math.round(chance * 1000000.0F);
         if (provider instanceof RecipeViewerSlotSemantics semantics && semantics.isPerTick()) {
            var10000 = true;
            break label17;
         }

         var10000 = false;
      }

      boolean perTick = var10000;
      return "slot_" + index + "_gtceu_semantics_" + scaledChance + "_" + (perTick ? 1 : 0);
   }

   public static Optional<GTJeiSlotMetadata> decode(IRecipeSlotView slot) {
      return slot.getSlotName().flatMap(GTJeiSlotMetadata::decode);
   }

   static Optional<GTJeiSlotMetadata> decode(String slotName) {
      if (!slotName.startsWith("slot_")) {
         return Optional.empty();
      } else {
         int metadataStart = slotName.indexOf("_gtceu_semantics_", "slot_".length());
         if (metadataStart >= 0 && isUnsignedInteger(slotName.substring("slot_".length(), metadataStart))) {
            String[] fields = slotName.substring(metadataStart + "_gtceu_semantics_".length()).split("_", -1);
            if (fields.length != 2) {
               return Optional.empty();
            } else {
               try {
                  int scaledChance = Integer.parseInt(fields[0]);
                  return scaledChance >= 0 && scaledChance <= 1000000 && (fields[1].equals("0") || fields[1].equals("1"))
                     ? Optional.of(new GTJeiSlotMetadata((float)scaledChance / 1000000.0F, fields[1].equals("1")))
                     : Optional.empty();
               } catch (NumberFormatException var4) {
                  return Optional.empty();
               }
            }
         } else {
            return Optional.empty();
         }
      }
   }

   private static boolean isUnsignedInteger(String value) {
      if (value.isEmpty()) {
         return false;
      } else {
         for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
               return false;
            }
         }

         return true;
      }
   }
}
