package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public final class GTRecipeViewerLayoutGameTest {

    private GTRecipeViewerLayoutGameTest() {}

    @TestHolder
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5")
    public static void largeChemicalLayoutHasEveryDeclaredInputSlot(GameTestHelper helper) {
        var type = GTRecipeTypes.LARGE_CHEMICAL_RECIPES;
        var layout = type.getUiLayout();
        int itemSlots = countSlots(layout.capabilityInfo(ItemRecipeCapability.CAP).getRecipeViewerGrid(IO.IN));
        int fluidSlots = countSlots(layout.capabilityInfo(FluidRecipeCapability.CAP).getRecipeViewerGrid(IO.IN));

        helper.assertTrue(itemSlots == type.getMaxSlots(ItemRecipeCapability.CAP, IO.IN),
                "Large chemical reactor item grid does not match its declared input capacity");
        helper.assertTrue(fluidSlots == type.getMaxSlots(FluidRecipeCapability.CAP, IO.IN),
                "Large chemical reactor fluid grid omits a declared input slot");
        helper.succeed();
    }

    private static int countSlots(String[] rows) {
        int slots = 0;
        for (String row : rows) {
            for (int index = 0; index < row.length(); index++) {
                if (row.charAt(index) == 's') slots++;
            }
        }
        return slots;
    }
}
