package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manages the recipe addition lifecycle as recipes are added to an {@link StagingRecipeDB}
 * and later baked into a {@link RecipeDB}
 */
@ApiStatus.Internal
@RequiredArgsConstructor
public final class RecipeAdditionHandler {

    private final @NotNull StagingRecipeDB stagingDB = new StagingRecipeDB();
    private final @NotNull RecipeDB db;

    private boolean isStaging;

    /**
     * Begin the staging process
     */
    @ApiStatus.Internal
    public void beginStaging() {
        if (isStaging) {
            throw new IllegalStateException("cannot begin staging while already in staging state");
        }
        this.isStaging = true;
    }

    /**
     * Add a recipe to the staging DB
     *
     * @param recipe the recipe
     */
    @ApiStatus.Internal
    public void addStaging(@NotNull GTRecipe recipe) {
        if (!isStaging) {
            throw new IllegalStateException("cannot add a staging recipe while not in staging state");
        }
        stagingDB.add(recipe);
    }

    /**
     * Complete the staging DB and bake it into an optimized storage
     */
    @ApiStatus.Internal
    public void completeStaging() {
        commitStaging(prepareStaging());
    }

    /**
     * Build a replacement lookup DB without publishing it. The caller can prepare
     * related indexes before committing the returned generation.
     */
    @ApiStatus.Internal
    public @NotNull StagingResult prepareStaging() {
        if (!isStaging) {
            throw new IllegalStateException("cannot prepare staging while not in staging state");
        }
        RecipeDB replacement = new RecipeDB();
        List<GTRecipe> addedRecipes = stagingDB.populateDBAndCollectAdded(replacement);
        return new StagingResult(replacement, List.copyOf(addedRecipes));
    }

    /**
     * Publish a previously prepared generation and leave staging mode.
     */
    @ApiStatus.Internal
    public void commitStaging(@NotNull StagingResult result) {
        if (!isStaging) {
            throw new IllegalStateException("cannot commit staging while not in staging state");
        }
        stagingDB.clear();
        this.isStaging = false;
        db.replaceWith(result.db());
    }

    /**
     * Discard an incomplete staging generation after recipe reload failed.
     */
    @ApiStatus.Internal
    public void abortStaging() {
        if (isStaging) {
            stagingDB.clear();
            this.isStaging = false;
        }
    }

    public record StagingResult(@NotNull RecipeDB db, @NotNull List<GTRecipe> recipes) {}
}
