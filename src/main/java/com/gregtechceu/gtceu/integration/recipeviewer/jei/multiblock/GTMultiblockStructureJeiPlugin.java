package com.gregtechceu.gtceu.integration.recipeviewer.jei.multiblock;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.recipeviewer.MultiblockStructurePreview;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class GTMultiblockStructureJeiPlugin implements IModPlugin {
   private static final ResourceLocation PLUGIN_UID = id("multiblock_structure_delegate");
   private static volatile IJeiRuntime runtime;
   public static final RecipeType<GTMultiblockStructureJeiPlugin.StructureRecipe> TYPE = new RecipeType(
      id("multiblock_info"), GTMultiblockStructureJeiPlugin.StructureRecipe.class
   );

   private static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("gtceu", path);
   }

   public ResourceLocation getPluginUid() {
      return PLUGIN_UID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(new IRecipeCategory[]{new GTMultiblockStructureJeiPlugin.StructureCategory(registration.getJeiHelpers().getGuiHelper())});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      List<GTMultiblockStructureJeiPlugin.StructureRecipe> recipes = new ArrayList<>();
      List<ResourceLocation> degenerate = new ArrayList<>();
      int candidates = 0;
      int strictAirStructures = 0;
      int strictAirCells = 0;

      for (MachineDefinition definition : GTRegistries.MACHINES) {
         if (definition instanceof MultiblockMachineDefinition multiblock
            && multiblock.isRenderXEIPreview()
            && multiblock.getStructurePatterns().containsKey("main")) {
            candidates++;

            try {
               GTMultiblockStructureJeiPlugin.StructureRecipe recipe = GTMultiblockStructureJeiPlugin.StructureRecipe.create(multiblock);
               GTMultiblockStructureJeiPlugin.StructureData data = recipe.initialStructure();
               recipes.add(recipe);
               if (data.parts().size() <= 1 || data.materials().isEmpty()) {
                  degenerate.add(recipe.id());
                  GTMultiblockJei.LOGGER
                     .error(
                        "Degenerate GTCEu structure schema for {}: schemaCells={}, renderedNonAir={}, materials={}, bounds={}x{}x{}",
                        recipe.id(),
                        data.schemaCells(),
                        data.parts().size(),
                        data.materials().size(),
                        data.width(),
                        data.height(),
                        data.depth()
                     );
               }

               if (data.strictAirCells() > 0) {
                  strictAirStructures++;
                  strictAirCells += data.strictAirCells();
               }

               GTMultiblockJei.LOGGER
                  .debug(
                     "GTCEu structure audit {}: schemaCells={}, renderedNonAir={}, materials={}, strictAir={}, bounds={}x{}x{}",
                     recipe.id(),
                     data.schemaCells(),
                     data.parts().size(),
                     data.materials().size(),
                     data.strictAirCells(),
                     data.width(),
                     data.height(),
                     data.depth()
                  );
            } catch (RuntimeException var12) {
               GTMultiblockJei.LOGGER.error("Could not create enhanced JEI structure preview for {}", multiblock.getId(), var12);
            }
         }
      }

      recipes.sort(Comparator.comparing(recipex -> recipex.id().toString()));
      registration.addRecipes(TYPE, List.copyOf(recipes));
      GTMultiblockJei.LOGGER
         .info(
            "Registered {}/{} enhanced GTCEu multiblock JEI previews; audit: degenerate={}, strictAirStructures={}, strictAirCells={}",
            recipes.size(),
            candidates,
            degenerate.size(),
            strictAirStructures,
            strictAirCells
         );
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      for (MachineDefinition definition : GTRegistries.MACHINES) {
         if (definition instanceof MultiblockMachineDefinition) {
            MultiblockMachineDefinition multiblock = (MultiblockMachineDefinition)definition;
            if (multiblock.isRenderXEIPreview() && multiblock.getStructurePatterns().containsKey("main")) {
               registration.addRecipeCatalyst(multiblock.asStack(), new RecipeType[]{TYPE});
            }
         }
      }
   }

   public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
      runtime = jeiRuntime;
   }

   public static MultiblockStructurePreview createSharedPreview(MultiblockMachineDefinition definition) {
      return new GTMultiblockStructureJeiPlugin.StructurePreviewWidget(GTMultiblockStructureJeiPlugin.StructureRecipe.create(definition));
   }

   private static record ConsistencySelection(StructureSemantics.ConsistencyKind kind, int members) {
   }

   public static record MaterialEntry(ItemStack stack, Set<StructureSemantics.CellRole> roles) {
   }

   private static final class MutableMaterial {
      private final Item item;
      private final EnumSet<StructureSemantics.CellRole> roles = EnumSet.noneOf(StructureSemantics.CellRole.class);
      private int count;

      private MutableMaterial(Item item) {
         this.item = item;
      }
   }

   private static final class PreviewModel {
      private final MultiblockMachineDefinition definition;
      private final IBlockPattern pattern;
      private final MultiblockSchemaInfo schema = new MultiblockSchemaInfo();
      private final List<StructureSemantics.DimensionOption> dimensions;
      private GTMultiblockStructureJeiPlugin.StructureData structure;
      private BlockPos selectedSchemaPosition;

      private PreviewModel(MultiblockMachineDefinition definition) {
         this.definition = definition;
         this.pattern = definition.getStructurePatterns().get("main").get();
         StructureSemantics.initializePreviewDimensions(definition, this.pattern, this.schema);
         StructureSemantics.refreshCompleteSchema(this.schema, definition, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false);
         this.dimensions = StructureSemantics.dimensionOptions(this.pattern);
         this.rebuild();
      }

      private GTMultiblockStructureJeiPlugin.StructureData structure() {
         return this.structure;
      }

      private void select(GTMultiblockStructureJeiPlugin.RenderPart part) {
         this.selectedSchemaPosition = part == null ? null : part.schemaPosition();
      }

      private GTMultiblockStructureJeiPlugin.SelectedCell selected() {
         if (this.selectedSchemaPosition == null) {
            return null;
         } else {
            BlockInfo current = this.schema.getStructureBlocks().get(this.selectedSchemaPosition);
            if (current == null) {
               this.selectedSchemaPosition = null;
               return null;
            } else {
               PatternPredicate predicate = StructureSemantics.predicateAt(
                  this.schema, this.pattern, this.selectedSchemaPosition, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
               );
               List<StructureSemantics.Candidate> candidates = StructureSemantics.candidates(predicate);
               GTMultiblockStructureJeiPlugin.ConsistencySelection consistency = this.consistencyFor(predicate, current);
               if (consistency != null) {
                  candidates = StructureSemantics.commonConsistencyCandidates(
                     this.schema, this.pattern, predicate, consistency.kind(), StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
                  );
               }

               return new GTMultiblockStructureJeiPlugin.SelectedCell(
                  this.selectedSchemaPosition,
                  current,
                  predicate,
                  candidates,
                  StructureSemantics.classify(predicate),
                  StructureSemantics.isReplaceable(predicate),
                  consistency
               );
            }
         }
      }

      private GTMultiblockStructureJeiPlugin.ConsistencySelection consistencyFor(GTMultiblockStructureJeiPlugin.RenderPart part) {
         if (part == null) {
            return null;
         } else {
            BlockInfo current = this.schema.getStructureBlocks().get(part.schemaPosition());
            return current == null ? null : this.consistencyFor(part.predicate(), current);
         }
      }

      private List<StructureSemantics.Candidate> candidatesFor(GTMultiblockStructureJeiPlugin.RenderPart part) {
         GTMultiblockStructureJeiPlugin.ConsistencySelection consistency = this.consistencyFor(part);
         return consistency == null
            ? StructureSemantics.candidates(part.predicate())
            : StructureSemantics.commonConsistencyCandidates(
               this.schema, this.pattern, part.predicate(), consistency.kind(), StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
            );
      }

      private GTMultiblockStructureJeiPlugin.ConsistencySelection consistencyFor(PatternPredicate predicate, BlockInfo current) {
         StructureSemantics.ConsistencyKind kind = StructureSemantics.activeConsistencyKind(predicate, current);
         if (kind == null) {
            return null;
         } else {
            int members = StructureSemantics.consistencyPositions(
                  this.schema, this.pattern, kind, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
               )
               .size();
            return new GTMultiblockStructureJeiPlugin.ConsistencySelection(kind, members);
         }
      }

      private boolean cycleCandidate(int delta) {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.selected();
         if (selected != null && selected.candidates().size() >= 2) {
            int currentIndex = 0;
            ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(selected.current().getBlockState().getBlock());

            for (int index = 0; index < selected.candidates().size(); index++) {
               if (selected.candidates().get(index).blockInfo().equals(selected.current()) || selected.candidates().get(index).blockId().equals(currentBlockId)
                  )
                {
                  currentIndex = index;
                  break;
               }
            }

            int nextIndex = Math.floorMod(currentIndex + delta, selected.candidates().size());
            StructureSemantics.Candidate selectedCandidate = selected.candidates().get(nextIndex);
            List<BlockPos> targetPositions = selected.consistency() == null
               ? List.of(selected.position())
               : StructureSemantics.consistencyPositions(
                  this.schema, this.pattern, selected.consistency().kind(), StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
               );
            List<GTMultiblockStructureJeiPlugin.TargetChoice> targetChoices = new ArrayList<>(targetPositions.size());

            for (BlockPos targetPosition : targetPositions) {
               PatternPredicate targetPredicate = StructureSemantics.predicateAt(
                  this.schema, this.pattern, targetPosition, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
               );
               StructureSemantics.Candidate targetCandidate = selected.consistency() == null
                  ? StructureSemantics.findCandidate(targetPredicate, selectedCandidate.blockId())
                  : StructureSemantics.findConsistencyCandidate(targetPredicate, selected.consistency().kind(), selectedCandidate.blockId());
               if (targetCandidate == null) {
                  return false;
               }

               targetChoices.add(new GTMultiblockStructureJeiPlugin.TargetChoice(targetPosition, targetCandidate.blockInfo()));
            }

            Long2ObjectOpenHashMap<BlockInfo> previousPreferences = new Long2ObjectOpenHashMap(this.schema.getUserGlobalBlockPreferences());

            for (GTMultiblockStructureJeiPlugin.TargetChoice target : targetChoices) {
               this.schema.getUserGlobalBlockPreferences().put(target.position().asLong(), target.blockInfo());
            }

            try {
               this.refresh();
               StructureSemantics.validateConsistency(this.schema, this.pattern, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false);
               return true;
            } catch (RuntimeException var13) {
               this.schema.getUserGlobalBlockPreferences().clear();
               this.schema.getUserGlobalBlockPreferences().putAll(previousPreferences);
               this.refresh();
               return false;
            }
         } else {
            return false;
         }
      }

      private int dimensionValue(StructureSemantics.DimensionOption option) {
         return switch (option.kind()) {
            case REPEATED_SLICE -> this.schema.getUserSliceRepeats().get(option.index());
            case EXPANDABLE_BOUND -> this.schema.getUserDimensions().getInt(option.index());
         };
      }

      private List<StructureSemantics.DimensionOption> dimensionsForAxis(Axis axis) {
         return this.dimensions.stream().filter(option -> StructureSemantics.dimensionAxis(this.pattern, option) == axis).toList();
      }

      private Axis firstAdjustableAxis() {
         for (Axis axis : List.of(Axis.X, Axis.Y, Axis.Z)) {
            if (!this.dimensionsForAxis(axis).isEmpty()) {
               return axis;
            }
         }

         return Axis.X;
      }

      private int axisValue(Axis axis) {
         return axisValue(this.structure, axis);
      }

      private int axisMinimum(Axis axis) {
         int minimum = this.axisValue(axis);

         for (StructureSemantics.DimensionOption option : this.dimensionsForAxis(axis)) {
            minimum -= this.dimensionValue(option) - option.minimum();
         }

         return minimum;
      }

      private int axisMaximum(Axis axis) {
         int maximum = this.axisValue(axis);

         for (StructureSemantics.DimensionOption option : this.dimensionsForAxis(axis)) {
            maximum += option.maximum() - this.dimensionValue(option);
         }

         return maximum;
      }

      private boolean canAdjustAxis(Axis axis, int delta) {
         if (delta == 0) {
            return false;
         } else {
            for (StructureSemantics.DimensionOption option : this.dimensionsForAxis(axis)) {
               int value = this.dimensionValue(option);
               if (delta > 0 ? value < option.maximum() : value > option.minimum()) {
                  return true;
               }
            }

            return false;
         }
      }

      private boolean adjustAxis(Axis axis, int delta) {
         if (delta == 0) {
            return false;
         } else {
            List<StructureSemantics.DimensionOption> candidates = new ArrayList<>();

            for (StructureSemantics.DimensionOption option : this.dimensionsForAxis(axis)) {
               int value = this.dimensionValue(option);
               if (delta > 0 ? value < option.maximum() : value > option.minimum()) {
                  candidates.add(option);
               }
            }

            candidates.sort((left, right) -> {
               if (this.pattern instanceof ExpandablePattern) {
                  int leftGrowth = this.dimensionValue(left) - left.minimum();
                  int rightGrowth = this.dimensionValue(right) - right.minimum();
                  int byValue = Integer.compare(leftGrowth, rightGrowth);
                  if (delta < 0) {
                     byValue = -byValue;
                  }

                  if (byValue != 0) {
                     return byValue;
                  }
               }

               return delta > 0 ? Integer.compare(left.index(), right.index()) : Integer.compare(right.index(), left.index());
            });

            for (StructureSemantics.DimensionOption optionx : candidates) {
               if (this.adjustDimension(optionx, delta > 0 ? 1 : -1, axis)) {
                  return true;
               }
            }

            return false;
         }
      }

      private boolean adjustDimension(StructureSemantics.DimensionOption option, int delta, Axis expectedAxis) {
         int oldValue = this.dimensionValue(option);
         int newValue = Mth.clamp(oldValue + delta, option.minimum(), option.maximum());
         if (newValue == oldValue) {
            return false;
         } else {
            int beforeX = this.structure.width();
            int beforeY = this.structure.height();
            int beforeZ = this.structure.depth();
            int[] previousSliceRepeats = StructureSemantics.currentSliceRepeats(this.schema, this.pattern);
            int[] previousExpandableBounds = StructureSemantics.currentExpandableBounds(this.schema, this.pattern);
            Long2ObjectOpenHashMap<BlockInfo> previousPreferences = new Long2ObjectOpenHashMap(this.schema.getUserGlobalBlockPreferences());
            BlockPos previousSelection = this.selectedSchemaPosition;

            try {
               switch (option.kind()) {
                  case REPEATED_SLICE:
                     this.schema.getUserSliceRepeats().put(option.index(), newValue);
                     break;
                  case EXPANDABLE_BOUND:
                     this.schema.getUserDimensions().set(option.index(), newValue);
               }

               this.schema.getUserGlobalBlockPreferences().clear();
               this.selectedSchemaPosition = null;
               this.refresh();
               int expectedX = beforeX + (expectedAxis == Axis.X ? delta : 0);
               int expectedY = beforeY + (expectedAxis == Axis.Y ? delta : 0);
               int expectedZ = beforeZ + (expectedAxis == Axis.Z ? delta : 0);
               if (this.structure.width() == expectedX && this.structure.height() == expectedY && this.structure.depth() == expectedZ) {
                  return true;
               } else {
                  throw new IllegalArgumentException("raw GTCEu dimension did not change the selected axis by one");
               }
            } catch (RuntimeException var18) {
               this.schema.getUserSliceRepeats().clear();

               for (int index = 0; index < previousSliceRepeats.length; index++) {
                  this.schema.getUserSliceRepeats().put(index, previousSliceRepeats[index]);
               }

               this.schema.getUserDimensions().clear();

               for (int bound : previousExpandableBounds) {
                  this.schema.getUserDimensions().add(bound);
               }

               this.schema.getUserGlobalBlockPreferences().clear();
               this.schema.getUserGlobalBlockPreferences().putAll(previousPreferences);
               this.selectedSchemaPosition = previousSelection;
               this.refresh();
               return false;
            }
         }
      }

      private static int axisValue(GTMultiblockStructureJeiPlugin.StructureData data, Axis axis) {
         return switch (axis) {
            case X -> data.width();
            case Y -> data.height();
            case Z -> data.depth();
            default -> throw new MatchException(null, null);
         };
      }

      private void refresh() {
         StructureSemantics.refreshCompleteSchema(this.schema, this.definition, StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false);
         this.rebuild();
      }

      private void rebuild() {
         Map<BlockPos, BlockInfo> source = this.schema.getStructureBlocks();
         List<GTMultiblockStructureJeiPlugin.RawPart> raw = new ArrayList<>();
         int minX = Integer.MAX_VALUE;
         int minY = Integer.MAX_VALUE;
         int minZ = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE;
         int maxY = Integer.MIN_VALUE;
         int maxZ = Integer.MIN_VALUE;
         int strictAirCells = 0;

         for (Entry<BlockPos, BlockInfo> entry : source.entrySet()) {
            BlockPos position = entry.getKey();
            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            minZ = Math.min(minZ, position.getZ());
            maxX = Math.max(maxX, position.getX());
            maxY = Math.max(maxY, position.getY());
            maxZ = Math.max(maxZ, position.getZ());
            PatternPredicate predicate = StructureSemantics.predicateAt(
               this.schema, this.pattern, entry.getKey(), StructureSemantics.PREVIEW_FRONT, StructureSemantics.PREVIEW_UP, false
            );
            BlockState state = entry.getValue().getBlockState();
            if (state.isAir()) {
               if (predicate == PatternPredicate.AIR) {
                  strictAirCells++;
               }
            } else {
               raw.add(new GTMultiblockStructureJeiPlugin.RawPart(position.immutable(), state, predicate));
            }
         }

         if (raw.isEmpty()) {
            throw new IllegalStateException("GTCEu generated an empty multiblock preview");
         } else {
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;
            int depth = maxZ - minZ + 1;
            List<GTMultiblockStructureJeiPlugin.RenderPart> parts = new ArrayList<>(raw.size());
            Map<Item, GTMultiblockStructureJeiPlugin.MutableMaterial> materialMap = new LinkedHashMap<>();

            for (GTMultiblockStructureJeiPlugin.RawPart rawPart : raw) {
               int x = rawPart.position().getX() - minX;
               int y = rawPart.position().getY() - minY;
               int z = rawPart.position().getZ() - minZ;
               StructureSemantics.CellRole role = StructureSemantics.classify(rawPart.predicate());
               parts.add(
                  new GTMultiblockStructureJeiPlugin.RenderPart(
                     rawPart.position(), x, y, z, rawPart.state(), rawPart.predicate(), role, StructureSemantics.isReplaceable(rawPart.predicate())
                  )
               );
               Item item = rawPart.state().getBlock().asItem();
               if (item != Items.AIR) {
                  GTMultiblockStructureJeiPlugin.MutableMaterial material = materialMap.computeIfAbsent(
                     item, GTMultiblockStructureJeiPlugin.MutableMaterial::new
                  );
                  material.count++;
                  material.roles.add(role);
                  if (StructureSemantics.isReplaceable(rawPart.predicate())) {
                     material.roles.add(StructureSemantics.CellRole.REPLACEABLE);
                  }
               }
            }

            ItemStack controller = this.definition.asStack();
            if (!controller.isEmpty()) {
               GTMultiblockStructureJeiPlugin.MutableMaterial material = materialMap.get(controller.getItem());
               if (material != null && material.count > 0) {
                  material.count--;
                  if (material.count == 0) {
                     materialMap.remove(controller.getItem());
                  }
               }
            }

            List<GTMultiblockStructureJeiPlugin.MaterialEntry> materials = materialMap.values()
               .stream()
               .sorted(Comparator.comparing(materialx -> BuiltInRegistries.ITEM.getKey(materialx.item).toString()))
               .map(materialx -> new GTMultiblockStructureJeiPlugin.MaterialEntry(new ItemStack(materialx.item, materialx.count), Set.copyOf(materialx.roles)))
               .toList();
            Set<BlockPos> occupied = new HashSet<>(parts.size());

            for (GTMultiblockStructureJeiPlugin.RenderPart part : parts) {
               occupied.add(new BlockPos(part.x(), part.y(), part.z()));
            }

            List<GTMultiblockStructureJeiPlugin.RenderPart> overview = parts.stream().filter(partx -> !isFullyEnclosed(partx, occupied)).toList();
            this.structure = new GTMultiblockStructureJeiPlugin.StructureData(
               List.copyOf(parts), List.copyOf(overview), List.copyOf(materials), width, height, depth, source.size(), strictAirCells
            );
         }
      }

      private static boolean isFullyEnclosed(GTMultiblockStructureJeiPlugin.RenderPart part, Set<BlockPos> occupied) {
         BlockPos position = new BlockPos(part.x(), part.y(), part.z());
         return occupied.contains(position.relative(Direction.WEST))
            && occupied.contains(position.relative(Direction.EAST))
            && occupied.contains(position.relative(Direction.DOWN))
            && occupied.contains(position.relative(Direction.UP))
            && occupied.contains(position.relative(Direction.NORTH))
            && occupied.contains(position.relative(Direction.SOUTH));
      }
   }

   private static record RawPart(BlockPos position, BlockState state, PatternPredicate predicate) {
   }

   public static record RenderPart(
      BlockPos schemaPosition, int x, int y, int z, BlockState state, PatternPredicate predicate, StructureSemantics.CellRole role, boolean replaceable
   ) {
   }

   private static record SelectedCell(
      BlockPos position,
      BlockInfo current,
      PatternPredicate predicate,
      List<StructureSemantics.Candidate> candidates,
      StructureSemantics.CellRole role,
      boolean replaceable,
      GTMultiblockStructureJeiPlugin.ConsistencySelection consistency
   ) {
      private ItemStack displayStack() {
         ItemStack stack = this.current.getItemStackForm();
         return stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack;
      }

      private Component name() {
         return (Component)(this.current.getBlockState().isAir() ? Component.translatable("jei.gtceu.multiblock.air") : this.displayStack().getHoverName());
      }

      private BasePredicate currentBasePredicate() {
         for (StructureSemantics.Candidate candidate : this.candidates) {
            if (candidate.blockInfo().equals(this.current)) {
               return candidate.basePredicate();
            }
         }

         return this.candidates.isEmpty() ? null : this.candidates.get(0).basePredicate();
      }
   }

   private static final class StructureCategory implements IRecipeCategory<GTMultiblockStructureJeiPlugin.StructureRecipe> {
      private final IDrawable icon;

      private StructureCategory(IGuiHelper guiHelper) {
         MachineDefinition blastFurnace = (MachineDefinition)GTRegistries.MACHINES
            .get(ResourceLocation.fromNamespaceAndPath("gtceu", "electric_blast_furnace"));
         ItemStack iconStack = blastFurnace == null ? new ItemStack(Items.NETHER_STAR) : blastFurnace.asStack();
         this.icon = guiHelper.createDrawableItemStack(iconStack);
      }

      public RecipeType<GTMultiblockStructureJeiPlugin.StructureRecipe> getRecipeType() {
         return GTMultiblockStructureJeiPlugin.TYPE;
      }

      public Component getTitle() {
         return Component.translatable("jei.gtceu.multiblock.title");
      }

      public IDrawable getIcon() {
         return this.icon;
      }

      public int getWidth() {
         return 200;
      }

      public int getHeight() {
         return 260;
      }

      public void setRecipe(IRecipeLayoutBuilder builder, GTMultiblockStructureJeiPlugin.StructureRecipe recipe, IFocusGroup focuses) {
      }

      public void createRecipeExtras(IRecipeExtrasBuilder builder, GTMultiblockStructureJeiPlugin.StructureRecipe recipe, IFocusGroup focuses) {
         GTMultiblockStructureJeiPlugin.StructurePreviewWidget widget = new GTMultiblockStructureJeiPlugin.StructurePreviewWidget(recipe);
         builder.addWidget(widget);
         builder.addInputHandler(widget);
      }

      public boolean needsRecipeBorder() {
         return false;
      }

      public ResourceLocation getRegistryName(GTMultiblockStructureJeiPlugin.StructureRecipe recipe) {
         return recipe.id();
      }
   }

   public static record StructureData(
      List<GTMultiblockStructureJeiPlugin.RenderPart> parts,
      List<GTMultiblockStructureJeiPlugin.RenderPart> overviewParts,
      List<GTMultiblockStructureJeiPlugin.MaterialEntry> materials,
      int width,
      int height,
      int depth,
      int schemaCells,
      int strictAirCells
   ) {
   }

   private static final class StructurePreviewWidget implements IRecipeWidget, IJeiInputHandler, MultiblockStructurePreview {
      private static final int WIDTH = 200;
      private static final int HEIGHT = 260;
      private static final int VIEW_LEFT = 4;
      private static final int VIEW_TOP = 67;
      private static final int VIEW_RIGHT = 196;
      private static final int VIEW_BOTTOM = 166;
      private static final int MODEL_LEFT = 5;
      private static final int MODEL_TOP = 68;
      private static final int MODEL_RIGHT = 195;
      private static final int MODEL_BOTTOM = 152;
      private static final float MODEL_CENTER_X = 100.0F;
      private static final float MODEL_CENTER_Y = 110.0F;
      private static final int MATERIAL_PANEL_TOP = 211;
      private static final int MATERIAL_SLOTS = 8;
      private static final int MATERIAL_SLOT_LEFT = 5;
      private static final int MATERIAL_SLOT_TOP = 239;
      private static final int MATERIAL_SLOT_SIZE = 18;
      private static final int MATERIAL_SLOT_STRIDE = 21;
      private static final int CONTROLLER_SLOT_LEFT = 178;
      private static final int CANDIDATE_POPUP_LEFT = 22;
      private static final int CANDIDATE_POPUP_TOP = 28;
      private static final int CANDIDATE_POPUP_RIGHT = 178;
      private static final int CANDIDATE_POPUP_ROW_TOP = 47;
      private static final int CANDIDATE_POPUP_ROW_HEIGHT = 19;
      private static final int CANDIDATE_POPUP_ROWS = 8;
      private static final int CANDIDATE_POPUP_BOTTOM = 201;
      private static final double LEFT_DRAG_THRESHOLD_SQUARED = 9.0;
      private static final float EXPLANATION_SCALE = 0.72F;
      private static final float COMPACT_SCALE = 0.78F;
      private static final ScreenPosition POSITION = new ScreenPosition(0, 0);
      private static final ScreenRectangle AREA = new ScreenRectangle(0, 0, 200, 260);
      private final GTMultiblockStructureJeiPlugin.StructureRecipe recipe;
      private final GTMultiblockStructureJeiPlugin.PreviewModel model;
      private float yaw = 45.0F;
      private float pitch = 25.0F;
      private float zoom = 1.0F;
      private int layer = -1;
      private Axis selectedDimensionAxis;
      private int materialPage;
      private boolean candidatePopupOpen;
      private int candidatePopupPage;
      private Component status;
      private boolean renderDisabled;
      private boolean renderFailureLogged;
      private final MultiblockPreviewPointerGesture leftGesture = new MultiblockPreviewPointerGesture(9.0);
      private Runnable pendingLeftClick;

      private StructurePreviewWidget(GTMultiblockStructureJeiPlugin.StructureRecipe recipe) {
         this.recipe = recipe;
         this.model = new GTMultiblockStructureJeiPlugin.PreviewModel(recipe.definition());
         this.selectedDimensionAxis = this.model.firstAdjustableAxis();
      }

      @Override
      public int width() {
         return 200;
      }

      @Override
      public int height() {
         return 260;
      }

      @Override
      public void draw(GuiGraphics graphics, double mouseX, double mouseY) {
         this.drawWidget(graphics, mouseX, mouseY);
      }

      public ScreenPosition getPosition() {
         return POSITION;
      }

      public ScreenRectangle getArea() {
         return AREA;
      }

      public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
         Font font = Minecraft.getInstance().font;
         graphics.fill(0, 0, 200, 260, -15721958);
         drawPanel(graphics, 3, 2, 197, 23);
         drawButton(graphics, font, 5, 4, 20, 21, Component.literal("↺"), isInside(mouseX, mouseY, 5, 4, 20, 21), true);
         drawCenteredTrimmed(graphics, font, this.recipe.controller().getHoverName(), 108, 8, 168, -921601);
         GTMultiblockStructureJeiPlugin.StructureData data = this.model.structure();
         drawPanel(graphics, 3, 25, 197, 44);
         drawButton(graphics, font, 5, 27, 19, 42, Component.literal("<"), isInside(mouseX, mouseY, 5, 27, 19, 42), true);
         drawButton(graphics, font, 81, 27, 95, 42, Component.literal(">"), isInside(mouseX, mouseY, 81, 27, 95, 42), true);
         Component layerLabel = this.layer < 0
            ? Component.translatable("jei.gtceu.multiblock.all_layers_compact")
            : Component.translatable("jei.gtceu.multiblock.layer_compact", new Object[]{compactNumber(this.layer + 1), compactNumber(data.height())});
         drawScaledCenteredTrimmed(graphics, font, layerLabel, 50, 31, 58, 0.78F, -1250068);
         Component finalDimensions = Component.translatable(
            "jei.gtceu.multiblock.final_dimensions_compact",
            new Object[]{compactNumber(data.width()), compactNumber(data.height()), compactNumber(data.depth())}
         );
         drawScaledCenteredTrimmed(graphics, font, finalDimensions, 148, 31, 96, 0.78F, -4608312);
         int dimensionValue = this.model.axisValue(this.selectedDimensionAxis);
         boolean adjustableAxis = !this.model.dimensionsForAxis(this.selectedDimensionAxis).isEmpty();
         drawPanel(graphics, 3, 46, 197, 65);
         this.drawAxisButton(graphics, font, Axis.X, 5, 23, mouseX, mouseY);
         this.drawAxisButton(graphics, font, Axis.Y, 25, 43, mouseX, mouseY);
         this.drawAxisButton(graphics, font, Axis.Z, 45, 63, mouseX, mouseY);
         drawButton(
            graphics,
            font,
            68,
            48,
            84,
            63,
            Component.literal("−"),
            isInside(mouseX, mouseY, 68, 48, 84, 63),
            this.model.canAdjustAxis(this.selectedDimensionAxis, -1)
         );
         drawButton(
            graphics,
            font,
            166,
            48,
            182,
            63,
            Component.literal("+"),
            isInside(mouseX, mouseY, 166, 48, 182, 63),
            this.model.canAdjustAxis(this.selectedDimensionAxis, 1)
         );
         Component axisName = axisName(this.selectedDimensionAxis);
         Component sizeLabel = adjustableAxis
            ? Component.translatable(
               "jei.gtceu.multiblock.dimension.axis_compact",
               new Object[]{
                  axisName,
                  compactNumber(dimensionValue),
                  compactNumber(this.model.axisMinimum(this.selectedDimensionAxis)),
                  compactNumber(this.model.axisMaximum(this.selectedDimensionAxis))
               }
            )
            : Component.translatable("jei.gtceu.multiblock.dimension.axis_fixed_compact", new Object[]{axisName, compactNumber(dimensionValue)});
         drawScaledCenteredTrimmed(graphics, font, sizeLabel, 125, 52, 76, 0.78F, -1250068);
         graphics.fill(4, 67, 196, 166, -16315123);
         if (this.renderDisabled) {
            drawScaledCenteredTrimmed(graphics, font, Component.translatable("jei.gtceu.multiblock.render_failed"), 100, 105, 182, 0.78F, -34953);
         } else {
            this.drawStructure(graphics);
         }

         outline(graphics, 4, 67, 196, 166, -9022629);
         this.drawSelectionPanel(graphics, font, mouseX, mouseY);
         this.drawMaterialPanel(graphics, font, mouseX, mouseY);
         this.drawCandidatePopup(graphics, font, mouseX, mouseY);
      }

      private void drawCandidatePopup(GuiGraphics graphics, Font font, double mouseX, double mouseY) {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (this.candidatePopupOpen && selected != null && selected.candidates().size() >= 2) {
            this.clampCandidatePopupPage();
            List<StructureSemantics.Candidate> candidates = selected.candidates();
            int start = this.candidatePopupPage * 8;
            int end = Math.min(start + 8, candidates.size());
            int pages = this.candidatePopupPageCount();
            graphics.fill(21, 27, 179, 202, -267907570);
            outline(graphics, 21, 27, 179, 202, -3830529);
            drawScaledTrimmed(
               graphics, font, Component.translatable("jei.gtceu.multiblock.candidate_list", new Object[]{candidates.size()}), 26, 33, 112, 0.78F, -855310
            );
            if (pages > 1) {
               drawScaledRightAligned(graphics, font, this.candidatePopupPage + 1 + "/" + pages, 159, 33, 30, 0.78F, -4667963);
            }

            drawButton(graphics, font, 161, 30, 176, 45, Component.literal("×"), isInside(mouseX, mouseY, 161, 30, 176, 45), true);

            for (int index = start; index < end; index++) {
               StructureSemantics.Candidate candidate = candidates.get(index);
               int row = index - start;
               int y = 47 + row * 19;
               boolean hovered = isInside(mouseX, mouseY, 24, y, 176, y + 19 - 1);
               boolean current = candidate.blockInfo().equals(selected.current())
                  || candidate.blockInfo().getBlockState().equals(selected.current().getBlockState());
               graphics.fill(24, y, 176, y + 19 - 1, hovered ? -12831676 : (current ? -14467541 : -15262434));
               ItemStack stack = candidate.displayStack().copyWithCount(1);
               graphics.renderItem(stack, 26, y + 1);
               drawTrimmed(graphics, font, candidate.displayStack().getHoverName(), 46, y + 5, 126, current ? -9251190 : -1513240);
            }
         } else {
            this.candidatePopupOpen = false;
         }
      }

      private void drawSelectionPanel(GuiGraphics graphics, Font font, double mouseX, double mouseY) {
         drawPanel(graphics, 3, 168, 197, 209);
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (selected == null) {
            Component line = (Component)(this.status != null ? this.status : Component.translatable("jei.gtceu.multiblock.click_block"));
            drawScaledCenteredTrimmed(graphics, font, line, 100, 181, 184, 0.78F, -4667963);
            drawScaledCenteredTrimmed(graphics, font, Component.translatable("jei.gtceu.multiblock.selection_hint"), 100, 195, 184, 0.72F, -8088685);
         } else {
            boolean alternatives = selected.candidates().size() > 1;
            drawButton(graphics, font, 5, 171, 20, 188, Component.literal("<"), isInside(mouseX, mouseY, 5, 171, 20, 188), alternatives);
            drawButton(graphics, font, 149, 171, 164, 188, Component.literal(">"), isInside(mouseX, mouseY, 149, 171, 164, 188), alternatives);
            drawButton(
               graphics,
               font,
               167,
               171,
               195,
               188,
               Component.translatable("jei.gtceu.multiblock.recipe"),
               isInside(mouseX, mouseY, 167, 171, 195, 188),
               !selected.current().getBlockState().isAir()
            );
            ItemStack stack = selected.displayStack();
            graphics.renderItem(stack, 24, 171);
            drawTrimmed(graphics, font, selected.name(), 43, 174, 103, -855310);
            graphics.fill(24, 194, 30, 200, selected.role().color());
            boolean showReplaceable = selected.replaceable() && selected.role() != StructureSemantics.CellRole.REPLACEABLE;
            drawScaledTrimmed(
               graphics, font, Component.translatable(selected.role().translationKey()), 33, 193, showReplaceable ? 42 : 75, 0.78F, selected.role().color()
            );
            if (showReplaceable) {
               drawScaledTrimmed(
                  graphics,
                  font,
                  Component.literal("+").append(Component.translatable(StructureSemantics.CellRole.REPLACEABLE.translationKey())),
                  78,
                  193,
                  31,
                  0.78F,
                  StructureSemantics.CellRole.REPLACEABLE.color()
               );
            }

            Component candidateSummary = selected.consistency() != null && selected.consistency().members() > 1
               ? Component.translatable(
                  "jei.gtceu.multiblock.consistency.summary", new Object[]{selected.consistency().members(), selected.candidates().size()}
               )
               : Component.translatable("jei.gtceu.multiblock.candidates", new Object[]{Math.max(1, selected.candidates().size())});
            drawScaledTrimmed(graphics, font, candidateSummary, 112, 193, 81, 0.78F, -6444374);
         }
      }

      private void drawMaterialPanel(GuiGraphics graphics, Font font, double mouseX, double mouseY) {
         drawPanel(graphics, 3, 211, 197, 259);
         this.clampMaterialPage();
         List<GTMultiblockStructureJeiPlugin.MaterialEntry> materials = this.model.structure().materials();
         int pages = this.materialPageCount();
         int start = Math.min(this.materialPage * 8, materials.size());
         int end = Math.min(start + 8, materials.size());
         boolean paged = pages > 1;
         drawButton(graphics, font, 5, 213, 19, 227, Component.literal("‹"), isInside(mouseX, mouseY, 5, 213, 19, 227), paged);
         drawButton(graphics, font, 181, 213, 195, 227, Component.literal("›"), isInside(mouseX, mouseY, 181, 213, 195, 227), paged);
         Component summary = Component.translatable(
            "jei.gtceu.multiblock.materials_page", new Object[]{materials.isEmpty() ? 0 : start + 1, end, materials.size(), this.materialPage + 1, pages}
         );
         drawScaledCenteredTrimmed(graphics, font, summary, 100, 215, 156, 0.78F, -986896);
         drawRoleLegend(graphics, font, StructureSemantics.CellRole.REQUIRED, 20, 230, 49);
         drawRoleLegend(graphics, font, StructureSemantics.CellRole.OPTIONAL, 76, 230, 45);
         drawRoleLegend(graphics, font, StructureSemantics.CellRole.REPLACEABLE, 128, 230, 47);

         for (int slot = 0; slot < 8; slot++) {
            int materialIndex = start + slot;
            if (materialIndex >= end) {
               break;
            }

            int x = 5 + slot * 21;
            drawMaterialSlot(graphics, font, materials.get(materialIndex), x, 239, isInside(mouseX, mouseY, x, 239, x + 18, 257));
         }

         this.drawControllerSlot(graphics, 178, 239, this.isControllerSlot(mouseX, mouseY));
      }

      private static void drawRoleLegend(GuiGraphics graphics, Font font, StructureSemantics.CellRole role, int x, int y, int maximumWidth) {
         graphics.fill(x, y + 1, x + 5, y + 6, role.color());
         drawScaledTrimmed(graphics, font, Component.translatable(role.translationKey()), x + 7, y, maximumWidth - 7, 0.72F, role.color());
      }

      private static void drawMaterialSlot(
         GuiGraphics graphics, Font font, GTMultiblockStructureJeiPlugin.MaterialEntry material, int x, int y, boolean hovered
      ) {
         int roleColor = material.roles().contains(StructureSemantics.CellRole.REQUIRED)
            ? StructureSemantics.CellRole.REQUIRED.color()
            : (
               material.roles().contains(StructureSemantics.CellRole.OPTIONAL)
                  ? StructureSemantics.CellRole.OPTIONAL.color()
                  : StructureSemantics.CellRole.REPLACEABLE.color()
            );
         graphics.fill(x, y, x + 18, y + 18, hovered ? -724744 : roleColor);
         graphics.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -15262434);
         graphics.renderItem(material.stack().copyWithCount(1), x + 1, y + 1);
         String exactCount = Integer.toString(material.stack().getCount());
         float countScale = Math.max(0.42F, Math.min(0.7F, 15.0F / (float)Math.max(1, font.width(exactCount))));
         drawScaledRightAligned(graphics, font, exactCount, x + 18 - 1, y + 18 - 7, 15, countScale, -1);
      }

      private void drawControllerSlot(GuiGraphics graphics, int x, int y, boolean hovered) {
         graphics.fill(x, y, x + 18, y + 18, hovered ? -7780 : -3830529);
         graphics.fill(x + 1, y + 1, x + 18 - 1, y + 18 - 1, -15262434);
         graphics.renderItem(this.recipe.controller().copyWithCount(1), x + 1, y + 1);
      }

      public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
         tooltip.addAll(this.tooltipAt(mouseX, mouseY));
      }

      @Override
      public List<Component> tooltipAt(double mouseX, double mouseY) {
         List<Component> tooltip = new ArrayList<>();
         StructureSemantics.Candidate popupCandidate = this.candidateAt(mouseX, mouseY);
         if (popupCandidate != null) {
            tooltip.add(popupCandidate.displayStack().getHoverName());
            return tooltip;
         } else {
            GTMultiblockStructureJeiPlugin.MaterialEntry material = this.materialAt(mouseX, mouseY);
            if (material == null) {
               if (this.isControllerSlot(mouseX, mouseY)) {
                  tooltip.add(this.recipe.controller().getHoverName());
                  return tooltip;
               } else {
                  GTMultiblockStructureJeiPlugin.RenderPart hovered = this.findPartAt(mouseX, mouseY);
                  if (hovered != null) {
                     this.addHoverTooltip(tooltip, hovered);
                     return tooltip;
                  } else if (isInside(mouseX, mouseY, 20, 27, 80, 42)) {
                     tooltip.add(
                        this.layer < 0
                           ? Component.translatable("jei.gtceu.multiblock.all_layers")
                           : Component.translatable("jei.gtceu.multiblock.layer", new Object[]{this.layer + 1, this.model.structure().height()})
                     );
                     return tooltip;
                  } else if (isInside(mouseX, mouseY, 97, 27, 196, 44)) {
                     GTMultiblockStructureJeiPlugin.StructureData data = this.model.structure();
                     tooltip.add(Component.translatable("jei.gtceu.multiblock.final_dimensions", new Object[]{data.width(), data.height(), data.depth()}));
                     if (data.strictAirCells() > 0) {
                        tooltip.add(Component.translatable("jei.gtceu.multiblock.clearance.required", new Object[]{data.strictAirCells()}).withColor(9426920));
                     }

                     return tooltip;
                  } else if (isInside(mouseX, mouseY, 5, 48, 182, 63)) {
                     Axis tooltipAxis = this.hoveredDimensionAxis(mouseX, mouseY);
                     boolean adjustable = !this.model.dimensionsForAxis(tooltipAxis).isEmpty();
                     tooltip.add(
                        adjustable
                           ? Component.translatable(
                              "jei.gtceu.multiblock.dimension.axis",
                              new Object[]{
                                 axisName(tooltipAxis),
                                 this.model.axisValue(tooltipAxis),
                                 this.model.axisMinimum(tooltipAxis),
                                 this.model.axisMaximum(tooltipAxis)
                              }
                           )
                           : Component.translatable(
                              "jei.gtceu.multiblock.dimension.axis_fixed", new Object[]{axisName(tooltipAxis), this.model.axisValue(tooltipAxis)}
                           )
                     );
                     return tooltip;
                  } else {
                     if (isInside(mouseX, mouseY, 21, 169, 165, 208)) {
                        this.addSelectionTooltip(tooltip);
                     }

                     return tooltip;
                  }
               }
            } else {
               tooltip.add(material.stack().getHoverName());
               tooltip.add(Component.translatable("jei.gtceu.multiblock.material_count", new Object[]{material.stack().getCount()}));

               for (StructureSemantics.CellRole role : material.roles()) {
                  tooltip.add(Component.translatable(role.translationKey()).withColor(role.color() & 16777215));
               }

               return tooltip;
            }
         }
      }

      private void addHoverTooltip(List<Component> tooltip, GTMultiblockStructureJeiPlugin.RenderPart hovered) {
         ItemStack stack = new ItemStack(hovered.state().getBlock());
         List<Component> lines = new ArrayList<>();
         lines.add(stack.getHoverName());
         lines.add(Component.translatable(hovered.role().translationKey()).withColor(hovered.role().color() & 16777215));
         if (hovered.replaceable() && hovered.role() != StructureSemantics.CellRole.REPLACEABLE) {
            lines.add(
               Component.translatable(StructureSemantics.CellRole.REPLACEABLE.translationKey())
                  .withColor(StructureSemantics.CellRole.REPLACEABLE.color() & 16777215)
            );
         }

         appendConsistencyInfo(lines, this.model.consistencyFor(hovered));
         List<StructureSemantics.Candidate> candidates = this.model.candidatesFor(hovered);
         lines.add(Component.translatable("jei.gtceu.multiblock.candidates", new Object[]{Math.max(1, candidates.size())}));
         BasePredicate active = candidates.stream()
            .filter(candidate -> candidate.blockInfo().getBlockState().equals(hovered.state()))
            .map(StructureSemantics.Candidate::basePredicate)
            .findFirst()
            .orElse(null);
         if (active != null) {
            lines.addAll(active.getTooltips(hovered.predicate()));
         }

         tooltip.addAll(lines);
      }

      private void addSelectionTooltip(List<Component> tooltip) {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (selected != null) {
            List<Component> lines = new ArrayList<>();
            lines.add(selected.name());
            lines.add(Component.translatable(selected.role().translationKey()).withColor(selected.role().color() & 16777215));
            if (selected.replaceable() && selected.role() != StructureSemantics.CellRole.REPLACEABLE) {
               lines.add(
                  Component.translatable(StructureSemantics.CellRole.REPLACEABLE.translationKey())
                     .withColor(StructureSemantics.CellRole.REPLACEABLE.color() & 16777215)
               );
            }

            appendConsistencyInfo(lines, selected.consistency());
            BasePredicate active = selected.currentBasePredicate();
            if (active != null) {
               lines.addAll(active.getTooltips(selected.predicate()));
            }

            tooltip.addAll(lines);
         }
      }

      private static void appendConsistencyInfo(List<Component> lines, GTMultiblockStructureJeiPlugin.ConsistencySelection consistency) {
         if (consistency != null && consistency.members() >= 2) {
            lines.add(Component.translatable(consistency.kind().translationKey()).withColor(12946687));
            lines.add(Component.translatable("jei.gtceu.multiblock.consistency.members", new Object[]{consistency.members()}).withColor(13084648));
         }
      }

      public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
         Key key = input.getKey();
         ItemStack hoveredStack = this.hoveredStackAt(mouseX, mouseY);
         IJeiRuntime jei = GTMultiblockStructureJeiPlugin.runtime;
         if (jei != null && !hoveredStack.isEmpty()) {
            if (input.is(jei.getKeyMappings().getShowRecipe())) {
               if (!input.isSimulate()) {
                  showRecipes(hoveredStack);
               }

               return true;
            }

            if (input.is(jei.getKeyMappings().getShowUses())) {
               if (!input.isSimulate()) {
                  showUses(hoveredStack);
               }

               return true;
            }

            if (key.getType() != Type.MOUSE && key.getValue() == 65) {
               if (!input.isSimulate()) {
                  toggleFavorite(hoveredStack);
               }

               return true;
            }
         }

         if (key.getType() != Type.MOUSE) {
            return false;
         } else {
            int button = key.getValue();
            if (button != 0 && button != 1) {
               return false;
            } else if (button == 0) {
               return input.isSimulate() ? this.beginLeftGesture(mouseX, mouseY) : this.completeLeftGesture(mouseX, mouseY);
            } else {
               GTMultiblockStructureJeiPlugin.MaterialEntry material = this.materialAt(mouseX, mouseY);
               if (material != null) {
                  if (!input.isSimulate()) {
                     showUses(material.stack());
                  }

                  return true;
               } else if (this.isControllerSlot(mouseX, mouseY)) {
                  if (!input.isSimulate()) {
                     showUses(this.recipe.controller());
                  }

                  return true;
               } else if (isInside(mouseX, mouseY, 43, 171, 195, 188) && this.model.selected() != null) {
                  if (!input.isSimulate()) {
                     this.showSelectedUses();
                  }

                  return true;
               } else {
                  return false;
               }
            }
         }
      }

      @Override
      public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
         if (button == 0) {
            return this.beginLeftGesture(mouseX, mouseY);
         } else {
            if (button == 1) {
               ItemStack hoveredStack = this.hoveredStackAt(mouseX, mouseY);
               if (!hoveredStack.isEmpty()) {
                  showUses(hoveredStack);
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
         return button == 0 && this.leftGesture.isActive() ? this.completeLeftGesture(mouseX, mouseY) : false;
      }

      @Override
      public boolean handleKeyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
         ItemStack hoveredStack = this.hoveredStackAt(mouseX, mouseY);
         if (hoveredStack.isEmpty()) {
            return false;
         } else if (keyCode == 82) {
            showRecipes(hoveredStack);
            return true;
         } else if (keyCode == 85) {
            showUses(hoveredStack);
            return true;
         } else if (keyCode == 65) {
            toggleFavorite(hoveredStack);
            return true;
         } else {
            return false;
         }
      }

      private boolean beginLeftGesture(double mouseX, double mouseY) {
         this.clearLeftGesture();
         Runnable action = this.leftClickAction(mouseX, mouseY);
         boolean insideModel = !this.candidatePopupOpen && isInside(mouseX, mouseY, 5, 68, 195, 152);
         if (!this.leftGesture.begin(mouseX, mouseY, insideModel, action != null)) {
            return false;
         } else {
            this.pendingLeftClick = action;
            return true;
         }
      }

      private boolean completeLeftGesture(double mouseX, double mouseY) {
         if (!this.leftGesture.isActive()) {
            Runnable immediate = this.leftClickAction(mouseX, mouseY);
            if (immediate != null) {
               immediate.run();
               return true;
            } else {
               return isInside(mouseX, mouseY, 5, 68, 195, 152) && this.findPartAt(mouseX, mouseY) == null;
            }
         } else {
            Runnable completedClick = this.pendingLeftClick;
            MultiblockPreviewPointerGesture.Finish finish = this.leftGesture.complete(mouseX, mouseY);
            this.pendingLeftClick = null;
            this.applyRotation(finish.drag());
            if (finish.completion() == MultiblockPreviewPointerGesture.Completion.CLICK && completedClick != null) {
               completedClick.run();
            }

            return true;
         }
      }

      private void clearLeftGesture() {
         this.leftGesture.clear();
         this.pendingLeftClick = null;
      }

      private Runnable leftClickAction(double mouseX, double mouseY) {
         if (this.candidatePopupOpen) {
            if (isInside(mouseX, mouseY, 161, 30, 176, 45)) {
               return () -> this.candidatePopupOpen = false;
            } else {
               int candidateIndex = this.candidateIndexAt(mouseX, mouseY);
               if (candidateIndex >= 0) {
                  return () -> this.chooseCandidate(candidateIndex);
               } else {
                  return isInside(mouseX, mouseY, 21, 27, 179, 202) ? () -> {
                  } : () -> this.candidatePopupOpen = false;
               }
            }
         } else if (isInside(mouseX, mouseY, 5, 4, 20, 21)) {
            return () -> {
               this.yaw = 45.0F;
               this.pitch = 25.0F;
               this.zoom = 1.0F;
            };
         } else if (isInside(mouseX, mouseY, 5, 27, 19, 42)) {
            return this::previousLayer;
         } else if (isInside(mouseX, mouseY, 81, 27, 95, 42)) {
            return this::nextLayer;
         } else if (isInside(mouseX, mouseY, 20, 27, 80, 42)) {
            return () -> this.layer = -1;
         } else if (isInside(mouseX, mouseY, 5, 48, 23, 63)) {
            return () -> this.selectDimensionAxis(Axis.X);
         } else if (isInside(mouseX, mouseY, 25, 48, 43, 63)) {
            return () -> this.selectDimensionAxis(Axis.Y);
         } else if (isInside(mouseX, mouseY, 45, 48, 63, 63)) {
            return () -> this.selectDimensionAxis(Axis.Z);
         } else if (this.model.canAdjustAxis(this.selectedDimensionAxis, -1) && isInside(mouseX, mouseY, 68, 48, 84, 63)) {
            return () -> this.adjustCurrentDimension(-1);
         } else if (this.model.canAdjustAxis(this.selectedDimensionAxis, 1) && isInside(mouseX, mouseY, 166, 48, 182, 63)) {
            return () -> this.adjustCurrentDimension(1);
         } else if (isInside(mouseX, mouseY, 5, 213, 19, 227) && this.materialPageCount() > 1) {
            return () -> this.cycleMaterialPage(-1);
         } else if (isInside(mouseX, mouseY, 181, 213, 195, 227) && this.materialPageCount() > 1) {
            return () -> this.cycleMaterialPage(1);
         } else {
            GTMultiblockStructureJeiPlugin.MaterialEntry material = this.materialAt(mouseX, mouseY);
            if (material != null) {
               return () -> showRecipes(material.stack());
            } else if (this.isControllerSlot(mouseX, mouseY)) {
               return () -> showRecipes(this.recipe.controller());
            } else {
               GTMultiblockStructureJeiPlugin.RenderPart hovered = this.findPartAt(mouseX, mouseY);
               if (hovered != null) {
                  return () -> {
                     this.model.select(hovered);
                     this.status = null;
                     this.openCandidatePopup();
                  };
               } else {
                  GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
                  if (selected != null && selected.candidates().size() > 1 && isInside(mouseX, mouseY, 5, 171, 20, 188)) {
                     return () -> this.setCandidate(-1);
                  } else if (selected != null && selected.candidates().size() > 1 && isInside(mouseX, mouseY, 149, 171, 164, 188)) {
                     return () -> this.setCandidate(1);
                  } else if (selected != null && selected.candidates().size() > 1 && isInside(mouseX, mouseY, 43, 171, 164, 188)) {
                     return this::openCandidatePopup;
                  } else {
                     return selected != null && isInside(mouseX, mouseY, 167, 171, 195, 188) ? this::showSelectedRecipes : null;
                  }
               }
            }
         }
      }

      private void selectDimensionAxis(Axis axis) {
         this.selectedDimensionAxis = axis;
         this.status = Component.translatable("jei.gtceu.multiblock.dimension.axis_selected", new Object[]{axisName(axis)});
      }

      private void adjustCurrentDimension(int delta) {
         boolean changed = this.model.adjustAxis(this.selectedDimensionAxis, delta);
         if (changed) {
            this.clampLayer();
            this.clampMaterialPage();
         }

         this.status = changed
            ? Component.translatable(
               "jei.gtceu.multiblock.dimension.axis_changed",
               new Object[]{axisName(this.selectedDimensionAxis), this.model.axisValue(this.selectedDimensionAxis)}
            )
            : Component.translatable("jei.gtceu.multiblock.dimension.rejected");
      }

      private boolean setCandidate(int delta) {
         GTMultiblockStructureJeiPlugin.SelectedCell before = this.model.selected();
         boolean grouped = before != null && before.consistency() != null && before.consistency().members() > 1;
         boolean changed = this.model.cycleCandidate(delta);
         this.status = changed
            ? Component.translatable(grouped ? "jei.gtceu.multiblock.consistency.changed" : "jei.gtceu.multiblock.candidate_changed")
            : Component.translatable("jei.gtceu.multiblock.candidate_rejected");
         this.clampMaterialPage();
         return changed;
      }

      private ItemStack selectedStack() {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         return selected != null && !selected.current().getBlockState().isAir() ? selected.current().getItemStackForm() : ItemStack.EMPTY;
      }

      private void showSelectedRecipes() {
         ItemStack stack = this.selectedStack();
         if (!stack.isEmpty()) {
            showRecipes(stack);
         }
      }

      private void showSelectedUses() {
         ItemStack stack = this.selectedStack();
         if (!stack.isEmpty()) {
            showUses(stack);
         }
      }

      private static void openStackInJei(ItemStack stack, RecipeIngredientRole role) {
         IJeiRuntime jei = GTMultiblockStructureJeiPlugin.runtime;
         if (jei != null && !stack.isEmpty()) {
            jei.getRecipesGui().show(jei.getJeiHelpers().getFocusFactory().createFocus(role, VanillaTypes.ITEM_STACK, stack));
         }
      }

      private ItemStack hoveredStackAt(double mouseX, double mouseY) {
         StructureSemantics.Candidate popupCandidate = this.candidateAt(mouseX, mouseY);
         if (popupCandidate != null) {
            return popupCandidate.displayStack().copyWithCount(1);
         } else {
            GTMultiblockStructureJeiPlugin.MaterialEntry material = this.materialAt(mouseX, mouseY);
            if (material != null) {
               return material.stack().copyWithCount(1);
            } else if (this.isControllerSlot(mouseX, mouseY)) {
               return this.recipe.controller().copyWithCount(1);
            } else {
               GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
               if (selected != null && isInside(mouseX, mouseY, 24, 171, 164, 188)) {
                  return selected.displayStack().copyWithCount(1);
               } else {
                  GTMultiblockStructureJeiPlugin.RenderPart hovered = this.findPartAt(mouseX, mouseY);
                  if (hovered != null) {
                     ItemStack stack = BlockInfo.fromBlockState(hovered.state()).getItemStackForm();
                     return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
                  } else {
                     return ItemStack.EMPTY;
                  }
               }
            }
         }
      }

      private static boolean isEmiScreen() {
         Screen screen = Minecraft.getInstance().screen;
         return screen != null && screen.getClass().getName().startsWith("dev.emi.emi.");
      }

      private static void showRecipes(ItemStack stack) {
         if (isEmiScreen()) {
            openStackInEmi(stack, "displayRecipes");
         } else {
            openStackInJei(stack, RecipeIngredientRole.OUTPUT);
         }
      }

      private static void showUses(ItemStack stack) {
         if (isEmiScreen()) {
            openStackInEmi(stack, "displayUses");
         } else {
            openStackInJei(stack, RecipeIngredientRole.INPUT);
         }
      }

      private static void toggleFavorite(ItemStack stack) {
         if (isEmiScreen()) {
            toggleEmiFavorite(stack);
         } else {
            toggleJeiBookmark(stack);
         }
      }

      private static void toggleEmiFavorite(ItemStack stack) {
         try {
            Class<?> ingredientType = Class.forName("dev.emi.emi.api.stack.EmiIngredient");
            Object ingredient = createEmiIngredient(stack);
            Class<?> favorites = Class.forName("dev.emi.emi.runtime.EmiFavorites");
            Method remove = favorites.getMethod("removeFavorite", ingredientType);
            boolean removed = (Boolean)remove.invoke(null, ingredient);
            if (!removed) {
               favorites.getMethod("addFavorite", ingredientType).invoke(null, ingredient);
            }
         } catch (LinkageError | ReflectiveOperationException var6) {
            GTMultiblockJei.LOGGER.warn("Could not update the pinned EMI favorite list", var6);
         }
      }

      private static void openStackInEmi(ItemStack stack, String methodName) {
         try {
            Class<?> ingredientType = Class.forName("dev.emi.emi.api.stack.EmiIngredient");
            Object ingredient = createEmiIngredient(stack);
            Class<?> emiApi = Class.forName("dev.emi.emi.api.EmiApi");
            emiApi.getMethod(methodName, ingredientType).invoke(null, ingredient);
         } catch (LinkageError | ReflectiveOperationException var5) {
            GTMultiblockJei.LOGGER.warn("Could not open the EMI recipe view", var5);
         }
      }

      private static Object createEmiIngredient(ItemStack stack) throws ReflectiveOperationException {
         Class<?> emiStack = Class.forName("dev.emi.emi.api.stack.EmiStack");
         return emiStack.getMethod("of", ItemStack.class).invoke(null, stack.copyWithCount(1));
      }

      private static void toggleJeiBookmark(ItemStack stack) {
         IJeiRuntime jei = GTMultiblockStructureJeiPlugin.runtime;
         if (jei != null) {
            Optional<ITypedIngredient<ItemStack>> typed = jei.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack.copyWithCount(1));
            if (!typed.isEmpty()) {
               try {
                  Object overlay = jei.getBookmarkOverlay();
                  Field bookmarkListField = findField(overlay.getClass(), "bookmarkList");
                  Object bookmarkList = bookmarkListField.get(overlay);
                  Field bookmarkFactoryField = findField(bookmarkList.getClass(), "bookmarkFactory");
                  Object bookmarkFactory = bookmarkFactoryField.get(bookmarkList);
                  Method create = findSingleArgumentMethod(bookmarkFactory.getClass(), "create");
                  Object bookmark = create.invoke(bookmarkFactory, typed.get());
                  Method toggle = findSingleArgumentMethod(bookmarkList.getClass(), "toggleBookmark");
                  toggle.invoke(bookmarkList, bookmark);
               } catch (ReflectiveOperationException var11) {
                  GTMultiblockJei.LOGGER.warn("Could not update the pinned JEI bookmark list", var11);
               }
            }
         }
      }

      private static Field findField(Class<?> owner, String name) throws ReflectiveOperationException {
         for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
               Field field = current.getDeclaredField(name);
               field.setAccessible(true);
               return field;
            } catch (NoSuchFieldException var4) {
            }
         }

         throw new NoSuchFieldException(owner.getName() + "." + name);
      }

      private static Method findSingleArgumentMethod(Class<?> owner, String name) throws NoSuchMethodException {
         for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
               return method;
            }
         }

         throw new NoSuchMethodException(owner.getName() + "." + name);
      }

      @Override
      public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
         if (scrollDeltaY == 0.0) {
            return false;
         } else if (this.candidatePopupOpen && isInside(mouseX, mouseY, 21, 27, 179, 202)) {
            int pages = this.candidatePopupPageCount();
            if (pages > 1) {
               this.candidatePopupPage = Math.floorMod(this.candidatePopupPage + (scrollDeltaY > 0.0 ? -1 : 1), pages);
            }

            return true;
         } else if (isInside(mouseX, mouseY, 5, 68, 195, 152)) {
            this.zoom = Mth.clamp((float)((double)this.zoom * Math.pow(1.12, scrollDeltaY)), 0.25F, 4.0F);
            return true;
         } else {
            return isInside(mouseX, mouseY, 0, 0, 200, 260);
         }
      }

      public boolean handleMouseDragged(double mouseX, double mouseY, Key key, double dragX, double dragY) {
         if (key.getType() != Type.MOUSE) {
            return false;
         } else if (key.getValue() == 0 && this.leftGesture.isActive()) {
            this.applyRotation(this.leftGesture.drag(mouseX, mouseY));
            return true;
         } else {
            return false;
         }
      }

      private void applyRotation(MultiblockPreviewPointerGesture.Drag drag) {
         if (drag.rotate()) {
            this.rotateByDrag(drag.deltaX(), drag.deltaY());
         }
      }

      @Override
      public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
         return this.handleMouseDragged(mouseX, mouseY, Type.MOUSE.getOrCreate(button), dragX, dragY);
      }

      private void rotateByDrag(double dragX, double dragY) {
         this.yaw = (float)(((double)this.yaw + dragX * 1.35) % 360.0);
         if (this.yaw < 0.0F) {
            this.yaw += 360.0F;
         }

         this.pitch = Mth.clamp((float)((double)this.pitch + dragY * 1.1), -85.0F, 85.0F);
      }

      private void previousLayer() {
         int height = this.model.structure().height();
         this.layer = this.layer < 0 ? height - 1 : (this.layer == 0 ? -1 : this.layer - 1);
      }

      private void nextLayer() {
         int height = this.model.structure().height();
         this.layer = this.layer < 0 ? 0 : (this.layer + 1 >= height ? -1 : this.layer + 1);
      }

      private void clampLayer() {
         if (this.layer >= this.model.structure().height()) {
            this.layer = -1;
         }
      }

      private int materialPageCount() {
         return Math.max(1, (this.model.structure().materials().size() + 8 - 1) / 8);
      }

      private void clampMaterialPage() {
         this.materialPage = Mth.clamp(this.materialPage, 0, this.materialPageCount() - 1);
      }

      private void cycleMaterialPage(int delta) {
         int pages = this.materialPageCount();
         if (pages < 2) {
            this.materialPage = 0;
         } else {
            this.materialPage = Math.floorMod(this.materialPage + delta, pages);
            this.status = Component.translatable("jei.gtceu.multiblock.material_page_changed", new Object[]{this.materialPage + 1, pages});
         }
      }

      private GTMultiblockStructureJeiPlugin.MaterialEntry materialAt(double mouseX, double mouseY) {
         if (!isInside(mouseX, mouseY, 5, 239, 173, 257)) {
            return null;
         } else {
            int relativeX = (int)Math.floor(mouseX) - 5;
            int slot = relativeX / 21;
            if (slot >= 0 && slot < 8 && relativeX % 21 < 18) {
               this.clampMaterialPage();
               int materialIndex = this.materialPage * 8 + slot;
               List<GTMultiblockStructureJeiPlugin.MaterialEntry> materials = this.model.structure().materials();
               return materialIndex >= 0 && materialIndex < materials.size() ? materials.get(materialIndex) : null;
            } else {
               return null;
            }
         }
      }

      private void openCandidatePopup() {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (selected != null && selected.candidates().size() >= 2) {
            int currentIndex = this.currentCandidateIndex(selected);
            this.candidatePopupPage = Math.max(0, currentIndex / 8);
            this.candidatePopupOpen = true;
         } else {
            this.candidatePopupOpen = false;
            this.candidatePopupPage = 0;
         }
      }

      private int candidatePopupPageCount() {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         int count = selected == null ? 0 : selected.candidates().size();
         return Math.max(1, (count + 8 - 1) / 8);
      }

      private void clampCandidatePopupPage() {
         this.candidatePopupPage = Mth.clamp(this.candidatePopupPage, 0, this.candidatePopupPageCount() - 1);
      }

      private StructureSemantics.Candidate candidateAt(double mouseX, double mouseY) {
         int index = this.candidateIndexAt(mouseX, mouseY);
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         return selected != null && index >= 0 && index < selected.candidates().size() ? selected.candidates().get(index) : null;
      }

      private int candidateIndexAt(double mouseX, double mouseY) {
         if (this.candidatePopupOpen && isInside(mouseX, mouseY, 24, 47, 176, 201)) {
            int row = ((int)Math.floor(mouseY) - 47) / 19;
            if (row >= 0 && row < 8) {
               int index = this.candidatePopupPage * 8 + row;
               GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
               return selected != null && index < selected.candidates().size() ? index : -1;
            } else {
               return -1;
            }
         } else {
            return -1;
         }
      }

      private int currentCandidateIndex(GTMultiblockStructureJeiPlugin.SelectedCell selected) {
         ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(selected.current().getBlockState().getBlock());

         for (int index = 0; index < selected.candidates().size(); index++) {
            StructureSemantics.Candidate candidate = selected.candidates().get(index);
            if (candidate.blockInfo().equals(selected.current()) || candidate.blockId().equals(currentBlockId)) {
               return index;
            }
         }

         return 0;
      }

      private void chooseCandidate(int targetIndex) {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (selected != null && targetIndex >= 0 && targetIndex < selected.candidates().size()) {
            int currentIndex = this.currentCandidateIndex(selected);
            this.setCandidate(targetIndex - currentIndex);
            this.candidatePopupOpen = false;
         }
      }

      private boolean isControllerSlot(double mouseX, double mouseY) {
         return isInside(mouseX, mouseY, 178, 239, 196, 257);
      }

      private void drawStructure(GuiGraphics graphics) {
         try {
            this.drawClippedStructure(graphics);
         } catch (RuntimeException var3) {
            this.renderDisabled = true;
            if (!this.renderFailureLogged) {
               this.renderFailureLogged = true;
               GTMultiblockJei.LOGGER.error("Enhanced multiblock render failed for {}", this.recipe.id(), var3);
            }
         }
      }

      private void drawClippedStructure(GuiGraphics graphics) {
         Minecraft minecraft = Minecraft.getInstance();
         PoseStack pose = graphics.pose();
         GTMultiblockStructureJeiPlugin.StructurePreviewWidget.RenderStateSnapshot state = GTMultiblockStructureJeiPlugin.StructurePreviewWidget.RenderStateSnapshot.capture();
         GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ViewportClip clip = GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ViewportClip.fromCurrentPose(
            graphics
         );
         boolean scissorPushed = false;
         boolean structurePosePushed = false;

         try {
            graphics.flush();
            graphics.enableScissor(clip.left(), clip.top(), clip.right(), clip.bottom());
            scissorPushed = true;
            state.intersectWithSavedScissor();
            state.applyFor3D();
            pose.pushPose();
            structurePosePushed = true;
            GTMultiblockStructureJeiPlugin.StructureData data = this.model.structure();
            float scale = this.renderScale(data);
            pose.translate(100.0F, 110.0F, 180.0F);
            pose.scale(scale, -scale, scale);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(this.pitch));
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(this.yaw));
            pose.translate(
               (float)(-data.width()) / 2.0F, -(this.layer < 0 ? (float)data.height() / 2.0F : (float)this.layer + 0.5F), (float)(-data.depth()) / 2.0F
            );
            BlockRenderDispatcher renderer = minecraft.getBlockRenderer();
            BufferSource buffers = graphics.bufferSource();

            for (GTMultiblockStructureJeiPlugin.RenderPart part : this.visibleParts(data)) {
               pose.pushPose();

               try {
                  pose.translate((float)part.x(), (float)part.y(), (float)part.z());
                  renderer.renderSingleBlock(part.state(), pose, buffers, 15728880, OverlayTexture.NO_OVERLAY);
               } finally {
                  pose.popPose();
               }
            }

            buffers.endBatch();
            pose.popPose();
            structurePosePushed = false;
            state.restoreNonScissor();
            this.drawSelectedOutline(graphics);
            graphics.flush();
         } finally {
            if (structurePosePushed) {
               pose.popPose();
            }

            try {
               graphics.flush();
            } finally {
               try {
                  if (scissorPushed) {
                     graphics.disableScissor();
                  }
               } finally {
                  state.restoreAll();
               }
            }
         }
      }

      private void drawSelectedOutline(GuiGraphics graphics) {
         GTMultiblockStructureJeiPlugin.SelectedCell selected = this.model.selected();
         if (selected != null) {
            GTMultiblockStructureJeiPlugin.RenderPart selectedPart = this.model
               .structure()
               .parts()
               .stream()
               .filter(part -> part.schemaPosition().equals(selected.position()))
               .findFirst()
               .orElse(null);
            if (selectedPart != null && (this.layer < 0 || selectedPart.y() == this.layer)) {
               GTMultiblockStructureJeiPlugin.StructurePreviewWidget.Projected projected = this.project(selectedPart);
               int color = selected.role().color();
               outline(graphics, (int)projected.x() - 4, (int)projected.y() - 4, (int)projected.x() + 5, (int)projected.y() + 5, color);
            }
         }
      }

      private List<GTMultiblockStructureJeiPlugin.RenderPart> visibleParts(GTMultiblockStructureJeiPlugin.StructureData data) {
         return this.layer < 0 ? data.overviewParts() : data.parts().stream().filter(part -> part.y() == this.layer).toList();
      }

      private float renderScale(GTMultiblockStructureJeiPlugin.StructureData data) {
         float halfX = (float)data.width() / 2.0F;
         float halfY = this.layer < 0 ? (float)data.height() / 2.0F : 0.5F;
         float halfZ = (float)data.depth() / 2.0F;
         float yawRadians = this.yaw * (float) (Math.PI / 180.0);
         float pitchRadians = this.pitch * (float) (Math.PI / 180.0);
         float cosYaw = Mth.cos(yawRadians);
         float sinYaw = Mth.sin(yawRadians);
         float cosPitch = Mth.cos(pitchRadians);
         float sinPitch = Mth.sin(pitchRadians);
         float maximumX = 0.5F;
         float maximumY = 0.5F;

         for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
               for (int sz = -1; sz <= 1; sz += 2) {
                  float x = (float)sx * halfX;
                  float y = (float)sy * halfY;
                  float z = (float)sz * halfZ;
                  float rotatedX = cosYaw * x + sinYaw * z;
                  float rotatedZ = -sinYaw * x + cosYaw * z;
                  float rotatedY = cosPitch * y - sinPitch * rotatedZ;
                  maximumX = Math.max(maximumX, Math.abs(rotatedX));
                  maximumY = Math.max(maximumY, Math.abs(rotatedY));
               }
            }
         }

         float availableWidth = 182.0F;
         float availableHeight = 76.0F;
         float base = Math.min(10.0F, Math.min(availableWidth / (maximumX * 2.0F), availableHeight / (maximumY * 2.0F)));
         return base * this.zoom;
      }

      private GTMultiblockStructureJeiPlugin.RenderPart findPartAt(double mouseX, double mouseY) {
         if (!isInside(mouseX, mouseY, 5, 68, 195, 152)) {
            return null;
         } else {
            GTMultiblockStructureJeiPlugin.RenderPart result = null;
            double bestDistance = Double.MAX_VALUE;
            float bestDepth = -Float.MAX_VALUE;
            float scale = this.renderScale(this.model.structure());
            double radius = (double)Mth.clamp(scale * 0.62F, 4.5F, 12.0F);

            for (GTMultiblockStructureJeiPlugin.RenderPart part : this.visibleParts(this.model.structure())) {
               GTMultiblockStructureJeiPlugin.StructurePreviewWidget.Projected projected = this.project(part);
               double dx = mouseX - (double)projected.x();
               double dy = mouseY - (double)projected.y();
               double distance = dx * dx + dy * dy;
               if (!(distance > radius * radius)
                  && (distance + 0.5 < bestDistance || Math.abs(distance - bestDistance) <= 0.5 && projected.depth() > bestDepth)) {
                  bestDistance = distance;
                  bestDepth = projected.depth();
                  result = part;
               }
            }

            return result;
         }
      }

      private GTMultiblockStructureJeiPlugin.StructurePreviewWidget.Projected project(GTMultiblockStructureJeiPlugin.RenderPart part) {
         GTMultiblockStructureJeiPlugin.StructureData data = this.model.structure();
         float x = (float)part.x() + 0.5F - (float)data.width() / 2.0F;
         float y = (float)part.y() + 0.5F - (this.layer < 0 ? (float)data.height() / 2.0F : (float)this.layer + 0.5F);
         float z = (float)part.z() + 0.5F - (float)data.depth() / 2.0F;
         float yawRadians = this.yaw * (float) (Math.PI / 180.0);
         float pitchRadians = this.pitch * (float) (Math.PI / 180.0);
         float cosYaw = Mth.cos(yawRadians);
         float sinYaw = Mth.sin(yawRadians);
         float rotatedX = cosYaw * x + sinYaw * z;
         float rotatedZ = -sinYaw * x + cosYaw * z;
         float cosPitch = Mth.cos(pitchRadians);
         float sinPitch = Mth.sin(pitchRadians);
         float rotatedY = cosPitch * y - sinPitch * rotatedZ;
         float depth = sinPitch * y + cosPitch * rotatedZ;
         float scale = this.renderScale(data);
         return new GTMultiblockStructureJeiPlugin.StructurePreviewWidget.Projected(100.0F + rotatedX * scale, 110.0F - rotatedY * scale, depth);
      }

      private static void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom) {
         graphics.fill(left, top, right, bottom, -16314352);
         graphics.fill(left + 1, top + 1, right - 1, bottom - 1, -13617091);
      }

      private void drawAxisButton(GuiGraphics graphics, Font font, Axis axis, int left, int right, double mouseX, double mouseY) {
         boolean hovered = isInside(mouseX, mouseY, left, 48, right, 63);
         drawButton(graphics, font, left, 48, right, 63, axisName(axis), hovered, true);
         if (this.selectedDimensionAxis == axis) {
            outline(graphics, left, 48, right, 63, -3830529);
         }
      }

      private Axis hoveredDimensionAxis(double mouseX, double mouseY) {
         if (isInside(mouseX, mouseY, 5, 48, 23, 63)) {
            return Axis.X;
         } else if (isInside(mouseX, mouseY, 25, 48, 43, 63)) {
            return Axis.Y;
         } else {
            return isInside(mouseX, mouseY, 45, 48, 63, 63) ? Axis.Z : this.selectedDimensionAxis;
         }
      }

      private static Component axisName(Axis axis) {
         return Component.literal(switch (axis) {
            case X -> "X";
            case Y -> "Y";
            case Z -> "Z";
            default -> throw new MatchException(null, null);
         });
      }

      private static void drawButton(
         GuiGraphics graphics, Font font, int left, int top, int right, int bottom, Component label, boolean hovered, boolean enabled
      ) {
         int outer = enabled ? (hovered ? -6525815 : -15262434) : -14671840;
         int inner = enabled ? (hovered ? -10600593 : -13883355) : -13619152;
         int textColor = enabled ? -724744 : -8947849;
         graphics.fill(left, top, right, bottom, outer);
         graphics.fill(left + 2, top + 2, right - 2, bottom - 2, inner);
         drawCenteredTrimmed(graphics, font, label, (left + right) / 2, top + 4, Math.max(1, right - left - 6), textColor);
      }

      private static void drawScaledCenteredTrimmed(
         GuiGraphics graphics, Font font, Component text, int centerX, int y, int maximumWidth, float scale, int color
      ) {
         int logicalWidth = Math.max(1, (int)Math.floor((double)((float)maximumWidth / scale)));
         String trimmed = font.plainSubstrByWidth(text.getString(), logicalWidth);
         PoseStack pose = graphics.pose();
         pose.pushPose();

         try {
            pose.translate((float)centerX, (float)y, 0.0F);
            pose.scale(scale, scale, 1.0F);
            graphics.drawString(font, trimmed, -font.width(trimmed) / 2, 0, color, false);
         } finally {
            pose.popPose();
         }
      }

      private static void drawScaledTrimmed(GuiGraphics graphics, Font font, Component text, int x, int y, int maximumWidth, float scale, int color) {
         int logicalWidth = Math.max(1, (int)Math.floor((double)((float)maximumWidth / scale)));
         String trimmed = font.plainSubstrByWidth(text.getString(), logicalWidth);
         PoseStack pose = graphics.pose();
         pose.pushPose();

         try {
            pose.translate((float)x, (float)y, 0.0F);
            pose.scale(scale, scale, 1.0F);
            graphics.drawString(font, trimmed, 0, 0, color, false);
         } finally {
            pose.popPose();
         }
      }

      private static void drawScaledRightAligned(
         GuiGraphics graphics, Font font, String text, int right, int y, int maximumWidth, float preferredScale, int color
      ) {
         int textWidth = Math.max(1, font.width(text));
         float scale = Math.min(preferredScale, (float)maximumWidth / (float)textWidth);
         PoseStack pose = graphics.pose();
         pose.pushPose();

         try {
            pose.translate((float)right, (float)y, 200.0F);
            pose.scale(scale, scale, 1.0F);
            graphics.drawString(font, text, -textWidth, 0, color, true);
         } finally {
            pose.popPose();
         }
      }

      private static String compactNumber(int value) {
         long absolute = Math.abs((long)value);
         if (absolute < 10000L) {
            return Integer.toString(value);
         } else {
            long tenths = (absolute + 50L) / 100L;
            String sign = value < 0 ? "-" : "";
            return sign + tenths / 10L + "." + tenths % 10L + "k";
         }
      }

      private static void outline(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
         graphics.fill(left, top, right, top + 1, color);
         graphics.fill(left, bottom - 1, right, bottom, color);
         graphics.fill(left, top, left + 1, bottom, color);
         graphics.fill(right - 1, top, right, bottom, color);
      }

      private static void drawCenteredTrimmed(GuiGraphics graphics, Font font, Component text, int centerX, int y, int maximumWidth, int color) {
         String trimmed = font.plainSubstrByWidth(text.getString(), maximumWidth);
         graphics.drawCenteredString(font, trimmed, centerX, y, color);
      }

      private static void drawTrimmed(GuiGraphics graphics, Font font, Component text, int x, int y, int maximumWidth, int color) {
         graphics.drawString(font, font.plainSubstrByWidth(text.getString(), maximumWidth), x, y, color, false);
      }

      private static boolean isInside(double mouseX, double mouseY, int left, int top, int right, int bottom) {
         return mouseX >= (double)left && mouseX < (double)right && mouseY >= (double)top && mouseY < (double)bottom;
      }

      private static record Projected(float x, float y, float depth) {
      }

      private static record RenderStateSnapshot(
         boolean depthTest,
         boolean depthMask,
         int depthFunction,
         boolean blend,
         int blendSourceRgb,
         int blendDestinationRgb,
         int blendSourceAlpha,
         int blendDestinationAlpha,
         boolean cull,
         boolean scissor,
         int[] scissorBox,
         float[] shaderColor,
         GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ShaderLights shaderLights
      ) {
         private static GTMultiblockStructureJeiPlugin.StructurePreviewWidget.RenderStateSnapshot capture() {
            RenderSystem.assertOnRenderThread();
            int[] scissorBox = new int[4];
            GL11.glGetIntegerv(3088, scissorBox);
            return new GTMultiblockStructureJeiPlugin.StructurePreviewWidget.RenderStateSnapshot(
               GL11.glIsEnabled(2929),
               GL11.glGetBoolean(2930),
               GL11.glGetInteger(2932),
               GL11.glIsEnabled(3042),
               GL11.glGetInteger(32969),
               GL11.glGetInteger(32968),
               GL11.glGetInteger(32971),
               GL11.glGetInteger(32970),
               GL11.glIsEnabled(2884),
               GL11.glIsEnabled(3089),
               scissorBox,
               (float[])RenderSystem.getShaderColor().clone(),
               GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ShaderLights.capture()
            );
         }

         private void applyFor3D() {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.depthFunc(515);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Lighting.setupFor3DItems();
         }

         private void intersectWithSavedScissor() {
            if (this.scissor) {
               int[] local = new int[4];
               GL11.glGetIntegerv(3088, local);
               int left = Math.max(local[0], this.scissorBox[0]);
               int bottom = Math.max(local[1], this.scissorBox[1]);
               int right = Math.min(local[0] + local[2], this.scissorBox[0] + this.scissorBox[2]);
               int top = Math.min(local[1] + local[3], this.scissorBox[1] + this.scissorBox[3]);
               RenderSystem.enableScissor(left, bottom, Math.max(0, right - left), Math.max(0, top - bottom));
            }
         }

         private void restoreNonScissor() {
            RenderSystem.depthMask(this.depthMask);
            RenderSystem.depthFunc(this.depthFunction);
            if (this.depthTest) {
               RenderSystem.enableDepthTest();
            } else {
               RenderSystem.disableDepthTest();
            }

            RenderSystem.blendFuncSeparate(this.blendSourceRgb, this.blendDestinationRgb, this.blendSourceAlpha, this.blendDestinationAlpha);
            if (this.blend) {
               RenderSystem.enableBlend();
            } else {
               RenderSystem.disableBlend();
            }

            if (this.cull) {
               RenderSystem.enableCull();
            } else {
               RenderSystem.disableCull();
            }

            if (this.shaderLights != null) {
               this.shaderLights.restore();
            } else {
               Lighting.setupForFlatItems();
            }

            RenderSystem.setShaderColor(this.shaderColor[0], this.shaderColor[1], this.shaderColor[2], this.shaderColor[3]);
         }

         private void restoreAll() {
            this.restoreNonScissor();
            if (this.scissor) {
               RenderSystem.enableScissor(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
            } else {
               RenderSystem.disableScissor();
            }
         }
      }

      private static record ShaderLights(Vector3f first, Vector3f second) {
         private static GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ShaderLights capture() {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null && shader.LIGHT0_DIRECTION != null && shader.LIGHT1_DIRECTION != null) {
               Vector3f first = read(shader.LIGHT0_DIRECTION.getFloatBuffer());
               Vector3f second = read(shader.LIGHT1_DIRECTION.getFloatBuffer());
               return first != null && second != null ? new GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ShaderLights(first, second) : null;
            } else {
               return null;
            }
         }

         private static Vector3f read(FloatBuffer source) {
            return source != null && source.capacity() >= 3 ? new Vector3f(source.get(0), source.get(1), source.get(2)) : null;
         }

         private void restore() {
            RenderSystem.setShaderLights(this.first, this.second);
         }
      }

      private static record ViewportClip(int left, int top, int right, int bottom) {
         private static GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ViewportClip fromCurrentPose(GuiGraphics graphics) {
            Matrix4f transform = graphics.pose().last().pose();
            Vector3f point = new Vector3f();
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            int[] xs = new int[]{5, 195};
            int[] ys = new int[]{68, 152};

            for (int x : xs) {
               for (int y : ys) {
                  transform.transformPosition((float)x, (float)y, 0.0F, point);
                  minX = Math.min(minX, point.x());
                  minY = Math.min(minY, point.y());
                  maxX = Math.max(maxX, point.x());
                  maxY = Math.max(maxY, point.y());
               }
            }

            int left = Mth.clamp((int)Math.floor((double)minX), 0, graphics.guiWidth());
            int top = Mth.clamp((int)Math.floor((double)minY), 0, graphics.guiHeight());
            int right = Mth.clamp((int)Math.ceil((double)maxX), left, graphics.guiWidth());
            int bottom = Mth.clamp((int)Math.ceil((double)maxY), top, graphics.guiHeight());
            return new GTMultiblockStructureJeiPlugin.StructurePreviewWidget.ViewportClip(left, top, right, bottom);
         }
      }
   }

   public static record StructureRecipe(
      ResourceLocation id, ItemStack controller, MultiblockMachineDefinition definition, GTMultiblockStructureJeiPlugin.StructureData initialStructure
   ) {
      private static GTMultiblockStructureJeiPlugin.StructureRecipe create(MultiblockMachineDefinition definition) {
         GTMultiblockStructureJeiPlugin.PreviewModel model = new GTMultiblockStructureJeiPlugin.PreviewModel(definition);
         return new GTMultiblockStructureJeiPlugin.StructureRecipe(definition.getId(), definition.asStack(), definition, model.structure());
      }
   }

   private static record TargetChoice(BlockPos position, BlockInfo blockInfo) {
   }
}
