package com.gregtechceu.gtceu.common.machine.multiblock.part;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemBusPartMachine extends TieredIOPartMachine implements IDistinctPart, IMuiMachine, IPaintable {
   @SaveField
   private final NotifiableItemStackHandler inventory;
   @Nullable
   protected TickableSubscription autoIOSubs;
   @Nullable
   protected ISubscription inventorySubs;
   @SaveField
   @SyncToClient
   private boolean isDistinct = false;
   @SaveField
   @SyncToClient
   protected final FilterHandler<ItemStack, ItemFilter> filterHandler;
   @SaveField
   protected final ProgrammableCircuitSlotTrait circuitSlot;

   public ItemBusPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
      this(info, tier, io, new NotifiableItemStackHandler(getInventorySize(tier), io));
   }

   public ItemBusPartMachine(BlockEntityCreationInfo info, int tier, IO io, NotifiableItemStackHandler inventory) {
      super(info, tier, io);
      this.inventory = this.attachTrait(inventory);
      this.circuitSlot = this.attachTrait(new ProgrammableCircuitSlotTrait());
      this.circuitSlot.setEnabled(io == IO.IN);
      this.filterHandler = FilterHandlers.item(this);
      inventory.setFilter(this::matchesFilter);
   }

   protected static int getInventorySize(int tier) {
      int sizeRoot = 1 + Math.min(9, tier);
      return sizeRoot * sizeRoot;
   }

   protected boolean matchesFilter(ItemStack stack) {
      return this.filterHandler.isFilterPresent() ? this.filterHandler.getFilter().test(stack) : true;
   }

   @Override
   public void onLoad() {
      super.onLoad();
      this.scheduleForNextServerTick(this::updateInventorySubscription);
      this.getHandlerList().setDistinct(this.isDistinct);
      this.getHandlerList().setColor(this.getPaintingColor());
      this.inventorySubs = this.getInventory().addChangedListener(this::updateInventorySubscription);
   }

   @Override
   public void onUnload() {
      super.onUnload();
      if (this.inventorySubs != null) {
         this.inventorySubs.unsubscribe();
         this.inventorySubs = null;
      }
   }

   @Override
   public void onMachineDestroyed() {
      if (this.filterHandler.isFilterPresent()) {
         Block.popResource(this.getLevel(), this.getBlockPos(), this.filterHandler.getFilterItem());
      }

      super.onMachineDestroyed();
   }

   @Override
   public void onPaintingColorChanged(int color) {
      this.getHandlerList().setColor(color, true);
   }

   @Override
   public void setDistinct(boolean distinct) {
      this.isDistinct = this.io != IO.OUT && distinct;
      this.syncDataHolder.markClientSyncFieldDirty("isDistinct");
      this.getHandlerList().setDistinctAndNotify(this.isDistinct);
   }

   @Override
   public boolean supportsDistinct() {
      return !this.io.support(IO.OUT);
   }

   @Override
   public int tintColor(int index) {
      return index == 9 ? this.getRealColor() : -1;
   }

   @Override
   public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
      super.onNeighborChanged(block, fromPos, isMoving);
      this.updateInventorySubscription();
   }

   @Override
   public void onRotated(Direction oldFacing, Direction newFacing) {
      super.onRotated(oldFacing, newFacing);
      this.updateInventorySubscription(newFacing);
   }

   protected void updateInventorySubscription() {
      this.updateInventorySubscription(this.getFrontFacing());
   }

   protected void updateInventorySubscription(Direction newFacing) {
      if (this.isWorkingEnabled()
         && (this.io.support(IO.OUT) && !this.getInventory().isEmpty() || this.io.support(IO.IN))
         && GTTransferUtils.hasAdjacentItemHandler(this.getLevel(), this.getBlockPos(), newFacing)) {
         this.autoIOSubs = this.subscribeServerTick(this.autoIOSubs, this::autoIO);
      } else if (this.autoIOSubs != null) {
         this.autoIOSubs.unsubscribe();
         this.autoIOSubs = null;
      }
   }

   protected void autoIO() {
      if (this.getOffsetTimer() % 5L == 0L) {
         if (this.isWorkingEnabled()) {
            if (this.io == IO.OUT) {
               this.getInventory().exportToNearby(this.getFrontFacing());
            } else if (this.io == IO.IN) {
               this.getInventory().importFromNearby(this.getFrontFacing());
            } else if (this.io == IO.BOTH) {
               this.getInventory().importFromNearby(this.getFrontFacing());
               this.getInventory().exportToNearby(this.getFrontFacing().getOpposite());
            }
         }

         this.updateInventorySubscription();
      }
   }

   @Override
   public void setWorkingEnabled(boolean workingEnabled) {
      super.setWorkingEnabled(workingEnabled);
      this.updateInventorySubscription();
   }

   @Override
   protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
      InteractionResult superResult = super.onScrewdriverClick(context);
      if (superResult != InteractionResult.PASS) {
         return superResult;
      } else if (this.io == IO.BOTH) {
         return InteractionResult.PASS;
      } else {
         return context.getPlayer().isShiftKeyDown() && this.swapIO() ? InteractionResult.sidedSuccess(this.isRemote()) : InteractionResult.PASS;
      }
   }

   public boolean swapIO() {
      BlockPos blockPos = this.getBlockPos();
      MachineDefinition newDefinition = null;
      if (this.io == IO.IN) {
         newDefinition = GTMachines.ITEM_EXPORT_BUS[this.getTier()];
      } else if (this.io == IO.OUT) {
         newDefinition = GTMachines.ITEM_IMPORT_BUS[this.getTier()];
      }

      if (newDefinition == null) {
         return false;
      } else {
         BlockState newBlockState = newDefinition.getBlock().defaultBlockState();
         this.getLevel().setBlockAndUpdate(blockPos, newBlockState);
         if (this.getLevel().getBlockEntity(blockPos) instanceof ItemBusPartMachine newMachine) {
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            newMachine.setPaintingColor(this.getPaintingColor());
         }

         return true;
      }
   }

   @Override
   public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
      int rowSize = (int)Math.sqrt((double)getInventorySize(this.tier));
      SlotGroup group = new SlotGroup("item_inv", rowSize, 0, true);
      mainWidget.child(
         ((Grid)((Grid)((Grid)new Grid().coverChildren()).center()).margin(7, 5))
            .gridOfSizeHeight(
               rowSize * rowSize,
               rowSize,
               (x, y, index) -> new ItemSlot()
                     .slot(SyncHandlers.itemSlot(this.inventory.storage, index).slotGroup(group).changeListener((oldStack, newStack, client, init) -> {
                        if (!init && ItemStack.isSameItem(oldStack, newStack)) {
                           this.inventory.onContentsChanged();
                        }
                     }).accessibility(this.inventory.handlerIO.support(IO.IN), true))
            )
      );
   }

   @Generated
   public NotifiableItemStackHandler getInventory() {
      return this.inventory;
   }

   @Generated
   @Override
   public boolean isDistinct() {
      return this.isDistinct;
   }

   @Generated
   public FilterHandler<ItemStack, ItemFilter> getFilterHandler() {
      return this.filterHandler;
   }

   @Generated
   public ProgrammableCircuitSlotTrait getCircuitSlot() {
      return this.circuitSlot;
   }
}
