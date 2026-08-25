package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {
   public transient String name;
   public transient String category;
   public final transient Object2IntMap<RecipeCapability<?>> maxInputs;
   public final transient Object2IntMap<RecipeCapability<?>> maxOutputs;
   @Nullable
   protected SoundEntry sound;
   protected boolean hasResearchSlot;
   protected int maxTooltips;
   private GTRecipeType smallRecipeMap;
   private Supplier<ItemStack> iconSupplier;
   private Consumer<GTRecipeTypeUILayout.Builder> layout;
   private ChanceBoostFunction chanceFunction = ChanceBoostFunction.NONE;

   public GTRecipeTypeBuilder(ResourceLocation id) {
      super(id);
      this.name = this.id.getPath();
      this.category = "custom";
      this.maxInputs = new Object2IntOpenHashMap();
      this.maxOutputs = new Object2IntOpenHashMap();
      this.sound = null;
      this.hasResearchSlot = false;
      this.maxTooltips = 4;
      this.smallRecipeMap = null;
      this.iconSupplier = null;
   }

   public GTRecipeTypeBuilder category(String category) {
      this.category = category;
      return this;
   }

   public GTRecipeTypeBuilder ui(Consumer<GTRecipeTypeUILayout.Builder> builder) {
      this.layout = builder;
      return this;
   }

   public GTRecipeTypeBuilder setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
      return this.setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs)
         .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
         .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs)
         .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
   }

   public GTRecipeTypeBuilder setEUIO(IO io) {
      if (io.support(IO.IN)) {
         this.setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
      }

      if (io.support(IO.OUT)) {
         this.setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
      }

      return this;
   }

   public GTRecipeTypeBuilder setMaxSize(IO io, RecipeCapability<?> cap, int max) {
      if (io == IO.IN || io == IO.BOTH) {
         this.maxInputs.put(cap, max);
      }

      if (io == IO.OUT || io == IO.BOTH) {
         this.maxOutputs.put(cap, max);
      }

      return this;
   }

   public GTRecipeTypeBuilder setSound(SoundEntry sound) {
      this.sound = sound;
      return this;
   }

   public GTRecipeTypeBuilder setHasResearchSlot(boolean hasResearchSlot) {
      this.hasResearchSlot = hasResearchSlot;
      return this;
   }

   public GTRecipeTypeBuilder setMaxTooltips(int maxTooltips) {
      this.maxTooltips = maxTooltips;
      return this;
   }

   public GTRecipeTypeBuilder setSmallRecipeMap(GTRecipeType smallRecipeMap) {
      this.smallRecipeMap = smallRecipeMap;
      return this;
   }

   public GTRecipeTypeBuilder setIconSupplier(Supplier<ItemStack> iconSupplier) {
      this.iconSupplier = iconSupplier;
      return this;
   }

   public GTRecipeTypeBuilder setChanceFunction(ChanceBoostFunction chanceFunction) {
      this.chanceFunction = chanceFunction == null ? ChanceBoostFunction.NONE : chanceFunction;
      return this;
   }

   public GTRecipeType createObject() {
      GTRecipeType type = GTRecipeTypes.register(this.id, this.category);
      type.maxInputs.putAll(this.maxInputs);
      type.maxOutputs.putAll(this.maxOutputs);
      if (this.layout != null) {
         GTRecipeTypeUILayout.Builder builder = new GTRecipeTypeUILayout.Builder(type);
         this.layout.accept(builder);
         type.setUiLayout(builder.build());
      }

      type.setSound(this.sound);
      type.setHasResearchSlot(this.hasResearchSlot);
      type.setMaxTooltips(this.maxTooltips);
      type.setSmallRecipeMap(this.smallRecipeMap);
      type.setIconSupplier(this.iconSupplier);
      type.setChanceFunction(this.chanceFunction);
      return type;
   }
}
