package com.gregtechceu.gtceu.integration.recipeviewer.jei;

/** Pure display rules shared by the JEI renderer and its headless regression probe. */
public final class JeiIngredientDisplayPolicy {

    private JeiIngredientDisplayPolicy() {}

    /**
     * Keeps ordinary JEI stack counts intact. A ranged GT amount owns the
     * amount label, so only that case suppresses JEI's sampled stack badge.
     */
    public static int delegateItemCount(int originalCount, boolean rangedAmountOverlay) {
        return rangedAmountOverlay ? 1 : originalCount;
    }
}
