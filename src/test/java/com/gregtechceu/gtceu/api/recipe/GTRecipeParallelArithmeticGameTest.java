package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class GTRecipeParallelArithmeticGameTest {

    private GTRecipeParallelArithmeticGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void totalRunsNeverWrapsNegative(GameTestHelper helper) {
        helper.assertTrue(GTRecipe.saturatedParallelProduct(2, 3, 4) == 24,
                "Ordinary parallel products changed");
        helper.assertTrue(GTRecipe.saturatedParallelProduct(50_000, 50_000, 1) == Integer.MAX_VALUE,
                "A large parallel product did not saturate at the int API boundary");
        helper.assertTrue(
                GTRecipe.saturatedParallelProduct(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) ==
                        Integer.MAX_VALUE,
                "A three-factor parallel product overflowed before saturation");
        helper.assertTrue(GTRecipe.saturatedParallelProduct(-1, 2, 3) == 0,
                "An invalid negative parallel count was exposed as a positive run count");
        helper.succeed();
    }
}
