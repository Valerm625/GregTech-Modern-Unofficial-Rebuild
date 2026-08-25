package com.gregtechceu.gtceu.common.machine.multiblock.part;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTMath;
import lombok.Generated;
import net.minecraft.util.Mth;

public class ParallelHatchPartMachine extends TieredPartMachine implements IMuiMachine {
   private static final int MIN_PARALLEL = 1;
   private final int maxParallel;
   @SaveField
   private int currentParallel = 1;

   public ParallelHatchPartMachine(BlockEntityCreationInfo info, int tier) {
      super(info, tier);
      this.maxParallel = (int)Math.pow(4.0, (double)(tier - 4));
      this.currentParallel = this.maxParallel;
   }

   public void setCurrentParallel(int parallelAmount) {
      this.currentParallel = Mth.clamp(parallelAmount, 1, this.maxParallel);

      for (MultiblockControllerMachine controller : this.getControllers()) {
         if (controller instanceof IRecipeLogicMachine rlm) {
            rlm.getRecipeLogic().markLastRecipeDirty();
         }
      }
   }

   @Override
   public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
      return false;
   }

   @Override
   public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
      IntSyncValue parallels = (IntSyncValue)new IntSyncValue(this::getCurrentParallel, this::setCurrentParallel).allowC2S();
      mainWidget.child(
         (IWidget)((Flow)((Flow)((Flow)((Flow)Flow.row().size(180, 60))
                     .child(
                        (IWidget)((ButtonWidget)((ButtonWidget)((ButtonWidget)((ButtonWidget)new ButtonWidget()
                                       .overlay(new IDrawable[]{new DynamicDrawable(() -> {
                                          MouseData mouseData = MouseData.create(-1);
                                          if (mouseData.ctrl() && mouseData.shift()) {
                                             return Text.str("/16");
                                          } else if (mouseData.ctrl()) {
                                             return Text.str("/8");
                                          } else {
                                             return mouseData.shift() ? Text.str("/4") : Text.str("/2");
                                          }
                                       })}))
                                    .width(32))
                                 .height(16))
                              .onMousePressed((context, button) -> {
                                 MouseData mouseData = MouseData.create(button);
                                 if (mouseData.ctrl() && mouseData.shift()) {
                                    parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() / 16), 1L, (long)this.maxParallel));
                                 } else if (mouseData.ctrl()) {
                                    parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() / 8), 1L, (long)this.maxParallel));
                                 } else if (mouseData.shift()) {
                                    parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() / 4), 1L, (long)this.maxParallel));
                                 } else {
                                    parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() / 2), 1L, (long)this.maxParallel));
                                 }

                                 return true;
                              })
                              .marginLeft(4))
                           .verticalCenter()
                     ))
                  .child(
                     (IWidget)((TextFieldWidget)((TextFieldWidget)((TextFieldWidget)new TextFieldWidget().width(40)).setTextAlignment(Alignment.CENTER))
                           .setNumbers(1, this.maxParallel)
                           .value(parallels)
                           .setDefaultNumber(1.0)
                           .marginLeft(4))
                        .verticalCenter()
                  ))
               .child(
                  (IWidget)((ButtonWidget)((ButtonWidget)((ButtonWidget)((ButtonWidget)new ButtonWidget().overlay(new IDrawable[]{new DynamicDrawable(() -> {
                     MouseData mouseData = MouseData.create(-1);
                     if (mouseData.ctrl() && mouseData.shift()) {
                        return Text.str("x16");
                     } else if (mouseData.ctrl()) {
                        return Text.str("x8");
                     } else {
                        return mouseData.shift() ? Text.str("x4") : Text.str("x2");
                     }
                  })})).width(32)).height(16)).onMousePressed((context, button) -> {
                     MouseData mouseData = MouseData.create(button);
                     if (mouseData.ctrl() && mouseData.shift()) {
                        parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() * 16), 1L, (long)this.maxParallel));
                     } else if (mouseData.ctrl()) {
                        parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() * 8), 1L, (long)this.maxParallel));
                     } else if (mouseData.shift()) {
                        parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() * 4), 1L, (long)this.maxParallel));
                     } else {
                        parallels.setValue((int)GTMath.clamp((long)(parallels.getValue() * 2), 1L, (long)this.maxParallel));
                     }

                     return true;
                  }).marginLeft(4)).verticalCenter()
               ))
            .child(
               (IWidget)((TextWidget)((TextWidget)Text.lang("gtceu.machine.parallel_hatch.parallel_ui").asWidget().marginLeft(4)).marginRight(4))
                  .verticalCenter()
            )
      );
   }

   @Generated
   public int getCurrentParallel() {
      return this.currentParallel;
   }
}
