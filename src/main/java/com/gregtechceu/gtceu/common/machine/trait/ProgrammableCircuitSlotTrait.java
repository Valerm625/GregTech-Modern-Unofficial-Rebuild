package com.gregtechceu.gtceu.common.machine.trait;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.feature.IAttachConfiguratorsTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.config.ConfigHolder;
import java.util.List;
import lombok.Generated;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ProgrammableCircuitSlotTrait extends NotifiableRecipeHandlerTrait<SizedIngredient> implements IAttachConfiguratorsTrait, ICapabilityTrait {
   public static final MachineTraitType<ProgrammableCircuitSlotTrait> TYPE = new MachineTraitType<>(ProgrammableCircuitSlotTrait.class);
   @SaveField
   @SyncToClient
   public final CustomItemStackHandler storage;
   private boolean enabled = true;
   @SaveField
   @SyncToClient
   private boolean controllerAllowsCircuits = true;

   public ProgrammableCircuitSlotTrait() {
      this.setEnabled(ConfigHolder.INSTANCE.machines.ghostCircuit);
      this.storage = new CustomItemStackHandler(1);
      this.storage.setFilter(IntCircuitBehaviour::isIntegratedCircuit);
      this.storage.setOnContentsChanged(this::onStorageContentsChanged);
   }

   private void onStorageContentsChanged() {
      this.syncDataHolder.markClientSyncFieldDirty("storage");
      this.notifyListeners();
   }

   @Override
   public MachineTraitType<ProgrammableCircuitSlotTrait> getTraitType() {
      return TYPE;
   }

   public int getCurrentCircuit() {
      return IntCircuitBehaviour.getCircuitConfiguration(this.storage.getStackInSlot(0));
   }

   public void setCurrentCircuit(int circuit) {
      this.storage.setStackInSlot(0, IntCircuitBehaviour.stack(circuit));
   }

   @Override
   public void attachLeftConfigurators(Flow flow, ModularPanel<?> panel, PanelSyncManager syncManager) {
      if (CircuitRequirementPolicy.requiresCircuit(this.getMachine())) {
         if (this.controllerAllowsCircuits && this.enabled && ConfigHolder.INSTANCE.machines.ghostCircuit) {
            flow.child(GTMuiWidgets.createCircuitSlotPanel(this, panel, syncManager));
         }
      }
   }

   @Override
   public void addedToController(MultiblockControllerMachine controller) {
      if (!controller.allowCircuitSlots()) {
         this.controllerAllowsCircuits = false;
         this.syncDataHolder.markClientSyncFieldDirty("controllerAllowsCircuits");
      }
   }

   @Override
   public void removedFromController(MultiblockControllerMachine controller) {
      boolean allControllersAllowCircuits = true;

      for (MultiblockControllerMachine c : ((MultiblockPartMachine)this.getMachine()).getControllers()) {
         if (!c.allowCircuitSlots()) {
            allControllersAllowCircuits = false;
            break;
         }
      }

      this.controllerAllowsCircuits = allControllersAllowCircuits;
      this.syncDataHolder.markClientSyncFieldDirty("controllerAllowsCircuits");
   }

   @Override
   public IO getHandlerIO() {
      if (!CircuitRequirementPolicy.requiresCircuit(this.getMachine())) {
         return IO.NONE;
      } else {
         return this.enabled ? IO.IN : IO.NONE;
      }
   }

   @Override
   public IO getCapabilityIO() {
      return IO.NONE;
   }

   @Override
   public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left, boolean simulate) {
      if (!this.controllerAllowsCircuits || !this.enabled || !ConfigHolder.INSTANCE.machines.ghostCircuit) {
         return left;
      }
      return CircuitRequirementPolicy.requiresCircuit(this.getMachine())
         ? NotifiableItemStackHandler.handleRecipe(io, recipe, left, simulate, IO.IN, this.storage)
         : left;
   }

   @Override
   public List<Object> getContents() {
      return List.of(this.storage.getStackInSlot(0));
   }

   @Override
   public double getTotalContentAmount() {
      return 1.0;
   }

   @Override
   public RecipeCapability<SizedIngredient> getCapability() {
      return ItemRecipeCapability.CAP;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public boolean isControllerAllowsCircuits() {
      return this.controllerAllowsCircuits;
   }
}
