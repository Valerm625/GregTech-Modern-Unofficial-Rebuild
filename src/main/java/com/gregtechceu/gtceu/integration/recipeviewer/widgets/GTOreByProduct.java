package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;
import brachy.modularui.screen.RichTooltip;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class GTOreByProduct {
   private static final List<TagPrefix> ORES = new ArrayList<>();
   private static ImmutableList<TagPrefix> IN_PROCESSING_STEPS;
   private static ImmutableList<ItemStack> ALWAYS_MACHINES;
   private final Int2ObjectMap<Content> chances = new Int2ObjectOpenHashMap();
   protected final List<ItemEntryList> itemInputs = new ArrayList<>();
   protected final NonNullList<ItemStack> itemOutputs = NonNullList.create();
   protected final List<FluidEntryList> fluidInputs = new ArrayList<>();
   private boolean hasDirectSmelt = false;
   private boolean hasChemBath = false;
   private boolean hasSeparator = false;
   private boolean hasSifter = false;
   private int currentSlot;

   public static void addOreByProductPrefix(TagPrefix orePrefix) {
      if (!ORES.contains(orePrefix)) {
         ORES.add(orePrefix);
      }
   }

   public GTOreByProduct(Material material) {
      if (IN_PROCESSING_STEPS == null) {
         IN_PROCESSING_STEPS = ImmutableList.of(
            TagPrefix.crushed, TagPrefix.crushedPurified, TagPrefix.dustImpure, TagPrefix.dustPure, TagPrefix.crushedRefined
         );
      }

      if (ALWAYS_MACHINES == null) {
         ALWAYS_MACHINES = ImmutableList.of(
            GTMachines.MACERATOR[1].asStack(),
            GTMachines.MACERATOR[1].asStack(),
            GTMachines.CENTRIFUGE[1].asStack(),
            GTMachines.ORE_WASHER[1].asStack(),
            GTMachines.THERMAL_CENTRIFUGE[1].asStack(),
            GTMachines.MACERATOR[1].asStack(),
            GTMachines.MACERATOR[1].asStack(),
            GTMachines.CENTRIFUGE[1].asStack()
         );
      }

      OreProperty property = material.getProperty(PropertyKey.ORE);
      int oreMultiplier = property.getOreMultiplier();
      int byproductMultiplier = property.getByProductMultiplier();
      this.currentSlot = 0;
      Material[] byproducts = new Material[]{
         property.getOreByProduct(0, material),
         property.getOreByProduct(1, material),
         property.getOreByProduct(2, material),
         property.getOreByProduct(3, material)
      };
      ObjectIntPair<Material> washedIn = property.getWashedIn();
      List<Material> separatedInto = property.getSeparatedInto();
      ItemTagList oreStacks = new ItemTagList();

      for (TagPrefix prefix : ORES) {
         oreStacks.add(Objects.requireNonNull(ChemicalHelper.getTag(prefix, material)), 1, DataComponentPatch.EMPTY);
      }

      oreStacks.add(Objects.requireNonNull(ChemicalHelper.getTag(TagPrefix.rawOre, material)), 1, DataComponentPatch.EMPTY);
      this.itemInputs.add(oreStacks);
      List<ItemStack> simpleWashers = new ArrayList<>();
      simpleWashers.add(new ItemStack(Items.CAULDRON));
      simpleWashers.add(GTMachines.ORE_WASHER[1].asStack());
      if (!material.hasProperty(PropertyKey.BLAST)) {
         this.addToInputs(new ItemStack(Blocks.FURNACE));
         this.hasDirectSmelt = true;
      } else {
         this.addToInputs(ItemStack.EMPTY);
      }

      UnmodifiableIterator var15 = ALWAYS_MACHINES.iterator();

      while (var15.hasNext()) {
         ItemStack stack = (ItemStack)var15.next();
         this.addToInputs(stack);
      }

      this.itemInputs.add(ItemStackList.of(simpleWashers));
      this.itemInputs.add(ItemStackList.of(simpleWashers));
      this.itemInputs.add(ItemStackList.of(simpleWashers));
      if (!((Material)washedIn.first()).isNull()) {
         this.hasChemBath = true;
         this.addToInputs(GTMachines.CHEMICAL_BATH[1].asStack());
      } else {
         this.addToInputs(ItemStack.EMPTY);
      }

      if (separatedInto != null && !separatedInto.isEmpty()) {
         this.hasSeparator = true;
         this.addToInputs(GTMachines.ELECTROMAGNETIC_SEPARATOR[1].asStack());
      } else {
         this.addToInputs(ItemStack.EMPTY);
      }

      if (material.hasProperty(PropertyKey.GEM)) {
         this.hasSifter = true;
         this.addToInputs(GTMachines.SIFTER[1].asStack());
      } else {
         this.addToInputs(ItemStack.EMPTY);
      }

      var15 = IN_PROCESSING_STEPS.iterator();

      while (var15.hasNext()) {
         TagPrefix prefix = (TagPrefix)var15.next();
         this.itemInputs.add(ItemTagList.of(Objects.requireNonNull(ChemicalHelper.getTag(prefix, material)), 1, DataComponentPatch.EMPTY));
      }

      this.currentSlot += 21;
      if (this.hasDirectSmelt) {
         Material smeltingMaterial = property.getDirectSmeltResult().isNull() ? material : property.getDirectSmeltResult();
         ItemStack smeltingResult;
         if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            smeltingResult = ChemicalHelper.get(TagPrefix.ingot, smeltingMaterial);
         } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            smeltingResult = ChemicalHelper.get(TagPrefix.gem, smeltingMaterial);
         } else {
            smeltingResult = ChemicalHelper.get(TagPrefix.dust, smeltingMaterial);
         }

         smeltingResult.setCount(smeltingResult.getCount() * oreMultiplier);
         this.addToOutputs(smeltingResult);
      } else {
         this.addEmptyOutputs(1);
      }

      this.addToOutputs(material, TagPrefix.crushed, 2 * oreMultiplier);
      if (!ChemicalHelper.get(TagPrefix.gem, byproducts[0]).isEmpty()) {
         this.addToOutputs(byproducts[0], TagPrefix.gem, 1);
      } else {
         this.addToOutputs(byproducts[0], TagPrefix.dust, 1);
      }

      this.addChance(1400, 0);
      this.addToOutputs(material, TagPrefix.dustImpure, 1);
      this.addToOutputs(byproducts[0], TagPrefix.dust, byproductMultiplier);
      this.addChance(1400, 0);
      this.addToOutputs(material, TagPrefix.dust, 1);
      this.addToOutputs(byproducts[0], TagPrefix.dust, 1);
      this.addChance(1111, 0);
      this.addToOutputs(material, TagPrefix.crushedPurified, 1);
      this.addToOutputs(byproducts[0], TagPrefix.dust, 1);
      this.addChance(3333, 0);
      FluidTagList tagList = new FluidTagList();
      tagList.add(GTMaterials.Water.getFluidTag(), 1000, DataComponentPatch.EMPTY);
      tagList.add(GTMaterials.DistilledWater.getFluidTag(), 100, DataComponentPatch.EMPTY);
      this.fluidInputs.add(tagList);
      this.addToOutputs(material, TagPrefix.crushedRefined, 1);
      this.addToOutputs(byproducts[1], TagPrefix.dust, byproductMultiplier);
      this.addChance(3333, 0);
      this.addToOutputs(material, TagPrefix.dust, 1);
      this.addToOutputs(byproducts[2], TagPrefix.dust, 1);
      this.addChance(1400, 0);
      this.addToOutputs(material, TagPrefix.dustPure, 1);
      this.addToOutputs(byproducts[1], TagPrefix.dust, 1);
      this.addChance(1400, 0);
      this.addToOutputs(material, TagPrefix.dust, 1);
      this.addToOutputs(byproducts[1], TagPrefix.dust, 1);
      this.addChance(1111, 0);
      this.addToOutputs(material, TagPrefix.crushed, 1);
      this.addToOutputs(material, TagPrefix.crushedPurified, 1);
      this.addToOutputs(material, TagPrefix.dustImpure, 1);
      this.addToOutputs(material, TagPrefix.dust, 1);
      this.addToOutputs(material, TagPrefix.dustPure, 1);
      this.addToOutputs(material, TagPrefix.dust, 1);
      if (this.hasChemBath) {
         this.addToOutputs(material, TagPrefix.crushedPurified, 1);
         this.addToOutputs(byproducts[3], TagPrefix.dust, byproductMultiplier);
         this.addChance(7000, 0);
         this.fluidInputs.add(FluidTagList.of(((Material)washedIn.first()).getFluidTag(), washedIn.secondInt(), DataComponentPatch.EMPTY));
      } else {
         this.addEmptyOutputs(2);
         this.fluidInputs.add(new FluidStackList());
      }

      if (this.hasSeparator) {
         TagPrefix prefix = separatedInto.getLast().getBlastTemperature() == 0 && separatedInto.getLast().hasProperty(PropertyKey.INGOT)
            ? TagPrefix.nugget
            : TagPrefix.dust;
         ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedInto.getLast(), prefix == TagPrefix.nugget ? 2 : 1);
         this.addToOutputs(material, TagPrefix.dust, 1);
         this.addToOutputs(separatedInto.getFirst(), TagPrefix.dust, 1);
         this.addChance(1000, 0);
         this.addToOutputs(separatedStack2);
         this.addChance(prefix == TagPrefix.dust ? 500 : 2000, 0);
      } else {
         this.addEmptyOutputs(3);
      }

      if (this.hasSifter) {
         boolean highOutput = material.hasFlag(MaterialFlags.HIGH_SIFTER_OUTPUT);
         ItemStack flawedStack = ChemicalHelper.get(TagPrefix.gemFlawed, material);
         ItemStack chippedStack = ChemicalHelper.get(TagPrefix.gemChipped, material);
         this.addToOutputs(material, TagPrefix.gemExquisite, 1);
         this.addGemChance(300, 0, 500, 0, highOutput);
         this.addToOutputs(material, TagPrefix.gemFlawless, 1);
         this.addGemChance(1000, 0, 1500, 0, highOutput);
         this.addToOutputs(material, TagPrefix.gem, 1);
         this.addGemChance(3500, 0, 5000, 0, highOutput);
         this.addToOutputs(material, TagPrefix.dustPure, 1);
         this.addGemChance(5000, 0, 2500, 0, highOutput);
         if (!flawedStack.isEmpty()) {
            this.addToOutputs(flawedStack);
            this.addGemChance(2500, 0, 2000, 0, highOutput);
         } else {
            this.addEmptyOutputs(1);
         }

         if (!chippedStack.isEmpty()) {
            this.addToOutputs(chippedStack);
            this.addGemChance(3500, 0, 3000, 0, highOutput);
         } else {
            this.addEmptyOutputs(1);
         }
      } else {
         this.addEmptyOutputs(6);
      }
   }

   public Consumer<RichTooltip> getTooltip(int slotIndex) {
      return tooltip -> {
         if (this.chances.containsKey(slotIndex)) {
            Content entry = (Content)this.chances.get(slotIndex);
            float chance = 100.0F * (float)entry.chance() / (float)entry.maxChance();
            if (entry.tierChanceBoost() != 0) {
               float tierChanceBoost = (float)entry.tierChanceBoost() / 100.0F;
               tooltip.addLine(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_base", chance));
               tooltip.addLine(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_tier_boost_plus", tierChanceBoost));
            } else {
               tooltip.addLine(FormattingUtil.formatPercentage2Places("gtceu.gui.content.chance_no_boost", chance));
            }
         }
      };
   }

   public Content getChance(int slot) {
      return (Content)this.chances.get(slot);
   }

   public boolean hasSifter() {
      return this.hasSifter;
   }

   public boolean hasSeparator() {
      return this.hasSeparator;
   }

   public boolean hasChemBath() {
      return this.hasChemBath;
   }

   public boolean hasDirectSmelt() {
      return this.hasDirectSmelt;
   }

   private void addToOutputs(Material material, TagPrefix prefix, int size) {
      this.addToOutputs(ChemicalHelper.get(prefix, material, size));
   }

   private void addToOutputs(ItemStack stack) {
      this.itemOutputs.add(stack);
      this.currentSlot++;
   }

   private void addEmptyOutputs(int amount) {
      for (int i = 0; i < amount; i++) {
         this.addToOutputs(ItemStack.EMPTY);
      }
   }

   private void addToInputs(ItemStack stack) {
      this.itemInputs.add(ItemStackList.of(stack));
   }

   private void addChance(int base, int boost) {
      this.chances.put(this.currentSlot - 1, new Content(ItemStack.EMPTY, base, ChanceLogic.getMaxChancedValue(), boost));
   }

   private void addGemChance(int baseLow, int boostLow, int baseHigh, int boostHigh, boolean high) {
      if (high) {
         this.addChance(baseHigh, boostHigh);
      } else {
         this.addChance(baseLow, boostLow);
      }
   }

   @Generated
   public List<ItemEntryList> getItemInputs() {
      return this.itemInputs;
   }

   @Generated
   public NonNullList<ItemStack> getItemOutputs() {
      return this.itemOutputs;
   }

   @Generated
   public List<FluidEntryList> getFluidInputs() {
      return this.fluidInputs;
   }
}
