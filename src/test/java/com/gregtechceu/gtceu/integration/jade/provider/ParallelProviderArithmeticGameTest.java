package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class ParallelProviderArithmeticGameTest {

    private ParallelProviderArithmeticGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void jadeTotalRunsNeverWraps(GameTestHelper helper) {
        helper.assertTrue(ParallelProvider.saturatedParallelProduct(2, 3, 4) == 24,
                "Ordinary Jade total-run multiplication changed");
        helper.assertTrue(ParallelProvider.saturatedParallelProduct(50_000, 50_000, 1) == Integer.MAX_VALUE,
                "Jade total runs did not saturate at the display boundary");
        helper.assertTrue(
                ParallelProvider.saturatedParallelProduct(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) ==
                        Integer.MAX_VALUE,
                "Jade's three-factor total-run calculation overflowed before saturation");
        helper.assertTrue(ParallelProvider.saturatedParallelProduct(-1, -1, 1) == 0,
                "Invalid negative Jade fields were multiplied into a positive run count");
        helper.succeed();
    }
}
