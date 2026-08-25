package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.ApiStatus;

public final class CircuitRequirementPolicy {
   private static final String PROGRAMMED_CIRCUIT = "gtceu:programmed_circuit";
   private static final AtomicReference<CacheState> CACHE_STATE = new AtomicReference<>(new CacheState());

   private CircuitRequirementPolicy() {
   }

   public static boolean requiresCircuit(MetaMachine machine) {
      if (!(machine instanceof IRecipeLogicMachine recipeMachine)) {
         return true;
      } else {
         GTRecipeType[] recipeTypes = recipeMachine.getRecipeTypes();
         if (recipeTypes != null && recipeTypes.length != 0) {
            while (true) {
               CacheState state = CACHE_STATE.get();
               boolean result = requiresCircuit(recipeTypes, state);
               if (CACHE_STATE.get() == state) return result;
            }
         } else {
            return true;
         }
      }
   }

   /** Invalidates results after the recipe manager has rebuilt GTCEu recipe databases. */
   @ApiStatus.Internal
   public static void clearCache() {
      CACHE_STATE.set(new CacheState());
   }

   private static boolean requiresCircuit(GTRecipeType[] recipeTypes, CacheState state) {
      boolean foundRecipe = false;

      for (GTRecipeType recipeType : recipeTypes) {
         if (recipeType == null) continue;
         CircuitUse use = state.results.computeIfAbsent(recipeType, CircuitRequirementPolicy::inspectType);
         if (use == CircuitUse.REQUIRES_CIRCUIT) return true;
         if (use == CircuitUse.PRESENT_WITHOUT_CIRCUIT) foundRecipe = true;
      }

      return !foundRecipe;
   }

   private static CircuitUse inspectType(GTRecipeType recipeType) {
      boolean foundRecipe = false;

      try {
         for (GTRecipeCategory category : recipeType.getCategories()) {
            for (GTRecipe recipe : recipeType.getRecipesInCategory(category)) {
               foundRecipe = true;
               if (hasCircuit(recipe.inputs.get(ItemRecipeCapability.CAP)) || hasCircuit(recipe.tickInputs.get(ItemRecipeCapability.CAP))) {
                  return CircuitUse.REQUIRES_CIRCUIT;
               }
            }
         }
      } catch (Throwable ignored) {
         // Recipe reloads can briefly expose a changing collection. Fail safe until the reload-tail clear.
         return CircuitUse.REQUIRES_CIRCUIT;
      }

      return foundRecipe ? CircuitUse.PRESENT_WITHOUT_CIRCUIT : CircuitUse.EMPTY;
   }

   private static boolean hasCircuit(List<Content> var0) {
      if (var0 == null) {
         return false;
      } else {
         for (Content var2 : var0) {
            if (var2 != null
               && var2.content() instanceof SizedIngredient var3
               && hasCircuit(var3.ingredient(), Collections.newSetFromMap(new IdentityHashMap<>()))) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean hasCircuit(Ingredient var0, Set<Object> var1) {
      if (var0 != null && var1.add(var0)) {
         ICustomIngredient var2 = var0.getCustomIngredient();
         if (var2 == null) {
            return false;
         } else if (var2 instanceof IntCircuitIngredient) {
            return true;
         } else {
            if (var2 instanceof IntProviderIngredient var3 && hasCircuit(var3.getInner(), var1)) {
               return true;
            }

            if (var2 instanceof IntersectionIngredient var7) {
               for (Ingredient var5 : var7.children()) {
                  if (hasCircuit(var5, var1)) {
                     return true;
                  }
               }
            }

            try {
               for (ItemStack var9 : var2.getItems().toList()) {
                  if (isProgrammedCircuit(var9)) {
                     return true;
                  }
               }
            } catch (Throwable var6) {
            }

            return inspectFields(var2, var1);
         }
      } else {
         return false;
      }
   }

   private static boolean inspectFields(Object var0, Set<Object> var1) {
      if (var0 != null && var1.add(var0)) {
         for (Class var2 = var0.getClass(); var2 != null && var2 != Object.class; var2 = var2.getSuperclass()) {
            for (Field var6 : var2.getDeclaredFields()) {
               if (!Modifier.isStatic(var6.getModifiers())) {
                  Class var7 = var6.getType();
                  if (Ingredient.class.isAssignableFrom(var7)
                     || ICustomIngredient.class.isAssignableFrom(var7)
                     || Iterable.class.isAssignableFrom(var7)
                     || var7.isArray()) {
                     try {
                        var6.setAccessible(true);
                        Object var8 = var6.get(var0);
                        if (var8 instanceof Ingredient var9 && hasCircuit(var9, var1)) {
                           return true;
                        }

                        if (var8 instanceof ICustomIngredient var14 && hasCircuitCustom(var14, var1)) {
                           return true;
                        }

                        if (var8 instanceof Iterable) {
                           for (Object var11 : (Iterable)var8) {
                              if (var11 instanceof Ingredient var12 && hasCircuit(var12, var1)) {
                                 return true;
                              }

                              if (var11 instanceof ICustomIngredient var19 && hasCircuitCustom(var19, var1)) {
                                 return true;
                              }
                           }
                        }

                        if (var7.isArray()) {
                           int var16 = Array.getLength(var8);

                           for (int var17 = 0; var17 < var16; var17++) {
                              Object var18 = Array.get(var8, var17);
                              if (var18 instanceof Ingredient var20 && hasCircuit(var20, var1)) {
                                 return true;
                              }

                              if (var18 instanceof ICustomIngredient var21 && hasCircuitCustom(var21, var1)) {
                                 return true;
                              }
                           }
                        }
                     } catch (Throwable var13) {
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean hasCircuitCustom(ICustomIngredient var0, Set<Object> var1) {
      if (var0 instanceof IntCircuitIngredient) {
         return true;
      } else {
         if (var0 instanceof IntProviderIngredient var2 && hasCircuit(var2.getInner(), var1)) {
            return true;
         }

         try {
            for (ItemStack var3 : var0.getItems().toList()) {
               if (isProgrammedCircuit(var3)) {
                  return true;
               }
            }
         } catch (Throwable var4) {
         }

         return inspectFields(var0, var1);
      }
   }

   private static boolean isProgrammedCircuit(ItemStack var0) {
      return var0 != null && !var0.isEmpty() && "gtceu:programmed_circuit".equals(String.valueOf(BuiltInRegistries.ITEM.getKey(var0.getItem())));
   }

   private enum CircuitUse {
      EMPTY,
      PRESENT_WITHOUT_CIRCUIT,
      REQUIRES_CIRCUIT
   }

   private static final class CacheState {
      private final ConcurrentMap<GTRecipeType, CircuitUse> results = new ConcurrentHashMap<>();
   }
}
