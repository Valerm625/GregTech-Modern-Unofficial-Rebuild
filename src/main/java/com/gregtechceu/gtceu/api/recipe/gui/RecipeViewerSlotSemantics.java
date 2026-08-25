package com.gregtechceu.gtceu.api.recipe.gui;

/**
 * Extra recipe semantics that are not represented by ModularUI's generic
 * recipe-viewer slot contract.
 *
 * <p>
 * Recipe-tree and pattern-encoding integrations use this marker to distinguish
 * an amount consumed once per operation from an amount consumed every tick.
 * </p>
 */
public interface RecipeViewerSlotSemantics {

    void setPerTick(boolean perTick);

    boolean isPerTick();
}
