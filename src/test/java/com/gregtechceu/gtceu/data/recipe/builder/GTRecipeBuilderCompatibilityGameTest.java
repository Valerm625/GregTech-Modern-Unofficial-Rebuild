package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;

@SuppressWarnings("deprecation")
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class GTRecipeBuilderCompatibilityGameTest {

    private GTRecipeBuilderCompatibilityGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void fourArgumentOverloadUsesMotherCountChanceSemantics(GameTestHelper helper) {
        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .chancedOutput(TagPrefix.ingot, GTMaterials.Iron, 4, 2500)
                .build();

        assertOutput(helper, recipe, new ItemStack(Items.IRON_INGOT), 4, 2500, 0);
        helper.succeed();
    }

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void fiveArgumentOverloadRetainsTierChanceBoost(GameTestHelper helper) {
        GTRecipe recipe = GTRecipeBuilder.ofRaw()
                .chancedOutput(TagPrefix.ingot, GTMaterials.Gold, 3, 4000, 750)
                .build();

        assertOutput(helper, recipe, new ItemStack(Items.GOLD_INGOT), 3, 4000, 750);
        helper.succeed();
    }

    private static void assertOutput(
            GameTestHelper helper,
            GTRecipe recipe,
            ItemStack expectedStack,
            int expectedCount,
            int expectedChance,
            int expectedTierChanceBoost) {
        List<Content> outputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
        helper.assertTrue(outputs.size() == 1, "Expected exactly one chanced item output, got " + outputs.size());

        Content output = outputs.getFirst();
        SizedIngredient ingredient = ItemRecipeCapability.CAP.of(output.content());
        helper.assertTrue(ingredient.ingredient().test(expectedStack),
                "Chanced output did not retain the requested material");
        helper.assertTrue(ingredient.count() == expectedCount,
                "Expected output count " + expectedCount + ", got " + ingredient.count());
        helper.assertTrue(output.chance() == expectedChance,
                "Expected base chance " + expectedChance + ", got " + output.chance());
        helper.assertTrue(output.maxChance() == ChanceLogic.getMaxChancedValue(),
                "Expected default maximum chance " + ChanceLogic.getMaxChancedValue() + ", got " + output.maxChance());
        helper.assertTrue(output.tierChanceBoost() == expectedTierChanceBoost,
                "Expected tier chance boost " + expectedTierChanceBoost + ", got " + output.tierChanceBoost());
    }
}
