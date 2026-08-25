package com.gregtechceu.gtceu.compat.atm10;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

public final class EmiGregStarVisibilityCompat {
   private static final ResourceLocation INDEX_RESOURCE = ResourceLocation.fromNamespaceAndPath("emi", "index/stacks/zzzz_gtceu_atm10_greg_star_restore.json");

   private EmiGregStarVisibilityCompat() {
   }

   public static void addDynamicResources() {
      if (shouldInstall(GTCEu::isModLoaded)) {
         JsonObject stack = new JsonObject();
         stack.addProperty("stack", "item:allthetweaks:greg_star_block");
         JsonArray added = new JsonArray();
         added.add(stack);
         JsonObject root = new JsonObject();
         root.add("added", added);
         GTDynamicResourcePack.addNamespace("emi");
         GTDynamicResourcePack.addResource(INDEX_RESOURCE, root);
      }
   }

   static boolean shouldInstall(Predicate<String> isLoaded) {
      return isLoaded.test("emi") && isLoaded.test("allthetweaks");
   }
}
