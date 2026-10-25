package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class MultiblockDisplayText {
   private static final Component EMPTY_COMPONENT = Component.empty();

   public static MultiblockDisplayText.Builder builder(List<Component> textList, PatternState state) {
      return builder(textList, state, true);
   }

   public static MultiblockDisplayText.Builder builder(List<Component> textList, PatternState state, boolean showIncompleteStructureWarning) {
      return new MultiblockDisplayText.Builder(textList, state, showIncompleteStructureWarning);
   }

   public static class Builder {
      private final List<Component> textList;
      private final boolean isStructureFormed;
      private boolean isWorkingEnabled;
      private boolean isActive;
      private String idlingKey = "gtceu.multiblock.idling";
      private String pausedKey = "gtceu.multiblock.work_paused";
      private String runningKey = "gtceu.multiblock.running";

      private Builder(List<Component> textList, PatternState state, boolean showIncompleteStructureWarning) {
         this.textList = textList;
         this.isStructureFormed = state.isFormed();
         if (!this.isStructureFormed && showIncompleteStructureWarning) {
            MutableComponent base = Component.translatable("gtceu.multiblock.invalid_structure").withStyle(ChatFormatting.RED);
            Component hover = Component.translatable("gtceu.multiblock.invalid_structure.tooltip").withStyle(ChatFormatting.GRAY);
            textList.add(base.withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover))));
         }

         if (!this.isStructureFormed && state.hasErrors() && state.getErrors() != null) {
         }
      }

      public MultiblockDisplayText.Builder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
         this.isWorkingEnabled = isWorkingEnabled;
         this.isActive = isActive;
         return this;
      }

      public MultiblockDisplayText.Builder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
         if (idlingKey != null) {
            this.idlingKey = idlingKey;
         }

         if (pausedKey != null) {
            this.pausedKey = pausedKey;
         }

         if (runningKey != null) {
            this.runningKey = runningKey;
         }

         return this;
      }

      public MultiblockDisplayText.Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0L) {
               long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
               String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
               byte voltageTier = GTUtil.getFloorTierByVoltage(maxVoltage);
               Component voltageName = Component.literal(GTValues.VNF[voltageTier]);
               MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_energy_per_tick", new Object[]{energyFormatted, voltageName})
                  .withStyle(ChatFormatting.GRAY);
               Component hoverText = Component.translatable("gtceu.multiblock.max_energy_per_tick_hover").withStyle(ChatFormatting.GRAY);
               this.textList.add(bodyText.withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText))));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addEnergyTierLine(int tier) {
         if (!this.isStructureFormed) {
            return this;
         } else if (tier >= 0 && tier <= 14) {
            Component voltageName = Component.literal(GTValues.VNF[tier]);
            MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_recipe_tier", new Object[]{voltageName}).withStyle(ChatFormatting.GRAY);
            Component hoverText = Component.translatable("gtceu.multiblock.max_recipe_tier_hover").withStyle(ChatFormatting.GRAY);
            this.textList.add(bodyText.withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText))));
            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addEnergyUsageExactLine(long energyUsage) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (energyUsage > 0L) {
               String energyFormatted = FormattingUtil.formatNumbers(energyUsage);
               Component voltageName = Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(energyUsage)]);
               this.textList
                  .add(Component.translatable("gtceu.multiblock.energy_consumption", new Object[]{energyFormatted, voltageName}).withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (maxVoltage != 0L && maxVoltage >= -recipeEUt) {
               String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
               Component voltageName = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)]);
               this.textList
                  .add(
                     Component.translatable("gtceu.multiblock.max_energy_per_tick", new Object[]{energyFormatted, voltageName}).withStyle(ChatFormatting.GRAY)
                  );
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (maxVoltage != 0L && amperage != 0) {
               String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
               Component voltageName = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)]);
               this.textList
                  .add(
                     Component.translatable("gtceu.multiblock.max_energy_per_tick_amps", new Object[]{energyFormatted, amperage, voltageName})
                        .withStyle(ChatFormatting.GRAY)
                  );
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addComputationUsageLine(int maxCWUt) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (maxCWUt > 0) {
               Component computation = Component.literal(FormattingUtil.formatNumbers(maxCWUt)).withStyle(ChatFormatting.AQUA);
               this.textList.add(Component.translatable("gtceu.multiblock.computation.max", new Object[]{computation}).withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addComputationUsageExactLine(int currentCWUt) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (this.isActive && currentCWUt > 0) {
               Component computation = Component.literal(FormattingUtil.formatNumbers(currentCWUt) + " CWU/t").withStyle(ChatFormatting.AQUA);
               this.textList.add(Component.translatable("gtceu.multiblock.computation.usage", new Object[]{computation}).withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addWorkingStatusLine() {
         if (!this.isStructureFormed) {
            return this;
         } else if (!this.isWorkingEnabled) {
            return this.addWorkPausedLine(false);
         } else {
            return this.isActive ? this.addRunningPerfectlyLine(false) : this.addIdlingLine(false);
         }
      }

      public MultiblockDisplayText.Builder addWorkPausedLine(boolean checkState) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (!checkState || !this.isWorkingEnabled) {
               this.textList.add(Component.translatable(this.pausedKey).withStyle(ChatFormatting.GOLD));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addRunningPerfectlyLine(boolean checkState) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (!checkState || this.isActive) {
               this.textList.add(Component.translatable(this.runningKey).withStyle(ChatFormatting.GREEN));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addIdlingLine(boolean checkState) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (!checkState || this.isWorkingEnabled && !this.isActive) {
               this.textList.add(Component.translatable(this.idlingKey).withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addProgressLineOnlyPercent(double progressPercent) {
         if (this.isStructureFormed && this.isActive) {
            int currentProgress = (int)(progressPercent * 100.0);
            this.textList.add(Component.translatable("gtceu.multiblock.progress_percent", new Object[]{currentProgress}));
            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addProgressLine(RecipeLogic recipeLogic) {
         return recipeLogic.hasCustomProgressLine()
            ? this.addCustomProgressLine(recipeLogic)
            : this.addProgressLine((double)recipeLogic.getProgress(), (double)recipeLogic.getMaxProgress(), recipeLogic.getProgressPercent());
      }

      public MultiblockDisplayText.Builder addProgressLine(double currentDuration, double maxDuration, double progressPercent) {
         if (this.isStructureFormed && this.isActive) {
            int currentProgress = (int)(progressPercent * 100.0);
            double currentInSec = currentDuration / 20.0;
            double maxInSec = maxDuration / 20.0;
            this.textList
               .add(
                  Component.translatable(
                     "gtceu.multiblock.progress",
                     new Object[]{String.format("%.2f", (float)currentInSec), String.format("%.2f", (float)maxInSec), currentProgress}
                  )
               );
            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addCustomProgressLine(RecipeLogic recipeLogic) {
         if (this.isStructureFormed && this.isActive) {
            Component line = recipeLogic.getCustomProgressLine();
            if (line != null) {
               this.textList.add(line);
            }

            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addRecipeFailReasonLine(RecipeLogic recipeLogic) {
         if (this.isStructureFormed && recipeLogic.isIdle()) {
            Component reason = recipeLogic.getBestFailureReason();
            if (reason != null) {
               this.textList.add(Component.translatable("gtceu.recipe_logic.setup_fail").withStyle(ChatFormatting.RED));
               this.textList.add(Component.literal(" - ").append(reason));
            }

            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addBatchModeLine(boolean batchEnabled, int batchAmount) {
         // Match the sibling lines below and the MUI replacement in GTMultiblockTextUtil: a batch
         // amount of one means batching is not doing anything and should not be advertised.
         if (batchEnabled && batchAmount > 1) {
            Component runs = Component.literal(FormattingUtil.formatNumbers(batchAmount)).withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.batch_enabled";
            this.textList.add(Component.translatable(key, new Object[]{runs}).withStyle(ChatFormatting.GRAY));
         }

         return this;
      }

      public MultiblockDisplayText.Builder addSubtickParallelsLine(int subtickParallels) {
         if (subtickParallels > 1) {
            Component runs = Component.literal(FormattingUtil.formatNumbers(subtickParallels)).withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.subtick_parallels";
            this.textList.add(Component.translatable(key, new Object[]{runs}).withStyle(ChatFormatting.GRAY));
         }

         return this;
      }

      public MultiblockDisplayText.Builder addTotalRunsLine(int totalRuns) {
         if (totalRuns > 1) {
            Component runs = Component.literal(FormattingUtil.formatNumbers(totalRuns)).withStyle(ChatFormatting.DARK_PURPLE);
            String key = "gtceu.multiblock.total_runs";
            this.textList.add(Component.translatable(key, new Object[]{runs}).withStyle(ChatFormatting.GRAY));
         }

         return this;
      }

      public MultiblockDisplayText.Builder addOutputLines(GTRecipe recipe) {
         if (this.isStructureFormed && this.isActive) {
            if (recipe != null) {
               int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
               int chanceTier = recipeTier + recipe.ocLevel;
               ChanceBoostFunction function = recipe.getType().getChanceFunction();
               double maxDurationSec = (double)recipe.duration / 20.0;
               List<Content> itemOutputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
               List<Content> fluidOutputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
               int runs = recipe.getTotalRuns();

               for (Content item : itemOutputs) {
                  boolean rounded = false;
                  int count = 0;
                  double countD = 1.0;
                  ItemStack stack;
                  Component displaycount;
                  if (item.content() instanceof IntProviderIngredient provider) {
                     rounded = true;
                     stack = provider.getMaxSizeStack();
                     displaycount = Component.translatable(
                        "gtceu.gui.content.range", new Object[]{provider.getCountProvider().getMinValue(), provider.getCountProvider().getMaxValue()}
                     );
                     if (item.chance() < item.maxChance()) {
                        countD = countD * (double)runs * (double)function.getBoostedChance(item, recipeTier, chanceTier) / (double)item.maxChance();
                     }

                     countD *= provider.getMidRoll();
                  } else {
                     ItemStack[] stacks = ItemRecipeCapability.CAP.of(item.content()).getItems();
                     if (stacks.length == 0) {
                        continue;
                     }

                     stack = stacks[0];
                     count = stack.getCount();
                     countD *= (double)count;
                     if (item.chance() < item.maxChance()) {
                        rounded = true;
                        countD = countD * (double)runs * (double)function.getBoostedChance(item, recipeTier, chanceTier) / (double)item.maxChance();
                     }

                     count = Math.max(1, (int)Math.round(countD));
                     displaycount = Component.literal(String.valueOf(count));
                  }

                  if (countD < maxDurationSec) {
                     String key = "gtceu.multiblock.output_line." + (rounded ? "2" : "0");
                     this.textList
                        .add(
                           Component.translatable(
                              key, new Object[]{stack.getHoverName(), displaycount, FormattingUtil.formatNumber2Places(maxDurationSec / countD)}
                           )
                        );
                  } else {
                     String key = "gtceu.multiblock.output_line." + (rounded ? "3" : "1");
                     this.textList
                        .add(
                           Component.translatable(
                              key, new Object[]{stack.getHoverName(), displaycount, FormattingUtil.formatNumber2Places(countD / maxDurationSec)}
                           )
                        );
                  }
               }

               for (Content fluid : fluidOutputs) {
                  boolean roundedx = false;
                  int amount = 0;
                  double amountD = 1.0;
                  FluidStack stackx;
                  Component displaycountx;
                  if (fluid.content() instanceof IntProviderFluidIngredient provider) {
                     roundedx = true;
                     stackx = provider.getMaxSizeStack();
                     displaycountx = Component.translatable(
                        "gtceu.gui.content.range", new Object[]{provider.getCountProvider().getMinValue(), provider.getCountProvider().getMaxValue()}
                     );
                     if (fluid.chance() < fluid.maxChance()) {
                        amountD = amountD * (double)runs * (double)function.getBoostedChance(fluid, recipeTier, chanceTier) / (double)fluid.maxChance();
                     }

                     amountD *= provider.getMidRoll();
                  } else {
                     FluidStack[] stacksx = FluidRecipeCapability.CAP.of(fluid.content()).getFluids();
                     if (stacksx.length == 0) {
                        continue;
                     }

                     stackx = stacksx[0];
                     amount = stackx.getAmount();
                     amountD *= (double)amount;
                     if (fluid.chance() < fluid.maxChance()) {
                        roundedx = true;
                        amountD = amountD * (double)runs * (double)function.getBoostedChance(fluid, recipeTier, chanceTier) / (double)fluid.maxChance();
                     }

                     amount = Math.max(1, (int)Math.round(amountD));
                     displaycountx = Component.literal(String.valueOf(amount));
                  }

                  if (amountD < maxDurationSec) {
                     String key = "gtceu.multiblock.output_line." + (roundedx ? "2" : "0");
                     this.textList
                        .add(
                           Component.translatable(
                              key, new Object[]{stackx.getHoverName(), displaycountx, FormattingUtil.formatNumber2Places(maxDurationSec / amountD)}
                           )
                        );
                  } else {
                     String key = "gtceu.multiblock.output_line." + (roundedx ? "3" : "1");
                     this.textList
                        .add(
                           Component.translatable(
                              key, new Object[]{stackx.getHoverName(), displaycountx, FormattingUtil.formatNumber2Places(amountD / maxDurationSec)}
                           )
                        );
                  }
               }
            }

            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addMachineModeLine(GTRecipeType recipeType, boolean hasMultipleModes) {
         if (this.isStructureFormed && hasMultipleModes) {
            this.textList.add(Component.translatable("gtceu.gui.machinemode", new Object[]{recipeType.getName()}).withStyle(ChatFormatting.AQUA));
            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addParallelsLine(int numParallels) {
         return this.addParallelsLine(numParallels, false);
      }

      public MultiblockDisplayText.Builder addParallelsLine(int numParallels, boolean exact) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (numParallels > 1) {
               Component parallels = Component.literal(FormattingUtil.formatNumbers(numParallels)).withStyle(ChatFormatting.DARK_PURPLE);
               String key = "gtceu.multiblock.parallel";
               if (exact) {
                  key = key + ".exact";
               }

               this.textList.add(Component.translatable(key, new Object[]{parallels}).withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addLowPowerLine(boolean isLowPower) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (isLowPower) {
               this.textList.add(Component.translatable("gtceu.multiblock.not_enough_energy").withStyle(ChatFormatting.YELLOW));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addLowComputationLine(boolean isLowComputation) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (isLowComputation) {
               this.textList.add(Component.translatable("gtceu.multiblock.computation.not_enough_computation").withStyle(ChatFormatting.YELLOW));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addLowDynamoTierLine(boolean isTooLow) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (isTooLow) {
               this.textList.add(Component.translatable("gtceu.multiblock.not_enough_energy_output").withStyle(ChatFormatting.YELLOW));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addMaintenanceProblemLines(byte maintenanceProblems) {
         if (this.isStructureFormed && ConfigHolder.INSTANCE.machines.enableMaintenance) {
            // A set bit means the problem is fixed (MaintenanceHatchPartMachine: ALL_PROBLEMS = 0,
            // NO_PROBLEMS = 0b111111), so 0 still has six lines to show and 63 has none.
            if (maintenanceProblems >= 0 && maintenanceProblems < 0b111111) {
               this.addMaintenanceProblemHeader();
               if ((maintenanceProblems & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.wrench").withStyle(ChatFormatting.GRAY));
               }

               if ((maintenanceProblems >> 1 & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.screwdriver").withStyle(ChatFormatting.GRAY));
               }

               if ((maintenanceProblems >> 2 & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.soft_mallet").withStyle(ChatFormatting.GRAY));
               }

               if ((maintenanceProblems >> 3 & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.hard_hammer").withStyle(ChatFormatting.GRAY));
               }

               if ((maintenanceProblems >> 4 & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.wire_cutter").withStyle(ChatFormatting.GRAY));
               }

               if ((maintenanceProblems >> 5 & 1) == 0) {
                  this.textList.add(Component.translatable("gtceu.multiblock.universal.problem.crowbar").withStyle(ChatFormatting.GRAY));
               }
            }

            return this;
         } else {
            return this;
         }
      }

      private void addMaintenanceProblemHeader() {
         this.textList.add(Component.translatable("gtceu.multiblock.universal.has_problems").withStyle(ChatFormatting.YELLOW));
      }

      public MultiblockDisplayText.Builder addMufflerObstructedLine(boolean isObstructed) {
         if (!this.isStructureFormed) {
            return this;
         } else {
            if (isObstructed) {
               this.textList.add(Component.translatable("gtceu.multiblock.universal.muffler_obstructed").withStyle(ChatFormatting.RED));
               this.textList.add(Component.translatable("gtceu.multiblock.universal.muffler_obstructed.tooltip").withStyle(ChatFormatting.GRAY));
            }

            return this;
         }
      }

      public MultiblockDisplayText.Builder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
         if (this.isStructureFormed && this.isActive && fuelName != null) {
            Component fuelNeeded = Component.literal(fuelName).withStyle(ChatFormatting.RED);
            Component numTicks = Component.literal(FormattingUtil.formatNumbers(previousRecipeDuration)).withStyle(ChatFormatting.AQUA);
            this.textList
               .add(Component.translatable("gtceu.multiblock.turbine.fuel_needed", new Object[]{fuelNeeded, numTicks}).withStyle(ChatFormatting.GRAY));
            return this;
         } else {
            return this;
         }
      }

      public MultiblockDisplayText.Builder addEmptyLine() {
         this.textList.add(MultiblockDisplayText.EMPTY_COMPONENT);
         return this;
      }

      public MultiblockDisplayText.Builder addCustom(Consumer<List<Component>> customConsumer) {
         customConsumer.accept(this.textList);
         return this;
      }

      public MultiblockDisplayText.Builder addCurrentEnergyProductionLine(long euOutput) {
         this.textList
            .add(
               Component.translatable("gtceu.multiblock.turbine.energy_per_tick_maxed", new Object[]{FormattingUtil.formatNumbers(euOutput)})
                  .withStyle(ChatFormatting.GRAY)
            );
         return this;
      }
   }
}
