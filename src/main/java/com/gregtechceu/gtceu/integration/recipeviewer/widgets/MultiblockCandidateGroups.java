package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import java.util.List;
import java.util.function.Function;

final class MultiblockCandidateGroups {
   private MultiblockCandidateGroups() {
   }

   static <T, C> List<T> nonEmpty(List<T> groups, Function<T, ? extends List<C>> candidates) {
      return groups.stream().filter(group -> !candidates.apply((T)group).isEmpty()).toList();
   }
}
