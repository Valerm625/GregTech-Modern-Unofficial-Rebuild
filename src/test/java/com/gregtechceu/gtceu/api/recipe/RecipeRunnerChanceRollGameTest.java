package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class RecipeRunnerChanceRollGameTest {

    private RecipeRunnerChanceRollGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void largeParallelProductIsSplitWithoutLoss(GameTestHelper helper) {
        List<Integer> batches = new ArrayList<>();
        RecipeRunner.forEachChanceRollBatch(2_500_000_000L, batches::add);

        helper.assertTrue(batches.equals(List.of(Integer.MAX_VALUE, 352_516_353)),
                "Chance roll product was clamped, overflowed, or split incorrectly");
        long total = batches.stream().mapToLong(Integer::longValue).sum();
        helper.assertTrue(total == 2_500_000_000L,
                "Chance roll batching did not preserve the exact parallel product");

        List<Integer> invalid = new ArrayList<>();
        RecipeRunner.forEachChanceRollBatch(0L, invalid::add);
        RecipeRunner.forEachChanceRollBatch(-1L, invalid::add);
        helper.assertTrue(invalid.isEmpty(), "Invalid non-positive parallel counts produced chance rolls");
        helper.succeed();
    }
}
