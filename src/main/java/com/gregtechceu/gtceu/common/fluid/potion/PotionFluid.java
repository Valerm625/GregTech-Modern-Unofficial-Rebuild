package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.common.data.GTFluids;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import org.jetbrains.annotations.NotNull;

public class PotionFluid extends BaseFlowingFluid {
   public PotionFluid(Properties properties) {
      super(properties.bucket(() -> Items.AIR).block(() -> (LiquidBlock)Blocks.WATER));
      this.registerDefaultState((FluidState)((FluidState)this.getStateDefinition().any()).setValue(LEVEL, 7));
   }

   protected void createFluidStateDefinition(@NotNull Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(new Property[]{LEVEL});
   }

   public static FluidStack of(int amount, @NotNull Holder<Potion> potion) {
      return of(amount, new PotionContents(potion));
   }

   public static FluidStack of(int amount, @NotNull PotionContents potion) {
      FluidStack fluidStack = new FluidStack(((PotionFluid)GTFluids.POTION.get()).getSource(), amount);
      fluidStack.set(DataComponents.POTION_CONTENTS, potion);
      return fluidStack;
   }

   public boolean isSource(@NotNull FluidState state) {
      return this == ((PotionFluid)GTFluids.POTION.get()).getSource();
   }

   public int getAmount(@NotNull FluidState state) {
      return (Integer)state.getValue(LEVEL);
   }

   public static class PotionFluidType extends FluidType {
      public PotionFluidType(net.neoforged.neoforge.fluids.FluidType.Properties properties, ResourceLocation still, ResourceLocation flow) {
         super(properties);
      }

      @NotNull
      public String getDescriptionId(FluidStack stack) {
         return Potion.getName(
            ((PotionContents)stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).potion(), Items.POTION.getDescriptionId() + ".effect."
         );
      }
   }
}
