package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Arrays;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class ParallelLogicArithmeticGameTest {

    private ParallelLogicArithmeticGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void failedSearchMidpointNeverWrapsNegative(GameTestHelper helper) {
        helper.assertTrue(Arrays.equals(
                ParallelLogic.adjustMultiplier(false, 10, 20, 30),
                new int[] { 10, 15, 20 }), "Ordinary failed-search midpoint changed");

        int[] firstFailure = ParallelLogic.adjustMultiplier(false, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        int[] upperProbe = ParallelLogic.adjustMultiplier(
                true, firstFailure[0], firstFailure[1], firstFailure[2]);
        int[] secondFailure = ParallelLogic.adjustMultiplier(
                false, upperProbe[0], upperProbe[1], upperProbe[2]);

        helper.assertTrue(Arrays.equals(firstFailure,
                new int[] { 0, 1_073_741_823, Integer.MAX_VALUE }),
                "Initial maximum-bound failure produced the wrong lower midpoint");
        helper.assertTrue(Arrays.equals(upperProbe,
                new int[] { 1_073_741_823, 1_610_612_735, Integer.MAX_VALUE }),
                "Successful lower midpoint produced the wrong upper probe");
        helper.assertTrue(Arrays.equals(secondFailure,
                new int[] { 1_073_741_823, 1_342_177_279, 1_610_612_735 }),
                "A large failed-search midpoint wrapped negative");
        helper.succeed();
    }
}
