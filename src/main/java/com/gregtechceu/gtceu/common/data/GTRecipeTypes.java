package com.gregtechceu.gtceu.common.data;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable.Direction;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.ArcFurnaceLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.BreweryLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.CannerLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.FormingPressLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.LargeBoilerLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.MaceratorLogic;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.SmallBoilerLogic;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.recipe.gui.GTRecipeUIModifiers;
import com.gregtechceu.gtceu.common.recipe.gui.JeiRecipeLayoutGrids;
import com.gregtechceu.gtceu.common.recipe.gui.JeiRecipeTypeCustomLayouts;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ResearchManager;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public class GTRecipeTypes {
   public static final String STEAM = "steam";
   public static final String ELECTRIC = "electric";
   public static final String GENERATOR = "generator";
   public static final String MULTIBLOCK = "multiblock";
   public static final String DUMMY = "dummy";
   public static final GTRecipeType STEAM_BOILER_RECIPES = register("steam_boiler", "steam")
      .setMaxIOSize(1, 0, 1, 1)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL))
      .onRecipeBuild((builder, provider) -> {
         int duration = builder.duration / 4;
         if (duration > 0) {
            GTRecipeTypes.LARGE_BOILER_RECIPES.copyFrom(builder).duration(duration).save(provider);
         }
      })
      .addCustomRecipeLogic(SmallBoilerLogic.INSTANCE)
      .setMaxTooltips(1)
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType FURNACE_RECIPES = register("electric_furnace", "electric", RecipeType.SMELTING)
      .setMaxIOSize(1, 1, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(4L))
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType ALLOY_SMELTER_RECIPES = register("alloy_smelter", "electric")
      .setMaxIOSize(2, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
      .setIconSupplier(() -> GTMachines.ALLOY_SMELTER[1].asStack())
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType ARC_FURNACE_RECIPES = register("arc_furnace", "electric")
      .setMaxIOSize(1, 9, 1, 1)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, l) -> {
            int slots = l.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP);
            if (machine instanceof ITieredMachine tieredMachine && tieredMachine.getTier() < 4) {
               slots = 4;
            }

            return GTMuiWidgets.createGrid(slots, (int)Mth.sqrt((float)slots), true, 's');
         }))
      .setSound(GTSoundEntries.ARC)
      .onRecipeBuild(
         (recipeBuilder, provider) -> {
            if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()
               && recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()) {
               recipeBuilder.inputFluids(GTMaterials.Oxygen.getFluid(recipeBuilder.duration));
            }
         }
      )
      .addCustomRecipeLogic(ArcFurnaceLogic.INSTANCE);
   public static final GTRecipeType ASSEMBLER_RECIPES = register("assembler", "electric")
      .setMaxIOSize(9, 1, 1, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER))
      .setSound(GTSoundEntries.ASSEMBLER);
   public static final GTRecipeType AUTOCLAVE_RECIPES = register("autoclave", "electric")
      .setMaxIOSize(2, 2, 1, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CRYSTAL_OVERLAY)
      )
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType BENDER_RECIPES = register("bender", "electric")
      .setMaxIOSize(2, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BENDING)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BENDER_OVERLAY)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.INT_CIRCUIT_OVERLAY)
      )
      .setSound(GTSoundEntries.MOTOR);
   public static final GTRecipeType BREWING_RECIPES = register("brewery", "electric")
      .setMaxIOSize(1, 0, 1, 1)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(128).EUt(4L))
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BREWER_OVERLAY))
      .addCustomRecipeLogic(BreweryLogic.INSTANCE)
      .setSound(GTSoundEntries.CHEMICAL);
   public static final GTRecipeType MACERATOR_RECIPES = register("macerator", "electric")
      .setMaxIOSize(1, 4, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(150).EUt(2L))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
               .setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, layout) -> {
                  int slots = layout.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP);
                  int width = 3;
                  if (machine instanceof ITieredMachine tieredMachine) {
                     if (tieredMachine.getTier() < 3) {
                        slots = 1;
                        width = 1;
                     } else if (tieredMachine.getTier() == 3) {
                        slots = 3;
                     } else {
                        slots = 4;
                        width = 2;
                     }
                  }

                  return GTMuiWidgets.createGrid(slots, width, true, 's');
               })
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
               .setItemSlotsOverlay(IO.OUT, 0, 3, GTGuiTextures.DUST_OVERLAY)
               .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("gtceu.recipe.byproduct_tier", new Object[]{GTValues.VNF[3]})))
      )
      .setIconSupplier(() -> GTMachines.MACERATOR[1].asStack())
      .addCustomRecipeLogic(MaceratorLogic.INSTANCE)
      .setSound(GTSoundEntries.MACERATOR);
   public static final GTRecipeType CANNER_RECIPES = register("canner", "electric")
      .setMaxIOSize(2, 2, 1, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CANNER)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CANNER_OVERLAY)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CANISTER_OVERLAY)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.DARK_CANISTER_OVERLAY)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.DARK_CANISTER_OVERLAY)
      )
      .addCustomRecipeLogic(CannerLogic.INSTANCE)
      .setSound(GTSoundEntries.BATH);
   public static final GTRecipeType CENTRIFUGE_RECIPES = register("centrifuge", "electric")
      .setMaxIOSize(2, 6, 1, 6)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(5L))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.EXTRACTOR_OVERLAY)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
      )
      .setSound(GTSoundEntries.CENTRIFUGE);
   public static final GTRecipeType CHEMICAL_BATH_RECIPES = register("chemical_bath", "electric")
      .setMaxIOSize(1, 6, 1, 1)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt((long)GTValues.VA[1]))
      .UI(
         builder -> builder.setProgressBarSupplier(
                  (l, v, m) -> ((CircularProgressDrawable)((CircularProgressDrawable)new CircularProgressDrawable()
                              .emptyTexture(GTGuiTextures.PROGRESS_BATH[0]))
                           .filledTexture(GTGuiTextures.PROGRESS_BATH[1]))
                        .clockwise()
                        .asWidget()
                        .value(v)
               )
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BREWER_OVERLAY)
               .setItemSlotsOverlay(IO.OUT, 0, 5, GTGuiTextures.DUST_OVERLAY)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
      )
      .setSound(GTSoundEntries.BATH);
   public static final GTRecipeType CHEMICAL_RECIPES = register("chemical_reactor", "electric")
      .setMaxIOSize(2, 2, 3, 2)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt((long)GTValues.VA[1]))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.MOLECULAR_OVERLAY_1)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.MOLECULAR_OVERLAY_2)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.MOLECULAR_OVERLAY_3)
               .setFluidSlotOverlay(IO.IN, 1, GTGuiTextures.MOLECULAR_OVERLAY_4)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_2)
      )
      .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
      .onRecipeBuild((recipeBuilder, provider) -> GTRecipeTypes.LARGE_CHEMICAL_RECIPES.copyFrom(recipeBuilder).save(provider));
   public static final GTRecipeType COMPRESSOR_RECIPES = register("compressor", "electric")
      .setMaxIOSize(1, 1, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(200).EUt(2L))
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.COMPRESSOR_OVERLAY))
      .setIconSupplier(() -> GTMachines.COMPRESSOR[1].asStack())
      .setSound(GTSoundEntries.COMPRESSOR);
   public static final GTRecipeType CUTTER_RECIPES = register("cutter", "electric")
      .setMaxIOSize(1, 2, 1, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CUTTER)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SAWBLADE_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CUTTER_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.CUT)
      .onRecipeBuild(
         (recipeBuilder, provider) -> {
            if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()
               && recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()) {
               recipeBuilder.copy(recipeBuilder.id.withSuffix("_water"))
                  .inputFluids(GTMaterials.Water.getFluid((int)GTMath.clamp((long)recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 320L, 4L, 1000L)))
                  .duration(recipeBuilder.duration * 2)
                  .save(provider);
               recipeBuilder.copy(recipeBuilder.id.withSuffix("_distilled_water"))
                  .inputFluids(
                     GTMaterials.DistilledWater.getFluid((int)GTMath.clamp((long)recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 426L, 3L, 750L))
                  )
                  .duration((int)((double)recipeBuilder.duration * 1.5))
                  .save(provider);
               recipeBuilder.inputFluids(
                  GTMaterials.Lubricant.getFluid((int)GTMath.clamp((long)recipeBuilder.duration * recipeBuilder.EUt().getTotalEU() / 1280L, 1L, 250L))
               );
            }
         }
      );
   public static final GTRecipeType DISTILLERY_RECIPES = register("distillery", "electric")
      .setMaxIOSize(1, 1, 1, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.BEAKER_OVERLAY_1)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.BEAKER_OVERLAY_4)
      )
      .setSound(GTSoundEntries.BOILER);
   public static final GTRecipeType ELECTROLYZER_RECIPES = register("electrolyzer", "electric")
      .setMaxIOSize(2, 6, 1, 6)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.LIGHTNING_OVERLAY_1)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
               .setFluidSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.LIGHTNING_OVERLAY_2)
      )
      .setSound(GTSoundEntries.ELECTROLYZER);
   public static final GTRecipeType ELECTROMAGNETIC_SEPARATOR_RECIPES = register("electromagnetic_separator", "electric")
      .setMaxIOSize(1, 3, 0, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MAGNET)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.ARC);
   public static final GTRecipeType EXTRACTOR_RECIPES = register("extractor", "electric")
      .setMaxIOSize(1, 1, 0, 1)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(2L))
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.EXTRACTOR_OVERLAY))
      .setIconSupplier(() -> GTMachines.EXTRACTOR[1].asStack());
   public static final GTRecipeType EXTRUDER_RECIPES = register("extruder", "electric")
      .setMaxIOSize(2, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRUDER).setItemSlotOverlay(IO.IN, 1, GTGuiTextures.MOLD_OVERLAY))
      .setSound(GTSoundEntries.COMPRESSOR);
   public static final GTRecipeType FERMENTING_RECIPES = register("fermenter", "electric")
      .setMaxIOSize(1, 1, 1, 1)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(2L))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.CHEMICAL);
   public static final GTRecipeType FLUID_HEATER_RECIPES = register("fluid_heater", "electric")
      .setMaxIOSize(1, 0, 1, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.HEATING_OVERLAY_1)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.HEATING_OVERLAY_2)
      )
      .setSound(GTSoundEntries.BOILER);
   public static final GTRecipeType FLUID_SOLIDFICATION_RECIPES = register("fluid_solidifier", "electric")
      .setMaxIOSize(1, 1, 1, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SOLIDIFIER_OVERLAY))
      .setSound(GTSoundEntries.COOLING);
   private static final GTRecipeTypeUILayout.ProgressWidgetSupplier forgeHammerProgressBar = (layout, value, machine) -> (Widget<?>)((Flow)((Flow)Flow.col()
               .coverChildren())
            .child(
               (IWidget)((ProgressWidget)new ProgressWidget().value(value).name("progressBar"))
                  .texture(GTGuiTextures.PROGRESS_HAMMER.get(machine), Direction.DOWN)
                  .size(20)
            ))
         .child((IWidget)GTGuiTextures.PROGRESS_HAMMER_BASE.get(machine).asWidget().height(5));
   public static final GTRecipeType FORGE_HAMMER_RECIPES = register("forge_hammer", "electric")
      .setMaxIOSize(1, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_HAMMER)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.HAMMER_OVERLAY)
               .setProgressBarSupplier(forgeHammerProgressBar)
      )
      .setIconSupplier(() -> GTMachines.FORGE_HAMMER[1].asStack())
      .setSound(GTSoundEntries.FORGE_HAMMER);
   public static final GTRecipeType FORMING_PRESS_RECIPES = register("forming_press", "electric")
      .setMaxIOSize(6, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS))
      .addCustomRecipeLogic(FormingPressLogic.INSTANCE)
      .setSound(GTSoundEntries.COMPRESSOR);
   public static final GTRecipeType LATHE_RECIPES = register("lathe", "electric")
      .setMaxIOSize(1, 2, 0, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setItemSlotOverlay(IO.IN, 0, GTGuiTextures.PIPE_OVERLAY_1)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.PIPE_OVERLAY_2)
               .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY)
               .setProgressBarSupplier(
                  (layout, value, machine) -> (Widget<?>)((Flow)((Flow)Flow.row().coverChildren())
                           .child(
                              (IWidget)((ProgressWidget)new ProgressWidget().value(value).name("progressBar"))
                                 .texture(GTGuiTextures.PROGRESS_BAR_LATHE, Direction.RIGHT)
                                 .size(20)
                           ))
                        .child((IWidget)GTGuiTextures.PROGRESS_BAR_LATHE_BASE.asWidget().width(5))
               )
      )
      .setSound(GTSoundEntries.CUT);
   public static final GTRecipeType MIXER_RECIPES = register("mixer", "electric")
      .setMaxIOSize(6, 1, 2, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBarSupplier(
                  (l, v, m) -> ((CircularProgressDrawable)((CircularProgressDrawable)new CircularProgressDrawable()
                              .emptyTexture(GTGuiTextures.PROGRESS_MIXER[0]))
                           .filledTexture(GTGuiTextures.PROGRESS_MIXER[1]))
                        .clockwise()
                        .asWidget()
                        .value(v)
               )
               .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.DUST_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.MIXER);
   public static final GTRecipeType ORE_WASHER_RECIPES = register("ore_washer", "electric")
      .setMaxIOSize(2, 3, 1, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(16L))
      .UI(
         builder -> builder.setProgressBarSupplier(
                  (l, v, m) -> ((CircularProgressDrawable)((CircularProgressDrawable)new CircularProgressDrawable()
                              .emptyTexture(GTGuiTextures.PROGRESS_BATH[0]))
                           .filledTexture(GTGuiTextures.PROGRESS_BATH[1]))
                        .clockwise()
                        .asWidget()
                        .value(v)
               )
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.BATH);
   public static final GTRecipeType PACKER_RECIPES = register("packer", "electric")
      .setMaxIOSize(2, 2, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(12L).duration(10))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_PACKER)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.BOX_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.BOX_OVERLAY)
      )
      .setSound(GTSoundEntries.ASSEMBLER);
   public static final GTRecipeType POLARIZER_RECIPES = register("polarizer", "electric")
      .setMaxIOSize(1, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MAGNET))
      .setSound(GTSoundEntries.ARC);
   public static final GTRecipeType LASER_ENGRAVER_RECIPES = register("laser_engraver", "electric")
      .setMaxIOSize(2, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).setItemSlotOverlay(IO.IN, 1, GTGuiTextures.LENS_OVERLAY))
      .setSound(GTSoundEntries.ELECTROLYZER);
   public static final GTRecipeType SIFTER_RECIPES = register("sifter", "electric")
      .setMaxIOSize(1, 6, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_SIFTER))
      .setSound(new ExistingSoundEntry(SoundEvents.SAND_PLACE, SoundSource.BLOCKS));
   public static final GTRecipeType THERMAL_CENTRIFUGE_RECIPES = register("thermal_centrifuge", "electric")
      .setMaxIOSize(1, 3, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(30L))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(GTSoundEntries.CENTRIFUGE);
   public static final GTRecipeType WIREMILL_RECIPES = register("wiremill", "electric")
      .setMaxIOSize(2, 1, 0, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_WIREMILL).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.WIREMILL_OVERLAY))
      .setSound(GTSoundEntries.MOTOR);
   public static final GTRecipeType CIRCUIT_ASSEMBLER_RECIPES = register("circuit_assembler", "electric")
      .setMaxIOSize(6, 1, 1, 0)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CIRCUIT_ASSEMBLER).setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CIRCUIT_OVERLAY))
      .setSound(GTSoundEntries.ASSEMBLER)
      .onRecipeBuild(
         (recipeBuilder, provider) -> {
            if (recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()
               && recipeBuilder.tickInput.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList()).isEmpty()) {
               recipeBuilder.copy(recipeBuilder.id.withSuffix("_soldering_alloy"))
                  .inputFluids(GTMaterials.SolderingAlloy.getFluid(Math.max(1, 72 * recipeBuilder.getSolderMultiplier())))
                  .save(provider);
               recipeBuilder.inputFluids(GTMaterials.Tin.getFluid(Math.max(1, 144 * recipeBuilder.getSolderMultiplier())));
            }
         }
      );
   public static final GTRecipeType GAS_COLLECTOR_RECIPES = register("gas_collector", "electric")
      .setMaxIOSize(1, 0, 0, 1)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.CENTRIFUGE_OVERLAY)
      )
      .setSound(GTSoundEntries.COOLING);
   public static final GTRecipeType AIR_SCRUBBER_RECIPES = register("air_scrubber", "electric")
      .setMaxIOSize(1, 3, 1, 3)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
      .setSound(GTSoundEntries.COOLING);
   public static final GTRecipeType RESEARCH_STATION_RECIPES = register("research_station", "electric")
      .setEUIO(IO.IN)
      .setMaxSize(IO.IN, GTRecipeCapabilities.CWU, 1)
      .setMaxIOSize(2, 1, 0, 0)
      .UI(
         builder -> {
            GTRecipeTypeUILayout.Builder var10000 = builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.SCANNER_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.RESEARCH_STATION_OVERLAY);
            builder.setRecipeViewerLayoutGridBuilder(ItemRecipeCapability.CAP, IO.IN, JeiRecipeLayoutGrids.RESEARCH_ITEM_INPUT);
            builder.customRecipeTypeUI(JeiRecipeTypeCustomLayouts.RESEARCH_STATION);
            return var10000;
         }
      )
      .setScanner(true)
      .setMaxTooltips(4)
      .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.COMPUTATION);
   public static final GTRecipeType ROCK_BREAKER_RECIPES = register("rock_breaker", "electric")
      .setMaxIOSize(1, 4, 0, 0)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
               .setMachineLayoutGridBuilder(ItemRecipeCapability.CAP, IO.OUT, (machine, layout) -> GTMuiWidgets.createGrid(4, 2, true, 's'))
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DUST_OVERLAY)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
      )
      .setIconSupplier(() -> GTMachines.ROCK_CRUSHER[1].asStack())
      .setSound(GTSoundEntries.FIRE);
   public static final GTRecipeType SCANNER_RECIPES = register("scanner", "electric")
      .setEUIO(IO.IN)
      .setMaxIOSize(2, 1, 1, 0)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.DATA_ORB_OVERLAY)
               .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.SCANNER_OVERLAY)
      )
      .addCustomRecipeLogic(new ResearchManager.DataStickCopyScannerLogic())
      .setScanner(true)
      .setSound(GTSoundEntries.ELECTROLYZER);
   public static final GTRecipeType COMBUSTION_GENERATOR_FUELS = register("combustion_generator", "generator")
      .setMaxIOSize(0, 0, 1, 0)
      .setEUIO(IO.OUT)
      .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_2).setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
      .setSound(GTSoundEntries.COMBUSTION);
   public static final GTRecipeType GAS_TURBINE_FUELS = register("gas_turbine", "generator")
      .setMaxIOSize(0, 0, 1, 0)
      .setEUIO(IO.OUT)
      .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.DARK_CANISTER_OVERLAY).setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
      .setSound(GTSoundEntries.TURBINE);
   public static final GTRecipeType STEAM_TURBINE_FUELS = register("steam_turbine", "generator")
      .setMaxIOSize(0, 0, 1, 1)
      .setEUIO(IO.OUT)
      .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY).setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
      .setSound(GTSoundEntries.TURBINE);
   public static final GTRecipeType PLASMA_GENERATOR_FUELS = register("plasma_generator", "generator")
      .setMaxIOSize(0, 0, 1, 1)
      .setEUIO(IO.OUT)
      .UI(builder -> builder.setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY).setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
      .setSound(GTSoundEntries.TURBINE);
   public static final GTRecipeType LARGE_BOILER_RECIPES = register("large_boiler", "multiblock")
      .setMaxIOSize(1, 0, 1, 1)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL))
      .addCustomRecipeLogic(LargeBoilerLogic.INSTANCE)
      .setMaxTooltips(1)
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType COKE_OVEN_RECIPES = register("coke_oven", "multiblock")
      .setMaxIOSize(1, 1, 0, 1)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
      .setMaxTooltips(1)
      .setSound(GTSoundEntries.FIRE);
   public static final GTRecipeType PRIMITIVE_BLAST_FURNACE_RECIPES = register("primitive_blast_furnace", "multiblock")
      .setMaxIOSize(3, 3, 0, 0)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
      .setMaxTooltips(1)
      .setSound(GTSoundEntries.FIRE);
   public static final GTRecipeType BLAST_RECIPES = register("electric_blast_furnace", "multiblock")
      .setMaxIOSize(3, 3, 1, 1)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW).addRecipeUIModifier(GTRecipeUIModifiers.TEMP_COIL_INFO))
      .setSound(GTSoundEntries.FURNACE);
   public static final GTRecipeType DISTILLATION_RECIPES = register("distillation_tower", "multiblock")
      .setMaxIOSize(0, 1, 1, 12)
      .setEUIO(IO.IN)
      .setSound(GTSoundEntries.CHEMICAL)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))
      .onRecipeBuild(
         (recipeBuilder, provider) -> {
            if (!recipeBuilder.data.getBoolean("disable_distillery")) {
               if (recipeBuilder.output.containsKey(FluidRecipeCapability.CAP)) {
                  Content inputContent = recipeBuilder.input.get(FluidRecipeCapability.CAP).getFirst();
                  SizedFluidIngredient input = FluidRecipeCapability.CAP.of(inputContent.content());
                  ItemStack[] outputs = recipeBuilder.output.containsKey(ItemRecipeCapability.CAP)
                     ? ItemRecipeCapability.CAP.of(recipeBuilder.output.get(ItemRecipeCapability.CAP).getFirst().content()).getItems()
                     : null;
                  ItemStack outputItem = outputs != null && outputs.length != 0 ? outputs[0] : ItemStack.EMPTY;
                  if (input.ingredient().isEmpty()) {
                     return;
                  }

                  List<Content> contents = recipeBuilder.output.get(FluidRecipeCapability.CAP);

                  for (int i = 0; i < contents.size(); i++) {
                     Content outputContent = contents.get(i);
                     SizedFluidIngredient output = FluidRecipeCapability.CAP.of(outputContent.content());
                     if (!output.ingredient().hasNoFluids()) {
                        GTRecipeBuilder builder = DISTILLERY_RECIPES.recipeBuilder(
                              recipeBuilder.id.getPath() + "_to_" + BuiltInRegistries.FLUID.getKey(output.getFluids()[0].getFluid()).getPath()
                           )
                           .EUt(Math.max(1L, recipeBuilder.EUt().voltage() / 4L), recipeBuilder.EUt().amperage())
                           .circuitMeta(i + 1);
                        int ratio = RecipeHelper.getRatioForDistillery(input, output, outputItem);
                        int recipeDuration = (int)((double)recipeBuilder.duration * 2.0);
                        boolean shouldDivide = ratio != 1;
                        boolean fluidsDivisible = RecipeHelper.isFluidStackDivisibleForDistillery(input, ratio)
                           && RecipeHelper.isFluidStackDivisibleForDistillery(output, ratio);
                        SizedFluidIngredient dividedInputFluid = new SizedFluidIngredient(input.ingredient(), Math.max(1, input.amount() / ratio));
                        SizedFluidIngredient dividedOutputFluid = new SizedFluidIngredient(output.ingredient(), Math.max(1, output.amount() / ratio));
                        if (shouldDivide && fluidsDivisible) {
                           builder.chance(inputContent.chance())
                              .tierChanceBoost(inputContent.tierChanceBoost())
                              .inputFluids(dividedInputFluid)
                              .chance(outputContent.chance())
                              .tierChanceBoost(outputContent.tierChanceBoost())
                              .outputFluids(dividedOutputFluid)
                              .duration(Math.max(1, recipeDuration / ratio));
                        } else if (!shouldDivide) {
                           if (!outputItem.isEmpty()) {
                              builder.outputItems(outputItem);
                           }

                           builder.conditions.addAll(recipeBuilder.conditions);
                           builder.chance(inputContent.chance())
                              .tierChanceBoost(inputContent.tierChanceBoost())
                              .inputFluids(input)
                              .chance(outputContent.chance())
                              .tierChanceBoost(outputContent.tierChanceBoost())
                              .outputFluids(output)
                              .duration(recipeDuration)
                              .save(provider);
                           continue;
                        }

                        if (!outputItem.isEmpty()) {
                           boolean itemsDivisible = outputItem.getCount() % ratio == 0 && fluidsDivisible;
                           if (fluidsDivisible && itemsDivisible) {
                              ItemStack stack = outputItem.copy();
                              stack.setCount(stack.getCount() / ratio);
                              builder.outputItems(stack);
                           }
                        }

                        builder.save(provider);
                     }
                  }
               }
            }
         }
      );
   public static final GTRecipeType PYROLYSE_RECIPES = register("pyrolyse_oven", "multiblock")
      .setMaxIOSize(2, 1, 1, 1)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
      .setSound(GTSoundEntries.FIRE);
   public static final GTRecipeType CRACKING_RECIPES = register("cracker", "multiblock")
      .setMaxIOSize(1, 0, 2, 2)
      .setEUIO(IO.IN)
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRACKING)
               .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CRACKING_OVERLAY_1)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.CRACKING_OVERLAY_2)
               .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CIRCUIT_OVERLAY)
      )
      .setSound(GTSoundEntries.FIRE);
   public static final GTRecipeType IMPLOSION_RECIPES = register("implosion_compressor", "multiblock")
      .setMaxIOSize(3, 2, 0, 0)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.duration(20).EUt((long)GTValues.VA[1]))
      .UI(
         builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
               .setItemSlotOverlay(IO.IN, 2, GTGuiTextures.IMPLOSION_OVERLAY_1)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.IMPLOSION_OVERLAY_2)
               .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY)
      )
      .setSound(new ExistingSoundEntry((SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS));
   public static final GTRecipeType VACUUM_RECIPES = register("vacuum_freezer", "multiblock")
      .setMaxIOSize(1, 1, 2, 1)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt((long)GTValues.VA[2]))
      .setSound(GTSoundEntries.COOLING);
   public static final GTRecipeType ASSEMBLY_LINE_RECIPES = register("assembly_line", "multiblock").setMaxIOSize(16, 1, 4, 0).setEUIO(IO.IN).UI(builder -> {
      GTRecipeTypeUILayout.Builder var10000 = builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW);
      builder.customRecipeTypeUI(JeiRecipeTypeCustomLayouts.ASSEMBLY_LINE);
      return var10000;
   }).setSound(GTSoundEntries.ASSEMBLER).setHasResearchSlot(true).onRecipeBuild(ResearchManager::createDefaultResearchRecipe);
   public static final GTRecipeType LARGE_CHEMICAL_RECIPES = register("large_chemical_reactor", "multiblock")
      .setMaxIOSize(3, 3, 5, 4)
      .setEUIO(IO.IN)
      .prepareBuilder(recipeBuilder -> recipeBuilder.EUt((long)GTValues.VA[1]))
      .UI(
         builder -> {
            GTRecipeTypeUILayout.Builder var10000 = builder.setItemSlotsOverlay(IO.IN, 0, 1, GTGuiTextures.MOLECULAR_OVERLAY_1)
               .setItemSlotOverlay(IO.IN, 2, GTGuiTextures.MOLECULAR_OVERLAY_2)
               .setFluidSlotsOverlay(IO.IN, 0, 1, GTGuiTextures.MOLECULAR_OVERLAY_3)
               .setFluidSlotOverlay(IO.IN, 2, GTGuiTextures.MOLECULAR_OVERLAY_4)
               .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
               .setFluidSlotOverlay(IO.OUT, 0, GTGuiTextures.VIAL_OVERLAY_1)
               .setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE);
            return var10000;
         }
      )
      .setSound(GTValues.FOOLS.getAsBoolean() ? GTSoundEntries.SCIENCE : GTSoundEntries.CHEMICAL)
      .setSmallRecipeMap(CHEMICAL_RECIPES);
   public static final GTRecipeType FUSION_RECIPES = register("fusion_reactor", "multiblock")
      .setMaxIOSize(0, 0, 4, 1)
      .setEUIO(IO.IN)
      .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_FUSION).addRecipeUIModifier(FusionReactorMachine::addEUToStartLabel))
      .setSound(GTSoundEntries.ARC)
      .setMaxTooltips(4);
   public static final GTRecipeType DUMMY_RECIPES = register("dummy", "dummy").setXEIVisible(false);

   protected static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
      return register(GTCEu.id(name), group, proxyRecipes);
   }

   public static GTRecipeType register(ResourceLocation id, String group, RecipeType<?>... proxyRecipes) {
      GTRecipeType recipeType = new GTRecipeType(id, group, proxyRecipes);
      GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
      recipeType.setSerializer(GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer()));
      return recipeType;
   }

   public static void init() {
      GCYMRecipeTypes.init();
   }

   @Nullable
   public static GTRecipeType get(String name) {
      RecipeType<?> type = (RecipeType<?>)BuiltInRegistries.RECIPE_TYPE.get(GTCEu.id(name));
      return type instanceof GTRecipeType ? (GTRecipeType)type : null;
   }
}
