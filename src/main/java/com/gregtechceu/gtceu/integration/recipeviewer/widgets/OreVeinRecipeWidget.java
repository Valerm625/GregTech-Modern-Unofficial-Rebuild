package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class OreVeinRecipeWidget extends ParentWidget<OreVeinRecipeWidget> {
   private final String nameLang;
   private final int weight;
   @Nullable
   private final String range;
   @Nullable
   private final Set<ResourceKey<Level>> dimensionFilter;
   @Nullable
   private final String veinYield;
   @Nullable
   private final String depleted;
   public static final int WIDTH = 120;
   public static final int BEDROCK_HEIGHT = 140;
   public static final int ORE_VEIN_HEIGHT = 160;

   private OreVeinRecipeWidget(
      int width,
      int height,
      String nameLang,
      int weight,
      @Nullable String range,
      @Nullable Set<ResourceKey<Level>> dimensionFilter,
      @Nullable String veinYield,
      @Nullable String depleted
   ) {
      this.size(width, height);
      this.nameLang = nameLang;
      this.weight = weight;
      this.range = range;
      this.dimensionFilter = dimensionFilter;
      this.veinYield = veinYield;
      this.depleted = depleted;
   }

   public OreVeinRecipeWidget(BedrockFluidDefinition fluid) {
      this(120, 140, getFluidName(fluid), fluid.getWeight(), null, fluid.dimensionFilter, veinYield(fluid), depletion(fluid));
      this.drawUI(
         (Flow)((Flow)Flow.row().coverChildren())
            .child(RecipeViewerSlotWidget.create().value(new FluidStack(fluid.getStoredFluid(), 1000)).recipeSlotRole(RecipeSlotRole.OUTPUT))
      );
   }

   public OreVeinRecipeWidget(GTOreDefinition oreDefinition) {
      this(120, 160, getOreName(oreDefinition), oreDefinition.weight(), range(oreDefinition), oreDefinition.dimensionFilter(), null, null);
      NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
      List<Integer> chances = oreDefinition.veinGenerator().getAllChances();
      containedOresAsItemStacks.addAll(getRawMaterialList(oreDefinition));
      Flow slots = (Flow)Flow.row().coverChildren();

      for (int i = 0; i < containedOresAsItemStacks.size(); i++) {
         RecipeViewerSlotWidget<?> oreSlot = RecipeViewerSlotWidget.create()
            .value((ItemStack)containedOresAsItemStacks.get(i))
            .recipeSlotRole(RecipeSlotRole.OUTPUT);
         int finalI = i;
         oreSlot.tooltipBuilder(r -> r.add(Text.lang("gtceu.jei.ore_vein_diagram.chance", new Object[]{chances.get(finalI)})));
         slots.child(oreSlot);
      }

      this.drawUI(slots);
   }

   public OreVeinRecipeWidget(BedrockOreDefinition bedrockOre) {
      this(120, 140, getBedrockOreName(bedrockOre), bedrockOre.weight(), null, bedrockOre.dimensionFilter(), veinYield(bedrockOre), depletion(bedrockOre));
      NonNullList<ItemStack> containedOresAsItemStacks = NonNullList.create();
      IntList chances = bedrockOre.getAllChances();
      containedOresAsItemStacks.addAll(getRawMaterialList(bedrockOre));
      Flow slots = (Flow)Flow.row().coverChildren();

      for (int i = 0; i < containedOresAsItemStacks.size(); i++) {
         RecipeViewerSlotWidget<?> oreSlot = RecipeViewerSlotWidget.create()
            .value((ItemStack)containedOresAsItemStacks.get(i))
            .recipeSlotRole(RecipeSlotRole.OUTPUT);
         int finalI = i;
         oreSlot.tooltipBuilder(r -> r.add(Text.lang("gtceu.jei.ore_vein_diagram.chance", new Object[]{chances.getInt(finalI)})));
         slots.child(oreSlot);
      }

      this.drawUI(slots);
   }

   private void drawUI(Flow contentsRow) {
      Flow col = (Flow)((Flow)((Flow)((Flow)((Flow)((Flow)((Flow)((Flow)((Flow)Flow.col().sizeRel(1.0F))
                              .child((IWidget)Text.lang(this.nameLang).asWidget().marginBottom(3)))
                           .child((IWidget)contentsRow.marginBottom(3)))
                        .childIf(this.range != null, () -> (IWidget)Text.lang("gtceu.jei.ore_vein_diagram.spawn_range").asWidget().marginBottom(1)))
                     .childIf(this.range != null, () -> (IWidget)Text.str(Objects.requireNonNull(this.range)).asWidget().marginBottom(3)))
                  .child((IWidget)Text.lang("gtceu.jei.ore_vein_diagram.weight", new Object[]{this.weight}).asWidget().marginBottom(3)))
               .childIf(this.veinYield != null, () -> (IWidget)Text.str(Objects.requireNonNull(this.veinYield)).asWidget().marginBottom(3)))
            .childIf(this.depleted != null, () -> (IWidget)Text.str(Objects.requireNonNull(this.depleted)).asWidget().marginBottom(3)))
         .child(Text.lang("gtceu.jei.ore_vein_diagram.dimensions").asWidget());
      if (this.dimensionFilter != null) {
         Flow row = (Flow)((Flow)Flow.row().coverChildren()).padding(2);

         for (DimensionMarker dimMarker : getDimensionMarkers(this.dimensionFilter)) {
            RecipeViewerSlotWidget<?> dimSlot = (RecipeViewerSlotWidget<?>)RecipeViewerSlotWidget.create()
               .value(dimMarker.getIcon())
               .recipeSlotRole(RecipeSlotRole.CATALYST)
               .background(new IDrawable[]{IDrawable.NONE});
            if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
               dimSlot.overlay(new IDrawable[]{Text.str("T" + (dimMarker.tier >= 99 ? "?" : dimMarker.tier))});
            }

            row.child(dimSlot);
         }

         col.child(row);
      } else {
         col.child(Text.str("Any").asWidget());
      }

      this.child(col);
   }

   private static String range(GTOreDefinition oreDefinition) {
      HeightProvider height = oreDefinition.heightRange().height;
      int minHeight = 0;
      int maxHeight = 0;
      if (height instanceof UniformHeight uniform) {
         minHeight = uniform.minInclusive.resolveY(null);
         maxHeight = uniform.maxInclusive.resolveY(null);
      }

      return String.format("%d - %d", minHeight, maxHeight);
   }

   public static List<ItemStack> getContainedOresAndBlocks(GTOreDefinition oreDefinition) {
      return oreDefinition.veinGenerator()
         .getAllEntries()
         .stream()
         .flatMap(entry -> entry.map(state -> Stream.of(state.getBlock().asItem().getDefaultInstance()), material -> {
               Set<ItemStack> ores = new HashSet<>();
               ores.add(ChemicalHelper.get(TagPrefix.rawOre, material));

               for (TagPrefix prefix : TagPrefix.ORES.keySet()) {
                  ores.add(ChemicalHelper.get(prefix, material));
               }

               return ores.stream();
            }))
         .toList();
   }

   public static List<ItemStack> getRawMaterialList(GTOreDefinition oreDefinition) {
      return oreDefinition.veinGenerator()
         .getAllEntries()
         .stream()
         .map(entry -> entry.map(state -> state.getBlock().asItem().getDefaultInstance(), material -> ChemicalHelper.get(TagPrefix.rawOre, material)))
         .toList();
   }

   public static List<ItemStack> getRawMaterialList(BedrockOreDefinition bedrockOreDefinition) {
      return bedrockOreDefinition.materials().stream().map(entry -> ChemicalHelper.get(TagPrefix.rawOre, entry.material())).toList();
   }

   public static DimensionMarker[] getDimensionMarkers(Set<ResourceKey<Level>> dimensionFilter) {
      return dimensionFilter.stream()
         .<ResourceLocation>map(ResourceKey::location)
         .map(loc -> GTRegistries.DIMENSION_MARKERS.getOptional(loc).orElse(new DimensionMarker(99, () -> Blocks.BARRIER, Component.literal(loc.toString()))))
         .sorted(Comparator.comparingInt(DimensionMarker::getTier))
         .toArray(DimensionMarker[]::new);
   }

   public static String getOreName(GTOreDefinition oreDefinition) {
      ResourceLocation id = Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.Keys.ORE_VEIN).getKey(oreDefinition);
      return "gtceu.jei.ore_vein." + id.getPath();
   }

   public static String getFluidName(BedrockFluidDefinition fluid) {
      ResourceLocation id = Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID).getKey(fluid);
      return "gtceu.jei.bedrock_fluid." + id.getPath();
   }

   public static String getBedrockOreName(BedrockOreDefinition oreDefinition) {
      ResourceLocation id = Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_ORE).getKey(oreDefinition);
      return "gtceu.jei.bedrock_ore." + id.getPath();
   }

   private static String veinYield(BedrockFluidDefinition fluidDefinition) {
      int minYield = fluidDefinition.getMinimumYield();
      int maxYield = fluidDefinition.getMaximumYield();
      return String.format("%d - %dmB/s", minYield, maxYield);
   }

   private static String depletion(BedrockFluidDefinition fluidDefinition) {
      int depletion = fluidDefinition.getDepletedYield();
      return String.format("%dmB/s", depletion);
   }

   private static String veinYield(BedrockOreDefinition oreDefinition) {
      IntProvider yieldProvider = oreDefinition.yield();
      int minYield = yieldProvider.getMinValue();
      int maxYield = yieldProvider.getMaxValue();
      return String.format("%d - %d", minYield, maxYield);
   }

   private static String depletion(BedrockOreDefinition oreDefinition) {
      int depletion = oreDefinition.depletedYield();
      return String.format("%d", depletion);
   }
}
