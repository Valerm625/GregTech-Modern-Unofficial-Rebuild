package com.gregtechceu.gtceu.api.recipe.gui;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Generated;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class GTRecipeTypeUILayout {
   private final GTRecipeType recipeType;
   private ProgressBarTextureSet progressBar;
   private final GTRecipeTypeUILayout.ProgressWidgetSupplier progressWidgetSupplier;
   private final Map<RecipeCapability<?>, GTRecipeTypeUILayout.CapabilityUIInfo> capabilityInfo;
   private final List<RecipeUIModifier> recipeUIModifiers;
   @Nullable
   private final Function<GTRecipe, Flow> customUIBuilder;

   public GTRecipeTypeUILayout(
      GTRecipeType recipeType,
      Map<RecipeCapability<?>, GTRecipeTypeUILayout.CapabilityUIInfo> capabilityInfo,
      List<RecipeUIModifier> recipeUIModifiers,
      GTRecipeTypeUILayout.ProgressWidgetSupplier progressWidgetSupplier,
      @Nullable Function<GTRecipe, Flow> customUIBuilder
   ) {
      this.recipeType = recipeType;
      this.capabilityInfo = capabilityInfo;
      this.recipeUIModifiers = recipeUIModifiers;
      this.progressWidgetSupplier = progressWidgetSupplier;
      this.customUIBuilder = customUIBuilder;
   }

   public GTRecipeTypeUILayout.CapabilityUIInfo capabilityInfo(RecipeCapability<?> cap) {
      GTRecipeTypeUILayout.CapabilityUIInfo info = this.capabilityInfo.computeIfAbsent(cap, GTRecipeTypeUILayout.CapabilityUIInfo::new);
      if (info.layout == null) {
         info.layout = this;
      }

      return info;
   }

   @Generated
   public GTRecipeType getRecipeType() {
      return this.recipeType;
   }

   @Generated
   public ProgressBarTextureSet getProgressBar() {
      return this.progressBar;
   }

   @Generated
   public GTRecipeTypeUILayout.ProgressWidgetSupplier getProgressWidgetSupplier() {
      return this.progressWidgetSupplier;
   }

   @Generated
   public List<RecipeUIModifier> getRecipeUIModifiers() {
      return this.recipeUIModifiers;
   }

   @Nullable
   @Generated
   public Function<GTRecipe, Flow> getCustomUIBuilder() {
      return this.customUIBuilder;
   }

   public static class Builder {
      private ProgressBarTextureSet progressBar = GTGuiTextures.PROGRESS_ARROW;
      private final Map<RecipeCapability<?>, GTRecipeTypeUILayout.CapabilityUIInfo> capabilityInfo = new Object2ObjectOpenHashMap();
      private final GTRecipeType recipeType;
      private final List<RecipeUIModifier> recipeUIModifiers = new ObjectArrayList();
      @Nullable
      private GTRecipeTypeUILayout.ProgressWidgetSupplier progressWidgetSupplier = null;
      @Nullable
      private Function<GTRecipe, Flow> customUIBuilder;

      public Builder(GTRecipeType recipeType) {
         this.recipeType = recipeType;
         this.getCapInfo(ItemRecipeCapability.CAP).machineLayoutBuilder = MachineCapabilityLayoutBuilder.ITEM;
         this.getCapInfo(FluidRecipeCapability.CAP).machineLayoutBuilder = MachineCapabilityLayoutBuilder.FLUID;
         this.getCapInfo(ItemRecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.ITEM;
         this.getCapInfo(FluidRecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.FLUID;
         this.getCapInfo(CWURecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.COMPUTATION;
         this.getCapInfo(EURecipeCapability.CAP).recipeViewerLayoutBuilder = RecipeViewerCapabilityLayoutBuilder.EU;
         this.getCapInfo(ItemRecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.ITEM;
         this.getCapInfo(FluidRecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.FLUID;
         this.getCapInfo(CWURecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.COMPUTATION;
         this.getCapInfo(EURecipeCapability.CAP).capabilityWidgetBuilder = CapabilityContentBuilder.EU;
      }

      private GTRecipeTypeUILayout.CapabilityUIInfo getCapInfo(RecipeCapability<?> cap) {
         return this.capabilityInfo.computeIfAbsent(cap, GTRecipeTypeUILayout.CapabilityUIInfo::new);
      }

      public GTRecipeTypeUILayout.Builder setSlotOverlay(IO ioMode, int slotIndex, RecipeCapability<?> cap, IDrawable overlay) {
         this.getCapInfo(cap).overlays.computeIfAbsent(ioMode, $ -> new Int2ObjectOpenHashMap()).put(slotIndex, overlay);
         return this;
      }

      public GTRecipeTypeUILayout.Builder setItemSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
         return this.setSlotOverlay(ioMode, slotIndex, ItemRecipeCapability.CAP, overlay);
      }

      public GTRecipeTypeUILayout.Builder setFluidSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
         return this.setSlotOverlay(ioMode, slotIndex, FluidRecipeCapability.CAP, overlay);
      }

      public GTRecipeTypeUILayout.Builder setItemSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
         for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
            this.setSlotOverlay(ioMode, i, ItemRecipeCapability.CAP, overlay);
         }

         return this;
      }

      public GTRecipeTypeUILayout.Builder setFluidSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd, IDrawable overlay) {
         for (int i = slotIndexStart; i <= slotIndexEnd; i++) {
            this.setSlotOverlay(ioMode, i, FluidRecipeCapability.CAP, overlay);
         }

         return this;
      }

      public GTRecipeTypeUILayout.Builder setProgressBar(ProgressBarTextureSet progressBar) {
         this.progressBar = progressBar;
         return this;
      }

      public GTRecipeTypeUILayout.Builder setProgressBarSupplier(GTRecipeTypeUILayout.ProgressWidgetSupplier progressBarSupplier) {
         this.progressWidgetSupplier = progressBarSupplier;
         return this;
      }

      public GTRecipeTypeUILayout.Builder setMachineCapabilityLayoutBuilder(RecipeCapability<?> cap, MachineCapabilityLayoutBuilder builder) {
         this.getCapInfo(cap).machineLayoutBuilder = builder;
         return this;
      }

      public GTRecipeTypeUILayout.Builder setMachineLayoutGridBuilder(
         RecipeCapability<?> cap, IO io, GTRecipeTypeUILayout.MachineCapabilityGridBuilder gridBuilder
      ) {
         this.getCapInfo(cap).machineLayoutGridBuilders.put(io, gridBuilder);
         return this;
      }

      public GTRecipeTypeUILayout.Builder loadRecipeTypeUIFromFile(String fileName) {
         throw new NotImplementedException();
      }

      public GTRecipeTypeUILayout.Builder customRecipeTypeUI(Function<GTRecipe, Flow> customUIBuilder) {
         this.customUIBuilder = customUIBuilder;
         return this;
      }

      public GTRecipeTypeUILayout.Builder setRecipeViewerLayoutCapabilityLayoutBuilder(RecipeCapability<?> cap, RecipeViewerCapabilityLayoutBuilder builder) {
         this.getCapInfo(cap).recipeViewerLayoutBuilder = builder;
         return this;
      }

      public GTRecipeTypeUILayout.Builder setRecipeViewerLayoutGridBuilder(
         RecipeCapability<?> cap, IO io, GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder gridBuilder
      ) {
         this.getCapInfo(cap).recipeViewerLayoutGridBuilders.put(io, gridBuilder);
         return this;
      }

      public GTRecipeTypeUILayout.Builder setLayoutGridBuilder(RecipeCapability<?> cap, IO io, Function<GTRecipeTypeUILayout, String[]> gridBuilder) {
         this.setRecipeViewerLayoutGridBuilder(cap, io, gridBuilder::apply);
         this.setMachineLayoutGridBuilder(cap, io, (m, l) -> gridBuilder.apply(l));
         return this;
      }

      public GTRecipeTypeUILayout.Builder addRecipeUIModifier(RecipeUIModifier recipeUIModifier) {
         this.recipeUIModifiers.add(recipeUIModifier);
         return this;
      }

      public GTRecipeTypeUILayout.Builder setCapabilityContentBuilder(RecipeCapability<?> cap, CapabilityContentBuilder contentBuilder) {
         this.getCapInfo(cap).capabilityWidgetBuilder = contentBuilder;
         return this;
      }

      public GTRecipeTypeUILayout build() {
         GTRecipeTypeUILayout.ProgressWidgetSupplier progressWidgetSupplier = this.progressWidgetSupplier;
         if (progressWidgetSupplier == null) {
            progressWidgetSupplier = (l, v, m) -> (Widget<?>)((ProgressWidget)new ProgressWidget().value(v).name("progressBar"))
                  .texture(this.progressBar.get(m), this.progressBar.fillDirection())
                  .size(this.progressBar.progressSize());
         }

         GTRecipeTypeUILayout layout = new GTRecipeTypeUILayout(
            this.recipeType, this.capabilityInfo, this.recipeUIModifiers, progressWidgetSupplier, this.customUIBuilder
         );
         layout.progressBar = this.progressBar;
         return layout;
      }
   }

   public static class CapabilityUIInfo {
      private final Map<IO, Int2ObjectOpenHashMap<IDrawable>> overlays = new Object2ObjectOpenHashMap();
      private final Map<IO, GTRecipeTypeUILayout.MachineCapabilityGridBuilder> machineLayoutGridBuilders = new EnumMap<>(IO.class);
      @Nullable
      public MachineCapabilityLayoutBuilder machineLayoutBuilder;
      @Nullable
      public CapabilityContentBuilder capabilityWidgetBuilder;
      private final Map<IO, GTRecipeTypeUILayout.RecipeViewerCapabilityGridBuilder> recipeViewerLayoutGridBuilders = new EnumMap<>(IO.class);
      @Nullable
      public RecipeViewerCapabilityLayoutBuilder recipeViewerLayoutBuilder;
      @UnknownNullability
      private GTRecipeTypeUILayout layout;
      private final RecipeCapability<?> cap;

      private CapabilityUIInfo(RecipeCapability<?> cap) {
         this.cap = cap;
      }

      public IDrawable getOverlay(IO io, int index) {
         return (IDrawable)this.overlays.computeIfAbsent(io, $ -> new Int2ObjectOpenHashMap()).getOrDefault(index, IDrawable.EMPTY);
      }

      public String[] getMachineGrid(IO io, MetaMachine machine) {
         if (this.machineLayoutGridBuilders.containsKey(io)) {
            return this.machineLayoutGridBuilders.get(io).buildGrid(machine, this.layout);
         } else {
            int slots = this.layout.recipeType.getMaxSlots(this.cap, io);
            return GTMuiWidgets.createGrid(slots, Math.min(3, slots), io.support(IO.OUT), 's');
         }
      }

      public String[] getRecipeViewerGrid(IO io) {
         if (this.layout.recipeType.registryName.getPath().equals("assembly_line") && io == IO.IN) {
            if (this.cap == ItemRecipeCapability.CAP) {
               return GTMuiWidgets.createGrid(16, 4, false, 's');
            }

            if (this.cap == FluidRecipeCapability.CAP) {
               return GTMuiWidgets.createGrid(4, 1, false, 's');
            }
         }

         return this.recipeViewerLayoutGridBuilders.containsKey(io)
            ? this.recipeViewerLayoutGridBuilders.get(io).buildGrid(this.layout)
            : GTMuiWidgets.createGrid(
               this.layout.recipeType.getMaxSlots(this.cap, io), Math.min(3, this.layout.recipeType.getMaxSlots(this.cap, io)), io.support(IO.OUT), 's'
            );
      }
   }

   @FunctionalInterface
   public interface MachineCapabilityGridBuilder {
      String[] buildGrid(MetaMachine var1, GTRecipeTypeUILayout var2);
   }

   @FunctionalInterface
   public interface ProgressWidgetSupplier {
      Widget<?> get(GTRecipeTypeUILayout var1, IDoubleValue<Double> var2, @Nullable MetaMachine var3);
   }

   @FunctionalInterface
   public interface RecipeViewerCapabilityGridBuilder {
      String[] buildGrid(GTRecipeTypeUILayout var1);
   }
}
