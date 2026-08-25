package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.trait.CircuitRequirementPolicy;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Unique
    @Nullable
    private Multimap<RecipeType<?>, RecipeHolder<?>> gtceu$previousByType;

    @Unique
    @Nullable
    private Map<ResourceLocation, RecipeHolder<?>> gtceu$previousByName;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void gtceu$capturePublishedRecipes(Map<ResourceLocation, JsonElement> map,
                                               ResourceManager resourceManager, ProfilerFiller profiler,
                                               CallbackInfo ci) {
        gtceu$previousByType = byType;
        gtceu$previousByName = byName;
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        Map<ResourceLocation, RecipeHolder<?>> previousByName = gtceu$previousByName;
        Multimap<RecipeType<?>, RecipeHolder<?>> previousByType = gtceu$previousByType;
        gtceu$previousByName = null;
        gtceu$previousByType = null;
        if (previousByName == null || previousByType == null) {
            throw new IllegalStateException("recipe reload reached GT staging without a published-generation snapshot");
        }
        List<GTRecipeType> stagingTypes = new ArrayList<>();
        Map<GTRecipeType, GTRecipeType.PreparedRecipeGeneration> generations = new HashMap<>();
        try {
            for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
                if (recipeType instanceof GTRecipeType gtRecipeType) {
                    gtRecipeType.beginStagingRecipes();
                    stagingTypes.add(gtRecipeType);
                }
            }

            // Every type must be staged before cloning. GTRecipeBuilder.save()
            // can register research entries as a side effect.
            var recipesByName = new HashMap<>(byName);
            byName.values().forEach(holder -> {
                if (holder.value() instanceof GTRecipe gtRecipe) {
                    new GTRecipeBuilder(gtRecipe, gtRecipe.recipeType)
                            .id(holder.id().withPath(path -> path.substring(path.indexOf('/') + 1)))
                            .onSave(gtRecipe.recipeType.getRecipeBuilder().onSave)
                            .save(new RecipeOutput() {

                                @SuppressWarnings("removal")
                                @Override
                                public Advancement.@NotNull Builder advancement() {
                                    return Advancement.Builder.recipeAdvancement()
                                            .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                                }

                                @Override
                                public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                                                   @Nullable AdvancementHolder advancement,
                                                   ICondition @NotNull... conditions) {
                                    recipesByName.put(id, new RecipeHolder<>(id, recipe));
                                }
                            });
                }
            });
            Multimap<RecipeType<?>, RecipeHolder<?>> stagedByType = gtceu$indexRecipes(recipesByName);

            for (GTRecipeType gtRecipeType : stagingTypes) {
                var additionHandler = gtRecipeType.getAdditionHandler();
                    Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes = new LinkedHashMap<>();
                    for (RecipeType<?> type : gtRecipeType.getProxyRecipes().keySet()) {
                        List<RecipeHolder<GTRecipe>> recipes = new ArrayList<>();
                        proxyRecipes.put(type, recipes);
                        if (stagedByType.containsKey(type)) {
                            for (var recipe : stagedByType.get(type)) {
                                recipes.add(gtRecipeType.toGTRecipe(recipe));
                            }
                        }
                    }

                    if (stagedByType.containsKey(gtRecipeType)) {
                        Stream.concat(
                                stagedByType.get(gtRecipeType).stream(),
                                proxyRecipes.values().stream().flatMap(List::stream))
                                .filter(holder -> holder != null && holder.value() instanceof GTRecipe)
                                .forEach(holder -> {
                                    GTRecipe recipe = (GTRecipe) holder.value();
                                    recipe.setId(holder.id());
                                    additionHandler.addStaging(recipe);
                                });
                    } else {
                        proxyRecipes.values().stream()
                                .flatMap(List::stream)
                                .forEach(holder -> additionHandler.addStaging(holder.value()));
                    }

                    generations.put(gtRecipeType, gtRecipeType.prepareStagingRecipes(proxyRecipes));
            }
            for (GTRecipeType type : stagingTypes) {
                type.commitStagingRecipes(generations.get(type));
            }
            gtceu$replaceRecipes(recipesByName);
            CircuitRequirementPolicy.clearCache();
            stagingTypes.forEach(GTRecipeType::finishStagingRecipes);
        } catch (RuntimeException | Error error) {
            stagingTypes.forEach(GTRecipeType::abortStagingRecipes);
            byName = previousByName;
            byType = previousByType;
            throw error;
        }
    }

    @Unique
    public void gtceu$replaceRecipes(Map<ResourceLocation, RecipeHolder<?>> map) {
        Multimap<RecipeType<?>, RecipeHolder<?>> indexed = gtceu$indexRecipes(map);
        byName = map;
        byType = indexed;
    }

    @Unique
    private Multimap<RecipeType<?>, RecipeHolder<?>> gtceu$indexRecipes(
            Map<ResourceLocation, RecipeHolder<?>> map) {
        var recipesByType = ImmutableMultimap.<RecipeType<?>, RecipeHolder<?>>builder();

        for (var entry : map.entrySet()) {
            recipesByType.put(entry.getValue().value().getType(), entry.getValue());
        }

        return recipesByType.build();
    }
}
