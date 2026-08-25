package com.gregtechceu.gtceu.common.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class GTRecipeUIModifiers {
   public static final RecipeUIModifier TEMP_COIL_INFO = (recipe, widget) -> {
      if (recipe.data.contains("ebf_temp")) {
         int temp = recipe.data.getInt("ebf_temp");
         widget.textComponents.child(new TextWidget(Text.lang("gtceu.recipe.temperature", new Object[]{FormattingUtil.formatTemperature(temp)})));
         Flow coilRow = (Flow)Flow.row().coverChildrenHeight();
         ICoilType requiredCoil = ICoilType.getMinRequiredType(temp);
         if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
            coilRow.child(
               new TextWidget(
                  Text.lang("gtceu.recipe.coil.tier", new Object[]{Component.translatable(requiredCoil.getMaterial().getUnlocalizedName()).getString()})
               )
            );
         }

         List<ItemStack> items = GTCEuAPI.HEATING_COILS
            .entrySet()
            .stream()
            .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
            .map(coil -> new ItemStack((ItemLike)coil.getValue().get()))
            .toList();
         coilRow.child(
            (IWidget)((RecipeViewerSlotWidget)RecipeViewerSlotWidget.create()
                  .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                  .value(ItemStackList.of(items))
                  .background(new IDrawable[]{IDrawable.EMPTY}))
               .right(20)
         );
         widget.textComponents.child(coilRow);
      }
   };
   public static final RecipeUIModifier RESEARCH_INFO = (var0, var1) -> JeiRecipeTypeCustomLayouts.attachResearchInfo(var0, var1);
}
