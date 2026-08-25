package com.gregtechceu.gtceu.api.recipe.chance.boost;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ChanceBoostFunction {
   ChanceBoostFunction OVERCLOCK = (entry, recipeTier, chanceTier) -> {
      long tierDiff = (long)chanceTier - recipeTier;
      if (tierDiff <= 0) {
         return entry.chance();
      } else {
         if (recipeTier == 0) {
            tierDiff--;
         }

         long boosted = entry.chance() + (long)entry.tierChanceBoost() * tierDiff;
         return (int)Math.max(0L, Math.min((long)entry.maxChance(), boosted));
      }
   };
   ChanceBoostFunction NONE = (entry, recipeTier, chanceTier) -> entry.chance();

   int getBoostedChance(@NotNull Content var1, int var2, int var3);
}
