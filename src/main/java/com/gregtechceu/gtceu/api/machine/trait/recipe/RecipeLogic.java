package com.gregtechceu.gtceu.api.machine.trait.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.api.sync_system.ClassSyncData;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.cover.MachineControllerCover;
import com.gregtechceu.gtceu.utils.GTMath;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class RecipeLogic extends MachineTrait implements IWorkable {
   public static final MachineTraitType<RecipeLogic> TYPE = new MachineTraitType<>(RecipeLogic.class, false);
   public static final EnumProperty<RecipeLogic.Status> STATUS_PROPERTY = GTMachineModelProperties.RECIPE_LOGIC_STATUS;
   @Nullable
   public List<GTRecipe> lastFailedMatches;
   @SaveField
   @SyncToClient
   private RecipeLogic.Status status = RecipeLogic.Status.IDLE;
   @SaveField
   @SyncToClient
   @RerenderOnChanged
   protected boolean isActive;
   @Nullable
   protected Component bestFailureReason;
   @Nullable
   protected GTRecipe bestFailureRecipe;
   protected double bestFailureScore = Double.NEGATIVE_INFINITY;
   @SaveField
   @SyncToClient
   @Nullable
   protected GTRecipe lastRecipe;
   @SaveField
   @Nullable
   protected GTRecipe lastOriginRecipe;
   @SaveField
   @SyncToClient
   protected int consecutiveRecipes = 0;
   @SaveField
   @SyncToClient
   protected int progress;
   @SyncToClient
   @SaveField
   protected int duration;
   protected boolean recipeDirty;
   @SaveField
   protected long totalContinuousRunningTime;
   protected int runAttempt = 0;
   protected int runDelay = 0;
   @SaveField
   protected boolean suspendAfterFinish = false;
   @SaveField(
      nbtKey = "chance_cache"
   )
   protected final IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> chanceCaches = this.makeChanceCaches();
   @Nullable
   protected TickableSubscription subscription;
   @Nullable
   protected Object workingSound;
   protected boolean regressWhenWaiting;
   protected boolean keepSubscribing = true;
   protected boolean alwaysTryModifyRecipe = true;

   @Override
   public MachineTraitType<RecipeLogic> getTraitType() {
      return TYPE;
   }

   public IRecipeLogicMachine getRLMachine() {
      return (IRecipeLogicMachine)this.getMachine();
   }

   @Override
   protected List<Class<?>> validMachineClasses() {
      return List.of(IRecipeLogicMachine.class);
   }

   @ClientFieldChangeListener(
      fieldName = "status"
   )
   protected void onStatusSynced() {
      this.scheduleRenderUpdate();
      this.updateSound();
   }

   public void resetRecipeLogic() {
      this.recipeDirty = false;
      this.lastRecipe = null;
      this.lastOriginRecipe = null;
      this.consecutiveRecipes = 0;
      this.progress = 0;
      this.duration = 0;
      this.isActive = false;
      this.lastFailedMatches = null;
      this.clearFailureReason();
      if (this.status != RecipeLogic.Status.SUSPEND) {
         this.setStatus(RecipeLogic.Status.IDLE);
      }

      this.updateTickSubscription();
      this.getSyncDataHolder().resyncAllFields();
   }

   @Override
   public void onTraitAttached() {
      super.onTraitAttached();
      this.regressWhenWaiting = this.getMachine().getDefinition().isRegressWhenWaiting();
   }

   @Override
   public void onMachineLoad() {
      super.onMachineLoad();
      this.updateTickSubscription();
   }

   public void updateTickSubscription() {
      if (!this.isSuspend() && this.getRLMachine().isRecipeLogicAvailable()) {
         this.subscription = this.getMachine().subscribeServerTick(this.subscription, this::serverTick);
      } else if (this.subscription != null) {
         this.subscription.unsubscribe();
         this.subscription = null;
      }
   }

   public void setProgress(int progress) {
      this.progress = progress;
      this.syncDataHolder.markClientSyncFieldDirty("progress");
   }

   public void setProgressDelta(int delta) {
      this.setProgress(this.getProgress() + delta);
   }

   public double getProgressPercent() {
      return this.duration == 0 ? 0.0 : (double)this.progress / ((double)this.duration * 1.0);
   }

   public RecipeManager getRecipeManager() {
      return GTCEu.getMinecraftServer().getRecipeManager();
   }

   public void serverTick() {
      if (!this.isSuspend()) {
         if (!this.isIdle() && this.lastRecipe != null) {
            if (this.progress < this.duration) {
               if (this.runDelay > 0) {
                  this.runDelay--;
                  this.regressRecipe();
               } else {
                  this.handleRecipeWorking();
               }
            }

            if (this.progress >= this.duration) {
               this.onRecipeFinish();
            }
         } else if (this.lastRecipe != null) {
            this.findAndHandleRecipe();
         } else if (!this.keepSubscribing || this.getMachine().getOffsetTimer() % 5L == 0L) {
            this.findAndHandleRecipe();
            if (this.lastFailedMatches != null) {
               for (GTRecipe match : this.lastFailedMatches) {
                  if (this.checkMatchedRecipeAvailable(match)) {
                     break;
                  }
               }
            }
         }
      }

      boolean unsubscribe = false;
      if (this.isSuspend()) {
         unsubscribe = true;
      } else if (this.lastRecipe == null && this.isIdle() && !this.keepSubscribing && !this.recipeDirty && this.lastFailedMatches == null) {
         unsubscribe = true;
      }

      if (unsubscribe && this.subscription != null) {
         this.subscription.unsubscribe();
         this.subscription = null;
      }
   }

   protected ActionResult matchRecipe(GTRecipe recipe) {
      return RecipeHelper.matchContents(this.getRLMachine(), recipe);
   }

   protected ActionResult checkRecipe(GTRecipe recipe) {
      ActionResult conditionResult = RecipeHelper.checkConditions(recipe, this);
      return !conditionResult.isSuccess() ? conditionResult : this.matchRecipe(recipe);
   }

   public boolean checkMatchedRecipeAvailable(GTRecipe match) {
      GTRecipe modified = this.getRLMachine().fullModifyRecipe(match);
      if (modified != null) {
         ActionResult recipeMatch = this.checkRecipe(modified);
         if (recipeMatch.isSuccess()) {
            this.setupRecipe(modified);
         } else {
            this.recordFailureReason(match, recipeMatch.reason(), recipeMatch.score());
         }

         if (this.lastRecipe != null && this.getStatus() == RecipeLogic.Status.WORKING) {
            this.lastOriginRecipe = match;
            this.lastFailedMatches = null;
            return true;
         }
      }

      return false;
   }

   public void handleRecipeWorking() {
      assert this.lastRecipe != null;

      ActionResult conditionResult = RecipeHelper.checkConditions(this.lastRecipe, this);
      if (conditionResult.isSuccess()) {
         ActionResult handleTick = this.handleTickRecipe(this.lastRecipe);
         if (handleTick.isSuccess()) {
            this.setStatus(RecipeLogic.Status.WORKING);
            if (!this.getRLMachine().onWorking()) {
               this.interruptRecipe();
               return;
            }

            this.progress++;
            this.runAttempt = 0;
            this.runDelay = 0;
            this.totalContinuousRunningTime++;
            this.syncDataHolder.markClientSyncFieldDirty("progress");
         } else {
            this.setWaiting(handleTick.reason());
            if (handleTick.io() == IO.IN && handleTick.capability() == EURecipeCapability.CAP) {
               this.runAttempt++;
               this.runAttempt = (int)GTMath.clamp((long)this.runAttempt, 0L, 5L);
               if (this.runAttempt == 5) {
                  boolean preventPowerFail = false;
                  if (this.getMachine() instanceof MultiblockControllerMachine) {
                     for (CoverBehavior cover : this.getMachine().getCoverContainer().getCovers()) {
                        if (cover instanceof MachineControllerCover mcc && mcc.preventPowerFail()) {
                           preventPowerFail = true;
                           break;
                        }
                     }
                  }

                  if (this.getMachine() instanceof MultiblockControllerMachine && !preventPowerFail) {
                     this.runAttempt = 0;
                     this.setStatus(RecipeLogic.Status.SUSPEND);
                  }
               }

               this.runDelay = this.runAttempt * 60;
            }
         }
      } else {
         this.setWaiting(conditionResult.reason());
      }

      if (this.isWaiting() || this.isSuspend()) {
         this.regressRecipe();
      }
   }

   protected void regressRecipe() {
      if (this.progress > 0 && this.regressWhenWaiting) {
         this.progress = Math.max(this.progress - 2, 1);
         this.getSyncDataHolder().markClientSyncFieldDirty("progress");
      }
   }

   public Iterator<GTRecipe> searchRecipe() {
      return this.getRLMachine().getRecipeType().searchRecipe(this.getRLMachine(), r -> true);
   }

   public void findAndHandleRecipe() {
      this.lastFailedMatches = null;
      this.clearFailureReason();
      GTRecipe last = this.lastRecipe;
      if (!this.recipeDirty && last != null) {
         ActionResult lastCheck = this.checkRecipe(last);
         if (lastCheck.isSuccess()) {
            this.lastRecipe = null;
            this.lastOriginRecipe = null;
            this.setupRecipe(last);
            this.recipeDirty = false;
            return;
         }

         this.recordFailureReason(last, lastCheck.reason(), Double.POSITIVE_INFINITY);
      }

      this.lastRecipe = null;
      this.lastOriginRecipe = null;
      this.handleSearchingRecipes(this.searchRecipe());
      this.syncDataHolder.markClientSyncFieldDirty("lastRecipe");
      this.recipeDirty = false;
   }

   protected void handleSearchingRecipes(Iterator<GTRecipe> matches) {
      while (matches.hasNext()) {
         GTRecipe match = matches.next();
         if (this.checkMatchedRecipeAvailable(match)) {
            return;
         }

         if (this.matchRecipe(match).isSuccess()) {
            if (this.lastFailedMatches == null) {
               this.lastFailedMatches = new ArrayList<>();
            }

            this.lastFailedMatches.add(match);
         }
      }
   }

   public ActionResult handleTickRecipe(GTRecipe recipe) {
      if (!recipe.hasTick()) {
         return ActionResult.SUCCESS;
      } else {
         ActionResult result = RecipeHelper.matchTickRecipe(this.getRLMachine(), recipe);
         if (!result.isSuccess()) {
            return result;
         } else {
            result = this.handleTickRecipeIO(recipe, IO.IN);
            return !result.isSuccess() ? result : this.handleTickRecipeIO(recipe, IO.OUT);
         }
      }
   }

   public void setupRecipe(GTRecipe recipe) {
      if (!this.getRLMachine().beforeWorking(recipe)) {
         this.setStatus(RecipeLogic.Status.IDLE);
         this.consecutiveRecipes = 0;
         this.progress = 0;
         this.duration = 0;
         this.isActive = false;
         this.syncDataHolder.resyncAllFields();
      } else {
         ActionResult handledIO = this.handleRecipeIO(recipe, IO.IN);
         if (handledIO.isSuccess()) {
            if (this.lastRecipe != null && !recipe.equals(this.lastRecipe)) {
               this.chanceCaches.clear();
            }

            this.clearFailureReason();
            this.recipeDirty = false;
            this.lastRecipe = recipe;
            this.setStatus(RecipeLogic.Status.WORKING);
            this.progress = 0;
            this.duration = recipe.duration;
            this.isActive = true;
            this.syncDataHolder.resyncAllFields();
         }
      }
   }

   public void setStatus(RecipeLogic.Status status) {
      if (!this.isRemote()) {
         if (this.status != status) {
            if (this.status == RecipeLogic.Status.WORKING) {
               this.totalContinuousRunningTime = 0L;
            }

            if ((status == RecipeLogic.Status.WAITING || status == RecipeLogic.Status.SUSPEND) && this.suspendAfterFinish) {
               status = RecipeLogic.Status.SUSPEND;
               this.suspendAfterFinish = false;
            }

            this.getRLMachine().recipeLogicStatusChanged(this.status, status);
            this.status = status;
            this.syncDataHolder.markClientSyncFieldDirty("status");
            this.setRenderState((MachineRenderState)this.getRenderState().setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS, status));
            this.updateTickSubscription();
            if (this.status == RecipeLogic.Status.WORKING || this.status == RecipeLogic.Status.IDLE) {
               this.clearFailureReason();
            }
         }
      }
   }

   public void setWaiting(@Nullable Component reason) {
      this.setStatus(RecipeLogic.Status.WAITING);
      this.clearFailureReason();
      this.recordFailureReason(this.lastRecipe, reason, Double.POSITIVE_INFINITY);
      this.getRLMachine().onWaiting();
   }

   public void markLastRecipeDirty() {
      this.recipeDirty = true;
   }

   public boolean isWorking() {
      return this.status == RecipeLogic.Status.WORKING;
   }

   public boolean isIdle() {
      return this.status == RecipeLogic.Status.IDLE;
   }

   public boolean isWaiting() {
      return this.status == RecipeLogic.Status.WAITING;
   }

   public boolean isSuspend() {
      return this.status == RecipeLogic.Status.SUSPEND;
   }

   @Override
   public boolean isWorkingEnabled() {
      return !this.isSuspend() && !this.isSuspendAfterFinish();
   }

   @Override
   public void setWorkingEnabled(boolean isWorkingAllowed) {
      if (!this.isRemote()) {
         if (!isWorkingAllowed && this.getStatus() == RecipeLogic.Status.IDLE) {
            this.clearFailureReason();
            this.setStatus(RecipeLogic.Status.SUSPEND);
         } else {
            this.setSuspendAfterFinish(!isWorkingAllowed);
            if (isWorkingAllowed) {
               if (this.lastRecipe != null && this.duration > 0) {
                  this.setStatus(RecipeLogic.Status.WORKING);
               } else {
                  this.setStatus(RecipeLogic.Status.IDLE);
               }
            }
         }
      }
   }

   @Override
   public int getMaxProgress() {
      return this.duration;
   }

   @Override
   public boolean isActive() {
      return this.isWorking() || this.isWaiting() || this.isSuspend() && this.isActive;
   }

   public boolean hasCustomProgressLine() {
      return false;
   }

   @Nullable
   public Component getCustomProgressLine() {
      return null;
   }

   public void onRecipeFinish() {
      this.getRLMachine().afterWorking();
      if (this.lastRecipe != null) {
         this.runAttempt = 0;
         this.runDelay = 0;
         this.consecutiveRecipes++;
         this.handleRecipeIO(this.lastRecipe, IO.OUT);
         if (this.suspendAfterFinish) {
            this.setStatus(RecipeLogic.Status.SUSPEND);
            this.consecutiveRecipes = 0;
            this.progress = 0;
            this.duration = 0;
            this.isActive = false;
            this.lastRecipe = null;
            this.syncDataHolder.resyncAllFields();
            return;
         }

         if (this.alwaysTryModifyRecipe) {
            if (this.lastOriginRecipe != null) {
               GTRecipe modified = this.getRLMachine().fullModifyRecipe(this.lastOriginRecipe.copy());
               if (modified == null) {
                  this.markLastRecipeDirty();
               } else {
                  this.lastRecipe = modified;
                  this.syncDataHolder.markClientSyncFieldDirty("lastRecipe");
               }
            } else {
               this.markLastRecipeDirty();
            }
         }

         ActionResult recipeCheck = this.checkRecipe(this.lastRecipe);
         if (!this.recipeDirty && recipeCheck.isSuccess()) {
            this.setupRecipe(this.lastRecipe);
         } else {
            this.setStatus(RecipeLogic.Status.IDLE);
            this.consecutiveRecipes = 0;
            this.progress = 0;
            this.duration = 0;
            this.isActive = false;
            this.syncDataHolder.resyncAllFields();
         }
      }
   }

   protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
      return RecipeHelper.handleRecipeIO(this.getRLMachine(), recipe, io, this.chanceCaches);
   }

   protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
      return RecipeHelper.handleTickRecipeIO(this.getRLMachine(), recipe, io, this.chanceCaches);
   }

   public void interruptRecipe() {
      this.getRLMachine().afterWorking();
      if (this.lastRecipe != null) {
         this.setStatus(RecipeLogic.Status.IDLE);
         this.progress = 0;
         this.duration = 0;
         this.syncDataHolder.markClientSyncFieldDirty("progress");
         this.syncDataHolder.markClientSyncFieldDirty("duration");
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void updateSound() {
      if (this.isWorking() && this.getRLMachine().shouldWorkingPlaySound()) {
         SoundEntry sound = this.getRLMachine().getRecipeType().getSound();
         if (this.workingSound instanceof AutoReleasedSound soundEntry) {
            if (soundEntry.soundEntry == sound && !soundEntry.isStopped()) {
               return;
            }

            soundEntry.release();
            this.workingSound = null;
         }

         if (sound != null) {
            this.workingSound = sound.playAutoReleasedSound(
               () -> this.getRLMachine().shouldWorkingPlaySound()
                     && this.isWorking()
                     && !this.getMachine().isRemoved()
                     && this.getMachine().getLevel().isLoaded(this.getMachine().getBlockPos())
                     && MetaMachine.getMachine(this.getMachine().getLevel(), this.getMachine().getBlockPos()) == this.getMachine(),
               this.getMachine().getBlockPos(),
               true,
               0,
               1.0F,
               1.0F
            );
         }
      } else if (this.workingSound instanceof AutoReleasedSound soundEntry) {
         soundEntry.release();
         this.workingSound = null;
      }
   }

   protected IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> makeChanceCaches() {
      IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> map = new IdentityHashMap<>();

      for (RecipeCapability<?> cap : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registryOrThrow(GTRegistries.Keys.RECIPE_CAPABILITY)) {
         map.put(cap, cap.makeChanceCache());
      }

      return map;
   }

   public static void putFailureReason(Object machine, GTRecipe recipe, Component reason) {
      if (machine instanceof IRecipeLogicMachine rlm) {
         putFailureReason(rlm.getRecipeLogic(), recipe, reason, Double.POSITIVE_INFINITY);
      }
   }

   public static void putFailureReason(RecipeLogic logic, GTRecipe recipe, Component reason, double score) {
      logic.recordFailureReason(recipe, reason, score);
   }

   protected void recordFailureReason(@Nullable GTRecipe recipe, @Nullable Component reason, double score) {
      if (reason != null && !reason.getString().isBlank() && score > this.bestFailureScore) {
         this.bestFailureScore = score;
         this.bestFailureReason = reason;
         this.bestFailureRecipe = recipe;
      }
   }

   protected void clearFailureReason() {
      this.bestFailureReason = null;
      this.bestFailureRecipe = null;
      this.bestFailureScore = Double.NEGATIVE_INFINITY;
   }

   @Generated
   public RecipeLogic.Status getStatus() {
      return this.status;
   }

   @Nullable
   @Generated
   public Component getBestFailureReason() {
      return this.bestFailureReason;
   }

   @Nullable
   @Generated
   public GTRecipe getBestFailureRecipe() {
      return this.bestFailureRecipe;
   }

   @Generated
   public double getBestFailureScore() {
      return this.bestFailureScore;
   }

   @Nullable
   @Generated
   public GTRecipe getLastRecipe() {
      return this.lastRecipe;
   }

   @Nullable
   @Generated
   public GTRecipe getLastOriginRecipe() {
      return this.lastOriginRecipe;
   }

   @Generated
   public int getConsecutiveRecipes() {
      return this.consecutiveRecipes;
   }

   @Generated
   @Override
   public int getProgress() {
      return this.progress;
   }

   @Generated
   public int getDuration() {
      return this.duration;
   }

   @VisibleForTesting
   @Generated
   public boolean isRecipeDirty() {
      return this.recipeDirty;
   }

   @Generated
   public long getTotalContinuousRunningTime() {
      return this.totalContinuousRunningTime;
   }

   @Generated
   @Override
   public boolean isSuspendAfterFinish() {
      return this.suspendAfterFinish;
   }

   @Generated
   @Override
   public void setSuspendAfterFinish(boolean suspendAfterFinish) {
      this.suspendAfterFinish = suspendAfterFinish;
   }

   @Generated
   public IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> getChanceCaches() {
      return this.chanceCaches;
   }

   @Generated
   public boolean isRegressWhenWaiting() {
      return this.regressWhenWaiting;
   }

   @Generated
   public void setRegressWhenWaiting(boolean regressWhenWaiting) {
      this.regressWhenWaiting = regressWhenWaiting;
   }

   @Generated
   public boolean isKeepSubscribing() {
      return this.keepSubscribing;
   }

   @Generated
   public void setKeepSubscribing(boolean keepSubscribing) {
      this.keepSubscribing = keepSubscribing;
   }

   static {
      ClassSyncData.getClassData(RecipeLogic.class)
         .setCustomTransformerForField(
            "chanceCaches",
            new ValueTransformer<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>>() {
               public Tag serializeNBT(
                  IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> value,
                  ValueTransformer.TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context
               ) {
                  CompoundTag chanceCache = new CompoundTag();
                  if (context.currentValue() == null) {
                     return chanceCache;
                  } else {
                     context.currentValue().forEach((cap, cache) -> {
                        ListTag cacheTag = new ListTag();
                        ObjectIterator var5 = cache.object2IntEntrySet().iterator();

                        while (var5.hasNext()) {
                           Entry<?> entry = (Entry<?>)var5.next();
                           CompoundTag compoundTag = new CompoundTag();
                           Tag obj = cap.toNbt(entry.getKey(), context.lookup());
                           compoundTag.put("entry", obj);
                           compoundTag.putInt("cached_chance", entry.getIntValue());
                           cacheTag.add(compoundTag);
                        }

                        chanceCache.put(cap.id.toString(), cacheTag);
                     });
                     return chanceCache;
                  }
               }

               @Nullable
               public IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> deserializeNBT(
                  Tag tag, ValueTransformer.TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context
               ) {
                  CompoundTag chanceCache = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
                  if (context.currentValue() != null) {
                     for (String strKey : chanceCache.getAllKeys()) {
                        ResourceLocation key = ResourceLocation.parse(strKey);
                        Optional<Reference<RecipeCapability<?>>> holder = context.lookup().holder(ResourceKey.create(GTRegistries.Keys.RECIPE_CAPABILITY, key));
                        if (!holder.isEmpty()) {
                           RecipeCapability<?> cap = (RecipeCapability<?>)holder.get().value();
                           Object2IntMap map = context.currentValue().computeIfAbsent(cap, RecipeCapability::makeChanceCache);
                           ListTag chanceTag = chanceCache.getList(strKey, 10);

                           for (int i = 0; i < chanceTag.size(); i++) {
                              CompoundTag chanceKey = chanceTag.getCompound(i);
                              Object entry = cap.serializer.fromNbt(chanceKey.get("entry"), context.lookup());
                              int value = chanceKey.getInt("cached_chance");
                              map.put(entry, value);
                           }
                        }
                     }
                  }

                  return context.currentValue();
               }
            }
         );
   }

   public static enum Status implements StringRepresentable {
      IDLE("idle"),
      WORKING("working"),
      WAITING("waiting"),
      SUSPEND("suspend");

      private final String serializedName;

      private Status(String name) {
         this.serializedName = name;
      }

      @Generated
      public String getSerializedName() {
         return this.serializedName;
      }
   }
}
