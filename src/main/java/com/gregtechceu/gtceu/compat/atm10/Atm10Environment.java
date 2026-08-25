package com.gregtechceu.gtceu.compat.atm10;

import com.gregtechceu.gtceu.GTCEu;
import java.util.Set;
import java.util.function.Predicate;

public final class Atm10Environment {
   private static final Set<String> PACK_MARKERS = Set.of("allthemodium", "allthetweaks");

   private Atm10Environment() {
   }

   public static boolean isDetected() {
      return matches(GTCEu::isModLoaded);
   }

   public static boolean matches(Predicate<String> isLoaded) {
      return PACK_MARKERS.stream().allMatch(isLoaded);
   }
}
