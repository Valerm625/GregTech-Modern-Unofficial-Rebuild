package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class EnergyStackArithmeticGameTest {

    private EnergyStackArithmeticGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void totalEUNeverWrapsNegative(GameTestHelper helper) {
        helper.assertTrue(new EnergyStack(32, 4).getTotalEU() == 128,
                "Ordinary voltage-amperage multiplication changed");
        helper.assertTrue(new EnergyStack(Long.MAX_VALUE / 2, 2).getTotalEU() == Long.MAX_VALUE - 1,
                "A representable voltage-amperage product changed");
        helper.assertTrue(new EnergyStack(Long.MAX_VALUE, 2).getTotalEU() == Long.MAX_VALUE,
                "A valid positive voltage-amperage product wrapped negative instead of saturating");
        helper.assertTrue(new EnergyStack(0, 2).getTotalEU() == 0,
                "Zero voltage no longer produces zero total EU");
        helper.assertTrue(new EnergyStack(-1, 2).getTotalEU() == -2,
                "An invalid negative voltage was converted into a huge positive amount");
        helper.assertTrue(new EnergyStack(2, -1).getTotalEU() == -2,
                "An invalid negative amperage was converted into a huge positive amount");
        helper.succeed();
    }
}
