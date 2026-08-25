package com.gregtechceu.gtceu.integration.recipeviewer.jei.multiblock;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class StructureSemantics {
   public static final Direction PREVIEW_FRONT = Direction.SOUTH;
   public static final Direction PREVIEW_UP = Direction.UP;
   public static final boolean PREVIEW_FLIPPED = false;
   private static final ResourceLocation CENTRAL_MONITOR = ResourceLocation.fromNamespaceAndPath("gtceu", "central_monitor");
   private static final ResourceLocation CHARCOAL_PILE_IGNITER = ResourceLocation.fromNamespaceAndPath("gtceu", "charcoal_pile_igniter");
   private static final ResourceLocation PRIMITIVE_BLAST_FURNACE = ResourceLocation.fromNamespaceAndPath("gtceu", "primitive_blast_furnace");
   private static final Set<ResourceLocation> LARGE_TURBINES = Set.of(
      ResourceLocation.fromNamespaceAndPath("gtceu", "steam_large_turbine"),
      ResourceLocation.fromNamespaceAndPath("gtceu", "gas_large_turbine"),
      ResourceLocation.fromNamespaceAndPath("gtceu", "plasma_large_turbine")
   );
   private static final Set<ResourceLocation> LARGE_COMBUSTION_ENGINES = Set.of(
      ResourceLocation.fromNamespaceAndPath("gtceu", "large_combustion_engine"), ResourceLocation.fromNamespaceAndPath("gtceu", "extreme_combustion_engine")
   );

   private StructureSemantics() {
   }

   public static void initializePreviewDimensions(MultiblockMachineDefinition definition, IBlockPattern pattern, MultiblockSchemaInfo schema) {
      if (definition.getId().equals(CENTRAL_MONITOR) && pattern instanceof ExpandablePattern expandable) {
         if (schema.getUserDimensions().isEmpty()) {
            List<IntIntPair> constraints = expandable.getBoundsConstraints().apply();
            int[] preferred = new int[]{1, 1, 3, 0, 0, 0};
            if (constraints.size() != preferred.length) {
               throw new IllegalStateException("central monitor exposes an unexpected expandable-bound layout");
            } else {
               for (int index = 0; index < preferred.length; index++) {
                  IntIntPair constraint = constraints.get(index);
                  int value = preferred[index];
                  if (value < constraint.leftInt() || value > constraint.rightInt()) {
                     throw new IllegalStateException("central monitor preview fallback is outside GTCEu bounds");
                  }

                  schema.getUserDimensions().add(value);
               }
            }
         }
      }
   }

   public static int refreshCompleteSchema(MultiblockSchemaInfo schema, MultiblockMachineDefinition definition, Direction front, Direction up, boolean flipped) {
      prepareDefinitionPredicates(definition, definition.getStructurePatterns().get("main").get());
      schema.refreshSchema(definition, front, up, flipped, null);
      IBlockPattern pattern = definition.getStructurePatterns().get("main").get();
      int restored = restoreStrictAirCells(schema, pattern, front, up, flipped);
      return restored + restoreSupplementalClearanceCells(schema, definition, front, up, flipped);
   }

   private static void prepareDefinitionPredicates(MultiblockMachineDefinition definition, IBlockPattern pattern) {
      if (definition.getId().equals(PRIMITIVE_BLAST_FURNACE) && pattern instanceof BlockPattern blockPattern) {
         List snowCandidates = List.of(
            BlockInfo.fromBlockState(Blocks.SNOW.defaultBlockState()), BlockInfo.fromBlockState(Blocks.SNOW_BLOCK.defaultBlockState())
         );
         ObjectIterator var4 = blockPattern.getPredicates().values().iterator();

         while (var4.hasNext()) {
            PatternPredicate predicate = (PatternPredicate)var4.next();
            if (predicate.hasAir()) {
               boolean hasConcreteBranch = predicate.subPredicates.stream().anyMatch(basex -> !basex.getCandidates().isEmpty());
               if (hasConcreteBranch) {
                  for (BasePredicate base : predicate.subPredicates) {
                     if (base.getCandidates().isEmpty()) {
                        base.candidates = snowCandidates;
                     }
                  }
               }
            }
         }
      }
   }

   private static int restoreStrictAirCells(MultiblockSchemaInfo schema, IBlockPattern pattern, Direction front, Direction up, boolean flipped) {
      if (pattern instanceof BlockPattern blockPattern && schema.getStructureHelper() != null) {
         int repeatedSlices = 0;
         PatternSlice[] slices = blockPattern.getSlices();

         for (int index = 0; index < slices.length; index++) {
            repeatedSlices += schema.getUserSliceRepeats().getOrDefault(index, slices[index].getMinRepeats());
         }

         int[] patternDimensions = blockPattern.getDimensions();
         if (patternDimensions.length < 3) {
            throw new IllegalStateException("GTCEu block pattern exposes fewer than three dimensions");
         }

         int[] logicalExtents = new int[]{repeatedSlices, patternDimensions[1], patternDimensions[2]};
         int[] absoluteExtents = new int[3];
         RelativeDirection[] directions = blockPattern.getDirections();
         if (directions.length != 3) {
            throw new IllegalStateException("GTCEu block pattern exposes fewer than three axes");
         }

         for (int index = 0; index < directions.length; index++) {
            Axis axis = directions[index].getRelativeFacing(front, up, flipped).getAxis();
            absoluteExtents[axis.ordinal()] = logicalExtents[index];
         }

         if (absoluteExtents[0] > 0 && absoluteExtents[1] > 0 && absoluteExtents[2] > 0) {
            int restored = 0;
            Map<BlockPos, BlockInfo> blocks = schema.getStructureBlocks();

            for (int x = 0; x < absoluteExtents[Axis.X.ordinal()]; x++) {
               for (int y = 0; y < absoluteExtents[Axis.Y.ordinal()]; y++) {
                  for (int z = 0; z < absoluteExtents[Axis.Z.ordinal()]; z++) {
                     BlockPos position = new BlockPos(x, y, z);
                     if (!blocks.containsKey(position)) {
                        PatternPredicate predicate = schema.getStructureHelper().getPredicateFromPos(pattern, position, front, up, flipped);
                        if (predicate == PatternPredicate.AIR) {
                           blocks.put(position, BlockInfo.EMPTY);
                           restored++;
                        }
                     }
                  }
               }
            }

            return restored;
         }

         throw new IllegalStateException("GTCEu block pattern produced an invalid logical footprint");
      }

      return 0;
   }

   private static int restoreSupplementalClearanceCells(
      MultiblockSchemaInfo schema, MultiblockMachineDefinition definition, Direction front, Direction up, boolean flipped
   ) {
      Set<BlockPos> clearance = new LinkedHashSet<>();
      if (LARGE_COMBUSTION_ENGINES.contains(definition.getId())) {
         BlockPos controller = schema.getMapSchema().getControllerPos();
         if (controller == null) {
            throw new IllegalStateException("combustion-engine schema has no controller position");
         }

         for (int vertical = -1; vertical <= 1; vertical++) {
            for (int horizontal = -1; horizontal <= 1; horizontal++) {
               if (vertical != 0 || horizontal != 0) {
                  clearance.add(RelativeDirection.offsetPos(controller, front, up, flipped, vertical, horizontal, 1));
               }
            }
         }
      } else if (LARGE_TURBINES.contains(definition.getId())) {
         for (Entry<BlockPos, BlockInfo> entry : schema.getStructureBlocks().entrySet()) {
            if (entry.getValue().getBlockState().getBlock() instanceof MetaMachineBlock machineBlock
               && machineBlock.getDefinition().getId().getPath().contains("rotor_holder")) {
               Direction rotorFront = machineBlock.getFrontFacing(entry.getValue().getBlockState());
               Direction rotorUp = rotorFront.getAxis() == Axis.Y ? Direction.NORTH : Direction.UP;

               for (int vertical = -1; vertical <= 1; vertical++) {
                  for (int horizontalx = -1; horizontalx <= 1; horizontalx++) {
                     clearance.add(RelativeDirection.offsetPos(entry.getKey(), rotorFront, rotorUp, false, vertical, horizontalx, 1));
                  }
               }
            }
         }

         if (clearance.isEmpty()) {
            throw new IllegalStateException("large-turbine schema has no rotor-holder candidate");
         }
      }

      int restored = 0;

      for (BlockPos position : clearance) {
         BlockInfo previous = schema.getStructureBlocks().putIfAbsent(position.immutable(), BlockInfo.EMPTY);
         if (previous == null) {
            restored++;
         } else if (!previous.getBlockState().isAir()) {
            throw new IllegalStateException("runtime clearance overlaps the materialized structure at " + position);
         }
      }

      return restored;
   }

   public static StructureSemantics.CellRole classify(PatternPredicate predicate) {
      List<StructureSemantics.Candidate> candidates = candidates(predicate);
      boolean air = predicate.hasAir() || candidates.stream().anyMatch(StructureSemantics.Candidate::isAir);
      if (air) {
         return StructureSemantics.CellRole.OPTIONAL;
      } else {
         return isReplaceable(predicate) ? StructureSemantics.CellRole.REPLACEABLE : StructureSemantics.CellRole.REQUIRED;
      }
   }

   public static boolean isReplaceable(PatternPredicate predicate) {
      List<StructureSemantics.Candidate> candidates = candidates(predicate);
      long nonAirKinds = candidates.stream().filter(candidate -> !candidate.isAir()).map(StructureSemantics.Candidate::blockId).distinct().count();
      return nonAirKinds > 1L || predicate.subPredicates.size() > 1;
   }

   public static List<StructureSemantics.Candidate> candidates(PatternPredicate predicate) {
      Map<String, StructureSemantics.Candidate> unique = new LinkedHashMap<>();

      for (BasePredicate base : predicate.subPredicates) {
         for (BlockInfo info : base.getCandidates()) {
            String stateKey = info.getBlockState().toString();
            unique.putIfAbsent(stateKey, new StructureSemantics.Candidate(base, info));
         }
      }

      return List.copyOf(unique.values());
   }

   public static StructureSemantics.ConsistencyKind consistencyKind(BasePredicate basePredicate) {
      Set<Block> blocks = new LinkedHashSet<>();

      for (BlockInfo candidate : basePredicate.getCandidates()) {
         Block block = candidate.getBlockState().getBlock();
         if (!candidate.getBlockState().isAir()) {
            blocks.add(block);
         }
      }

      if (blocks.size() < 2) {
         return null;
      } else if (blocks.stream().allMatch(StructureSemantics::isRegisteredHeatingCoil)) {
         return StructureSemantics.ConsistencyKind.HEATING_COILS;
      } else {
         return blocks.stream().allMatch(StructureSemantics::isRegisteredCleanroomFilter) ? StructureSemantics.ConsistencyKind.CLEANROOM_FILTERS : null;
      }
   }

   private static boolean isRegisteredHeatingCoil(Block block) {
      return GTCEuAPI.HEATING_COILS.values().stream().anyMatch(supplier -> supplier.get() == block);
   }

   private static boolean isRegisteredCleanroomFilter(Block block) {
      return GTCEuAPI.CLEANROOM_FILTERS.values().stream().anyMatch(supplier -> supplier.get() == block);
   }

   public static StructureSemantics.ConsistencyKind consistencyKind(Block block) {
      if (isRegisteredHeatingCoil(block)) {
         return StructureSemantics.ConsistencyKind.HEATING_COILS;
      } else {
         return isRegisteredCleanroomFilter(block) ? StructureSemantics.ConsistencyKind.CLEANROOM_FILTERS : null;
      }
   }

   public static StructureSemantics.Candidate activeCandidate(PatternPredicate predicate, BlockInfo current) {
      for (StructureSemantics.Candidate candidate : candidates(predicate)) {
         if (candidate.blockInfo().equals(current)) {
            return candidate;
         }
      }

      for (StructureSemantics.Candidate candidatex : candidates(predicate)) {
         if (candidatex.blockInfo().getBlockState().equals(current.getBlockState())) {
            return candidatex;
         }
      }

      Block currentBlock = current.getBlockState().getBlock();

      for (StructureSemantics.Candidate candidatexx : candidates(predicate)) {
         if (candidatexx.blockInfo().getBlockState().getBlock() == currentBlock) {
            return candidatexx;
         }
      }

      return null;
   }

   public static StructureSemantics.ConsistencyKind activeConsistencyKind(PatternPredicate predicate, BlockInfo current) {
      StructureSemantics.Candidate active = activeCandidate(predicate, current);
      return active == null ? null : consistencyKind(active.basePredicate());
   }

   public static List<StructureSemantics.Candidate> consistencyCandidates(PatternPredicate predicate, StructureSemantics.ConsistencyKind kind) {
      Map<ResourceLocation, StructureSemantics.Candidate> unique = new LinkedHashMap<>();

      for (StructureSemantics.Candidate candidate : candidates(predicate)) {
         if (consistencyKind(candidate.basePredicate()) == kind) {
            unique.putIfAbsent(candidate.blockId(), candidate);
         }
      }

      return List.copyOf(unique.values());
   }

   public static StructureSemantics.Candidate findConsistencyCandidate(
      PatternPredicate predicate, StructureSemantics.ConsistencyKind kind, ResourceLocation blockId
   ) {
      for (StructureSemantics.Candidate candidate : consistencyCandidates(predicate, kind)) {
         if (candidate.blockId().equals(blockId)) {
            return candidate;
         }
      }

      return null;
   }

   public static List<BlockPos> consistencyPositions(
      MultiblockSchemaInfo schema, IBlockPattern pattern, StructureSemantics.ConsistencyKind kind, Direction front, Direction up, boolean flipped
   ) {
      List<BlockPos> result = new ArrayList<>();

      for (Entry<BlockPos, BlockInfo> entry : schema.getStructureBlocks().entrySet()) {
         PatternPredicate predicate = predicateAt(schema, pattern, entry.getKey(), front, up, flipped);
         if (activeConsistencyKind(predicate, entry.getValue()) == kind) {
            result.add(entry.getKey().immutable());
         }
      }

      result.sort((left, right) -> {
         int x = Integer.compare(left.getX(), right.getX());
         if (x != 0) {
            return x;
         } else {
            int y = Integer.compare(left.getY(), right.getY());
            return y != 0 ? y : Integer.compare(left.getZ(), right.getZ());
         }
      });
      return List.copyOf(result);
   }

   public static List<StructureSemantics.Candidate> commonConsistencyCandidates(
      MultiblockSchemaInfo schema,
      IBlockPattern pattern,
      PatternPredicate selectedPredicate,
      StructureSemantics.ConsistencyKind kind,
      Direction front,
      Direction up,
      boolean flipped
   ) {
      List<StructureSemantics.Candidate> ordered = consistencyCandidates(selectedPredicate, kind);
      Set<ResourceLocation> common = null;

      for (BlockPos position : consistencyPositions(schema, pattern, kind, front, up, flipped)) {
         PatternPredicate predicate = predicateAt(schema, pattern, position, front, up, flipped);
         Set<ResourceLocation> accepted = new HashSet<>();

         for (StructureSemantics.Candidate candidate : consistencyCandidates(predicate, kind)) {
            accepted.add(candidate.blockId());
         }

         if (common == null) {
            common = accepted;
         } else {
            common.retainAll(accepted);
         }
      }

      if (common == null) {
         return List.of();
      } else {
         Set<ResourceLocation> intersection = common;
         return ordered.stream().filter(candidatex -> intersection.contains(candidatex.blockId())).toList();
      }
   }

   public static void validateConsistency(MultiblockSchemaInfo schema, IBlockPattern pattern, Direction front, Direction up, boolean flipped) {
      Set<StructureSemantics.ConsistencyKind> activeKinds = EnumSet.noneOf(StructureSemantics.ConsistencyKind.class);

      for (Entry<BlockPos, BlockInfo> entry : schema.getStructureBlocks().entrySet()) {
         PatternPredicate predicate = predicateAt(schema, pattern, entry.getKey(), front, up, flipped);
         StructureSemantics.ConsistencyKind kind = activeConsistencyKind(predicate, entry.getValue());
         if (kind != null) {
            activeKinds.add(kind);
         }
      }

      validateConsistency(schema, activeKinds);
   }

   public static void validateConsistency(MultiblockSchemaInfo schema, Set<StructureSemantics.ConsistencyKind> requiredKinds) {
      Map<StructureSemantics.ConsistencyKind, ResourceLocation> selectedBlocks = new EnumMap<>(StructureSemantics.ConsistencyKind.class);

      for (Entry<BlockPos, BlockInfo> entry : schema.getStructureBlocks().entrySet()) {
         StructureSemantics.ConsistencyKind kind = consistencyKind(entry.getValue().getBlockState().getBlock());
         if (kind != null && requiredKinds.contains(kind)) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlockState().getBlock());
            ResourceLocation previous = selectedBlocks.putIfAbsent(kind, blockId);
            if (previous != null && !previous.equals(blockId)) {
               throw new IllegalArgumentException("mixed GTCEu consistency group: " + kind);
            }
         }
      }
   }

   public static long predicateKey(PatternPredicate predicate) {
      StringBuilder canonical = new StringBuilder(256);
      canonical.append(predicate.isController()).append('|').append(predicate.isSingle()).append('|').append(predicate.hasAir());

      for (BasePredicate base : predicate.subPredicates) {
         canonical.append("||")
            .append(base.getPredicateName())
            .append('|')
            .append(base.minCount)
            .append('|')
            .append(base.maxCount)
            .append('|')
            .append(base.minSliceCount)
            .append('|')
            .append(base.maxSliceCount)
            .append('|')
            .append(base.previewCount);

         for (BlockInfo info : base.getCandidates()) {
            canonical.append('|').append(BuiltInRegistries.BLOCK.getKey(info.getBlockState().getBlock())).append('@').append(info.getBlockState());
         }
      }

      return fnv1a64(canonical.toString().getBytes(StandardCharsets.UTF_8));
   }

   private static long fnv1a64(byte[] bytes) {
      long hash = -3750763034362895579L;

      for (byte value : bytes) {
         hash ^= (long)value & 255L;
         hash *= 1099511628211L;
      }

      return hash;
   }

   public static List<StructureSemantics.DimensionOption> dimensionOptions(IBlockPattern pattern) {
      List<StructureSemantics.DimensionOption> result = new ArrayList<>();
      if (pattern instanceof BlockPattern blockPattern) {
         PatternSlice[] slices = blockPattern.getSlices();

         for (int index = 0; index < slices.length; index++) {
            PatternSlice slice = slices[index];
            if (slice.getMinRepeats() != slice.getMaxRepeats()) {
               result.add(
                  new StructureSemantics.DimensionOption(
                     StructureSemantics.DimensionOption.Kind.REPEATED_SLICE, index, slice.getMinRepeats(), slice.getMaxRepeats()
                  )
               );
            }
         }
      } else if (pattern instanceof ExpandablePattern expandablePattern && expandablePattern.getBoundsConstraints() != null) {
         List<IntIntPair> constraints = expandablePattern.getBoundsConstraints().apply();

         for (int indexx = 0; indexx < constraints.size(); indexx++) {
            IntIntPair constraint = constraints.get(indexx);
            if (constraint.leftInt() != constraint.rightInt()) {
               result.add(
                  new StructureSemantics.DimensionOption(
                     StructureSemantics.DimensionOption.Kind.EXPANDABLE_BOUND, indexx, constraint.leftInt(), constraint.rightInt()
                  )
               );
            }
         }
      }

      return List.copyOf(result);
   }

   public static Axis dimensionAxis(IBlockPattern pattern, StructureSemantics.DimensionOption option) {
      if (option.kind() == StructureSemantics.DimensionOption.Kind.REPEATED_SLICE
         && pattern instanceof BlockPattern blockPattern
         && blockPattern.getDirections().length > 0) {
         RelativeDirection relative = blockPattern.getDirections()[0];
         return relative.getRelativeFacing(PREVIEW_FRONT, PREVIEW_UP, false).getAxis();
      }

      if (option.kind() != StructureSemantics.DimensionOption.Kind.EXPANDABLE_BOUND
         || !(pattern instanceof ExpandablePattern)
         || option.index() < 0
         || option.index() >= RelativeDirection.VALUES.length) {
         throw new IllegalArgumentException("dimension option does not belong to this pattern");
      } else {
         RelativeDirection relative = RelativeDirection.VALUES[option.index()];
         return relative.getRelativeFacing(PREVIEW_FRONT, PREVIEW_UP, false).getAxis();
      }
   }

   public static int[] currentSliceRepeats(MultiblockSchemaInfo schema, IBlockPattern pattern) {
      if (!(pattern instanceof BlockPattern blockPattern)) {
         return new int[0];
      } else {
         PatternSlice[] slices = blockPattern.getSlices();
         int[] values = new int[slices.length];

         for (int index = 0; index < slices.length; index++) {
            values[index] = schema.getUserSliceRepeats().getOrDefault(index, slices[index].getMinRepeats());
         }

         return values;
      }
   }

   public static int[] currentExpandableBounds(MultiblockSchemaInfo schema, IBlockPattern pattern) {
      if (pattern instanceof ExpandablePattern expandablePattern && expandablePattern.getBoundsConstraints() != null) {
         List<IntIntPair> constraints = expandablePattern.getBoundsConstraints().apply();
         int[] values = new int[constraints.size()];

         for (int index = 0; index < constraints.size(); index++) {
            values[index] = index < schema.getUserDimensions().size() ? schema.getUserDimensions().getInt(index) : constraints.get(index).leftInt();
         }

         return values;
      }

      return new int[0];
   }

   public static void applyAndValidateDimensions(MultiblockSchemaInfo schema, IBlockPattern pattern, int[] sliceRepeats, int[] expandableBounds) {
      if (pattern instanceof BlockPattern blockPattern) {
         PatternSlice[] slices = blockPattern.getSlices();
         if (sliceRepeats.length != slices.length) {
            throw new IllegalArgumentException("wrong repeated-slice count");
         } else {
            for (int index = 0; index < slices.length; index++) {
               int value = sliceRepeats[index];
               if (value < slices[index].getMinRepeats() || value > slices[index].getMaxRepeats()) {
                  throw new IllegalArgumentException("repeated slice outside GTCEu bounds");
               }

               schema.getUserSliceRepeats().put(index, value);
            }

            if (expandableBounds.length != 0) {
               throw new IllegalArgumentException("unexpected expandable bounds");
            }
         }
      } else if (pattern instanceof ExpandablePattern expandablePattern) {
         if (expandablePattern.getBoundsConstraints() == null) {
            if (expandableBounds.length != 0 || sliceRepeats.length != 0) {
               throw new IllegalArgumentException("unexpected dimensions for fixed expandable pattern");
            }
         } else {
            List<IntIntPair> constraints = expandablePattern.getBoundsConstraints().apply();
            if (expandableBounds.length == constraints.size() && sliceRepeats.length == 0) {
               for (int index = 0; index < constraints.size(); index++) {
                  IntIntPair constraint = constraints.get(index);
                  int value = expandableBounds[index];
                  if (value < constraint.leftInt() || value > constraint.rightInt()) {
                     throw new IllegalArgumentException("expandable bound outside GTCEu bounds");
                  }

                  while (schema.getUserDimensions().size() <= index) {
                     schema.getUserDimensions().add(0);
                  }

                  schema.getUserDimensions().set(index, value);
               }
            } else {
               throw new IllegalArgumentException("wrong expandable-bound count");
            }
         }
      } else if (sliceRepeats.length != 0 || expandableBounds.length != 0) {
         throw new IllegalArgumentException("dimensions supplied for unsupported pattern");
      }
   }

   public static PatternPredicate predicateAt(
      MultiblockSchemaInfo schema, IBlockPattern pattern, BlockPos position, Direction front, Direction up, boolean flipped
   ) {
      if (schema.getStructureHelper() == null) {
         throw new IllegalStateException("GTCEu did not create a structure helper");
      } else {
         PatternPredicate predicate = schema.getStructureHelper().getPredicateFromPos(pattern, position, front, up, flipped);
         if (predicate == null) {
            throw new IllegalStateException("GTCEu returned a null predicate for " + position);
         } else {
            return predicate;
         }
      }
   }

   public static BlockPos canonicalPosition(
      MultiblockSchemaInfo schema, IBlockPattern pattern, BlockPos position, Direction front, Direction up, boolean flipped
   ) {
      if (schema.getMapSchema() != null && schema.getMapSchema().getControllerPos() != null) {
         RelativeDirection[] axes;
         if (pattern instanceof BlockPattern blockPattern) {
            axes = blockPattern.getDirections();
         } else {
            if (!(pattern instanceof ExpandablePattern expandablePattern)) {
               throw new IllegalArgumentException("unsupported GTCEu pattern coordinate system");
            }

            axes = expandablePattern.getDirections();
         }

         if (axes.length != 3) {
            throw new IllegalStateException("GTCEu pattern does not expose three coordinate axes");
         } else {
            BlockPos controller = schema.getMapSchema().getControllerPos();
            int dx = position.getX() - controller.getX();
            int dy = position.getY() - controller.getY();
            int dz = position.getZ() - controller.getZ();
            return new BlockPos(
               project(dx, dy, dz, axes[0].getRelativeFacing(front, up, flipped)),
               project(dx, dy, dz, axes[1].getRelativeFacing(front, up, flipped)),
               project(dx, dy, dz, axes[2].getRelativeFacing(front, up, flipped))
            );
         }
      } else {
         throw new IllegalStateException("GTCEu schema has no controller position");
      }
   }

   private static int project(int x, int y, int z, Direction axis) {
      return x * axis.getStepX() + y * axis.getStepY() + z * axis.getStepZ();
   }

   public static StructureSemantics.Candidate findCandidate(PatternPredicate predicate, ResourceLocation blockId) {
      for (StructureSemantics.Candidate candidate : candidates(predicate)) {
         if (candidate.blockId().equals(blockId)) {
            return candidate;
         }
      }

      return null;
   }

   public static MultiblockSchemaInfo createSchema(
      MultiblockMachineDefinition definition, Direction front, Direction up, boolean flipped, int[] slices, int[] bounds, int maximumBlocks
   ) {
      IBlockPattern pattern = definition.getStructurePatterns().get("main").get();
      MultiblockSchemaInfo schema = new MultiblockSchemaInfo();
      validateDefinitionDimensions(definition, pattern, bounds);
      applyAndValidateDimensions(schema, pattern, slices, bounds);
      preflightSchemaAllocation(pattern, slices, bounds, maximumBlocks);
      refreshCompleteSchema(schema, definition, front, up, flipped);
      return schema;
   }

   private static void validateDefinitionDimensions(MultiblockMachineDefinition definition, IBlockPattern pattern, int[] bounds) {
      if (definition.getId().equals(CHARCOAL_PILE_IGNITER) && pattern instanceof ExpandablePattern) {
         if (bounds.length != 6 || Math.abs(bounds[2] - bounds[3]) > 1 || Math.abs(bounds[4] - bounds[5]) > 1) {
            throw new IllegalArgumentException("charcoal pile opposite sides must differ by at most one block");
         }
      }
   }

   private static void preflightSchemaAllocation(IBlockPattern pattern, int[] slices, int[] bounds, int maximumBlocks) {
      if (maximumBlocks <= 0) {
         throw new IllegalArgumentException("invalid schema allocation limit");
      } else {
         try {
            if (pattern instanceof BlockPattern blockPattern) {
               PatternSlice[] patternSlices = blockPattern.getSlices();
               long estimated = 0L;

               for (int index = 0; index < patternSlices.length; index++) {
                  long cellsPerSlice = 0L;

                  for (char[] row : patternSlices[index].getPattern()) {
                     cellsPerSlice = Math.addExact(cellsPerSlice, (long)row.length);
                  }

                  estimated = Math.addExact(estimated, Math.multiplyExact(cellsPerSlice, (long)slices[index]));
                  if (estimated > (long)maximumBlocks) {
                     throw new IllegalArgumentException("structure exceeds pre-allocation safety limit");
                  }
               }
            } else {
               if (pattern instanceof ExpandablePattern expandablePattern && expandablePattern.getBoundsConstraints() != null) {
                  RelativeDirection[] axes = expandablePattern.getDirections();
                  if (axes.length != 3) {
                     throw new IllegalArgumentException("expandable pattern does not expose three axes");
                  }

                  long estimated = 1L;

                  for (RelativeDirection axis : axes) {
                     int negativeIndex = axis.oppositeOrdinal();
                     int positiveIndex = axis.ordinal();
                     if (negativeIndex < 0 || positiveIndex < 0 || negativeIndex >= bounds.length || positiveIndex >= bounds.length) {
                        throw new IllegalArgumentException("expandable bounds do not cover every axis");
                     }

                     long extent = Math.addExact(Math.addExact((long)bounds[negativeIndex], (long)bounds[positiveIndex]), 1L);
                     if (extent <= 0L) {
                        throw new IllegalArgumentException("expandable pattern has an invalid extent");
                     }

                     estimated = Math.multiplyExact(estimated, extent);
                     if (estimated > (long)maximumBlocks) {
                        throw new IllegalArgumentException("structure exceeds pre-allocation safety limit");
                     }
                  }
               }
            }
         } catch (ArithmeticException var16) {
            throw new IllegalArgumentException("structure allocation estimate overflowed", var16);
         }
      }
   }

   public static record Candidate(BasePredicate basePredicate, BlockInfo blockInfo) {
      public boolean isAir() {
         return this.blockInfo.getBlockState().isAir();
      }

      public ItemStack displayStack() {
         ItemStack stack = this.blockInfo.getItemStackForm();
         return stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack;
      }

      public ResourceLocation blockId() {
         return BuiltInRegistries.BLOCK.getKey(this.blockInfo.getBlockState().getBlock());
      }
   }

   public static enum CellRole {
      REQUIRED("jei.gtceu.multiblock.role.required", -9251190),
      OPTIONAL("jei.gtceu.multiblock.role.optional", -14249),
      REPLACEABLE("jei.gtceu.multiblock.role.replaceable", -9910296);

      private final String translationKey;
      private final int color;

      private CellRole(String translationKey, int color) {
         this.translationKey = translationKey;
         this.color = color;
      }

      public String translationKey() {
         return this.translationKey;
      }

      public int color() {
         return this.color;
      }
   }

   public static enum ConsistencyKind {
      HEATING_COILS("jei.gtceu.multiblock.consistency.heating_coils"),
      CLEANROOM_FILTERS("jei.gtceu.multiblock.consistency.cleanroom_filters");

      private final String translationKey;

      private ConsistencyKind(String translationKey) {
         this.translationKey = translationKey;
      }

      public String translationKey() {
         return this.translationKey;
      }
   }

   public static record DimensionOption(StructureSemantics.DimensionOption.Kind kind, int index, int minimum, int maximum) {
      public static enum Kind {
         REPEATED_SLICE,
         EXPANDABLE_BOUND;
      }
   }
}
