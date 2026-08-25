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

        helper.assertTrue(db.add(incumbent, ingredients(Items.APPLE, Items.OAK_LOG)),
                "The incumbent recipe should be indexed");

        // The first candidate (birch) creates a complete path. The second
        // candidate (apple) collides with the incumbent and must roll back the
        // path created for birch before returning failure.
        List<List<AbstractMapIngredient>> alternatives = List.of(
                List.of(item(Items.BIRCH_LOG), item(Items.APPLE)),
                List.of(item(Items.OAK_LOG)));
        helper.assertFalse(db.add(candidate, alternatives),
                "A conflicting alternative set must be rejected");
        helper.assertTrue(db.find(ingredients(Items.APPLE, Items.OAK_LOG), ignored -> true) == incumbent,
                "The incumbent path was changed by a failed addition");
        helper.assertTrue(db.find(ingredients(Items.BIRCH_LOG, Items.OAK_LOG), ignored -> true) == null,
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

        helper.assertTrue(type.db().find(ingredients(Items.GRAVEL), ignored -> true) == replacement,
                "The replacement recipe was not published during staging commit");
        helper.assertTrue(type.db().find(ingredients(Items.COBBLESTONE), ignored -> true) == null,
                "The old recipe remained visible after staging commit");

        type.abortStagingRecipes();

        helper.assertTrue(type.db().find(ingredients(Items.COBBLESTONE), ignored -> true) == oldRecipe,
                "Abort did not restore the previous lookup generation");
        helper.assertTrue(type.db().find(ingredients(Items.GRAVEL), ignored -> true) == null,
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

    private static List<List<AbstractMapIngredient>> ingredients(net.minecraft.world.item.Item... items) {
        return List.of(java.util.Arrays.stream(items).map(RecipeIndexTransactionGameTest::item).toList());
    }

    private static AbstractMapIngredient item(net.minecraft.world.item.Item item) {
        return new ItemStackMapIngredient(new ItemStack(item));
    }
}
