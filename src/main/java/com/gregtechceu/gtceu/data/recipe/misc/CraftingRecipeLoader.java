package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.FacadeCoverRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class CraftingRecipeLoader {
   public static void init(RecipeOutput provider) {
      VanillaRecipeHelper.addShapedRecipe(
         provider, "small_wooden_pipe", ChemicalHelper.get(TagPrefix.pipeSmallFluid, GTMaterials.Wood), "sWr", 'W', ItemTags.PLANKS
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "normal_wooden_pipe", ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.Wood), "WWW", "s r", 'W', ItemTags.PLANKS
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "large_wooden_pipe", ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.Wood), "WWW", "s r", "WWW", 'W', ItemTags.PLANKS
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "small_treated_wooden_pipe",
         ChemicalHelper.get(TagPrefix.pipeSmallFluid, GTMaterials.TreatedWood),
         "sWr",
         'W',
         GTBlocks.TREATED_WOOD_PLANK.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "normal_treated_wooden_pipe",
         ChemicalHelper.get(TagPrefix.pipeNormalFluid, GTMaterials.TreatedWood),
         "WWW",
         "s r",
         'W',
         GTBlocks.TREATED_WOOD_PLANK.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "large_treated_wooden_pipe",
         ChemicalHelper.get(TagPrefix.pipeLargeFluid, GTMaterials.TreatedWood),
         "WWW",
         "s r",
         "WWW",
         'W',
         GTBlocks.TREATED_WOOD_PLANK.asStack()
      );
      addDuctRecipes(provider, GTMaterials.Steel, 2);
      addDuctRecipes(provider, GTMaterials.StainlessSteel, 4);
      addDuctRecipes(provider, GTMaterials.TungstenSteel, 8);
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "item_filter",
         GTItems.ITEM_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Steel)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "fluid_filter_lapis",
         GTItems.FLUID_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Lapis)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "fluid_filter_lazurite",
         GTItems.FLUID_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Lazurite)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "fluid_filter_sodalite",
         GTItems.FLUID_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Sodalite)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "tag_filter_olivine",
         GTItems.TAG_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Olivine)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "tag_filter_emerald",
         GTItems.TAG_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Emerald)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "fluid_tag_filter",
         GTItems.TAG_FLUID_FILTER.asStack(),
         "XXX",
         "XYX",
         "XXX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'Y',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Amethyst)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "item_smart_filter_olivine",
         GTItems.SMART_ITEM_FILTER.asStack(),
         "XEX",
         "XCX",
         "XEX",
         'X',
         new MaterialEntry(TagPrefix.foil, GTMaterials.Zinc),
         'C',
         CustomTags.LV_CIRCUITS,
         'E',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Ruby)
      );
      VanillaRecipeHelper.addShapedRecipe(provider, "plank_to_wooden_shape", GTItems.WOODEN_FORM_EMPTY.asStack(), "   ", " X ", "s  ", 'X', ItemTags.PLANKS);
      VanillaRecipeHelper.addShapedRecipe(
         provider, "wooden_shape_brick", GTItems.WOODEN_FORM_BRICK.asStack(), "k ", " X", 'X', GTItems.WOODEN_FORM_EMPTY.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "compressed_coke_clay",
         GTItems.COMPRESSED_COKE_CLAY.asStack(3),
         "XXX",
         "SYS",
         "SSS",
         'Y',
         GTItems.WOODEN_FORM_BRICK.asStack(),
         'X',
         new ItemStack(Items.CLAY_BALL),
         'S',
         ItemTags.SAND
      );
      VanillaRecipeHelper.addShapelessRecipe(
         provider,
         "fireclay_dust",
         ChemicalHelper.get(TagPrefix.dust, GTMaterials.Fireclay, 2),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Brick),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Clay)
      );
      VanillaRecipeHelper.addSmeltingRecipe(provider, "coke_oven_brick", GTItems.COMPRESSED_COKE_CLAY.asStack(), GTItems.COKE_OVEN_BRICK.asStack(), 0.3F);
      VanillaRecipeHelper.addSmeltingRecipe(provider, "fireclay_brick", GTItems.COMPRESSED_FIRECLAY.asStack(), GTItems.FIRECLAY_BRICK.asStack(), 0.3F);
      VanillaRecipeHelper.addSmeltingRecipe(
         provider,
         "wrought_iron_nugget",
         ChemicalHelper.getTag(TagPrefix.nugget, GTMaterials.Iron),
         ChemicalHelper.get(TagPrefix.nugget, GTMaterials.WroughtIron)
      );
      VanillaRecipeHelper.addShapelessRecipe(provider, "nugget_disassembling_iron", new ItemStack(Items.IRON_NUGGET, 9), new ItemStack(Items.IRON_INGOT), 's');
      VanillaRecipeHelper.addShapedFluidContainerRecipe(
         provider,
         "treated_wood_planks",
         GTBlocks.TREATED_WOOD_PLANK.asStack(8),
         "PPP",
         "PBP",
         "PPP",
         'P',
         ItemTags.PLANKS,
         'B',
         new FluidContainerIngredient(GTMaterials.Creosote.getFluidTag(), 1000)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "rubber_ring", ChemicalHelper.get(TagPrefix.ring, GTMaterials.Rubber), "k", "X", 'X', new MaterialEntry(TagPrefix.plate, GTMaterials.Rubber)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "silicone_rubber_ring",
         ChemicalHelper.get(TagPrefix.ring, GTMaterials.SiliconeRubber),
         "k",
         "P",
         'P',
         ChemicalHelper.get(TagPrefix.plate, GTMaterials.SiliconeRubber)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "styrene_rubber_ring",
         ChemicalHelper.get(TagPrefix.ring, GTMaterials.StyreneButadieneRubber),
         "k",
         "P",
         'P',
         ChemicalHelper.get(TagPrefix.plate, GTMaterials.StyreneButadieneRubber)
      );
      VanillaRecipeHelper.addShapelessRecipe(
         provider,
         "iron_magnetic_stick",
         ChemicalHelper.get(TagPrefix.rod, GTMaterials.IronMagnetic),
         new MaterialEntry(TagPrefix.rod, GTMaterials.Iron),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone),
         new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "component_grinder_diamond",
         GTItems.COMPONENT_GRINDER_DIAMOND.asStack(),
         "XSX",
         "SDS",
         "XSX",
         'X',
         new MaterialEntry(TagPrefix.dust, GTMaterials.Diamond),
         'S',
         new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel),
         'D',
         new MaterialEntry(TagPrefix.gem, GTMaterials.Diamond)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "component_grinder_tungsten",
         GTItems.COMPONENT_GRINDER_TUNGSTEN.asStack(),
         "WSW",
         "SDS",
         "WSW",
         'W',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Tungsten),
         'S',
         new MaterialEntry(TagPrefix.plateDouble, GTMaterials.VanadiumSteel),
         'D',
         new MaterialEntry(TagPrefix.gem, GTMaterials.Diamond)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "minecart_wheels_iron",
         GTItems.IRON_MINECART_WHEELS.asStack(),
         " h ",
         "RSR",
         " w ",
         'R',
         new MaterialEntry(TagPrefix.ring, GTMaterials.Iron),
         'S',
         new MaterialEntry(TagPrefix.rod, GTMaterials.Iron)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "minecart_wheels_steel",
         GTItems.STEEL_MINECART_WHEELS.asStack(),
         " h ",
         "RSR",
         " w ",
         'R',
         new MaterialEntry(TagPrefix.ring, GTMaterials.Steel),
         'S',
         new MaterialEntry(TagPrefix.rod, GTMaterials.Steel)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nano_saber",
         GTItems.NANO_SABER.asStack(),
         "PIC",
         "PIC",
         "XEX",
         'P',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Platinum),
         'I',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Ruridit),
         'C',
         GTItems.CARBON_FIBER_PLATE.asStack(),
         'X',
         CustomTags.EV_CIRCUITS,
         'E',
         GTItems.ENERGIUM_CRYSTAL.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "solar_panel_basic",
         GTItems.COVER_SOLAR_PANEL.asStack(),
         "WGW",
         "CPC",
         'W',
         GTItems.SILICON_WAFER.asStack(),
         'G',
         new ItemStack(Blocks.GLASS_PANE),
         'C',
         CustomTags.LV_CIRCUITS,
         'P',
         GTItems.CARBON_FIBER_PLATE.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "solar_panel_ulv",
         GTItems.COVER_SOLAR_PANEL_ULV.asStack(),
         "WGW",
         "CAC",
         "P P",
         'W',
         GTItems.PHOSPHORUS_WAFER.asStack(),
         'G',
         net.neoforged.neoforge.common.Tags.Items.GLASS_PANES,
         'C',
         CustomTags.HV_CIRCUITS,
         'P',
         ChemicalHelper.get(TagPrefix.plate, GTMaterials.GalliumArsenide),
         'A',
         ChemicalHelper.get(TagPrefix.wireGtQuadruple, GTMaterials.Graphene)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "solar_panel_lv",
         GTItems.COVER_SOLAR_PANEL_LV.asStack(),
         "WGW",
         "CAC",
         "P P",
         'W',
         GTItems.NAQUADAH_WAFER.asStack(),
         'G',
         GTBlocks.CASING_TEMPERED_GLASS.asStack(),
         'C',
         CustomTags.LuV_CIRCUITS,
         'P',
         ChemicalHelper.get(TagPrefix.plate, GTMaterials.IndiumGalliumPhosphide),
         'A',
         ChemicalHelper.get(TagPrefix.wireGtHex, GTMaterials.Graphene)
      );
      VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell", GTItems.FLUID_CELL_UNIVERSAL.asStack(), "C ", "  ", 'C', GTItems.FLUID_CELL);
      VanillaRecipeHelper.addShapedRecipe(provider, "universal_fluid_cell_revert", GTItems.FLUID_CELL.asStack(), "C ", "  ", 'C', GTItems.FLUID_CELL_UNIVERSAL);
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "blacklight",
         GTItems.BLACKLIGHT.asStack(),
         "SPS",
         "GRG",
         "CPK",
         'S',
         new MaterialEntry(TagPrefix.screw, GTMaterials.TungstenCarbide),
         'P',
         new MaterialEntry(TagPrefix.plate, GTMaterials.TungstenCarbide),
         'G',
         GTBlocks.CASING_LAMINATED_GLASS.asStack(),
         'R',
         new MaterialEntry(TagPrefix.spring, GTMaterials.Europium),
         'C',
         CustomTags.IV_CIRCUITS,
         'K',
         new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.Platinum)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         true,
         "filter_casing",
         GTBlocks.FILTER_CASING.asStack(),
         "BBB",
         "III",
         "MFR",
         'B',
         new ItemStack(Blocks.IRON_BARS),
         'I',
         GTItems.ITEM_FILTER.asStack(),
         'M',
         GTItems.ELECTRIC_MOTOR_MV.asStack(),
         'F',
         new MaterialEntry(TagPrefix.frameGt, GTMaterials.Steel),
         'R',
         new MaterialEntry(TagPrefix.rotor, GTMaterials.Steel)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         true,
         "filter_casing_sterile",
         GTBlocks.FILTER_CASING_STERILE.asStack(),
         "BEB",
         "ISI",
         "MFR",
         'B',
         new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Polybenzimidazole),
         'E',
         GTItems.EMITTER_ZPM.asStack(),
         'I',
         GTItems.ITEM_FILTER.asStack(),
         'S',
         GTItems.BLACKLIGHT.asStack(),
         'M',
         GTItems.ELECTRIC_MOTOR_ZPM.asStack(),
         'F',
         new MaterialEntry(TagPrefix.frameGt, GTMaterials.Tritanium),
         'R',
         new MaterialEntry(TagPrefix.rotor, GTMaterials.NaquadahAlloy)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "shape_empty", GTItems.SHAPE_EMPTY.asStack(), "hf", "PP", "PP", 'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel)
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_bottle", GTItems.SHAPE_EXTRUDER_BOTTLE.asStack(), "  x", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_gear", GTItems.SHAPE_EXTRUDER_GEAR.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_block", GTItems.SHAPE_EXTRUDER_BLOCK.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_INGOT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_pipe_huge", GTItems.SHAPE_EXTRUDER_PIPE_HUGE.asStack(), "   ", " S ", "  x", 'S', GTItems.SHAPE_EXTRUDER_BOLT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_pipe_large", GTItems.SHAPE_EXTRUDER_PIPE_LARGE.asStack(), "   ", " Sx", "   ", 'S', GTItems.SHAPE_EXTRUDER_BOLT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_pipe_normal", GTItems.SHAPE_EXTRUDER_PIPE_NORMAL.asStack(), "  x", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_BOLT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_pipe_small", GTItems.SHAPE_EXTRUDER_PIPE_SMALL.asStack(), " x ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_BOLT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_pipe_tiny", GTItems.SHAPE_EXTRUDER_PIPE_TINY.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_BOLT.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_wire", GTItems.SHAPE_EXTRUDER_WIRE.asStack(), " x ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_ROD.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_ingot", GTItems.SHAPE_EXTRUDER_INGOT.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_cell", GTItems.SHAPE_EXTRUDER_CELL.asStack(), "   ", " Sx", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_ring", GTItems.SHAPE_EXTRUDER_RING.asStack(), "   ", " S ", " x ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_bolt", GTItems.SHAPE_EXTRUDER_BOLT.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_ROD.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_rod", GTItems.SHAPE_EXTRUDER_ROD.asStack(), "   ", " Sx", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_plate", GTItems.SHAPE_EXTRUDER_PLATE.asStack(), "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_FOIL.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_gear_small", GTItems.SHAPE_EXTRUDER_GEAR_SMALL.asStack(), " x ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_foil", GTItems.SHAPE_EXTRUDER_FOIL.asStack(), "   ", " S ", "  x", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_extruder_rotor", GTItems.SHAPE_EXTRUDER_ROTOR.asStack(), "   ", " S ", "x  ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_pill", GTItems.SHAPE_MOLD_PILL.asStack(), "  h", "  S", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_rotor", GTItems.SHAPE_MOLD_ROTOR.asStack(), "  h", " S ", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_gear_small", GTItems.SHAPE_MOLD_GEAR_SMALL.asStack(), "   ", "   ", "h S", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_name", GTItems.SHAPE_MOLD_NAME.asStack(), "  S", "   ", "h  ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_anvil", GTItems.SHAPE_MOLD_ANVIL.asStack(), "  S", "   ", " h ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_cylinder", GTItems.SHAPE_MOLD_CYLINDER.asStack(), "  S", "   ", "  h", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_nugget", GTItems.SHAPE_MOLD_NUGGET.asStack(), "S h", "   ", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_block", GTItems.SHAPE_MOLD_BLOCK.asStack(), "   ", "hS ", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_ball", GTItems.SHAPE_MOLD_BALL.asStack(), "   ", " S ", "h  ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_ingot", GTItems.SHAPE_MOLD_INGOT.asStack(), "   ", " S ", " h ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_bottle", GTItems.SHAPE_MOLD_BOTTLE.asStack(), "   ", " S ", "  h", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_gear", GTItems.SHAPE_MOLD_GEAR.asStack(), "   ", " Sh", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addStrictShapedRecipe(
         provider, "shape_mold_plate", GTItems.SHAPE_MOLD_PLATE.asStack(), " h ", " S ", "   ", 'S', GTItems.SHAPE_EMPTY.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nightvision_goggles",
         GTItems.NIGHTVISION_GOGGLES.asStack(),
         "CSC",
         "RBR",
         "LdL",
         'C',
         CustomTags.ULV_CIRCUITS,
         'S',
         new MaterialEntry(TagPrefix.screw, GTMaterials.Steel),
         'R',
         new MaterialEntry(TagPrefix.ring, GTMaterials.Rubber),
         'B',
         GTItems.BATTERY_LV_SODIUM,
         'L',
         new MaterialEntry(TagPrefix.lens, GTMaterials.Glass)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "fluid_jetpack",
         GTItems.LIQUID_FUEL_JETPACK.asStack(),
         "xCw",
         "SUS",
         "RIR",
         'C',
         CustomTags.LV_CIRCUITS,
         'S',
         GTItems.FLUID_CELL_LARGE_STEEL.asStack(),
         'U',
         GTItems.ELECTRIC_PUMP_LV.asStack(),
         'R',
         new MaterialEntry(TagPrefix.rotor, GTMaterials.Lead),
         'I',
         new MaterialEntry(TagPrefix.pipeSmallFluid, GTMaterials.Potin)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "electric_jetpack",
         GTItems.ELECTRIC_JETPACK.asStack(),
         "xCd",
         "TBT",
         "I I",
         'C',
         CustomTags.MV_CIRCUITS,
         'T',
         GTItems.POWER_THRUSTER.asStack(),
         'B',
         GTItems.BATTERY_MV_LITHIUM.asStack(),
         'I',
         new MaterialEntry(TagPrefix.wireGtDouble, GTMaterials.AnnealedCopper)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "electric_jetpack_advanced",
         GTItems.ELECTRIC_JETPACK_ADVANCED.asStack(),
         "xJd",
         "TBT",
         "WCW",
         'J',
         GTItems.ELECTRIC_JETPACK.asStack(),
         'T',
         GTItems.POWER_THRUSTER_ADVANCED.asStack(),
         'B',
         GTItems.ENERGIUM_CRYSTAL.asStack(),
         'W',
         new MaterialEntry(TagPrefix.wireGtQuadruple, GTMaterials.Gold),
         'C',
         CustomTags.HV_CIRCUITS
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nano_helmet",
         GTItems.NANO_HELMET.asStack(),
         "PPP",
         "PNP",
         "xEd",
         'P',
         GTItems.CARBON_FIBER_PLATE.asStack(),
         'N',
         GTItems.NIGHTVISION_GOGGLES.asStack(),
         'E',
         GTItems.ENERGIUM_CRYSTAL.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nano_chestplate",
         GTItems.NANO_CHESTPLATE.asStack(),
         "PEP",
         "PPP",
         "PPP",
         'P',
         GTItems.CARBON_FIBER_PLATE.asStack(),
         'E',
         GTItems.ENERGIUM_CRYSTAL.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nano_leggings",
         GTItems.NANO_LEGGINGS.asStack(),
         "PPP",
         "PEP",
         "PxP",
         'P',
         GTItems.CARBON_FIBER_PLATE.asStack(),
         'E',
         GTItems.ENERGIUM_CRYSTAL.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider, "nano_boots", GTItems.NANO_BOOTS.asStack(), "PxP", "PEP", 'P', GTItems.CARBON_FIBER_PLATE.asStack(), 'E', GTItems.ENERGIUM_CRYSTAL.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "nano_chestplate_advanced",
         GTItems.NANO_CHESTPLATE_ADVANCED.asStack(),
         "xJd",
         "PNP",
         "WCW",
         'J',
         GTItems.ELECTRIC_JETPACK_ADVANCED.asStack(),
         'P',
         GTItems.LOW_POWER_INTEGRATED_CIRCUIT.asStack(),
         'N',
         GTItems.NANO_CHESTPLATE.asStack(),
         'W',
         new MaterialEntry(TagPrefix.wireGtQuadruple, GTMaterials.Platinum),
         'C',
         CustomTags.IV_CIRCUITS
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "gravitation_engine",
         GTItems.GRAVITATION_ENGINE.asStack(),
         "ESE",
         "POP",
         "ESE",
         'E',
         GTItems.EMITTER_LuV.asStack(),
         'S',
         new MaterialEntry(TagPrefix.wireGtQuadruple, GTMaterials.Osmium),
         'P',
         new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Iridium),
         'O',
         GTItems.ENERGY_LAPOTRONIC_ORB.asStack()
      );
      VanillaRecipeHelper.addShapedRecipe(provider, "face_mask", GTItems.FACE_MASK.asStack(), "S S", "PPP", 'S', Items.STRING, 'P', Items.PAPER);
      VanillaRecipeHelper.addShapedRecipe(
         provider, "rubber_gloves", GTItems.RUBBER_GLOVES.asStack(), "P P", 'P', new MaterialEntry(TagPrefix.plate, GTMaterials.Rubber)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "powderbarrel",
         new ItemStack(GTBlocks.POWDERBARREL),
         "PSP",
         "GGG",
         "PGP",
         'P',
         new MaterialEntry(TagPrefix.plate, GTMaterials.Wood),
         'S',
         new ItemStack(Items.STRING),
         'G',
         new MaterialEntry(TagPrefix.dust, GTMaterials.Gunpowder)
      );
      SpecialRecipeBuilder.special(FacadeCoverRecipe::new).save(provider, FacadeCoverRecipe.ID);
   }

   private static void addDuctRecipes(@NotNull RecipeOutput provider, @NotNull Material material, int outputAmount) {
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "small_duct_%s".formatted(material.getName()),
         GTBlocks.DUCT_PIPES[DuctPipeType.SMALL.ordinal()].asStack(outputAmount * 2),
         "w",
         "X",
         "h",
         'X',
         new MaterialEntry(TagPrefix.plate, material)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "medium_duct_%s".formatted(material.getName()),
         GTBlocks.DUCT_PIPES[DuctPipeType.NORMAL.ordinal()].asStack(outputAmount),
         " X ",
         "wXh",
         " X ",
         'X',
         new MaterialEntry(TagPrefix.plate, material)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "large_duct_%s".formatted(material.getName()),
         GTBlocks.DUCT_PIPES[DuctPipeType.LARGE.ordinal()].asStack(outputAmount),
         "XwX",
         "X X",
         "XhX",
         'X',
         new MaterialEntry(TagPrefix.plate, material)
      );
      VanillaRecipeHelper.addShapedRecipe(
         provider,
         "huge_duct_%s".formatted(material.getName()),
         GTBlocks.DUCT_PIPES[DuctPipeType.HUGE.ordinal()].asStack(outputAmount),
         "XwX",
         "X X",
         "XhX",
         'X',
         new MaterialEntry(TagPrefix.plateDouble, material)
      );
   }
}
