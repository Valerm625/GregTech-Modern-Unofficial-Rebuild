package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class ModifierFunctionParallelArithmeticGameTest {

    private ModifierFunctionParallelArithmeticGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void composedParallelCountsNeverWrap(GameTestHelper helper) {
        helper.assertTrue(ModifierFunction.FunctionBuilder.saturatedParallelProduct(125, 8) == 1_000,
                "Ordinary modifier parallel multiplication changed");
        helper.assertTrue(
                ModifierFunction.FunctionBuilder.saturatedParallelProduct(50_000, 50_000) == Integer.MAX_VALUE,
                "A composed modifier parallel count did not saturate at the int field boundary");
        helper.assertTrue(
                ModifierFunction.FunctionBuilder.saturatedParallelProduct(Integer.MAX_VALUE, 2) ==
                        Integer.MAX_VALUE,
                "A maximum modifier parallel count wrapped negative");
        helper.assertTrue(ModifierFunction.FunctionBuilder.saturatedParallelProduct(-1, -1) == 0,
                "Invalid negative counts were multiplied into a positive parallel count");
        helper.succeed();
    }
}
