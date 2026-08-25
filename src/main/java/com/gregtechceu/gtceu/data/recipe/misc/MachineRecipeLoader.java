package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.common.block.StoneBlockType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.ItemEntry;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class MachineRecipeLoader {
   private static final MaterialStack[][] alloySmelterList = new MaterialStack[][]{
      {new MaterialStack(GTMaterials.Copper, 3L), new MaterialStack(GTMaterials.Tin, 1L), new MaterialStack(GTMaterials.Bronze, 4L)},
      {new MaterialStack(GTMaterials.Copper, 3L), new MaterialStack(GTMaterials.Zinc, 1L), new MaterialStack(GTMaterials.Brass, 4L)},
      {new MaterialStack(GTMaterials.Copper, 1L), new MaterialStack(GTMaterials.Nickel, 1L), new MaterialStack(GTMaterials.Cupronickel, 2L)},
      {new MaterialStack(GTMaterials.Copper, 1L), new MaterialStack(GTMaterials.Redstone, 4L), new MaterialStack(GTMaterials.RedAlloy, 1L)},
      {new MaterialStack(GTMaterials.AnnealedCopper, 3L), new MaterialStack(GTMaterials.Tin, 1L), new MaterialStack(GTMaterials.Bronze, 4L)},
      {new MaterialStack(GTMaterials.AnnealedCopper, 3L), new MaterialStack(GTMaterials.Zinc, 1L), new MaterialStack(GTMaterials.Brass, 4L)},
      {new MaterialStack(GTMaterials.AnnealedCopper, 1L), new MaterialStack(GTMaterials.Nickel, 1L), new MaterialStack(GTMaterials.Cupronickel, 2L)},
      {new MaterialStack(GTMaterials.AnnealedCopper, 1L), new MaterialStack(GTMaterials.Redstone, 4L), new MaterialStack(GTMaterials.RedAlloy, 1L)},
      {new MaterialStack(GTMaterials.Iron, 1L), new MaterialStack(GTMaterials.Tin, 1L), new MaterialStack(GTMaterials.TinAlloy, 2L)},
      {new MaterialStack(GTMaterials.WroughtIron, 1L), new MaterialStack(GTMaterials.Tin, 1L), new MaterialStack(GTMaterials.TinAlloy, 2L)},
      {new MaterialStack(GTMaterials.Iron, 2L), new MaterialStack(GTMaterials.Nickel, 1L), new MaterialStack(GTMaterials.Invar, 3L)},
      {new MaterialStack(GTMaterials.WroughtIron, 2L), new MaterialStack(GTMaterials.Nickel, 1L), new MaterialStack(GTMaterials.Invar, 3L)},
      {new MaterialStack(GTMaterials.Lead, 4L), new MaterialStack(GTMaterials.Antimony, 1L), new MaterialStack(GTMaterials.BatteryAlloy, 5L)},
      {new MaterialStack(GTMaterials.Gold, 1L), new MaterialStack(GTMaterials.Silver, 1L), new MaterialStack(GTMaterials.Electrum, 2L)},
      {new MaterialStack(GTMaterials.Magnesium, 1L), new MaterialStack(GTMaterials.Aluminium, 2L), new MaterialStack(GTMaterials.Magnalium, 3L)},
      {new MaterialStack(GTMaterials.Silver, 1L), new MaterialStack(GTMaterials.Electrotine, 4L), new MaterialStack(GTMaterials.BlueAlloy, 1L)}
   };

   private MachineRecipeLoader() {
   }

   public static void init(RecipeOutput provider) {
      ComputerRecipes.init(provider);
      registerDecompositionRecipes(provider);
      registerBlastFurnaceRecipes(provider);
      registerAssemblerRecipes(provider);
      registerAlloyRecipes(provider);
      registerBendingCompressingRecipes(provider);
      registerCokeOvenRecipes(provider);
      registerFluidRecipes(provider);
      registerMixingCrystallizationRecipes(provider);
      registerPrimitiveBlastFurnaceRecipes(provider);
      registerRecyclingRecipes(provider);
      registerStoneBricksRecipes(provider);
      registerNBTRemoval(provider);
      registerHatchConversion(provider);
   }

   private static void registerBendingCompressingRecipes(RecipeOutput provider) {
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("compressed_fireclay")
         .inputItems(TagPrefix.dust, GTMaterials.Fireclay)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COMPRESSED_FIRECLAY)
         .duration(80)
         .EUt(4L)
         .save(provider);

      for (ItemEntry<Item> shapeMold : GTItems.SHAPE_MOLDS) {
         GTRecipeTypes.FORMING_PRESS_RECIPES
            .recipeBuilder(shapeMold.getId().withPrefix("copy_mold_"))
            .duration(120)
            .EUt(22L)
            .notConsumable(shapeMold)
            .inputItems((Supplier<? extends Item>)GTItems.SHAPE_EMPTY)
            .outputItems((Supplier<? extends ItemLike>)shapeMold)
            .save(provider);
      }

      for (ItemEntry<Item> shapeExtruder : GTItems.SHAPE_EXTRUDERS) {
         if (shapeExtruder != null) {
            GTRecipeTypes.FORMING_PRESS_RECIPES
               .recipeBuilder(shapeExtruder.getId().withPrefix("copy_shape_"))
               .duration(120)
               .EUt(22L)
               .notConsumable(shapeExtruder)
               .inputItems((Supplier<? extends Item>)GTItems.SHAPE_EMPTY)
               .outputItems((Supplier<? extends ItemLike>)shapeExtruder)
               .save(provider);
         }
      }

      GTRecipeTypes.BENDER_RECIPES
         .recipeBuilder("empty_shape")
         .circuitMeta(4)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
         .outputItems((Supplier<? extends ItemLike>)GTItems.SHAPE_EMPTY)
         .duration(180)
         .EUt(12L)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.BENDER_RECIPES
         .recipeBuilder("fluid_cell_tin")
         .circuitMeta(12)
         .inputItems(TagPrefix.plate, GTMaterials.Tin, 2)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL)
         .duration(200)
         .EUt((long)GTValues.VA[0])
         .save(provider);
      GTRecipeTypes.BENDER_RECIPES
         .recipeBuilder("fluid_cell_steel")
         .circuitMeta(12)
         .inputItems(TagPrefix.plate, GTMaterials.Steel)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL)
         .duration(100)
         .EUt((long)GTValues.VA[0])
         .save(provider);
      GTRecipeTypes.BENDER_RECIPES
         .recipeBuilder("fluid_cell_ptfe")
         .circuitMeta(12)
         .inputItems(TagPrefix.plate, GTMaterials.Polytetrafluoroethylene)
         .outputItems(GTItems.FLUID_CELL, 4)
         .duration(100)
         .EUt((long)GTValues.VA[0])
         .save(provider);
      GTRecipeTypes.BENDER_RECIPES
         .recipeBuilder("fluid_cell_pbi")
         .circuitMeta(12)
         .inputItems(TagPrefix.plate, GTMaterials.Polybenzimidazole)
         .outputItems(GTItems.FLUID_CELL, 16)
         .duration(100)
         .EUt((long)GTValues.VA[0])
         .save(provider);
      GTRecipeTypes.EXTRUDER_RECIPES
         .recipeBuilder("fluid_cell_tin")
         .inputItems(TagPrefix.ingot, GTMaterials.Tin, 2)
         .notConsumable(GTItems.SHAPE_EXTRUDER_CELL)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL)
         .duration(128)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.EXTRUDER_RECIPES
         .recipeBuilder("fluid_cell_steel")
         .inputItems(TagPrefix.ingot, GTMaterials.Steel)
         .notConsumable(GTItems.SHAPE_EXTRUDER_CELL)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL)
         .duration(128)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.EXTRUDER_RECIPES
         .recipeBuilder("fluid_cell_ptfe")
         .inputItems(TagPrefix.ingot, GTMaterials.Polytetrafluoroethylene)
         .notConsumable(GTItems.SHAPE_EXTRUDER_CELL)
         .outputItems(GTItems.FLUID_CELL, 4)
         .duration(128)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.EXTRUDER_RECIPES
         .recipeBuilder("fluid_cell_pbi")
         .inputItems(TagPrefix.ingot, GTMaterials.Polybenzimidazole)
         .notConsumable(GTItems.SHAPE_EXTRUDER_CELL)
         .outputItems(GTItems.FLUID_CELL, 16)
         .duration(128)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.EXTRUDER_RECIPES
         .recipeBuilder("glass_vial")
         .inputItems(TagPrefix.dust, GTMaterials.Glass)
         .notConsumable(GTItems.SHAPE_EXTRUDER_CELL)
         .outputItems(GTItems.FLUID_CELL_GLASS_VIAL, 4)
         .duration(128)
         .EUt((long)GTValues.VA[1])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("nether_quartz_plate")
         .inputItems(TagPrefix.dust, GTMaterials.NetherQuartz)
         .outputItems(TagPrefix.plate, GTMaterials.NetherQuartz)
         .duration(400)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("certus_quartz_plate")
         .inputItems(TagPrefix.dust, GTMaterials.CertusQuartz)
         .outputItems(TagPrefix.plate, GTMaterials.CertusQuartz)
         .duration(400)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("quartzite_plate")
         .inputItems(TagPrefix.dust, GTMaterials.Quartzite)
         .outputItems(TagPrefix.plate, GTMaterials.Quartzite)
         .duration(400)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("coke_bricks")
         .inputItems(GTItems.COKE_OVEN_BRICK, 4)
         .outputItems(GTBlocks.CASING_COKE_BRICKS.asStack())
         .duration(300)
         .EUt(2L)
         .save(provider);
   }

   private static void registerPrimitiveBlastFurnaceRecipes(RecipeOutput provider) {
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_gem")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.gem, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(1800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_dust")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.dust, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(1800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_gem")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.gem, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(1800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_dust")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.dust, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(1800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_gem")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.gem, GTMaterials.Coke)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .duration(1500)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_dust")
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputItems(TagPrefix.dust, GTMaterials.Coke)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .duration(1500)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_block")
         .inputItems(TagPrefix.block, GTMaterials.Iron)
         .inputItems(TagPrefix.block, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.DarkAsh, 2)
         .duration(16200)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_block")
         .inputItems(TagPrefix.block, GTMaterials.Iron)
         .inputItems(TagPrefix.block, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.DarkAsh, 2)
         .duration(16200)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_block")
         .inputItems(TagPrefix.block, GTMaterials.Iron)
         .inputItems(TagPrefix.block, GTMaterials.Coke)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.Ash)
         .duration(13500)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_gem_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.gem, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_dust_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.dust, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_gem_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.gem, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_dust_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.dust, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .outputItems(TagPrefix.dustTiny, GTMaterials.DarkAsh, 2)
         .duration(800)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_gem_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.gem, GTMaterials.Coke)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .duration(600)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_dust_wrought")
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.dust, GTMaterials.Coke)
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .duration(600)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coal_block_wrought")
         .inputItems(TagPrefix.block, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.block, GTMaterials.Coal, 2)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.DarkAsh, 2)
         .duration(7200)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_charcoal_block_wrought")
         .inputItems(TagPrefix.block, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.block, GTMaterials.Charcoal, 2)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.DarkAsh, 2)
         .duration(7200)
         .save(provider);
      GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES
         .recipeBuilder("steel_from_coke_block_wrought")
         .inputItems(TagPrefix.block, GTMaterials.WroughtIron)
         .inputItems(TagPrefix.block, GTMaterials.Coke)
         .outputItems(TagPrefix.block, GTMaterials.Steel)
         .outputItems(TagPrefix.dust, GTMaterials.Ash)
         .duration(5400)
         .save(provider);
   }

   private static void registerCokeOvenRecipes(RecipeOutput provider) {
      GTRecipeTypes.COKE_OVEN_RECIPES
         .recipeBuilder("log_to_charcoal")
         .inputItems(ItemTags.LOGS_THAT_BURN)
         .outputItems(TagPrefix.gem, GTMaterials.Charcoal)
         .outputFluids(GTMaterials.Creosote.getFluid(250))
         .duration(900)
         .save(provider);
      GTRecipeTypes.COKE_OVEN_RECIPES
         .recipeBuilder("coal_to_coke")
         .inputItems(TagPrefix.gem, GTMaterials.Coal)
         .outputItems(TagPrefix.gem, GTMaterials.Coke)
         .outputFluids(GTMaterials.Creosote.getFluid(500))
         .duration(900)
         .save(provider);
      GTRecipeTypes.COKE_OVEN_RECIPES
         .recipeBuilder("coal_to_coke_block")
         .inputItems(TagPrefix.block, GTMaterials.Coal)
         .outputItems(TagPrefix.block, GTMaterials.Coke)
         .outputFluids(GTMaterials.Creosote.getFluid(4500))
         .duration(8100)
         .save(provider);
   }

   private static void registerStoneBricksRecipes(RecipeOutput provider) {
      List<ItemStack> cobbles = GTBlocks.STONE_BLOCKS.row(StoneBlockType.COBBLE).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> mossCobbles = GTBlocks.STONE_BLOCKS.row(StoneBlockType.COBBLE_MOSSY).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> stones = GTBlocks.STONE_BLOCKS.row(StoneBlockType.STONE).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> polisheds = GTBlocks.STONE_BLOCKS.row(StoneBlockType.POLISHED).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> bricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.BRICKS).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> crackedBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.BRICKS_CRACKED).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> mossBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.BRICKS_MOSSY).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> chiseledBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.CHISELED).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> tiledBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.TILED).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> smallTiledBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.TILED_SMALL).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> windmillA = GTBlocks.STONE_BLOCKS.row(StoneBlockType.WINDMILL_A).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> windmillB = GTBlocks.STONE_BLOCKS.row(StoneBlockType.WINDMILL_B).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> squareBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.BRICKS_SQUARE).values().stream().<ItemStack>map(ItemStack::new).toList();
      List<ItemStack> smallBricks = GTBlocks.STONE_BLOCKS.row(StoneBlockType.BRICKS_SMALL).values().stream().<ItemStack>map(ItemStack::new).toList();
      registerSmoothRecipe(provider, cobbles, stones);
      registerCobbleRecipe(provider, stones, cobbles);
      registerMossRecipe(provider, cobbles, mossCobbles);
      registerSmoothRecipe(provider, stones, polisheds);
      registerBricksRecipe(provider, polisheds, bricks, MarkerMaterials.Color.LightBlue);
      registerCobbleRecipe(provider, bricks, crackedBricks);
      registerMossRecipe(provider, bricks, mossBricks);
      registerBricksRecipe(provider, polisheds, chiseledBricks, MarkerMaterials.Color.White);
      registerBricksRecipe(provider, polisheds, tiledBricks, MarkerMaterials.Color.Red);
      registerBricksRecipe(provider, tiledBricks, smallTiledBricks, MarkerMaterials.Color.Red);
      registerBricksRecipe(provider, polisheds, windmillA, MarkerMaterials.Color.Blue);
      registerBricksRecipe(provider, polisheds, windmillB, MarkerMaterials.Color.Yellow);
      registerBricksRecipe(provider, polisheds, squareBricks, MarkerMaterials.Color.Green);
      registerBricksRecipe(provider, polisheds, smallBricks, MarkerMaterials.Color.Pink);

      for (int i = 0; i < stones.size(); i++) {
         ResourceLocation bricksId = BuiltInRegistries.ITEM.getKey(bricks.get(i).getItem());
         GTRecipeTypes.EXTRUDER_RECIPES
            .recipeBuilder("extrude_" + bricksId.getPath())
            .inputItems(stones.get(i))
            .notConsumable(GTItems.SHAPE_EXTRUDER_INGOT)
            .outputItems(bricks.get(i))
            .duration(24)
            .EUt(8L)
            .save(provider);
      }
   }

   private static void registerMixingCrystallizationRecipes(RecipeOutput provider) {
      GTRecipeTypes.AUTOCLAVE_RECIPES
         .recipeBuilder("silicon_dioxide_to_quartzite_gem")
         .inputItems(TagPrefix.dust, GTMaterials.SiliconDioxide)
         .inputFluids(GTMaterials.DistilledWater.getFluid(250))
         .chancedOutput(ChemicalHelper.get(TagPrefix.gem, GTMaterials.Quartzite), 4500)
         .duration(1200)
         .EUt(24L)
         .save(provider);
      GTRecipeTypes.MIXER_RECIPES
         .recipeBuilder("indium_concentrate")
         .inputItems(TagPrefix.crushedPurified, GTMaterials.Sphalerite)
         .inputItems(TagPrefix.crushedPurified, GTMaterials.Galena)
         .inputFluids(GTMaterials.SulfuricAcid.getFluid(4000))
         .outputFluids(GTMaterials.IndiumConcentrate.getFluid(1000))
         .duration(60)
         .EUt(150L)
         .save(provider);
   }

   private static void registerAlloyRecipes(RecipeOutput provider) {
      for (MaterialStack[] stack : alloySmelterList) {
         String recipeNape = stack[0].material().getName() + "_%s_and_" + stack[1].material().getName() + "_%s_into_" + stack[2].material().getName();
         if (stack[0].material().hasProperty(PropertyKey.INGOT)) {
            GTRecipeTypes.ALLOY_SMELTER_RECIPES
               .recipeBuilder(String.format(recipeNape, "ingot", "dust"))
               .duration((int)stack[2].amount() * 50)
               .EUt(16L)
               .inputItems(TagPrefix.ingot, stack[0].material(), (int)stack[0].amount())
               .inputItems(TagPrefix.dust, stack[1].material(), (int)stack[1].amount())
               .outputItems(ChemicalHelper.get(TagPrefix.ingot, stack[2].material(), (int)stack[2].amount()))
               .save(provider);
         }

         if (stack[1].material().hasProperty(PropertyKey.INGOT)) {
            GTRecipeTypes.ALLOY_SMELTER_RECIPES
               .recipeBuilder(String.format(recipeNape, "dust", "ingot"))
               .duration((int)stack[2].amount() * 50)
               .EUt(16L)
               .inputItems(TagPrefix.dust, stack[0].material(), (int)stack[0].amount())
               .inputItems(TagPrefix.ingot, stack[1].material(), (int)stack[1].amount())
               .outputItems(ChemicalHelper.get(TagPrefix.ingot, stack[2].material(), (int)stack[2].amount()))
               .save(provider);
         }

         if (stack[0].material().hasProperty(PropertyKey.INGOT) && stack[1].material().hasProperty(PropertyKey.INGOT)) {
            GTRecipeTypes.ALLOY_SMELTER_RECIPES
               .recipeBuilder(String.format(recipeNape, "ingot", "ingot"))
               .duration((int)stack[2].amount() * 50)
               .EUt(16L)
               .inputItems(TagPrefix.ingot, stack[0].material(), (int)stack[0].amount())
               .inputItems(TagPrefix.ingot, stack[1].material(), (int)stack[1].amount())
               .outputItems(ChemicalHelper.get(TagPrefix.ingot, stack[2].material(), (int)stack[2].amount()))
               .save(provider);
         }

         GTRecipeTypes.ALLOY_SMELTER_RECIPES
            .recipeBuilder(String.format(recipeNape, "dust", "dust"))
            .duration((int)stack[2].amount() * 50)
            .EUt(16L)
            .inputItems(TagPrefix.dust, stack[0].material(), (int)stack[0].amount())
            .inputItems(TagPrefix.dust, stack[1].material(), (int)stack[1].amount())
            .outputItems(ChemicalHelper.get(TagPrefix.ingot, stack[2].material(), (int)stack[2].amount()))
            .save(provider);
      }

      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("carbon_mesh")
         .inputItems(GTItems.CARBON_FIBERS, 2)
         .outputItems((Supplier<? extends ItemLike>)GTItems.CARBON_MESH)
         .duration(100)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("carbon_fiber_plate")
         .inputItems((Supplier<? extends Item>)GTItems.CARBON_MESH)
         .outputItems((Supplier<? extends ItemLike>)GTItems.CARBON_FIBER_PLATE)
         .duration(200)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.ALLOY_SMELTER_RECIPES
         .recipeBuilder("rubber_sheet")
         .duration(10)
         .EUt((long)GTValues.VA[0])
         .inputItems(TagPrefix.ingot, GTMaterials.Rubber, 2)
         .notConsumable(GTItems.SHAPE_MOLD_PLATE)
         .outputItems(TagPrefix.plate, GTMaterials.Rubber)
         .save(provider);
      GTRecipeTypes.ALLOY_SMELTER_RECIPES
         .recipeBuilder("rubber_bar")
         .duration(100)
         .EUt((long)GTValues.VA[0])
         .inputItems(TagPrefix.dust, GTMaterials.Sulfur)
         .inputItems(TagPrefix.dust, GTMaterials.RawRubber, 3)
         .outputItems(TagPrefix.ingot, GTMaterials.Rubber)
         .save(provider);
      GTRecipeTypes.ALLOY_SMELTER_RECIPES
         .recipeBuilder("coke_oven_brick")
         .duration(150)
         .EUt((long)GTValues.VA[0])
         .inputItems(Items.SANDS)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.CLAY_BALL))
         .outputItems(GTItems.COKE_OVEN_BRICK, 2)
         .save(provider);
   }

   private static void registerAssemblerRecipes(RecipeOutput provider) {
      for (int i = 0; i < GTMaterials.CHEMICAL_DYES.length; i++) {
         GTRecipeTypes.CANNER_RECIPES
            .recipeBuilder("spray_can_" + GTMaterials.CHEMICAL_DYES[i].getName())
            .inputItems((Supplier<? extends Item>)GTItems.SPRAY_EMPTY)
            .inputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(576))
            .outputItems((Supplier<? extends ItemLike>)GTItems.SPRAY_CAN_DYES[i])
            .EUt((long)GTValues.VA[0])
            .duration(200)
            .addMaterialInfo(true)
            .save(provider);
         DyeColor color = DyeColor.byId(i);
         LampBlock lamp = (LampBlock)GTBlocks.LAMPS.get(color).get();

         for (int lampMeta = 0; lampMeta < 8; lampMeta++) {
            GTRecipeTypes.ASSEMBLER_RECIPES
               .recipeBuilder("lamp_" + color + "_" + lampMeta)
               .inputItems(TagPrefix.plate, GTMaterials.Glass, 6)
               .inputItems(TagPrefix.dust, GTMaterials.Glowstone, 1)
               .inputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(144))
               .outputItems(lamp.getStackFromIndex(lampMeta))
               .circuitMeta(lampMeta + 1)
               .EUt((long)GTValues.VA[0])
               .duration(40)
               .addMaterialInfo(true)
               .save(provider);
         }

         lamp = (LampBlock)GTBlocks.BORDERLESS_LAMPS.get(color).get();

         for (int lampMeta = 0; lampMeta < 8; lampMeta++) {
            GTRecipeTypes.ASSEMBLER_RECIPES
               .recipeBuilder("borderless_lamp_" + color + "_" + lampMeta)
               .inputItems(TagPrefix.plate, GTMaterials.Glass, 6)
               .inputItems(TagPrefix.dust, GTMaterials.Glowstone, 1)
               .inputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(144))
               .outputItems(lamp.getStackFromIndex(lampMeta))
               .circuitMeta(lampMeta + 9)
               .EUt((long)GTValues.VA[0])
               .duration(40)
               .addMaterialInfo(true)
               .save(provider);
         }
      }

      GTRecipeTypes.CANNER_RECIPES
         .recipeBuilder("spray_can_solvent")
         .inputItems((Supplier<? extends Item>)GTItems.SPRAY_EMPTY)
         .inputFluids(GTMaterials.Acetone.getFluid(1000))
         .outputItems((Supplier<? extends ItemLike>)GTItems.SPRAY_SOLVENT)
         .EUt((long)GTValues.VA[0])
         .duration(200)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.CANNER_RECIPES
         .recipeBuilder("mask_filter")
         .inputItems(TagPrefix.plate, GTMaterials.Polyethylene, 4)
         .inputItems(TagPrefix.dust, GTMaterials.ActivatedCarbon, 2)
         .outputItems((Supplier<? extends ItemLike>)GTItems.MASK_FILTER)
         .addMaterialInfo(true)
         .duration(100)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.CANNER_RECIPES
         .recipeBuilder("pack_paracetamol")
         .inputItems(TagPrefix.dust, GTMaterials.Paracetamol, 16)
         .notConsumable(GTItems.SHAPE_MOLD_PILL)
         .outputItems(GTItems.PARACETAMOL_PILL.asStack(16))
         .duration(60)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.CANNER_RECIPES
         .recipeBuilder("pack_rad_away")
         .inputItems(TagPrefix.dust, GTMaterials.RadAway, 16)
         .notConsumable(GTItems.SHAPE_MOLD_PILL)
         .outputItems(GTItems.RAD_AWAY_PILL.asStack(16))
         .duration(60)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_shutter")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.IRON_DOOR))
         .inputItems(TagPrefix.plate, GTMaterials.Iron, 2)
         .outputItems(GTItems.COVER_SHUTTER, 2)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_machine_controller")
         .inputItems(new ItemStack(Blocks.LEVER))
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_MACHINE_CONTROLLER)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_energy_detector")
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Copper, 4)
         .inputItems(CustomTags.LV_CIRCUITS)
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ENERGY_DETECTOR)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_advanced_energy_detector")
         .inputItems((Supplier<? extends Item>)GTItems.COVER_ENERGY_DETECTOR)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_HV)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ENERGY_DETECTOR_ADVANCED)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_activity_detector")
         .inputItems(new ItemStack(Blocks.REDSTONE_TORCH))
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ACTIVITY_DETECTOR)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_advanced_activity_cover")
         .inputItems(TagPrefix.wireFine, GTMaterials.Gold, 4)
         .inputItems(CustomTags.HV_CIRCUITS)
         .inputItems(TagPrefix.plate, GTMaterials.Aluminium)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_fluid_detector")
         .inputItems(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE))
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_FLUID_DETECTOR)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_item_detector")
         .inputItems(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ITEM_DETECTOR)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_advanced_fluid_detector")
         .inputItems((Supplier<? extends Item>)GTItems.COVER_FLUID_DETECTOR)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_HV)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_FLUID_DETECTOR_ADVANCED)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_advanced_item_detector")
         .inputItems((Supplier<? extends Item>)GTItems.COVER_ITEM_DETECTOR)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_HV)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ITEM_DETECTOR_ADVANCED)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_maintenance_detector")
         .inputItems((Supplier<? extends Item>)GTItems.EMITTER_LV)
         .inputItems(TagPrefix.plate, GTMaterials.Steel)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_MAINTENANCE_DETECTOR)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_screen")
         .inputItems(TagPrefix.plate, GTMaterials.Glass)
         .inputItems(TagPrefix.foil, GTMaterials.Aluminium, 4)
         .inputItems(CustomTags.LV_CIRCUITS)
         .inputItems(TagPrefix.wireFine, GTMaterials.Copper, 4)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_SCREEN)
         .EUt(16L)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_infinite_water")
         .inputItems(GTItems.ELECTRIC_PUMP_HV, 2)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.CAULDRON))
         .inputItems(CustomTags.HV_CIRCUITS)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_INFINITE_WATER)
         .EUt((long)GTValues.VA[3])
         .duration(100)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_ender_fluid_link")
         .inputItems(TagPrefix.plate, GTMaterials.EnderPearl, 9)
         .inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_HV)
         .inputItems((Supplier<? extends Item>)GTItems.EMITTER_HV)
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_PUMP_HV)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ENDER_FLUID_LINK)
         .EUt((long)GTValues.VA[3])
         .duration(320)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_ender_item_link")
         .inputItems(TagPrefix.plate, GTMaterials.EnderPearl, 9)
         .inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_HV)
         .inputItems((Supplier<? extends Item>)GTItems.EMITTER_HV)
         .inputItems((Supplier<? extends Item>)GTItems.CONVEYOR_MODULE_HV)
         .inputFluids(GTMaterials.Polytetrafluoroethylene, 288)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ENDER_ITEM_LINK)
         .EUt((long)GTValues.VA[3])
         .duration(320)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_ender_redstone_link")
         .inputItems(TagPrefix.plate, GTMaterials.EnderPearl, 4)
         .inputItems(TagPrefix.plateDouble, GTMaterials.Aluminium)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_MV)
         .inputItems((Supplier<? extends Item>)GTItems.EMITTER_MV)
         .inputItems(CustomTags.MV_CIRCUITS)
         .inputFluids(GTMaterials.SolderingAlloy, 288)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_ENDER_REDSTONE_LINK)
         .EUt((long)GTValues.VA[2])
         .duration(320)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cover_storage")
         .inputItems(Items.CHESTS_WOODEN)
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_PISTON_LV)
         .inputItems(TagPrefix.plate, GTMaterials.Iron)
         .inputFluids(GTMaterials.SolderingAlloy, 72)
         .outputItems((Supplier<? extends ItemLike>)GTItems.COVER_STORAGE)
         .EUt(16L)
         .duration(100)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_ulv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.WroughtIron, 8)
         .outputItems(GTBlocks.MACHINE_CASING_ULV.asStack())
         .circuitMeta(8)
         .duration(25)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_lv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 8)
         .outputItems(GTBlocks.MACHINE_CASING_LV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_mv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 8)
         .outputItems(GTBlocks.MACHINE_CASING_MV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_hv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 8)
         .outputItems(GTBlocks.MACHINE_CASING_HV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_ev")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 8)
         .outputItems(GTBlocks.MACHINE_CASING_EV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_iv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 8)
         .outputItems(GTBlocks.MACHINE_CASING_IV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_luv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium, 8)
         .outputItems(GTBlocks.MACHINE_CASING_LuV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_zpm")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.NaquadahAlloy, 8)
         .outputItems(GTBlocks.MACHINE_CASING_ZPM.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_uv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Darmstadtium, 8)
         .outputItems(GTBlocks.MACHINE_CASING_UV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_uhv")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Neutronium, 8)
         .outputItems(GTBlocks.MACHINE_CASING_UHV.asStack())
         .circuitMeta(8)
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_cupronickel")
         .EUt((long)GTValues.VA[1])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Cupronickel, 8)
         .inputItems(TagPrefix.foil, GTMaterials.Bronze, 8)
         .inputFluids(GTMaterials.TinAlloy, 144)
         .outputItems(GTBlocks.COIL_CUPRONICKEL.asStack())
         .duration(200)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_kanthal")
         .EUt((long)GTValues.VA[2])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Kanthal, 8)
         .inputItems(TagPrefix.foil, GTMaterials.Aluminium, 8)
         .inputFluids(GTMaterials.Copper, 144)
         .outputItems(GTBlocks.COIL_KANTHAL.asStack())
         .duration(300)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_nichrome")
         .EUt((long)GTValues.VA[3])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Nichrome, 8)
         .inputItems(TagPrefix.foil, GTMaterials.StainlessSteel, 8)
         .inputFluids(GTMaterials.Aluminium, 144)
         .outputItems(GTBlocks.COIL_NICHROME.asStack())
         .duration(400)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_rtm_alloy")
         .EUt((long)GTValues.VA[4])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.RTMAlloy, 8)
         .inputItems(TagPrefix.foil, GTMaterials.VanadiumSteel, 8)
         .inputFluids(GTMaterials.Nichrome, 144)
         .outputItems(GTBlocks.COIL_RTMALLOY.asStack())
         .duration(500)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_hssg")
         .EUt((long)GTValues.VA[5])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.HSSG, 8)
         .inputItems(TagPrefix.foil, GTMaterials.TungstenCarbide, 8)
         .inputFluids(GTMaterials.Tungsten, 144)
         .outputItems(GTBlocks.COIL_HSSG.asStack())
         .duration(600)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_naquadah")
         .EUt((long)GTValues.VA[6])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Naquadah, 8)
         .inputItems(TagPrefix.foil, GTMaterials.Osmium, 8)
         .inputFluids(GTMaterials.TungstenSteel, 144)
         .outputItems(GTBlocks.COIL_NAQUADAH.asStack())
         .duration(700)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_trinium")
         .EUt((long)GTValues.VA[7])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Trinium, 8)
         .inputItems(TagPrefix.foil, GTMaterials.NaquadahEnriched, 8)
         .inputFluids(GTMaterials.Naquadah, 144)
         .outputItems(GTBlocks.COIL_TRINIUM.asStack())
         .duration(800)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("coil_tritanium")
         .EUt((long)GTValues.VA[8])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.Tritanium, 8)
         .inputItems(TagPrefix.foil, GTMaterials.Naquadria, 8)
         .inputFluids(GTMaterials.Trinium, 144)
         .outputItems(GTBlocks.COIL_TRITANIUM.asStack())
         .duration(900)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_bronze_bricks")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Bronze, 6)
         .inputItems(new ItemStack(Blocks.BRICKS))
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_BRONZE_BRICKS.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_invar_heatproof")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Invar, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Invar)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_INVAR_HEATPROOF.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_steel_solid")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_STEEL_SOLID.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_aluminium_frostproof")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Aluminium)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_tungsteensteel_robust")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_stainless_clean")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.StainlessSteel)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_STAINLESS_CLEAN.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_titanium_stable")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Titanium)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_TITANIUM_STABLE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_hsse_sturdy")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.HSSE, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Europium)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_HSSE_STURDY.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_palladium_substation")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Palladium, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Iridium)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_PALLADIUM_SUBSTATION.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_ptfe_inert")
         .EUt(16L)
         .inputItems(GTBlocks.CASING_STEEL_SOLID.asStack())
         .inputFluids(GTMaterials.Polytetrafluoroethylene.getFluid(216))
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_PTFE_INERT.asStack())
         .duration(50)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_bronze_firebox")
         .inputItems(TagPrefix.rod, GTMaterials.Bronze, 3)
         .inputItems(TagPrefix.frameGt, GTMaterials.Bronze)
         .inputItems(TagPrefix.plate, GTMaterials.Bronze, 3)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.FIREBOX_BRONZE, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .addMaterialInfo(true, true)
         .duration(100)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_steel_firebox")
         .inputItems(TagPrefix.rod, GTMaterials.Steel, 3)
         .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 3)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.FIREBOX_STEEL, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .addMaterialInfo(true, true)
         .duration(200)
         .EUt((long)GTValues.VA[1])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_titanium_firebox")
         .inputItems(TagPrefix.rod, GTMaterials.Titanium, 3)
         .inputItems(TagPrefix.frameGt, GTMaterials.Titanium)
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 3)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.FIREBOX_TITANIUM, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .addMaterialInfo(true, true)
         .duration(300)
         .EUt((long)GTValues.VA[3])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_tungstensteel_firebox")
         .inputItems(TagPrefix.rod, GTMaterials.TungstenSteel, 3)
         .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 3)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.FIREBOX_TUNGSTENSTEEL, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .addMaterialInfo(true, true)
         .duration(400)
         .EUt((long)GTValues.VA[4])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("superconducting_coil_luv")
         .EUt((long)GTValues.VA[6])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.IndiumTinBariumTitaniumCuprate, 32)
         .inputItems(TagPrefix.foil, GTMaterials.NiobiumTitanium, 32)
         .inputFluids(GTMaterials.Trinium, 3456)
         .outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("superconducting_coil_zpm")
         .EUt((long)GTValues.VA[7])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.UraniumRhodiumDinaquadide, 16)
         .inputItems(TagPrefix.foil, GTMaterials.NiobiumTitanium, 16)
         .inputFluids(GTMaterials.Trinium, 2304)
         .outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("superconducting_coil_uv")
         .EUt((long)GTValues.VA[8])
         .inputItems(TagPrefix.wireGtDouble, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 8)
         .inputItems(TagPrefix.foil, GTMaterials.NiobiumTitanium, 8)
         .inputFluids(GTMaterials.Trinium, 1152)
         .outputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fusion_coil")
         .EUt((long)GTValues.VA[7])
         .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
         .inputItems(GTItems.FIELD_GENERATOR_IV.asStack(2))
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_PUMP_IV)
         .inputItems(GTItems.NEUTRON_REFLECTOR.asStack(2))
         .inputItems(CustomTags.LuV_CIRCUITS, 4)
         .inputItems(TagPrefix.pipeSmallFluid, GTMaterials.Naquadah, 4)
         .inputItems(TagPrefix.plate, GTMaterials.Europium, 4)
         .inputFluids(GTMaterials.VanadiumGallium, 576)
         .outputItems(GTBlocks.FUSION_COIL.asStack())
         .duration(100)
         .cleanroom(CleanroomType.CLEANROOM)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fusion_glass")
         .EUt((long)GTValues.VA[6])
         .inputItems(GTBlocks.CASING_LAMINATED_GLASS.asStack())
         .inputItems(TagPrefix.plate, GTMaterials.Naquadah, 4)
         .inputItems(GTItems.NEUTRON_REFLECTOR.asStack(4))
         .outputItems(GTBlocks.FUSION_GLASS.asStack(2))
         .inputFluids(GTMaterials.Polybenzimidazole, 144)
         .duration(50)
         .cleanroom(CleanroomType.CLEANROOM)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fusion_casing")
         .EUt((long)GTValues.VA[6])
         .inputItems(GTBlocks.MACHINE_CASING_LuV.asStack())
         .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
         .inputItems((Supplier<? extends Item>)GTItems.NEUTRON_REFLECTOR)
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_PUMP_LuV)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 6)
         .inputFluids(GTMaterials.Polybenzimidazole, 144)
         .outputItems(GTBlocks.FUSION_CASING.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .addMaterialInfo(true, true)
         .duration(100)
         .cleanroom(CleanroomType.CLEANROOM)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fusion_casing_mk2")
         .EUt((long)GTValues.VA[7])
         .inputItems(GTBlocks.MACHINE_CASING_ZPM.asStack())
         .inputItems(GTBlocks.FUSION_COIL.asStack())
         .inputItems(GTItems.VOLTAGE_COIL_ZPM.asStack(2))
         .inputItems((Supplier<? extends Item>)GTItems.FIELD_GENERATOR_LuV)
         .inputItems(TagPrefix.plate, GTMaterials.Europium, 6)
         .inputFluids(GTMaterials.Polybenzimidazole, 288)
         .outputItems(GTBlocks.FUSION_CASING_MK2.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(100)
         .cleanroom(CleanroomType.CLEANROOM)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fusion_casing_mk3")
         .EUt((long)GTValues.VA[8])
         .inputItems(GTBlocks.MACHINE_CASING_UV.asStack())
         .inputItems(GTBlocks.FUSION_COIL.asStack())
         .inputItems(GTItems.VOLTAGE_COIL_UV.asStack(2))
         .inputItems((Supplier<? extends Item>)GTItems.FIELD_GENERATOR_ZPM)
         .inputItems(TagPrefix.plate, GTMaterials.Americium, 6)
         .inputFluids(GTMaterials.Polybenzimidazole, 576)
         .outputItems(GTBlocks.FUSION_CASING_MK3.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(100)
         .cleanroom(CleanroomType.CLEANROOM)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_steel_turbine")
         .EUt(16L)
         .inputItems(TagPrefix.plate, GTMaterials.Magnalium, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.BlueSteel, 1)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_STEEL_TURBINE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_stainless_steel_turbine")
         .EUt(16L)
         .inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack())
         .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_STAINLESS_TURBINE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_titanium_turbine")
         .EUt(16L)
         .inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack())
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 6)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_TITANIUM_TURBINE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_tungstensteel_turbine")
         .EUt(16L)
         .inputItems(GTBlocks.CASING_STEEL_TURBINE.asStack())
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 6)
         .circuitMeta(6)
         .outputItems(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(50)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("casing_grate_casing")
         .inputItems(net.minecraft.world.item.Items.IRON_BARS, 6)
         .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_MOTOR_MV)
         .inputItems(TagPrefix.rotor, GTMaterials.Steel)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.CASING_GRATE, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .duration(800)
         .EUt((long)GTValues.VA[5])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("assembly_line_casing")
         .inputItems(TagPrefix.gear, GTMaterials.Ruridit, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
         .inputItems(GTItems.ROBOT_ARM_IV, 2)
         .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.CASING_ASSEMBLY_LINE, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .duration(650)
         .EUt((long)GTValues.VA[5])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("assembly_control_casing")
         .inputItems((Supplier<? extends Item>)GTItems.HIGH_POWER_INTEGRATED_CIRCUIT)
         .inputItems(CustomTags.EV_CIRCUITS, 4)
         .inputItems((Supplier<? extends Item>)GTItems.SENSOR_IV)
         .inputItems((Supplier<? extends Item>)GTItems.EMITTER_IV)
         .inputItems((Supplier<? extends Item>)GTItems.ELECTRIC_MOTOR_IV)
         .inputItems(TagPrefix.frameGt, GTMaterials.TungstenSteel)
         .outputItems((Supplier<? extends ItemLike>)GTBlocks.CASING_ASSEMBLY_CONTROL, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
         .duration(650)
         .EUt((long)GTValues.VA[5])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("plascrete")
         .EUt(48L)
         .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
         .inputItems(TagPrefix.plate, GTMaterials.Polyethylene, 6)
         .inputFluids(GTMaterials.Concrete, 144)
         .outputItems(GTBlocks.PLASTCRETE.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(200)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("cleanroom_glass")
         .EUt(48L)
         .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
         .inputItems(TagPrefix.plate, GTMaterials.Polyethylene, 6)
         .inputFluids(GTMaterials.Glass, 144)
         .outputItems(GTBlocks.CLEANROOM_GLASS.asStack(ConfigHolder.INSTANCE.recipes.casingsPerCraft))
         .duration(200)
         .addMaterialInfo(true, true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_ulv")
         .duration(25)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_ULV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[0])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_lv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_LV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Tin, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[1])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_mv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_MV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Copper, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[2])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_mv_annealed")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_MV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.AnnealedCopper, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[2])
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_hv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_HV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Gold, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[3])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_ev")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_EV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Aluminium, 2)
         .inputFluids(GTMaterials.Polyethylene, 288)
         .outputItems(GTMachines.HULL[4])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_iv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_IV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Platinum, 2)
         .inputFluids(GTMaterials.Polytetrafluoroethylene, 288)
         .outputItems(GTMachines.HULL[5])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_luv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_LuV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 2)
         .inputFluids(GTMaterials.Polytetrafluoroethylene, 288)
         .outputItems(GTMachines.HULL[6])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_zpm")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_ZPM.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 2)
         .inputFluids(GTMaterials.Polybenzimidazole, 288)
         .outputItems(GTMachines.HULL[7])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_uv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_UV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
         .inputFluids(GTMaterials.Polybenzimidazole, 288)
         .outputItems(GTMachines.HULL[8])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hull_uhv")
         .duration(50)
         .EUt(16L)
         .inputItems(GTBlocks.MACHINE_CASING_UHV.asStack())
         .inputItems(TagPrefix.cableGtSingle, GTMaterials.Europium, 2)
         .inputFluids(GTMaterials.Polybenzimidazole, 288)
         .outputItems(GTMachines.HULL[9])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hopper_iron")
         .EUt(2L)
         .inputItems(Items.CHESTS_WOODEN)
         .inputItems(TagPrefix.plate, GTMaterials.Iron, 5)
         .outputItems(new ItemStack(Blocks.HOPPER))
         .duration(800)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("hopper_wrought_iron")
         .EUt(2L)
         .inputItems(Items.CHESTS_WOODEN)
         .inputItems(TagPrefix.plate, GTMaterials.WroughtIron, 5)
         .outputItems(new ItemStack(Blocks.HOPPER))
         .duration(800)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("wooden_crate")
         .EUt(16L)
         .inputItems(ItemTags.PLANKS, 4)
         .inputItems(TagPrefix.screw, GTMaterials.Iron, 4)
         .outputItems(GTMachines.WOODEN_CRATE)
         .duration(100)
         .circuitMeta(5)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("bronze_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Bronze, 4)
         .inputItems(TagPrefix.plate, GTMaterials.Bronze, 4)
         .outputItems(GTMachines.BRONZE_CRATE)
         .duration(200)
         .circuitMeta(1)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("steel_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Steel, 4)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
         .outputItems(GTMachines.STEEL_CRATE)
         .duration(200)
         .circuitMeta(1)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("aluminium_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Aluminium, 4)
         .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4)
         .outputItems(GTMachines.ALUMINIUM_CRATE)
         .duration(200)
         .circuitMeta(1)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("stainless_steel_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.StainlessSteel, 4)
         .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
         .outputItems(GTMachines.STAINLESS_STEEL_CRATE)
         .circuitMeta(1)
         .duration(200)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("titanium_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Titanium, 4)
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 4)
         .outputItems(GTMachines.TITANIUM_CRATE)
         .duration(200)
         .circuitMeta(1)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("tungstensteel_crate")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.TungstenSteel, 4)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 4)
         .outputItems(GTMachines.TUNGSTENSTEEL_CRATE)
         .duration(200)
         .circuitMeta(1)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("wood_barrel")
         .EUt(16L)
         .inputItems(ItemTags.PLANKS, 4)
         .inputItems(TagPrefix.rodLong, GTMaterials.Iron, 2)
         .outputItems(GTMachines.WOODEN_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("bronze_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Bronze, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Bronze, 4)
         .outputItems(GTMachines.BRONZE_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("steel_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Steel, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
         .outputItems(GTMachines.STEEL_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("aluminium_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Aluminium, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Aluminium, 4)
         .outputItems(GTMachines.ALUMINIUM_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("stainless_steel_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.StainlessSteel, 2)
         .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
         .outputItems(GTMachines.STAINLESS_STEEL_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("gold_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Gold, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Gold, 4)
         .outputItems(GTMachines.GOLD_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("titanium_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.Titanium, 2)
         .inputItems(TagPrefix.plate, GTMaterials.Titanium, 4)
         .outputItems(GTMachines.TITANIUM_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("tungstensteel_drum")
         .EUt(16L)
         .inputItems(TagPrefix.rodLong, GTMaterials.TungstenSteel, 2)
         .inputItems(TagPrefix.plate, GTMaterials.TungstenSteel, 4)
         .outputItems(GTMachines.TUNGSTENSTEEL_DRUM)
         .duration(200)
         .circuitMeta(2)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("duct_tape_polyethylene")
         .EUt((long)GTValues.VA[1])
         .inputItems(TagPrefix.foil, GTMaterials.Polyethylene, 4)
         .inputItems((Supplier<? extends Item>)GTItems.CARBON_MESH)
         .inputFluids(GTMaterials.Polyethylene.getFluid(288))
         .outputItems((Supplier<? extends ItemLike>)GTItems.DUCT_TAPE)
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("duct_tape_silicone_rubber")
         .EUt((long)GTValues.VA[1])
         .inputItems(TagPrefix.foil, GTMaterials.SiliconeRubber, 2)
         .inputItems((Supplier<? extends Item>)GTItems.CARBON_MESH)
         .inputFluids(GTMaterials.Polyethylene.getFluid(288))
         .outputItems(GTItems.DUCT_TAPE, 2)
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("duct_tape_polycaprolactam")
         .EUt((long)GTValues.VA[1])
         .inputItems(TagPrefix.foil, GTMaterials.Polycaprolactam, 2)
         .inputItems((Supplier<? extends Item>)GTItems.CARBON_MESH)
         .inputFluids(GTMaterials.Polyethylene.getFluid(144))
         .outputItems(GTItems.DUCT_TAPE, 4)
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("duct_tape_polybenzimidazole")
         .EUt((long)GTValues.VA[1])
         .inputItems(TagPrefix.foil, GTMaterials.Polybenzimidazole)
         .inputItems((Supplier<? extends Item>)GTItems.CARBON_MESH)
         .inputFluids(GTMaterials.Polyethylene.getFluid(72))
         .outputItems(GTItems.DUCT_TAPE, 8)
         .duration(100)
         .save(provider);
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "basic_tape",
         GTItems.BASIC_TAPE.asStack(),
         " P ",
         "PSP",
         " P ",
         'P',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Paper),
         'S',
         GTItems.STICKY_RESIN.asItem()
      );
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("basic_tape")
         .EUt((long)GTValues.VA[0])
         .inputItems(TagPrefix.plate, GTMaterials.Paper, 2)
         .inputItems((Supplier<? extends Item>)GTItems.STICKY_RESIN)
         .outputItems(GTItems.BASIC_TAPE, 2)
         .duration(100)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fluid_cell_large_steel")
         .inputItems(TagPrefix.plateDouble, GTMaterials.Steel, 2)
         .inputItems(TagPrefix.ring, GTMaterials.Bronze, 2)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL_LARGE_STEEL)
         .duration(200)
         .EUt((long)GTValues.VA[1])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fluid_cell_large_aluminium")
         .inputItems(TagPrefix.plateDouble, GTMaterials.Aluminium, 2)
         .inputItems(TagPrefix.ring, GTMaterials.Silver, 2)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL_LARGE_ALUMINIUM)
         .duration(200)
         .EUt(64L)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fluid_cell_large_stainless_steel")
         .inputItems(TagPrefix.plateDouble, GTMaterials.StainlessSteel, 3)
         .inputItems(TagPrefix.ring, GTMaterials.Electrum, 3)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL_LARGE_STAINLESS_STEEL)
         .duration(200)
         .EUt((long)GTValues.VA[2])
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fluid_cell_large_titanium")
         .inputItems(TagPrefix.plateDouble, GTMaterials.Titanium, 3)
         .inputItems(TagPrefix.ring, GTMaterials.RoseGold, 3)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL_LARGE_TITANIUM)
         .duration(200)
         .EUt(256L)
         .addMaterialInfo(true)
         .save(provider);
      GTRecipeTypes.ASSEMBLER_RECIPES
         .recipeBuilder("fluid_cell_large_tungstensteel")
         .inputItems(TagPrefix.plateDouble, GTMaterials.TungstenSteel, 4)
         .inputItems(TagPrefix.ring, GTMaterials.Platinum, 4)
         .outputItems((Supplier<? extends ItemLike>)GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL)
         .duration(200)
         .EUt((long)GTValues.VA[3])
         .addMaterialInfo(true)
         .save(provider);
   }

   private static void registerBlastFurnaceRecipes(RecipeOutput provider) {
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_ruby_dust")
         .duration(400)
         .EUt(100L)
         .inputItems(TagPrefix.dust, GTMaterials.Ruby)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_ruby_gem")
         .duration(320)
         .EUt(100L)
         .inputItems(TagPrefix.gem, GTMaterials.Ruby)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_green_sapphire_dust")
         .duration(400)
         .EUt(100L)
         .inputItems(TagPrefix.dust, GTMaterials.GreenSapphire)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_green_sapphire_gem")
         .duration(320)
         .EUt(100L)
         .inputItems(TagPrefix.gem, GTMaterials.GreenSapphire)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_sapphire_dust")
         .duration(400)
         .EUt(100L)
         .inputItems(TagPrefix.dust, GTMaterials.Sapphire)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("aluminium_from_sapphire_gem")
         .duration(320)
         .EUt(100L)
         .inputItems(TagPrefix.gem, GTMaterials.Sapphire)
         .outputItems(TagPrefix.nugget, GTMaterials.Aluminium, 3)
         .blastFurnaceTemp(1200)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("steel_from_iron")
         .duration(500)
         .EUt((long)GTValues.VA[2])
         .inputItems(TagPrefix.ingot, GTMaterials.Iron)
         .inputFluids(GTMaterials.Oxygen.getFluid(200))
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1000)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("steel_from_wrought_iron")
         .duration(300)
         .EUt((long)GTValues.VA[2])
         .inputItems(TagPrefix.ingot, GTMaterials.WroughtIron)
         .inputFluids(GTMaterials.Oxygen.getFluid(200))
         .outputItems(TagPrefix.ingot, GTMaterials.Steel)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .blastFurnaceTemp(1000)
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("tempered_glass_blasting")
         .inputItems(TagPrefix.block, GTMaterials.Glass)
         .inputFluids(GTMaterials.Oxygen.getFluid(100))
         .outputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack())
         .blastFurnaceTemp(1000)
         .duration(200)
         .EUt((long)GTValues.VA[2])
         .save(provider);
      registerBlastFurnaceMetallurgyRecipes(provider);
   }

   private static void registerBlastFurnaceMetallurgyRecipes(RecipeOutput provider) {
      createSulfurDioxideRecipe(provider, GTMaterials.Stibnite, GTMaterials.AntimonyTrioxide, 1500);
      createSulfurDioxideRecipe(provider, GTMaterials.Sphalerite, GTMaterials.Zincite, 1000);
      createSulfurDioxideRecipe(provider, GTMaterials.Pyrite, GTMaterials.Hematite, 2000);
      createSulfurDioxideRecipe(provider, GTMaterials.Pentlandite, GTMaterials.Garnierite, 1000);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("tetrahedrite_metallurgy")
         .duration(120)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(1200)
         .inputItems(TagPrefix.dust, GTMaterials.Tetrahedrite)
         .inputFluids(GTMaterials.Oxygen.getFluid(3000))
         .outputItems(TagPrefix.dust, GTMaterials.CupricOxide)
         .outputItems(TagPrefix.dustTiny, GTMaterials.AntimonyTrioxide, 3)
         .outputFluids(GTMaterials.SulfurDioxide.getFluid(2000))
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("cobaltite_metallurgy")
         .duration(120)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(1200)
         .inputItems(TagPrefix.dust, GTMaterials.Cobaltite)
         .inputFluids(SizedFluidIngredient.of(CustomTags.COBALTITE_METALLURGY_OXYGEN, 3000))
         .outputItems(TagPrefix.dust, GTMaterials.CobaltOxide)
         .outputItems(TagPrefix.dust, GTMaterials.ArsenicTrioxide)
         .outputFluids(GTMaterials.SulfurDioxide.getFluid(1000))
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("galena_metallurgy")
         .duration(120)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(1200)
         .inputItems(TagPrefix.dust, GTMaterials.Galena)
         .inputFluids(GTMaterials.Oxygen.getFluid(3000))
         .outputItems(TagPrefix.dust, GTMaterials.Massicot)
         .outputItems(TagPrefix.nugget, GTMaterials.Silver, 6)
         .outputFluids(GTMaterials.SulfurDioxide.getFluid(1000))
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("chalcopyrite_metallurgy")
         .duration(120)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(1200)
         .inputItems(TagPrefix.dust, GTMaterials.Chalcopyrite)
         .inputItems(TagPrefix.dust, GTMaterials.SiliconDioxide)
         .inputFluids(GTMaterials.Oxygen.getFluid(3000))
         .outputItems(TagPrefix.dust, GTMaterials.CupricOxide)
         .outputItems(TagPrefix.dust, GTMaterials.Ferrosilite)
         .outputFluids(GTMaterials.SulfurDioxide.getFluid(2000))
         .save(provider);
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder("blast_silicon_dioxide")
         .duration(240)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(2273)
         .inputItems(TagPrefix.dust, GTMaterials.SiliconDioxide, 3)
         .inputItems(TagPrefix.dust, GTMaterials.Carbon, 2)
         .outputItems(TagPrefix.ingotHot, GTMaterials.Silicon)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .outputFluids(GTMaterials.CarbonMonoxide.getFluid(2000))
         .save(provider);
   }

   private static void createSulfurDioxideRecipe(RecipeOutput provider, Material inputMaterial, Material outputMaterial, int sulfurDioxideAmount) {
      GTRecipeTypes.BLAST_RECIPES
         .recipeBuilder(inputMaterial.getName() + "_metallurgy")
         .duration(120)
         .EUt((long)GTValues.VA[2])
         .blastFurnaceTemp(1200)
         .inputItems(TagPrefix.dust, inputMaterial)
         .inputFluids(GTMaterials.Oxygen.getFluid(3000))
         .outputItems(TagPrefix.dust, outputMaterial)
         .chancedOutput(TagPrefix.dust, GTMaterials.Ash, "1/9")
         .outputFluids(GTMaterials.SulfurDioxide.getFluid(sulfurDioxideAmount))
         .save(provider);
   }

   private static void registerDecompositionRecipes(RecipeOutput provider) {
      GTRecipeTypes.EXTRACTOR_RECIPES
         .recipeBuilder("raw_rubber_from_resin")
         .inputItems((Supplier<? extends Item>)GTItems.STICKY_RESIN)
         .outputItems(TagPrefix.dust, GTMaterials.RawRubber, 3)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.EXTRACTOR_RECIPES
         .recipeBuilder("raw_rubber_from_leaves")
         .duration(300)
         .EUt(2L)
         .inputItems(GTBlocks.RUBBER_LEAVES.asStack(16))
         .outputItems(TagPrefix.dust, GTMaterials.RawRubber)
         .save(provider);
      GTRecipeTypes.EXTRACTOR_RECIPES
         .recipeBuilder("raw_rubber_from_log")
         .duration(300)
         .EUt(2L)
         .inputItems(GTBlocks.RUBBER_LOG.asStack())
         .outputItems(TagPrefix.dust, GTMaterials.RawRubber)
         .save(provider);
      GTRecipeTypes.EXTRACTOR_RECIPES
         .recipeBuilder("raw_rubber_from_sapling")
         .duration(300)
         .EUt(2L)
         .inputItems(GTBlocks.RUBBER_SAPLING.asStack())
         .outputItems(TagPrefix.dust, GTMaterials.RawRubber)
         .save(provider);
      GTRecipeTypes.EXTRACTOR_RECIPES
         .recipeBuilder("raw_rubber_from_slime")
         .duration(150)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.SLIME_BALL))
         .outputItems(TagPrefix.dust, GTMaterials.RawRubber, 2)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_sapling")
         .duration(300)
         .EUt(2L)
         .inputItems(ItemTags.SAPLINGS)
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_wheat")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.WHEAT, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_potato")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.POTATO, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_carrot")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.CARROT, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_cactus")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(Blocks.CACTUS, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_sugar_cane")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.SUGAR_CANE, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_brown_mushroom")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(Blocks.BROWN_MUSHROOM, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_red_mushroom")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(Blocks.RED_MUSHROOM, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_beetroot")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.BEETROOT, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_moss")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.MOSS_BLOCK, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_nether_wart")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.NETHER_WART_BLOCK, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_crimson_stem")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.CRIMSON_STEM, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_warped_stem")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.WARPED_STEM, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_brain_coral")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.BRAIN_CORAL, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_bubble_coral")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.BUBBLE_CORAL, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_fire_coral")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.FIRE_CORAL, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_tube_coral")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.TUBE_CORAL, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
      GTRecipeTypes.COMPRESSOR_RECIPES
         .recipeBuilder("plant_ball_from_horn_coral")
         .duration(300)
         .EUt(2L)
         .inputItems(new ItemStack(net.minecraft.world.item.Items.HORN_CORAL, 8))
         .outputItems((Supplier<? extends ItemLike>)GTItems.PLANT_BALL)
         .save(provider);
   }

   private static void registerRecyclingRecipes(RecipeOutput provider) {
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_end_stone")
         .inputItems(new ItemStack(Blocks.END_STONE))
         .outputItems(TagPrefix.dust, GTMaterials.Endstone)
         .chancedOutput(TagPrefix.dust, GTMaterials.Tungstate, 330)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_netherrack")
         .inputItems(new ItemStack(Blocks.NETHERRACK))
         .outputItems(TagPrefix.dust, GTMaterials.Netherrack)
         .chancedOutput(TagPrefix.nugget, GTMaterials.Gold, 750)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_obsidian")
         .inputItems(new ItemStack(Blocks.OBSIDIAN))
         .outputItems(TagPrefix.dust, GTMaterials.Obsidian)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_marble")
         .inputItems(TagPrefix.rock, GTMaterials.Marble)
         .outputItems(TagPrefix.dust, GTMaterials.Marble)
         .chancedOutput(TagPrefix.dust, GTMaterials.Marble, 1500)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_basalt")
         .inputItems(Blocks.BASALT.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Basalt)
         .chancedOutput(TagPrefix.dust, GTMaterials.Basalt, 1500)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_deepslate")
         .inputItems(Blocks.DEEPSLATE.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Deepslate)
         .chancedOutput(TagPrefix.dust, GTMaterials.Thorium, 150)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_red_granite")
         .inputItems(TagPrefix.rock, GTMaterials.RedGranite)
         .outputItems(TagPrefix.dust, GTMaterials.RedGranite)
         .chancedOutput(TagPrefix.dust, GTMaterials.Uranium238, 25)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_andesite")
         .inputItems(Blocks.ANDESITE.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Andesite)
         .chancedOutput(TagPrefix.dust, GTMaterials.Stone, 25)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_diorite")
         .inputItems(Blocks.DIORITE.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Diorite)
         .chancedOutput(TagPrefix.dust, GTMaterials.Stone, 25)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_granite")
         .inputItems(Blocks.GRANITE.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Granite)
         .chancedOutput(TagPrefix.dust, GTMaterials.Stone, 25)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_calcite")
         .inputItems(Blocks.CALCITE.asItem())
         .outputItems(TagPrefix.dust, GTMaterials.Calcite)
         .chancedOutput(TagPrefix.dust, GTMaterials.Stone, 25)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_coral_block")
         .inputItems(CustomTags.CORAL_BLOCK_ITEMS)
         .outputItems(TagPrefix.dust, GTMaterials.Calcite)
         .duration(150)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_coral")
         .inputItems(CustomTags.CORAL_ITEMS)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Calcite)
         .duration(25)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_pork_chop")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.PORKCHOP))
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .chancedOutput(TagPrefix.dust, GTMaterials.Meat, 6500)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_fish")
         .inputItems(ItemTags.FISHES)
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .chancedOutput(TagPrefix.dust, GTMaterials.Meat, 5000)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_chicken")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.CHICKEN))
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_steak")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.BEEF))
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .chancedOutput(TagPrefix.dust, GTMaterials.Meat, 5000)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_rabbit")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.RABBIT))
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .chancedOutput(TagPrefix.dust, GTMaterials.Meat, 5000)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
      GTRecipeTypes.MACERATOR_RECIPES
         .recipeBuilder("macerate_mutton")
         .inputItems(new ItemStack(net.minecraft.world.item.Items.MUTTON))
         .outputItems(TagPrefix.dust, GTMaterials.Meat)
         .outputItems(TagPrefix.dustTiny, GTMaterials.Bone)
         .duration(102)
         .EUt(2L)
         .save(provider);
   }

   private static void registerFluidRecipes(RecipeOutput provider) {
      GTRecipeTypes.FLUID_HEATER_RECIPES
         .recipeBuilder("heat_ice_to_water")
         .duration(32)
         .EUt(4L)
         .inputFluids(GTMaterials.Ice.getFluid(144))
         .circuitMeta(1)
         .outputFluids(GTMaterials.Water.getFluid(144))
         .save(provider);
      GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES
         .recipeBuilder("solidify_gelled_toluene")
         .inputFluids(GTMaterials.Toluene.getFluid(100))
         .notConsumable(GTItems.SHAPE_MOLD_BALL)
         .outputItems((Supplier<? extends ItemLike>)GTItems.GELLED_TOLUENE)
         .duration(100)
         .EUt(16L)
         .save(provider);

      for (int i = 0; i < GTMaterials.CHEMICAL_DYES.length; i++) {
         GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES
            .recipeBuilder("solidify_" + GTMaterials.CHEMICAL_DYES[i].getName() + "_to_ball")
            .inputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(72))
            .notConsumable(GTItems.SHAPE_MOLD_BALL)
            .outputItems((Supplier<? extends ItemLike>)GTItems.DYE_ONLY_ITEMS[i])
            .duration(100)
            .EUt(16L)
            .save(provider);
      }

      GTRecipeTypes.FLUID_HEATER_RECIPES
         .recipeBuilder("heat_water_to_steam")
         .duration(30)
         .EUt((long)GTValues.VA[1])
         .inputFluids(GTMaterials.Water.getFluid(6))
         .circuitMeta(1)
         .outputFluids(GTMaterials.Steam.getFluid(960))
         .save(provider);
      GTRecipeTypes.FLUID_HEATER_RECIPES
         .recipeBuilder("heat_distilled_to_steam")
         .duration(30)
         .EUt((long)GTValues.VA[1])
         .inputFluids(GTMaterials.DistilledWater.getFluid(6))
         .circuitMeta(1)
         .outputFluids(GTMaterials.Steam.getFluid(960))
         .save(provider);
   }

   private static void registerSmoothRecipe(RecipeOutput provider, List<ItemStack> roughStack, List<ItemStack> stoneStack) {
      for (int i = 0; i < roughStack.size(); i++) {
         ResourceLocation stoneId = BuiltInRegistries.ITEM.getKey(stoneStack.get(i).getItem());
         VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + stoneId.getPath(), roughStack.get(i), stoneStack.get(i), 0.1F);
         GTRecipeTypes.EXTRUDER_RECIPES
            .recipeBuilder("extrude_" + stoneId.getPath())
            .inputItems(roughStack.get(i))
            .notConsumable(GTItems.SHAPE_EXTRUDER_BLOCK.asStack())
            .outputItems(stoneStack.get(i))
            .duration(24)
            .EUt(8L)
            .save(provider);
      }
   }

   private static void registerCobbleRecipe(RecipeOutput provider, List<ItemStack> stoneStack, List<ItemStack> cobbleStack) {
      for (int i = 0; i < stoneStack.size(); i++) {
         ResourceLocation cobbleId = BuiltInRegistries.ITEM.getKey(cobbleStack.get(i).getItem());
         GTRecipeTypes.FORGE_HAMMER_RECIPES
            .recipeBuilder("hammer_" + cobbleId.getPath())
            .inputItems(stoneStack.get(i))
            .outputItems(cobbleStack.get(i))
            .duration(12)
            .EUt(4L)
            .save(provider);
      }
   }

   private static void registerBricksRecipe(RecipeOutput provider, List<ItemStack> polishedStack, List<ItemStack> brickStack, MarkerMaterial color) {
      for (int i = 0; i < polishedStack.size(); i++) {
         ResourceLocation brickId = BuiltInRegistries.ITEM.getKey(brickStack.get(i).getItem());
         GTRecipeTypes.LASER_ENGRAVER_RECIPES
            .recipeBuilder("engrave_" + brickId.getPath())
            .inputItems(polishedStack.get(i))
            .notConsumable(TagPrefix.lens, color)
            .outputItems(brickStack.get(i))
            .duration(50)
            .EUt(16L)
            .save(provider);
      }
   }

   private static void registerMossRecipe(RecipeOutput provider, List<ItemStack> regularStack, List<ItemStack> mossStack) {
      for (int i = 0; i < regularStack.size(); i++) {
         ResourceLocation mossId = BuiltInRegistries.ITEM.getKey(mossStack.get(i).getItem());
         GTRecipeTypes.MIXER_RECIPES
            .recipeBuilder(mossId.getPath() + "_from_moss_block")
            .inputItems(regularStack.get(i))
            .inputItems(new ItemStack(Blocks.MOSS_BLOCK))
            .inputFluids(GTMaterials.Water.getFluid(250))
            .outputItems(mossStack.get(i))
            .duration(40)
            .EUt(1L)
            .save(provider);
         GTRecipeTypes.MIXER_RECIPES
            .recipeBuilder(mossId.getPath() + "_from_vine")
            .inputItems(regularStack.get(i))
            .inputItems(new ItemStack(Blocks.VINE))
            .inputFluids(GTMaterials.Water.getFluid(250))
            .outputItems(mossStack.get(i))
            .duration(40)
            .EUt(1L)
            .save(provider);
      }
   }

   private static void registerNBTRemoval(RecipeOutput provider) {
      for (MachineDefinition chest : GTMachines.SUPER_CHEST) {
         if (chest != null) {
            VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "super_chest_nbt_" + chest.getTier(), chest.asStack(), chest.asStack());
         }
      }

      for (MachineDefinition tank : GTMachines.SUPER_TANK) {
         if (tank != null) {
            VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "super_tank_nbt_" + tank.getTier(), tank.asStack(), tank.asStack());
         }
      }

      for (MachineDefinition chestx : GTMachines.QUANTUM_CHEST) {
         if (chestx != null) {
            VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "quantum_chest_nbt_" + chestx.getTier(), chestx.asStack(), chestx.asStack());
         }
      }

      for (MachineDefinition tankx : GTMachines.QUANTUM_TANK) {
         if (tankx != null) {
            VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "quantum_tank_nbt_" + tankx.getTier(), tankx.asStack(), tankx.asStack());
         }
      }

      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "drum_nbt_wood", GTMachines.WOODEN_DRUM.asStack(), GTMachines.WOODEN_DRUM.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "drum_nbt_bronze", GTMachines.BRONZE_DRUM.asStack(), GTMachines.BRONZE_DRUM.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "drum_nbt_steel", GTMachines.STEEL_DRUM.asStack(), GTMachines.STEEL_DRUM.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "drum_nbt_aluminium", GTMachines.ALUMINIUM_DRUM.asStack(), GTMachines.ALUMINIUM_DRUM.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "drum_nbt_stainless_steel", GTMachines.STAINLESS_STEEL_DRUM.asStack(), GTMachines.STAINLESS_STEEL_DRUM.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "drum_nbt_gold", GTMachines.GOLD_DRUM.asStack(), GTMachines.GOLD_DRUM.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "drum_nbt_titanium", GTMachines.TITANIUM_DRUM.asStack(), GTMachines.TITANIUM_DRUM.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "drum_nbt_tungstensteel", GTMachines.TUNGSTENSTEEL_DRUM.asStack(), GTMachines.TUNGSTENSTEEL_DRUM.asStack()
      );
      VanillaRecipeHelper.addShapedNBTClearingRecipe(provider, "cell_nbt_regular", GTItems.FLUID_CELL.asStack(), " C", "  ", 'C', GTItems.FLUID_CELL.asStack());
      VanillaRecipeHelper.addShapedNBTClearingRecipe(
         provider, "cell_nbt_universal", GTItems.FLUID_CELL_UNIVERSAL.asStack(), " C", "  ", 'C', GTItems.FLUID_CELL_UNIVERSAL.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_nbt_steel", GTItems.FLUID_CELL_LARGE_STEEL.asStack(), GTItems.FLUID_CELL_LARGE_STEEL.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_nbt_aluminium", GTItems.FLUID_CELL_LARGE_ALUMINIUM.asStack(), GTItems.FLUID_CELL_LARGE_ALUMINIUM.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_nbt_stainless_steel", GTItems.FLUID_CELL_LARGE_STAINLESS_STEEL.asStack(), GTItems.FLUID_CELL_LARGE_STAINLESS_STEEL.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_nbt_titanium", GTItems.FLUID_CELL_LARGE_TITANIUM.asStack(), GTItems.FLUID_CELL_LARGE_TITANIUM.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_nbt_tungstensteel", GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.asStack(), GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(
         provider, "cell_vial_nbt", GTItems.FLUID_CELL_GLASS_VIAL.asStack(), GTItems.FLUID_CELL_GLASS_VIAL.asStack()
      );
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "data_stick_nbt", GTItems.TOOL_DATA_STICK.asStack(), GTItems.TOOL_DATA_STICK.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "data_orb_nbt", GTItems.TOOL_DATA_ORB.asStack(), GTItems.TOOL_DATA_ORB.asStack());
      VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "data_module_nbt", GTItems.TOOL_DATA_MODULE.asStack(), GTItems.TOOL_DATA_MODULE.asStack());
      VanillaRecipeHelper.addShapelessRecipe(provider, "fluid_jetpack_clear", GTItems.LIQUID_FUEL_JETPACK.asStack(), GTItems.LIQUID_FUEL_JETPACK.asStack());
      VanillaRecipeHelper.addShapelessRecipe(provider, "item_filter_nbt", GTItems.ITEM_FILTER.asStack(), GTItems.ITEM_FILTER.asStack());
      VanillaRecipeHelper.addShapelessRecipe(provider, "fluid_filter_nbt", GTItems.FLUID_FILTER.asStack(), GTItems.FLUID_FILTER.asStack());
      VanillaRecipeHelper.addShapelessRecipe(provider, "item_tag_filter_nbt", GTItems.TAG_FILTER.asStack(), GTItems.TAG_FILTER.asStack());
      VanillaRecipeHelper.addShapelessRecipe(provider, "fluid_tag_filter_nbt", GTItems.TAG_FLUID_FILTER.asStack(), GTItems.TAG_FLUID_FILTER.asStack());
   }

   private static void registerHatchConversion(RecipeOutput provider) {
      for (int i = 0; i < GTMachines.FLUID_IMPORT_HATCH.length; i++) {
         if (GTMachines.FLUID_IMPORT_HATCH[i] != null && GTMachines.FLUID_EXPORT_HATCH[i] != null) {
            VanillaRecipeHelper.addShapedRecipe(
               provider,
               "fluid_hatch_output_to_input_" + GTMachines.FLUID_IMPORT_HATCH[i].getTier(),
               GTMachines.FLUID_IMPORT_HATCH[i].asStack(),
               "d",
               "B",
               'B',
               GTMachines.FLUID_EXPORT_HATCH[i].asStack()
            );
            VanillaRecipeHelper.addShapedRecipe(
               provider,
               "fluid_hatch_input_to_output_" + GTMachines.FLUID_EXPORT_HATCH[i].getTier(),
               GTMachines.FLUID_EXPORT_HATCH[i].asStack(),
               "d",
               "B",
               'B',
               GTMachines.FLUID_IMPORT_HATCH[i].asStack()
            );
         }
      }

      for (int ix = 0; ix < GTMachines.ITEM_IMPORT_BUS.length; ix++) {
         if (GTMachines.ITEM_IMPORT_BUS[ix] != null && GTMachines.ITEM_EXPORT_BUS[ix] != null) {
            VanillaRecipeHelper.addShapedRecipe(
               provider,
               "item_bus_output_to_input_" + GTMachines.ITEM_IMPORT_BUS[ix].getTier(),
               GTMachines.ITEM_IMPORT_BUS[ix].asStack(),
               "d",
               "B",
               'B',
               GTMachines.ITEM_EXPORT_BUS[ix].asStack()
            );
            VanillaRecipeHelper.addShapedRecipe(
               provider,
               "item_bus_input_to_output_" + GTMachines.ITEM_EXPORT_BUS[ix].getTier(),
               GTMachines.ITEM_EXPORT_BUS[ix].asStack(),
               "d",
               "B",
               'B',
               GTMachines.ITEM_IMPORT_BUS[ix].asStack()
            );
         }
      }

      for (int tier : GTMachineUtils.MULTI_HATCH_TIERS) {
         String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
         MachineDefinition importHatch4x = GTMachines.FLUID_IMPORT_HATCH_4X[tier];
         MachineDefinition exportHatch4x = GTMachines.FLUID_EXPORT_HATCH_4X[tier];
         MachineDefinition importHatch9x = GTMachines.FLUID_IMPORT_HATCH_9X[tier];
         MachineDefinition exportHatch9x = GTMachines.FLUID_EXPORT_HATCH_9X[tier];
         VanillaRecipeHelper.addShapedRecipe(
            provider, "fluid_hatch_4x_output_to_input_" + tierName, importHatch4x.asStack(), "d", "B", 'B', exportHatch4x.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider, "fluid_hatch_4x_input_to_output_" + tierName, exportHatch4x.asStack(), "d", "B", 'B', importHatch4x.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider, "fluid_hatch_9x_output_to_input_" + tierName, importHatch9x.asStack(), "d", "B", 'B', exportHatch9x.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider, "fluid_hatch_9x_input_to_output_" + tierName, exportHatch9x.asStack(), "d", "B", 'B', importHatch9x.asStack()
         );
      }

      for (int tier : GTMachineUtils.DUAL_HATCH_TIERS) {
         String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
         MachineDefinition inputBuffer = GTMachines.DUAL_IMPORT_HATCH[tier];
         MachineDefinition outputBuffer = GTMachines.DUAL_EXPORT_HATCH[tier];
         VanillaRecipeHelper.addShapedRecipe(provider, "dual_hatch_output_to_input_" + tierName, inputBuffer.asStack(), "d", "B", 'B', outputBuffer.asStack());
         VanillaRecipeHelper.addShapedRecipe(provider, "dual_hatch_input_to_output_" + tierName, outputBuffer.asStack(), "d", "B", 'B', inputBuffer.asStack());
      }

      VanillaRecipeHelper.addShapedRecipe(
         provider, "steam_bus_output_to_input", GTMachines.STEAM_EXPORT_BUS.asStack(), "d", "B", 'B', GTMachines.STEAM_IMPORT_BUS.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "steam_bus_input_to_output", GTMachines.STEAM_IMPORT_BUS.asStack(), "d", "B", 'B', GTMachines.STEAM_EXPORT_BUS.asStack()
      );
      if (GTCEu.Mods.isAE2Loaded()) {
         VanillaRecipeHelper.addShapedRecipe(
            provider,
            "me_fluid_hatch_output_to_input",
            GTAEMachines.FLUID_IMPORT_HATCH_ME.asStack(),
            "d",
            "B",
            'B',
            GTAEMachines.FLUID_EXPORT_HATCH_ME.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider,
            "me_fluid_hatch_input_to_output",
            GTAEMachines.FLUID_EXPORT_HATCH_ME.asStack(),
            "d",
            "B",
            'B',
            GTAEMachines.FLUID_IMPORT_HATCH_ME.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider, "me_item_bus_output_to_input", GTAEMachines.ITEM_IMPORT_BUS_ME.asStack(), "d", "B", 'B', GTAEMachines.ITEM_EXPORT_BUS_ME.asStack()
         );
         VanillaRecipeHelper.addShapedRecipe(
            provider, "me_item_bus_input_to_output", GTAEMachines.ITEM_EXPORT_BUS_ME.asStack(), "d", "B", 'B', GTAEMachines.ITEM_IMPORT_BUS_ME.asStack()
         );
      }
   }
}
