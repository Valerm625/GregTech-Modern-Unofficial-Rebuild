package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

public class BlockPatternHelper extends AbstractStructureHelper {
   private final Int2IntMap sliceRepeats;
   private char[][][] flattenedBlockPattern = new char[0][][];
   private IBlockPattern setupPattern;
   private Direction setupFrontFacing;
   private Direction setupUpFacing;
   private boolean setupFlipped;

   protected BlockPatternHelper(Int2IntMap sliceRepeats) {
      this.sliceRepeats = sliceRepeats;
   }

   @Override
   public PatternPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos, Direction frontFacing, Direction upFacing, boolean isFlipped) {
      BlockPattern blockPattern = (BlockPattern)pattern;
      boolean matchesSetup = this.flattenedBlockPattern.length > 0
         && pattern == this.setupPattern
         && frontFacing == this.setupFrontFacing
         && upFacing == this.setupUpFacing
         && isFlipped == this.setupFlipped;
      char[][][] adjustedBlockPattern;
      if (matchesSetup) {
         adjustedBlockPattern = this.flattenedBlockPattern;
      } else {
         adjustedBlockPattern = rotateAndFlipPattern(this.flattenBlockPattern(blockPattern), blockPattern.getDirections(), frontFacing, upFacing, isFlipped);
      }

      Vec3i dimensions = getDimensions(adjustedBlockPattern);
      if (pos.getX() >= 0
         && pos.getX() < dimensions.getX()
         && pos.getY() >= 0
         && pos.getY() < dimensions.getY()
         && pos.getZ() >= 0
         && pos.getZ() < dimensions.getZ()) {
         char c = adjustedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
         return (PatternPredicate)blockPattern.getPredicates().get(c);
      } else {
         return PatternPredicate.AIR;
      }
   }

   @Override
   protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {
      BlockPattern blockPattern = (BlockPattern)pattern;
      this.flattenedBlockPattern = rotateAndFlipPattern(this.flattenBlockPattern(blockPattern), blockPattern.getDirections(), frontFacing, upFacing, isFlipped);
      this.setupPattern = pattern;
      this.setupFrontFacing = frontFacing;
      this.setupUpFacing = upFacing;
      this.setupFlipped = isFlipped;
   }

   @Override
   protected void populateWithUserBlockPreferences(
      Map<BlockPos, BlockInfo> resultStructure,
      IBlockPattern pattern,
      Long2ObjectMap<BlockInfo> userBlockPreferences,
      Direction frontFacing,
      Direction upFacing,
      boolean isFlipped
   ) {
      BlockPattern blockPattern = (BlockPattern)pattern;
      Vec3i dimensions = getDimensions(this.flattenedBlockPattern);
      Direction sliceDir = blockPattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
      ObjectIterator var10 = userBlockPreferences.long2ObjectEntrySet().iterator();

      while (var10.hasNext()) {
         Entry<BlockInfo> blockPreference = (Entry<BlockInfo>)var10.next();
         BlockPos pos = BlockPos.of(blockPreference.getLongKey());
         BlockInfo blockInfo = (BlockInfo)blockPreference.getValue();
         if (pos.getX() >= dimensions.getX() || pos.getY() >= dimensions.getY() || pos.getZ() >= dimensions.getZ()) {
            throw new IllegalStateException(
               "BlockPos preference "
                  + pos
                  + "is outside of bounds for pattern of size "
                  + dimensions.getX()
                  + ","
                  + dimensions.getY()
                  + ","
                  + dimensions.getZ()
            );
         }

         char c = this.flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
         PatternPredicate predicate = (PatternPredicate)blockPattern.getPredicates().get(c);
         if (!this.isValidCandidate(resultStructure, predicate, pos, blockInfo, sliceDir)) {
            throw new IllegalStateException("Invalid preference " + blockInfo.getBlockState().getBlock().getName() + " for position " + pos);
         }

         resultStructure.put(pos, blockInfo);
      }
   }

   @Override
   protected void populateFromPattern(
      Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped
   ) {
      BlockPattern blockPattern = (BlockPattern)pattern;
      Vec3i dimensions = getDimensions(this.flattenedBlockPattern);
      Direction sliceDir = blockPattern.getDirections()[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Direction stringDir = blockPattern.getDirections()[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Direction charDir = blockPattern.getDirections()[2].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Axis sliceAxis = sliceDir.getAxis();
      Axis stringAxis = stringDir.getAxis();
      Axis charAxis = charDir.getAxis();

      for (int sliceCoord = 0; sliceCoord < dimensions.get(sliceAxis); sliceCoord++) {
         for (int stringCoord = 0; stringCoord < dimensions.get(stringAxis); stringCoord++) {
            for (int charCoord = 0; charCoord < dimensions.get(charAxis); charCoord++) {
               MutableBlockPos pos = new MutableBlockPos();
               setAxis(pos, sliceAxis, sliceCoord);
               setAxis(pos, stringAxis, stringCoord);
               setAxis(pos, charAxis, charCoord);
               if (!resultStructure.containsKey(pos)) {
                  char c = this.flattenedBlockPattern[pos.getX()][pos.getY()][pos.getZ()];
                  PatternPredicate predicate = (PatternPredicate)blockPattern.getPredicates().get(c);
                  if (predicate != PatternPredicate.AIR
                     && predicate != PatternPredicate.ANY
                     && !this.tryMinCount(resultStructure, predicate, pos, sliceDir, sliceCoord)
                     && !this.tryMaxCount(resultStructure, predicate, pos, sliceDir, sliceCoord)) {
                     throw new IllegalStateException("Could not place a block without breaking maxCount requirements for character " + c);
                  }
               }
            }
         }
      }
   }

   private boolean tryMinCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate, BlockPos pos, Direction dir, int offset) {
      for (BasePredicate basePredicate : predicate.subPredicates) {
         int minCount = this.getMinCount(predicate, basePredicate);
         if (minCount != 0) {
            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, dir, offset);
            boolean globalMinUnmet = minCount > 0 && totalAlreadyPopulated < minCount;
            boolean layerMinUnmet = basePredicate.minSliceCount > 0 && layerAlreadyPopulated < basePredicate.minSliceCount;
            if (globalMinUnmet || layerMinUnmet) {
               BlockInfo toInsert = (BlockInfo)this.blockPreferences.get(predicate, basePredicate);
               if (toInsert == null && !basePredicate.getCandidates().isEmpty()) {
                  toInsert = basePredicate.getCandidates().get(0);
               }

               if (toInsert != null && this.isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) {
                  resultStructure.put(pos, toInsert);
                  if (this.controllerBlock == null && predicate.isController()) {
                     this.controllerBlock = toInsert.getBlockState().getBlock();
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean tryMaxCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate, BlockPos pos, Direction dir, int offset) {
      for (BasePredicate basePredicate : predicate.subPredicates) {
         int maxCount = this.getMaxCount(predicate, basePredicate);
         if (maxCount != 0) {
            int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
            int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, dir, offset);
            if ((maxCount == -1 || totalAlreadyPopulated < maxCount)
               && (basePredicate.maxSliceCount == -1 || layerAlreadyPopulated < basePredicate.maxSliceCount)) {
               BlockInfo toInsert = (BlockInfo)this.blockPreferences.get(predicate, basePredicate);
               if (toInsert == null && !basePredicate.getCandidates().isEmpty()) {
                  toInsert = basePredicate.getCandidates().get(0);
               }

               if (toInsert != null && this.isValidCandidate(resultStructure, predicate, pos, toInsert, dir)) {
                  resultStructure.put(pos, toInsert);
                  if (this.controllerBlock == null && predicate.isController()) {
                     this.controllerBlock = toInsert.getBlockState().getBlock();
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isValidCandidate(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate, BlockPos pos, BlockInfo newInfo, Direction sliceDir) {
      int sliceCoord = getCoordFromDir(pos, sliceDir);

      for (BasePredicate basePredicate : predicate.subPredicates) {
         if (basePredicate.candidates.contains(newInfo)) {
            int maxCount = this.getMaxCount(predicate, basePredicate);
            if (maxCount != 0) {
               int totalAlreadyPopulated = countPopulatedGlobal(resultStructure, basePredicate);
               int layerAlreadyPopulated = countPopulatedInLayer(resultStructure, basePredicate, sliceDir, sliceCoord);
               if ((maxCount == -1 || totalAlreadyPopulated < maxCount)
                  && (basePredicate.maxSliceCount == -1 || layerAlreadyPopulated < basePredicate.maxSliceCount)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private char[][][] flattenBlockPattern(BlockPattern pattern) {
      PatternSlice[] slices = pattern.getSlices();
      // Count the repeats the same way the fill loop below resolves them, so a slice without an
      // explicit entry still occupies its default of one slot instead of writing past the buffer.
      int totalSlices = 0;
      for (int sliceIndex = 0; sliceIndex < slices.length; sliceIndex++) {
         totalSlices += Math.max(0, this.sliceRepeats.getOrDefault(sliceIndex, 1));
      }
      int[] dimensions = pattern.getDimensions();
      char[][][] flattenedPattern = new char[totalSlices][dimensions[1]][dimensions[2]];
      int totalSlicesIndex = 0;

      for (int sliceIndex = 0; sliceIndex < slices.length; sliceIndex++) {
         PatternSlice slice = slices[sliceIndex];
         int repeats = this.sliceRepeats.getOrDefault(sliceIndex, 1);

         for (int i = 0; i < repeats; i++) {
            flattenedPattern[totalSlicesIndex] = slice.getPattern();
            totalSlicesIndex++;
         }
      }

      assert totalSlicesIndex == totalSlices;

      return flattenedPattern;
   }

   private static Vec3i getDimensions(char[][][] charPattern) {
      int d0 = charPattern.length;
      int d1 = d0 > 0 ? charPattern[0].length : 0;
      int d2 = d1 > 0 ? charPattern[0][0].length : 0;
      return new Vec3i(d0, d1, d2);
   }

   private static char[][][] rotateAndFlipPattern(
      char[][][] localFlattenedPattern, RelativeDirection[] patternDirections, Direction frontFacing, Direction upFacing, boolean isFlipped
   ) {
      Direction absoluteX = patternDirections[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Direction absoluteY = patternDirections[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Direction absoluteZ = patternDirections[2].getRelativeFacing(frontFacing, upFacing, isFlipped);
      Vec3i dimensions = getDimensions(localFlattenedPattern);
      if (dimensions.getX() != 0 && dimensions.getY() != 0 && dimensions.getZ() != 0) {
         int[][] steps = new int[][]{
            {absoluteX.getStepX(), absoluteX.getStepY(), absoluteX.getStepZ()},
            {absoluteY.getStepX(), absoluteY.getStepY(), absoluteY.getStepZ()},
            {absoluteZ.getStepX(), absoluteZ.getStepY(), absoluteZ.getStepZ()}
         };
         MutableBlockPos min = new MutableBlockPos();
         MutableBlockPos max = new MutableBlockPos();

         for (Axis axis : Axis.VALUES) {
            for (Axis world : Axis.VALUES) {
               int contribution = steps[axis.ordinal()][world.ordinal()] * (dimensions.get(axis) - 1);
               Direction worldDir = Direction.fromAxisAndDirection(world, AxisDirection.POSITIVE);
               min.move(worldDir, Math.min(0, contribution));
               max.move(worldDir, Math.max(0, contribution));
            }
         }

         Vec3i size = max.move(-min.getX() + 1, -min.getY() + 1, -min.getZ() + 1);
         char[][][] result = new char[size.getX()][size.getY()][size.getZ()];

         for (int x = 0; x < dimensions.getX(); x++) {
            for (int y = 0; y < dimensions.getY(); y++) {
               for (int z = 0; z < dimensions.getZ(); z++) {
                  int worldX = absoluteX.getStepX() * x + absoluteY.getStepX() * y + absoluteZ.getStepX() * z;
                  int worldY = absoluteX.getStepY() * x + absoluteY.getStepY() * y + absoluteZ.getStepY() * z;
                  int worldZ = absoluteX.getStepZ() * x + absoluteY.getStepZ() * y + absoluteZ.getStepZ() * z;
                  result[worldX - min.getX()][worldY - min.getY()][worldZ - min.getZ()] = localFlattenedPattern[x][y][z];
               }
            }
         }

         return result;
      } else {
         return new char[0][0][0];
      }
   }

   private static MutableBlockPos setAxis(MutableBlockPos pos, Axis axis, int amount) {
      return switch (axis) {
         case X -> pos.setX(amount);
         case Y -> pos.setY(amount);
         case Z -> pos.setZ(amount);
         default -> throw new MatchException(null, null);
      };
   }
}
