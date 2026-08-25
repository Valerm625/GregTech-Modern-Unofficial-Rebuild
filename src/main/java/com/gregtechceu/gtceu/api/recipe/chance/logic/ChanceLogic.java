package com.gregtechceu.gtceu.api.recipe.chance.logic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
public abstract class ChanceLogic {

    private final boolean hasLegacyRollOverride;

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    public static final ChanceLogic OR = new ChanceLogic("or") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (Content entry : chancedEntries) {
                int maxChance = entry.maxChance();

                // OR Chanced outputs are deterministic
                // If a large batch is being done we can calculate how many we expect to get.
                // Add the guaranteed part of that to the list, then roll for the remaining chanced part.
                int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                long totalChance = (long) times * newChance;
                long guaranteed = totalChance / maxChance;
                if (guaranteed > 0) builder.add(entry.copyChanced(cap, ContentModifier.multiplier(guaranteed)));
                newChance = (int) (totalChance % maxChance);

                int cached = getCachedChance(entry, cache);
                int chance = newChance + cached;
                while (passesChance(chance, maxChance)) {
                    builder.add(entry);
                    chance -= maxChance;
                    newChance -= maxChance;
                }
                updateCachedChance(entry.content(), cache, newChance / 2 + cached);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.or");
        }

        @Override
        public String toString() {
            return "ChanceLogic{OR}";
        }
    };

    /**
     * Chanced Output Logic where all ingredients must succeed their roll in order for any to be produced
     */
    public static final ChanceLogic AND = new ChanceLogic("and") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                boolean failed = false;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance())) newChance -= entry.maxChance();
                    else failed = true;
                    updateCachedChance(entry.content(), cache, newChance / 2 + cached);
                    if (failed) break;
                }
                if (!failed) builder.addAll(chancedEntries);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.and");
        }

        @Override
        public String toString() {
            return "ChanceLogic{AND}";
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     * Deprecated following the rewrite of XOR
     */
    @Deprecated
    public static final ChanceLogic FIRST = new ChanceLogic("first") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                Content selected = null;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance())) {
                        selected = entry;
                        newChance -= entry.maxChance();
                    }
                    updateCachedChance(entry.content(), cache, newChance / 2 + cached);
                    if (selected != null) break;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.first");
        }

        @Override
        public String toString() {
            return "ChanceLogic{FIRST}";
        }
    };

    /**
     * Chanced Output Logic where only one of the ingredients will be output, in a manner weighted to the input chances
     */
    public static final ChanceLogic XOR = new ChanceLogic("xor") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            List<Content> normalizedEntries = normalizeXorChances(
                    chancedEntries, boostFunction, recipeTier, chanceTier);
            if (normalizedEntries.isEmpty()) return Collections.emptyList();

            // Use the new, normalized list for the logic
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            // for high run counts: calculate guaranteed rolls
            long nonGuaranteedTimes = times;
            if (times > 1) {
                for (Content entry : normalizedEntries) {
                    int newChance = entry.chance();
                    long totalChance = (long) times * newChance;
                    long guaranteed = totalChance / getMaxChancedValue();
                    if (guaranteed > 0) {
                        builder.add(entry.copyChanced(cap, ContentModifier.multiplier(guaranteed)));
                        nonGuaranteedTimes -= guaranteed;
                    }
                }
            }
            // roll for non-guaranteed
            for (long i = 0; i < nonGuaranteedTimes; ++i) {
                Content selected = null;
                int maxChance = getMaxChancedValue();
                for (Content entry : normalizedEntries) {
                    int newChance = entry.chance();
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, maxChance)) {
                        selected = entry;
                        newChance -= maxChance;
                    }
                    updateCachedChance(entry.content(), cache, newChance / 2 + cached);
                    if (selected != null) break;
                    maxChance -= newChance;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.xor");
        }

        @Override
        public String toString() {
            return "ChanceLogic{XOR}";
        }
    };

    /**
     * Chanced Output Logic where nothing is produced
     */
    public static final ChanceLogic NONE = new ChanceLogic("none") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(RecipeCapability<?> cap,
                                                         @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.none");
        }

        @Override
        public String toString() {
            return "ChanceLogic{NONE}";
        }
    };

    public ChanceLogic(ResourceLocation id) {
        this.hasLegacyRollOverride = hasLegacyRollOverride(getClass());
        GTRegistries.register(GTRegistries.CHANCE_LOGICS, id, this);
    }

    private ChanceLogic(String id) {
        this(GTCEu.id(id));
    }

    private static boolean hasLegacyRollOverride(Class<?> implementationClass) {
        try {
            var method = implementationClass
                    .getMethod("roll", RecipeCapability.class, List.class, Object2IntMap.class, int.class);
            return method.getDeclaringClass() != ChanceLogic.class && !Modifier.isAbstract(method.getModifiers());
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("The legacy ChanceLogic roll descriptor is missing", exception);
        }
    }

    /**
     * @param entry the entry to get the complete chance for
     * @return the total chance for the entry
     */
    static int getChance(@NotNull Content entry, @NotNull ChanceBoostFunction boostFunction,
                         int recipeTier, int chanceTier) {
        return boostFunction.getBoostedChance(entry, recipeTier, chanceTier);
    }

    /**
     * Applies tier boosts and normalizes XOR weights to exactly {@link #getMaxChancedValue()}.
     * Recipe viewers use the same normalized values so displayed probabilities cannot drift
     * from the values rolled by {@link #XOR}.
     */
    @ApiStatus.Internal
    public static @Unmodifiable List<@NotNull Content> normalizeXorChances(
            @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
            @NotNull ChanceBoostFunction boostFunction, int recipeTier, int chanceTier) {
        // Apply tier boosts before normalizing. Normalizing the base chances first can turn an all-zero
        // list into a guaranteed final entry and makes the viewer disagree with boosted XOR rolls.
        IntList effectiveChances = new IntArrayList(chancedEntries.size());
        long effectiveChanceTotal = 0;
        for (Content original : chancedEntries) {
            int maxChance = original.maxChance();
            int effectiveChance = maxChance <= 0 ? 0 :
                    Math.max(0, Math.min(maxChance,
                            getChance(original, boostFunction, recipeTier, chanceTier)));
            int scaledChance = maxChance <= 0 ? 0 :
                    (int) ((long) effectiveChance * getMaxChancedValue() / maxChance);
            effectiveChances.add(scaledChance);
            effectiveChanceTotal += scaledChance;
        }

        // No entry is eligible. In particular, do not assign the normalization remainder to the final entry.
        if (effectiveChanceTotal == 0) return Collections.emptyList();

        // Cumulative integer ratios avoid floating-point drift, keep zero weights at zero and assign every
        // rounding remainder while preserving order.
        List<Content> normalizedEntries = new ArrayList<>(chancedEntries.size());
        long cumulativeChance = 0;
        int normalizedTotal = 0;
        for (int i = 0; i < effectiveChances.size(); i++) {
            cumulativeChance += effectiveChances.getInt(i);
            int cumulativeNormalized = (int) (cumulativeChance * getMaxChancedValue() /
                    effectiveChanceTotal);
            int normalizedChance = cumulativeNormalized - normalizedTotal;
            normalizedTotal = cumulativeNormalized;

            Content original = chancedEntries.get(i);
            normalizedEntries.add(new Content(original.content(), normalizedChance,
                    getMaxChancedValue(), 0));
        }
        return Collections.unmodifiableList(normalizedEntries);
    }

    /**
     * @param chance the chance to check
     * @return if the roll with the chance is successful
     */
    static boolean passesChance(int chance, int maxChance) {
        return chance >= maxChance;
    }

    /**
     * @return the upper bound for rolling chances
     */
    public static int getMaxChancedValue() {
        return 10_000;
    }

    /**
     * @param entry the current entry
     * @param cache the cache of previously rolled chances, can be null
     * @return the cached chance, otherwise a random initial chance
     *         between 0 and {@link Content#maxChance()} (exclusive)
     */
    static int getCachedChance(Content entry, @Nullable Object2IntMap<?> cache) {
        if (cache == null || !cache.containsKey(entry.content()))
            return GTValues.RNG.nextInt(entry.maxChance());

        return cache.getInt(entry.content());
    }

    /**
     * @param ingredient the key used for the cache
     * @param cache      the cache of previously rolled chances, can be null
     * @param chance     the chance to update the cache with
     */
    static void updateCachedChance(Object ingredient, @Nullable Object2IntMap<?> cache, int chance) {
        if (cache == null) return;
        // noinspection unchecked,rawtypes
        ((Object2IntMap) cache).put(ingredient, chance);
    }

    /**
     * Roll the chance and attempt to produce the output.
     * <p>
     * New implementations should override this overload. Its default body is a
     * binary-compatibility bridge for implementations compiled against the Neo
     * mother API, where only the legacy four-argument overload was abstract.
     * Effective boosted chances are materialized before invoking that legacy
     * implementation. A legacy implementation that returns an input entry will
     * therefore expose the effective chance with a consumed tier boost. Custom
     * boost functions should be side-effect-free. Implementations should
     * override one overload directly rather than delegate between both through
     * {@code super}.
     *
     * @param chancedEntries the list of entries to roll
     * @param cache          the cache of previously rolled chances, can be null
     * @param times          the number of times to roll
     * @return a list of the produced outputs, empty if roll fails
     */
    @Unmodifiable
    public List<@NotNull Content> roll(RecipeCapability<?> cap,
                                       @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       @NotNull ChanceBoostFunction boostFunction,
                                       int recipeTier, int chanceTier,
                                       @Nullable Object2IntMap<?> cache, int times) {
        if (!hasLegacyRollOverride) {
            throw new IllegalStateException(
                    "ChanceLogic implementation " + getClass().getName() +
                            " must override either the boosted or legacy roll method");
        }

        List<Content> effectiveEntries = new ArrayList<>(chancedEntries.size());
        boolean changed = false;
        for (Content entry : chancedEntries) {
            int effectiveChance = getChance(entry, boostFunction, recipeTier, chanceTier);
            changed |= effectiveChance != entry.chance();
            effectiveEntries.add(effectiveChance == entry.chance() ? entry :
                    new Content(entry.content(), effectiveChance, entry.maxChance(), 0));
        }
        return roll(cap, changed ? Collections.unmodifiableList(effectiveEntries) : chancedEntries, cache, times);
    }

    /**
     * Legacy Neo-mother extension point. Implementations compiled for the ATM10
     * boosted API are forwarded to the boosted overload with no tier adjustment.
     * A subclass must override this method or the boosted overload above.
     */
    @Unmodifiable
    public List<@NotNull Content> roll(RecipeCapability<?> cap,
                                       @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       @Nullable Object2IntMap<?> cache, int times) {
        return roll(cap, chancedEntries, ChanceBoostFunction.NONE, 0, 0, cache, times);
    }

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param times          the number of times to roll
     * @return a list of the produced outputs
     */
    @Unmodifiable
    public List<@NotNull Content> roll(RecipeCapability<?> cap,
                                       @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       int times) {
        return roll(cap, chancedEntries, ChanceBoostFunction.NONE, 0, 0, null, times);
    }

    @NotNull
    public abstract Component getTranslation();

    @ApiStatus.Internal
    public static void init() {}
}
