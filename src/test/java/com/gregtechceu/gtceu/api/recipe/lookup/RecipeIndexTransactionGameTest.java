package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class RecipeIndexTransactionGameTest {

    private RecipeIndexTransactionGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "RecipeIndexTransaction")
    public static void failedAlternativePathLeavesNoLookupResidue(GameTestHelper helper) {
        GTRecipeType type = TestUtils.createRecipeType("recipe_db_transaction", 2, 1, 0, 0);
        RecipeDB db = new RecipeDB();

        GTRecipe incumbent = type.recipeBuilder("incumbent")
                .inputItems(Items.APPLE, 1)
                .inputItems(Items.OAK_LOG, 1)
                .outputItems(Items.STONE, 1)
                .build();
        GTRecipe candidate = type.recipeBuilder("candidate")
                .inputItems(Items.BIRCH_LOG, 1)
                .inputItems(Items.OAK_LOG, 1)
                .outputItems(Items.STONE, 1)
                .build();

        List<List<AbstractMapIngredient>> incumbentIngredients = recipeIngredients(Items.APPLE, Items.OAK_LOG);
        helper.assertTrue(incumbentIngredients.size() == 2 &&
                        incumbentIngredients.stream().allMatch(slot -> slot.size() == 1),
                "The incumbent test path must contain two single-candidate slots: " + incumbentIngredients);
        helper.assertTrue(db.add(incumbent, incumbentIngredients),
                "The incumbent recipe should be indexed");
        helper.assertTrue(db.find(lookupIngredients(Items.APPLE, Items.OAK_LOG), ignored -> true) == incumbent,
                "The incumbent path should be visible with equivalent ingredient keys");
        helper.assertFalse(db.add(candidate, recipeIngredients(Items.APPLE, Items.OAK_LOG)),
                "A direct conflict with the incumbent must be rejected");

        // The first candidate (birch) creates a complete path. The second
        // candidate (apple) collides with the incumbent and must roll back the
        // path created for birch before returning failure.
        List<List<AbstractMapIngredient>> alternatives = List.of(
                recipeSlot(Items.BIRCH_LOG, Items.APPLE),
                recipeSlot(Items.OAK_LOG));
        helper.assertFalse(db.add(candidate, alternatives),
                "A conflicting alternative set must be rejected");
        helper.assertTrue(db.find(lookupIngredients(Items.APPLE, Items.OAK_LOG), ignored -> true) == incumbent,
                "The incumbent path was changed by a failed addition");
        helper.assertTrue(db.find(lookupIngredients(Items.BIRCH_LOG, Items.OAK_LOG), ignored -> true) == null,
                "A failed addition left a candidate path in the lookup DB");
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "RecipeIndexTransaction")
    public static void abortRestoresLookupDisplayAndResearchIndexes(GameTestHelper helper) {
        GTRecipeType type = TestUtils.createRecipeType("recipe_type_transaction", 1, 1, 0, 0);
        GTRecipe oldRecipe = type.recipeBuilder("old_recipe")
                .inputItems(Items.COBBLESTONE, 1)
                .outputItems(Items.STONE, 1)
                .build();
        GTRecipe replacement = type.recipeBuilder("replacement_recipe")
                .inputItems(Items.GRAVEL, 1)
                .outputItems(Items.SAND, 1)
                .build();

        type.beginStagingRecipes();
        type.getAdditionHandler().addStaging(oldRecipe);
        type.completeStagingRecipes();
        type.addDataStickEntry("old-research", oldRecipe);

        type.beginStagingRecipes();
        type.getAdditionHandler().addStaging(replacement);
        GTRecipeType.PreparedRecipeGeneration generation =
                type.prepareStagingRecipes(type.getProxyRecipes());
        type.commitStagingRecipes(generation);

        helper.assertTrue(type.db().find(lookupIngredients(Items.GRAVEL), ignored -> true) == replacement,
                "The replacement recipe was not published during staging commit");
        helper.assertTrue(type.db().find(lookupIngredients(Items.COBBLESTONE), ignored -> true) == null,
                "The old recipe remained visible after staging commit");

        type.abortStagingRecipes();

        helper.assertTrue(type.db().find(lookupIngredients(Items.COBBLESTONE), ignored -> true) == oldRecipe,
                "Abort did not restore the previous lookup generation");
        helper.assertTrue(type.db().find(lookupIngredients(Items.GRAVEL), ignored -> true) == null,
                "Abort left the replacement lookup generation visible");
        helper.assertTrue(type.getRecipesInCategory(oldRecipe.recipeCategory).contains(oldRecipe),
                "Abort did not restore the previous category index");
        helper.assertFalse(type.getRecipesInCategory(oldRecipe.recipeCategory).contains(replacement),
                "Abort left the replacement in the category index");
        helper.assertTrue(type.getDataStickEntry("old-research") != null &&
                        type.getDataStickEntry("old-research").contains(oldRecipe),
                "Abort did not restore the previous research index");
        helper.succeed();
    }

    private static List<List<AbstractMapIngredient>> recipeIngredients(net.minecraft.world.item.Item... items) {
        List<List<AbstractMapIngredient>> ingredients = new ArrayList<>(items.length);
        for (var input : items) {
            ingredients.add(recipeSlot(input));
        }
        return ingredients;
    }

    private static List<AbstractMapIngredient> recipeSlot(net.minecraft.world.item.Item... items) {
        // RecipeDB.add receives one candidate list per independent recipe input slot.
        List<AbstractMapIngredient> slot = new ArrayList<>(items.length);
        for (var input : items) {
            slot.add(item(input));
        }
        MapIngredientPool.applyPooling(slot);
        return slot;
    }

    private static List<List<AbstractMapIngredient>> lookupIngredients(net.minecraft.world.item.Item... items) {
        // RecipeDB.find receives the available candidates for one input handler as one list.
        return List.of(Arrays.stream(items).map(RecipeIndexTransactionGameTest::item).toList());
    }

    private static AbstractMapIngredient item(net.minecraft.world.item.Item item) {
        return new ItemStackMapIngredient(new ItemStack(item));
    }
}
