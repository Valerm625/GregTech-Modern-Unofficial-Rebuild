package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Grid;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;

public class ProgrammedCircuitRecipeWidget extends ParentWidget<ProgrammedCircuitRecipeWidget> {
   public static final int WIDTH = 150;
   public static final int HEIGHT = 80;

   public ProgrammedCircuitRecipeWidget() {
      this.size(150, 80);
      Grid circuits = ((Grid)new Grid().coverChildren())
         .gridOfSizeWidth(
            32, 8, (x, y, i) -> RecipeViewerSlotWidget.create().recipeSlotRole(RecipeSlotRole.RENDER_ONLY).value(IntCircuitBehaviour.stack(i + 1))
         );
      this.child((IWidget)circuits.center());
   }
}
