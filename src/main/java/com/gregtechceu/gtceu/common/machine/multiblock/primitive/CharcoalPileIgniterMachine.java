package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.item.behavior.LighterBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

public class CharcoalPileIgniterMachine extends WorkableMultiblockMachine implements IWorkable {
   private static final int MIN_RADIUS = 1;
   private static final int MIN_DEPTH = 2;
   private static final int MAX_DEPTH = 5;
   private static final int MAX_RADIUS = 5;
   private final Collection<BlockPos> logPos = new ObjectOpenHashSet();
   private List<Integer> bounds = new ArrayList<>(List.of(0, 2, 1, 1, 1, 1));
   private int maxTime = 0;
   private boolean hasAir = false;

   public CharcoalPileIgniterMachine(BlockEntityCreationInfo info) {
      super(info, new CharcoalPileIgniterMachine.CharcoalRecipeLogic());
   }

   @Override
   public void formStructure(@NotNull String substructureName) {
      super.formStructure(substructureName);
      this.logPos.clear();
      this.hasAir = false;
      this.forEachFormed("main", (info, pos) -> {
         if (info.getBlockState().is(BlockTags.LOGS_THAT_BURN)) {
            this.logPos.add(pos.immutable());
         } else if (info.getBlockState().isAir()) {
            this.hasAir = true;
         }
      });
      this.getRecipeLogic().setDuration(Math.max(1, (int)Math.sqrt((double)(this.logPos.size() * 240000))));
   }

   public static ExpandablePattern.BoundsProvider boundsFunction() {
      return (level, controllerPos, front, up) -> {
         BlockPos firstInteriorLayer = controllerPos.relative(up.getOpposite());
         Direction back = front.getOpposite();
         Direction left = front.getCounterClockWise();
         Direction right = left.getOpposite();
         int l = findWallPos(level, left, firstInteriorLayer.mutable());
         int r = findWallPos(level, right, firstInteriorLayer.mutable());
         int b = findWallPos(level, back, firstInteriorLayer.mutable());
         int f = findWallPos(level, front, firstInteriorLayer.mutable());
         int d = findFloorPos(level, up.getOpposite(), controllerPos.mutable());
         return d >= 2 && l >= 1 && r >= 1 && b >= 1 && f >= 1 && Math.abs(l - r) <= 1 && Math.abs(f - b) <= 1
            ? new IntArrayList(new int[]{0, d, l, r, f, b})
            : new IntArrayList();
      };
   }

   public CharcoalPileIgniterMachine.CharcoalRecipeLogic getRecipeLogic() {
      return (CharcoalPileIgniterMachine.CharcoalRecipeLogic)super.getRecipeLogic();
   }

   @Override
   public boolean isActive() {
      return this.recipeLogic.isWorking();
   }

   @Override
   public boolean isWorkingEnabled() {
      return true;
   }

   @Override
   public void setWorkingEnabled(boolean isWorkingAllowed) {
   }

   public static Function<MultiblockMachineDefinition, IBlockPattern> getPattern() {
      return definition -> {
         PatternPredicate floor = Predicates.blocks(Blocks.BRICKS);
         PatternPredicate logs = Predicates.blockTag(BlockTags.LOGS_THAT_BURN).or(PatternPredicate.AIR);
         PatternPredicate walls = Predicates.blockTag(CustomTags.CHARCOAL_PILE_IGNITER_WALLS);
         return ExpandableMultiblockPatternBuilder.start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
            .boundsProvider(boundsFunction())
            .constraintProvider(
               () -> List.of(IntIntPair.of(0, 0), IntIntPair.of(2, 5), IntIntPair.of(1, 5), IntIntPair.of(1, 5), IntIntPair.of(1, 5), IntIntPair.of(1, 5))
            )
            .predicateProvider((bp, b) -> {
               if (bp.equals(BlockPos.ZERO)) {
                  return Predicates.controller(definition);
               } else {
                  int intersects = 0;
                  boolean topAisle = bp.getX() == b.get(0);
                  boolean bottomAisle = bp.getX() == -b.get(1);
                  if (topAisle || bottomAisle) {
                     intersects++;
                  }

                  if (bp.getY() == -b.get(2) || bp.getY() == b.get(3)) {
                     intersects++;
                  }

                  if (bp.getZ() == b.get(4) || bp.getZ() == -b.get(5)) {
                     intersects++;
                  }

                  if (intersects >= 2) {
                     return PatternPredicate.ANY;
                  } else if (intersects == 1) {
                     return bottomAisle ? floor : walls;
                  } else {
                     return logs;
                  }
               }
            })
            .build();
      };
   }

   private static PatternPredicate wallPredicate() {
      return new PatternPredicate(
         "Wall Blocks",
         multiblockState -> {
            BlockPos p = multiblockState.getBlockPos();
            return multiblockState.getBlockState().is(CustomTags.CHARCOAL_PILE_IGNITER_WALLS)
               ? null
               : new PatternStringError(Component.translatable("gtceu.predicate_error.charcoal.walls", new Object[]{p.getX(), p.getY(), p.getZ()}));
         },
         null
      );
   }

   private static PatternPredicate logPredicate() {
      return new PatternPredicate(multiblockState -> {
         boolean match = multiblockState.getBlockState().is(BlockTags.LOGS_THAT_BURN);
         return match ? null : new PatternStringError(Component.translatable("gtceu.predicate_error.charcoal.logs"));
      }, null);
   }

   private static int findWallPos(Level level, Direction direction, MutableBlockPos pos) {
      for (int i = 1; i <= 5; i++) {
         BlockState state = level.getBlockState(pos.move(direction));
         if (state.is(CustomTags.CHARCOAL_PILE_IGNITER_WALLS)) {
            return i;
         }
      }

      return -1;
   }

   private static int findFloorPos(Level level, Direction dir, MutableBlockPos pos) {
      for (int i = 1; i <= 5; i++) {
         BlockState state = level.getBlockState(pos.move(dir));
         if (state.is(Blocks.BRICKS)) {
            return i;
         }
      }

      return -1;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void clientTick() {
      super.clientTick();
      if (this.isActive()) {
         BlockPos pos = this.getBlockPos();
         Direction facing = Direction.UP;
         float xPos = (float)facing.getStepX() * 0.76F + (float)pos.getX() + 0.25F + GTValues.RNG.nextFloat() / 2.0F;
         float yPos = (float)facing.getStepY() * 0.76F + (float)pos.getY() + 0.25F;
         float zPos = (float)facing.getStepZ() * 0.76F + (float)pos.getZ() + 0.25F + GTValues.RNG.nextFloat() / 2.0F;
         float ySpd = (float)facing.getStepY() * 0.1F + 0.01F * GTValues.RNG.nextFloat();
         float horSpd = 0.03F * GTValues.RNG.nextFloat();
         float horSpd2 = 0.03F * GTValues.RNG.nextFloat();
         if (GTValues.RNG.nextFloat() < 0.1F) {
            this.getLevel().playLocalSound((double)xPos, (double)yPos, (double)zPos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         for (float xi = xPos - 1.0F; xi <= xPos + 1.0F; xi++) {
            for (float zi = zPos - 1.0F; zi <= zPos + 1.0F; zi++) {
               if (!(GTValues.RNG.nextFloat() < 0.9F)) {
                  this.getLevel().addParticle(ParticleTypes.LARGE_SMOKE, (double)xi, (double)yPos, (double)zi, (double)horSpd, (double)ySpd, (double)horSpd2);
               }
            }
         }
      }
   }

   public void convertLogBlocks() {
      Level level = this.getLevel();

      for (BlockPos pos : this.logPos) {
         level.setBlockAndUpdate(pos, GTBlocks.BRITTLE_CHARCOAL.getDefaultState());
      }

      this.logPos.clear();
   }

   @Override
   public InteractionResult onUse(ExtendedUseOnContext context) {
      ItemStack stack = context.getItemInHand();
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      if (!this.isFormed() || this.hasAir) {
         return super.onUseWithItem(context);
      } else if (!stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) && !stack.is(CustomTags.TOOLS_IGNITER)) {
         return InteractionResult.PASS;
      } else if (this.getLevel().isClientSide && !this.isActive()) {
         return InteractionResult.SUCCESS;
      } else {
         if (!this.isActive()) {
            boolean shouldActivate = false;
            if (stack.getItem() instanceof ComponentItem compItem) {
               for (IItemComponent component : compItem.getComponents()) {
                  if (component instanceof LighterBehavior lighter && lighter.consumeFuel(player, stack)) {
                     shouldActivate = true;
                     break;
                  }
               }
            } else if (stack.isDamageableItem()) {
               stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
               shouldActivate = true;
            } else {
               stack.shrink(1);
               shouldActivate = true;
            }

            if (shouldActivate) {
               this.getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
               this.getLevel()
                  .playSound(
                     null,
                     this.getBlockPos(),
                     stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                     SoundSource.BLOCKS,
                     1.0F,
                     1.0F
                  );
               return InteractionResult.CONSUME;
            }
         }

         return super.onUse(context);
      }
   }

   public static class CharcoalRecipeLogic extends RecipeLogic {
      public CharcoalPileIgniterMachine getMachine() {
         return (CharcoalPileIgniterMachine)super.getMachine();
      }

      @Override
      public void serverTick() {
         super.serverTick();
         if (this.isWorking() && this.duration > 0 && ++this.progress >= this.duration) {
            this.progress = 0;
            this.duration = 0;
            this.getMachine().convertLogBlocks();
            this.setStatus(RecipeLogic.Status.IDLE);
         }
      }

      public void setDuration(int max) {
         this.duration = max;
      }
   }
}
