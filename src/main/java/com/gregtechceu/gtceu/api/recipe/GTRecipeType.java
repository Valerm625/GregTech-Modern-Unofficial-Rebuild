package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.Generated;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class GTRecipeType implements RecipeType<GTRecipe> {
   public static final String LANGUAGE_KEY_PATH = "recipe_type";
   private static final ResourceLocation COMBUSTION_GENERATOR_ID = GTCEu.id("combustion_generator");
   public GTRecipeSerializer serializer;
   public final ResourceLocation registryName;
   public final String group;
   public final Object2IntSortedMap<RecipeCapability<?>> maxInputs = new Object2IntAVLTreeMap(RecipeCapability.COMPARATOR);
   public final Object2IntSortedMap<RecipeCapability<?>> maxOutputs = new Object2IntAVLTreeMap(RecipeCapability.COMPARATOR);
   private GTRecipeBuilder recipeBuilder;
   private ChanceBoostFunction chanceFunction = ChanceBoostFunction.NONE;
   private GTRecipeType smallRecipeMap;
   @Nullable
   private Supplier<ItemStack> iconSupplier;
   @Nullable
   protected SoundEntry sound;
   protected List<Function<CompoundTag, String>> dataInfos = new ArrayList<>();
   protected boolean isScanner;
   protected boolean hasResearchSlot;
   protected volatile Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes;
   private final GTRecipeCategory category;
   private volatile Map<GTRecipeCategory, Set<GTRecipe>> categoryMap = new Object2ObjectOpenHashMap();
   @Nullable
   private Map<GTRecipeCategory, Set<GTRecipe>> stagingCategoryMap;
   @Nullable
   private RecipeDB previousRecipeDb;
   @Nullable
   private Map<GTRecipeCategory, Set<GTRecipe>> previousCategoryMap;
   @Nullable
   private Map<String, Collection<GTRecipe>> previousResearchEntries;
   @Nullable
   private Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> previousProxyRecipes;
   @Nullable
   private AbstractObject2IntMap<SizedFluidIngredient> previousCombustionFuels;
   private final RecipeDB db = new RecipeDB();
   @Internal
   private final RecipeAdditionHandler additionHandler = new RecipeAdditionHandler(this.db);
   private boolean offsetVoltageText = false;
   private int voltageTextOffset = 20;
   private volatile Map<String, Collection<GTRecipe>> researchEntries = new Object2ObjectOpenHashMap();
   @Nullable
   private Map<String, Collection<GTRecipe>> stagingResearchEntries;
   private final List<GTRecipeType.ICustomRecipeLogic> customRecipeLogicRunners = new ArrayList<>();
   private int minRecipeConditions = 0;
   private GTRecipeTypeUILayout uiLayout;

   public GTRecipeType(ResourceLocation registryName, String group, RecipeType<?>... proxyRecipes) {
      this.registryName = registryName;
      this.group = group;
      this.category = GTRecipeCategory.registerDefault(this);
      this.recipeBuilder = new GTRecipeBuilder(registryName, this);
      Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> map = new Object2ObjectLinkedOpenHashMap();

      for (RecipeType<?> proxyRecipe : proxyRecipes) {
         map.put(proxyRecipe, new ArrayList<>());
      }

      this.proxyRecipes = map;
   }

   public GTRecipeType setMaxIOSize(int maxItemInputs, int maxItemOutputs, int maxFluidInputs, int maxFluidOutputs) {
      return this.setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxItemInputs)
         .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
         .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxItemOutputs)
         .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
   }

   public GTRecipeType setEUIO(IO io) {
      if (io.support(IO.IN)) {
         this.setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
      }

      if (io.support(IO.OUT)) {
         this.setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
      }

      return this;
   }

   public GTRecipeType setMaxSize(IO io, RecipeCapability<?> cap, int max) {
      if (io == IO.IN || io == IO.BOTH) {
         this.maxInputs.put(cap, max);
      }

      if (io == IO.OUT || io == IO.BOTH) {
         this.maxOutputs.put(cap, max);
      }

      return this;
   }

   public GTRecipeType UI(UnaryOperator<GTRecipeTypeUILayout.Builder> builder) {
      this.uiLayout = builder.apply(new GTRecipeTypeUILayout.Builder(this)).build();
      return this;
   }

   public GTRecipeType setMaxTooltips(int maxTooltips) {
      return this;
   }

   public GTRecipeType setXEIVisible(boolean XEIVisible) {
      this.category.setXEIVisible(XEIVisible);
      return this;
   }

   public void setMinRecipeConditions(int n) {
      this.minRecipeConditions = Math.max(this.minRecipeConditions, n);
   }

   public GTRecipeType addCustomRecipeLogic(GTRecipeType.ICustomRecipeLogic recipeLogic) {
      this.customRecipeLogicRunners.add(recipeLogic);
      return this;
   }

   @Override
   public String toString() {
      return this.registryName.toString();
   }

   @NotNull
   public Iterator<GTRecipe> searchRecipe(IRecipeCapabilityHolder holder, Predicate<GTRecipe> canHandle) {
      if (!holder.hasCapabilityProxies()) {
         return Collections.emptyIterator();
      } else {
         RecipeDB.RecipeIterator iterator = this.db.iterator(holder, canHandle);
         if (iterator == null) {
            return Collections.emptyIterator();
         } else {
            boolean any = false;

            while (iterator.hasNext()) {
               GTRecipe recipe = iterator.next();
               if (recipe != null) {
                  any = true;
                  break;
               }
            }

            if (any) {
               iterator.reset();
               return iterator;
            } else {
               for (GTRecipeType.ICustomRecipeLogic logic : this.customRecipeLogicRunners) {
                  GTRecipe recipe = logic.createCustomRecipe(holder);
                  if (recipe != null && canHandle.test(recipe)) {
                     return Collections.singleton(recipe).iterator();
                  }
               }

               return Collections.emptyIterator();
            }
         }
      }
   }

   public int getMaxInputs(RecipeCapability<?> cap) {
      return this.maxInputs.getOrDefault(cap, 0);
   }

   public int getMaxOutputs(RecipeCapability<?> cap) {
      return this.maxOutputs.getOrDefault(cap, 0);
   }

   public int getMaxSlots(RecipeCapability<?> cap, IO io) {
      return io == IO.IN ? this.getMaxInputs(cap) : this.getMaxOutputs(cap);
   }

   public GTRecipeType prepareBuilder(Consumer<GTRecipeBuilder> onPrepare) {
      onPrepare.accept(this.recipeBuilder);
      return this;
   }

   public GTRecipeBuilder recipeBuilder(ResourceLocation id) {
      return this.recipeBuilder.copy(id);
   }

   public GTRecipeBuilder recipeBuilder(ResourceLocation id, Object... append) {
      if (append.length > 0) {
         String toAppend = Arrays.stream(append).map(Object::toString).map(FormattingUtil::toLowerCaseUnderscore).reduce("", (a, b) -> a + "_" + b);
         id = id.withSuffix(toAppend);
      }

      return this.recipeBuilder(id);
   }

   public GTRecipeBuilder recipeBuilder(String id) {
      return this.recipeBuilder(GTCEu.id(id));
   }

   public GTRecipeBuilder recipeBuilder(String id, Object... append) {
      return this.recipeBuilder(GTCEu.id(id), append);
   }

   public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
      return this.recipeBuilder.copyFrom(builder);
   }

   public GTRecipeType onRecipeBuild(BiConsumer<GTRecipeBuilder, RecipeOutput> onBuild) {
      this.recipeBuilder.onSave(onBuild);
      return this;
   }

   public void addDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
      Map<String, Collection<GTRecipe>> entries = this.stagingResearchEntries != null
         ? this.stagingResearchEntries
         : this.researchEntries;
      Collection<GTRecipe> collection = entries.computeIfAbsent(researchId, k -> new ObjectOpenHashSet());
      collection.add(recipe);
   }

   @Nullable
   public Collection<GTRecipe> getDataStickEntry(@NotNull String researchId) {
      return this.researchEntries.get(researchId);
   }

   public boolean removeDataStickEntry(@NotNull String researchId, @NotNull GTRecipe recipe) {
      // Use the same map selection as addDataStickEntry: during a reload transaction both
      // operations must act on the staging map, otherwise a staged addition cannot be removed and
      // a removal would mutate the published map that abortStagingRecipes() expects to be intact.
      Map<String, Collection<GTRecipe>> entries = this.stagingResearchEntries != null
         ? this.stagingResearchEntries
         : this.researchEntries;
      Collection<GTRecipe> collection = entries.get(researchId);
      if (collection == null) {
         return false;
      } else if (collection.remove(recipe)) {
         return collection.isEmpty() ? entries.remove(researchId) != null : true;
      } else {
         return false;
      }
   }

   public RecipeHolder<GTRecipe> toGTRecipe(RecipeHolder<?> holder) {
      GTRecipeBuilder builder = this.recipeBuilder(holder.id());
      Recipe<?> recipe = holder.value();

      for (Ingredient ingredient : recipe.getIngredients()) {
         builder.inputItems(new SizedIngredient(ingredient, 1));
      }

      builder.outputItems(recipe.getResultItem(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
      if (recipe instanceof SmeltingRecipe smeltingRecipe) {
         builder.duration(smeltingRecipe.getCookingTime());
      }

      GTRecipe built = builder.build();
      return new RecipeHolder(built.id, built);
   }

   public void buildRepresentativeRecipes() {
      for (GTRecipeType.ICustomRecipeLogic logic : this.customRecipeLogicRunners) {
         logic.buildRepresentativeRecipes();
      }
   }

   public void addToMainCategory(GTRecipe recipe) {
      this.addToCategoryMap(this.category, recipe);
   }

   public void addToCategoryMap(GTRecipeCategory category, GTRecipe recipe) {
      Map<GTRecipeCategory, Set<GTRecipe>> categories = this.stagingCategoryMap != null
         ? this.stagingCategoryMap
         : this.categoryMap;
      categories.computeIfAbsent(category, k -> new ObjectLinkedOpenHashSet()).add(recipe);
   }

   public Set<GTRecipeCategory> getCategories() {
      return Collections.unmodifiableSet(this.categoryMap.keySet());
   }

   public Set<GTRecipe> getRecipesInCategory(GTRecipeCategory category) {
      return Collections.unmodifiableSet(this.categoryMap.getOrDefault(category, Set.of()));
   }

   public String getTranslationKey() {
      return this.registryName.toLanguageKey("recipe_type");
   }

   public Component getName() {
      return Component.translatable(this.getTranslationKey());
   }

   @NotNull
   public RecipeDB db() {
      return this.db;
   }

   @Internal
   public void beginStagingRecipes() {
      if (this.stagingCategoryMap != null || this.stagingResearchEntries != null || this.previousRecipeDb != null) {
         throw new IllegalStateException("cannot begin recipe index staging while already staging");
      }
      RecipeDB previousRecipeDb = this.db.snapshot();
      Map<GTRecipeCategory, Set<GTRecipe>> previousCategories = this.categoryMap;
      Map<String, Collection<GTRecipe>> previousResearch = this.researchEntries;
      Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> previousProxies = this.proxyRecipes;
      AbstractObject2IntMap<SizedFluidIngredient> previousFuels = this.registryName.equals(COMBUSTION_GENERATOR_ID)
         ? PowerlessJetpack.FUELS
         : null;
      Map<GTRecipeCategory, Set<GTRecipe>> stagedCategories = new Object2ObjectOpenHashMap();
      Map<String, Collection<GTRecipe>> stagedResearch = new Object2ObjectOpenHashMap();

      // This is the only state transition above that can reject the begin.
      // All allocations and snapshots are complete before it succeeds.
      this.additionHandler.beginStaging();
      this.previousRecipeDb = previousRecipeDb;
      this.previousCategoryMap = previousCategories;
      this.previousResearchEntries = previousResearch;
      this.previousProxyRecipes = previousProxies;
      this.previousCombustionFuels = previousFuels;
      this.stagingCategoryMap = stagedCategories;
      this.stagingResearchEntries = stagedResearch;
   }

   @Internal
   public void completeStagingRecipes() {
      commitStagingRecipes(prepareStagingRecipes(this.proxyRecipes));
      finishStagingRecipes();
   }

   @Internal
   public PreparedRecipeGeneration prepareStagingRecipes(
      Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes
   ) {
      Map<GTRecipeCategory, Set<GTRecipe>> categories = requireStagingCategories();
      Map<String, Collection<GTRecipe>> research = requireStagingResearchEntries();
      RecipeAdditionHandler.StagingResult lookup = this.additionHandler.prepareStaging();

      categories.clear();
      research.clear();
      for (GTRecipe recipe : lookup.recipes()) {
         categories.computeIfAbsent(recipe.recipeCategory, ignored -> new ObjectLinkedOpenHashSet()).add(recipe);
         indexResearchRecipe(research, recipe);
      }

      AbstractObject2IntMap<SizedFluidIngredient> combustionFuels = null;
      if (this.registryName.equals(COMBUSTION_GENERATOR_ID)) {
         combustionFuels = collectCombustionFuels(lookup.recipes());
      }
      return new PreparedRecipeGeneration(lookup, categories, research, proxyRecipes, combustionFuels);
   }

   @Internal
   public void commitStagingRecipes(PreparedRecipeGeneration generation) {
      if (generation.categories() != requireStagingCategories() ||
         generation.researchEntries() != requireStagingResearchEntries()) {
         throw new IllegalArgumentException("prepared recipe generation belongs to another staging cycle");
      }

      this.additionHandler.commitStaging(generation.lookup());
      this.categoryMap = generation.categories();
      this.researchEntries = generation.researchEntries();
      this.proxyRecipes = generation.proxyRecipes();
      if (generation.combustionFuels() != null) {
         PowerlessJetpack.FUELS = generation.combustionFuels();
      }
      this.stagingCategoryMap = null;
      this.stagingResearchEntries = null;
   }

   /**
    * Finish a reload transaction after every recipe type and the manager's
    * vanilla indexes have been published successfully.
    */
   @Internal
   public void finishStagingRecipes() {
      clearPreviousRecipeState();
   }

   @Internal
   public void abortStagingRecipes() {
      this.additionHandler.abortStaging();
      if (this.previousRecipeDb != null) {
         this.db.replaceWith(this.previousRecipeDb);
      }
      if (this.previousCategoryMap != null) {
         this.categoryMap = this.previousCategoryMap;
      }
      if (this.previousResearchEntries != null) {
         this.researchEntries = this.previousResearchEntries;
      }
      if (this.previousProxyRecipes != null) {
         this.proxyRecipes = this.previousProxyRecipes;
      }
      if (this.registryName.equals(COMBUSTION_GENERATOR_ID) && this.previousCombustionFuels != null) {
         PowerlessJetpack.FUELS = this.previousCombustionFuels;
      }
      this.stagingCategoryMap = null;
      this.stagingResearchEntries = null;
      clearPreviousRecipeState();
   }

   private void clearPreviousRecipeState() {
      this.previousRecipeDb = null;
      this.previousCategoryMap = null;
      this.previousResearchEntries = null;
      this.previousProxyRecipes = null;
      this.previousCombustionFuels = null;
   }

   @Internal
   public void rebuildDisplayIndexes(Iterable<RecipeHolder<?>> recipes) {
      Map<GTRecipeCategory, Set<GTRecipe>> categories = new Object2ObjectOpenHashMap();
      Map<String, Collection<GTRecipe>> research = new Object2ObjectOpenHashMap();
      for (RecipeHolder<?> holder : recipes) {
         if (holder.value() instanceof GTRecipe recipe && recipe.recipeType == this) {
            recipe.setId(holder.id());
            categories.computeIfAbsent(recipe.recipeCategory, ignored -> new ObjectLinkedOpenHashSet()).add(recipe);
            indexResearchRecipe(research, recipe);
         }
      }
      this.categoryMap = categories;
      this.researchEntries = research;
   }

   @Internal
   public void rebuildClientRecipeIndexes(RecipeManager manager) {
      Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxies = new Object2ObjectLinkedOpenHashMap<>();
      List<RecipeHolder<?>> recipes = new ArrayList<>(manager.getAllRecipesFor(this));
      for (RecipeType<?> proxyType : this.proxyRecipes.keySet()) {
         List<RecipeHolder<GTRecipe>> converted = new ArrayList<>();
         for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value().getType() == proxyType) {
               converted.add(toGTRecipe(holder));
            }
         }
         proxies.put(proxyType, converted);
         recipes.addAll(converted);
      }
      this.proxyRecipes = proxies;
      rebuildDisplayIndexes(recipes);
      if (this.registryName.equals(COMBUSTION_GENERATOR_ID)) {
         List<GTRecipe> fuelRecipes = recipes.stream()
            .map(RecipeHolder::value)
            .filter(GTRecipe.class::isInstance)
            .map(GTRecipe.class::cast)
            .toList();
         PowerlessJetpack.FUELS = collectCombustionFuels(fuelRecipes);
      }
   }

   private Map<GTRecipeCategory, Set<GTRecipe>> requireStagingCategories() {
      if (this.stagingCategoryMap == null) {
         throw new IllegalStateException("cannot complete recipe index staging while not staging");
      }
      return this.stagingCategoryMap;
   }

   private Map<String, Collection<GTRecipe>> requireStagingResearchEntries() {
      if (this.stagingResearchEntries == null) {
         throw new IllegalStateException("cannot complete research index staging while not staging");
      }
      return this.stagingResearchEntries;
   }

   private static void indexResearchRecipe(Map<String, Collection<GTRecipe>> index, GTRecipe recipe) {
      recipe.conditions.stream()
         .filter(ResearchCondition.class::isInstance)
         .map(ResearchCondition.class::cast)
         .findAny()
         .ifPresent(condition -> {
            for (ResearchData.ResearchEntry entry : condition.data) {
               index.computeIfAbsent(entry.researchId(), ignored -> new ObjectOpenHashSet()).add(recipe);
            }
         });
   }

   private static AbstractObject2IntMap<SizedFluidIngredient> collectCombustionFuels(List<GTRecipe> recipes) {
      AbstractObject2IntMap<SizedFluidIngredient> fuels = new Object2IntOpenHashMap<>();
      for (GTRecipe recipe : recipes) {
         List<Content> inputs = recipe.getInputContents(FluidRecipeCapability.CAP);
         if (!inputs.isEmpty()) {
            fuels.putIfAbsent(FluidRecipeCapability.CAP.of(inputs.getFirst().content()), recipe.duration);
         }
      }
      return fuels;
   }

   @Internal
   public record PreparedRecipeGeneration(
      @NotNull RecipeAdditionHandler.StagingResult lookup,
      @NotNull Map<GTRecipeCategory, Set<GTRecipe>> categories,
      @NotNull Map<String, Collection<GTRecipe>> researchEntries,
      @NotNull Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> proxyRecipes,
      @Nullable AbstractObject2IntMap<SizedFluidIngredient> combustionFuels
   ) {}

   @Generated
   public GTRecipeSerializer getSerializer() {
      return this.serializer;
   }

   @NotNull
   @Internal
   @Generated
   public GTRecipeType setSerializer(GTRecipeSerializer serializer) {
      this.serializer = serializer;
      return this;
   }

   @Generated
   public ResourceLocation getRegistryName() {
      return this.registryName;
   }

   @Generated
   public GTRecipeBuilder getRecipeBuilder() {
      return this.recipeBuilder;
   }

   @NotNull
   @Generated
   public GTRecipeType setRecipeBuilder(GTRecipeBuilder recipeBuilder) {
      this.recipeBuilder = recipeBuilder;
      return this;
   }

   @Generated
   public ChanceBoostFunction getChanceFunction() {
      return this.chanceFunction;
   }

   @NotNull
   @Generated
   public GTRecipeType setChanceFunction(ChanceBoostFunction chanceFunction) {
      this.chanceFunction = chanceFunction;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeType setSmallRecipeMap(GTRecipeType smallRecipeMap) {
      this.smallRecipeMap = smallRecipeMap;
      return this;
   }

   @Generated
   public GTRecipeType getSmallRecipeMap() {
      return this.smallRecipeMap;
   }

   @NotNull
   @Generated
   public GTRecipeType setIconSupplier(@Nullable Supplier<ItemStack> iconSupplier) {
      this.iconSupplier = iconSupplier;
      return this;
   }

   @Nullable
   @Generated
   public Supplier<ItemStack> getIconSupplier() {
      return this.iconSupplier;
   }

   @NotNull
   @Generated
   public GTRecipeType setSound(@Nullable SoundEntry sound) {
      this.sound = sound;
      return this;
   }

   @Nullable
   @Generated
   public SoundEntry getSound() {
      return this.sound;
   }

   @Generated
   public List<Function<CompoundTag, String>> getDataInfos() {
      return this.dataInfos;
   }

   @Generated
   public boolean isScanner() {
      return this.isScanner;
   }

   @NotNull
   @Generated
   public GTRecipeType setScanner(boolean isScanner) {
      this.isScanner = isScanner;
      return this;
   }

   @Generated
   public boolean isHasResearchSlot() {
      return this.hasResearchSlot;
   }

   @NotNull
   @Generated
   public GTRecipeType setHasResearchSlot(boolean hasResearchSlot) {
      this.hasResearchSlot = hasResearchSlot;
      return this;
   }

   @Generated
   public Map<RecipeType<?>, List<RecipeHolder<GTRecipe>>> getProxyRecipes() {
      return this.proxyRecipes;
   }

   @Generated
   public GTRecipeCategory getCategory() {
      return this.category;
   }

   @Generated
   public Map<GTRecipeCategory, Set<GTRecipe>> getCategoryMap() {
      return this.categoryMap;
   }

   @Generated
   public RecipeAdditionHandler getAdditionHandler() {
      return this.additionHandler;
   }

   @NotNull
   @Generated
   public GTRecipeType setOffsetVoltageText(boolean offsetVoltageText) {
      this.offsetVoltageText = offsetVoltageText;
      return this;
   }

   @Generated
   public boolean isOffsetVoltageText() {
      return this.offsetVoltageText;
   }

   @NotNull
   @Generated
   public GTRecipeType setVoltageTextOffset(int voltageTextOffset) {
      this.voltageTextOffset = voltageTextOffset;
      return this;
   }

   @Generated
   public int getVoltageTextOffset() {
      return this.voltageTextOffset;
   }

   @Generated
   public List<GTRecipeType.ICustomRecipeLogic> getCustomRecipeLogicRunners() {
      return this.customRecipeLogicRunners;
   }

   @Generated
   public int getMinRecipeConditions() {
      return this.minRecipeConditions;
   }

   @Generated
   public GTRecipeTypeUILayout getUiLayout() {
      return this.uiLayout;
   }

   @NotNull
   @Generated
   public GTRecipeType setUiLayout(GTRecipeTypeUILayout uiLayout) {
      this.uiLayout = uiLayout;
      return this;
   }

   public interface ICustomRecipeLogic {
      @Nullable
      GTRecipe createCustomRecipe(IRecipeCapabilityHolder var1);

      default void buildRepresentativeRecipes() {
      }
   }
}
