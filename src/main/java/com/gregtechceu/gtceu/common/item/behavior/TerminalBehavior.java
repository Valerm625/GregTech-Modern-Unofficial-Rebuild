package com.gregtechceu.gtceu.common.item.behavior;

import brachy.modularui.factory.ClientGUI;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TerminalBehavior implements IInteractionItem, IItemUIHolder {
   private MultiblockMachineDefinition multiblockDefinition = null;
   private MultiblockSchemaInfo multiblockSchemaInfo;
   private BlockPos controllerPos;
   private Direction frontFacing;
   private Direction upFacing;
   private boolean isFlipped = false;

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      if (player == null || !player.isShiftKeyDown()) {
         return InteractionResult.PASS;
      } else if (!player.isCreative()) {
         return InteractionResult.PASS;
      } else if (MetaMachine.getMachine(level, pos) instanceof MultiblockControllerMachine controller) {
         if (controller.getDefaultPatternState().isFormed()) {
            return InteractionResult.PASS;
         } else {
            if (controller.getDefinition() == this.multiblockDefinition && this.multiblockSchemaInfo != null) {
               this.refreshSchema();
            }

            if (this.multiblockSchemaInfo == null) {
               return InteractionResult.PASS;
            } else if (this.multiblockSchemaInfo.getStructureBlocks() != null && !this.multiblockSchemaInfo.getStructureBlocks().isEmpty()) {
               BlockPos controllerOffset = controller.getBlockPos().offset(this.multiblockSchemaInfo.getMapSchema().getControllerPos().multiply(-1));
               if (context.getPlayer().isCreative()) {
                  for (Entry<BlockPos, BlockInfo> entry : this.multiblockSchemaInfo.getStructureBlocks().entrySet()) {
                     level.setBlockAndUpdate(entry.getKey().offset(controllerOffset), entry.getValue().getBlockState());
                  }

                  if (!level.isClientSide()) {
                     controller.getDefaultPatternState().getCache().clear();
                     controller.checkAndFormStructure();
                  }
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
               return InteractionResult.PASS;
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
      Player player = context.getPlayer();
      Level level = context.getLevel();
      BlockPos blockPos = context.getClickedPos();
      if (MetaMachine.getMachine(level, blockPos) instanceof MultiblockControllerMachine controller) {
         if (controller.getDefinition() != this.multiblockDefinition && this.multiblockSchemaInfo != null) {
            this.multiblockSchemaInfo = null;
         }

         this.multiblockDefinition = controller.getDefinition();
         this.controllerPos = controller.getBlockPos();
         this.frontFacing = controller.getFrontFacing();
         this.upFacing = controller.getUpwardsFacing();
         this.isFlipped = controller.isFlipped();
         if (player != null && !player.isShiftKeyDown()) {
            if (level.isClientSide) {
               player.displayClientMessage(Component.literal("Loaded controller information"), false);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public boolean shouldOpenUI() {
      return this.multiblockDefinition != null;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player, InteractionHand usedHand) {
      if (!this.shouldOpenUI()) {
         return IItemUIHolder.super.use(item, level, player, usedHand);
      } else {
         if (level.isClientSide) {
            PlayerInventoryGuiData<?> guiData = PlayerInventoryGuiData.of(
               player, InventoryTypes.PLAYER, null, usedHand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected
            );
            ModularPanel<?> clientPanel = this.clientPanel();
            ClientGUI.open(this.createScreen(guiData, clientPanel));
         }

         return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
      }
   }

   private ModularPanel<?> clientPanel() {
      MultiblockPreviewWidget previewWidget = new MultiblockPreviewWidget(
            this.multiblockDefinition, this.multiblockSchemaInfo, 200, 200, MultiblockPreviewWidget.CandidateInteractionMode.TERMINAL_EDITABLE
         )
         .setControllerPos(this.controllerPos)
         .setFrontFacing(this.frontFacing)
         .setUpFacing(this.upFacing)
         .setFlipped(this.isFlipped);
      previewWidget.refreshSchema();
      return (ModularPanel<?>)((ModularPanel)ModularPanel.defaultPanel("terminal").coverChildren())
         .onCloseAction(() -> this.multiblockSchemaInfo = previewWidget.getMultiblockSchemaInfo())
         .child(previewWidget);
   }

   public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
      return null;
   }

   private void refreshSchema() {
      this.multiblockSchemaInfo.refreshSchema(this.multiblockDefinition, this.frontFacing, this.upFacing, this.isFlipped, null);
   }
}
