package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class EmiUsageRecipeSorter {
   private EmiUsageRecipeSorter() {
   }

   public static <T> List<T> filterAndSort(List<T> recipes, Predicate<? super T> matches, ToIntFunction<? super T> sortKey) {
      List<EmiUsageRecipeSorter.RankedRecipe<T>> rankedRecipes = new ArrayList<>();

      for (T recipe : recipes) {
         if (matches.test(recipe)) {
            rankedRecipes.add(new EmiUsageRecipeSorter.RankedRecipe<>(recipe, sortKey.applyAsInt(recipe)));
         }
      }

      rankedRecipes.sort(Comparator.comparingInt(EmiUsageRecipeSorter.RankedRecipe::sortKey));
      return rankedRecipes.stream().map(EmiUsageRecipeSorter.RankedRecipe::recipe).toList();
   }

   private static record RankedRecipe<T>(T recipe, int sortKey) {
   }
}
