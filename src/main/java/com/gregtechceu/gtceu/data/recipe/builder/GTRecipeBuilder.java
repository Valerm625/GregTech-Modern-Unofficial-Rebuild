package com.gregtechceu.gtceu.data.recipe.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.ResearchRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentBlockCondition;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.common.recipe.condition.BiomeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.BiomeTagCondition;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.common.recipe.condition.DaytimeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.common.recipe.condition.EnvironmentalHazardCondition;
import com.gregtechceu.gtceu.common.recipe.condition.FTBQuestCondition;
import com.gregtechceu.gtceu.common.recipe.condition.HeraclesQuestCondition;
import com.gregtechceu.gtceu.common.recipe.condition.PositionYCondition;
import com.gregtechceu.gtceu.common.recipe.condition.RainingCondition;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;
import com.gregtechceu.gtceu.common.recipe.condition.ThunderCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.gregtechceu.gtceu.utils.codec.CodecUtils;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.Generated;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTRecipeBuilder {
   public final Map<RecipeCapability<?>, List<Content>> input = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, List<Content>> tickInput = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, List<Content>> output = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, List<Content>> tickOutput = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, ChanceLogic> inputChanceLogic = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, ChanceLogic> outputChanceLogic = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogic = new IdentityHashMap<>();
   public final Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogic = new IdentityHashMap<>();
   public final List<RecipeCondition<?>> conditions = new ArrayList<>();
   @NotNull
   public CompoundTag data = new CompoundTag();
   public ResourceLocation id;
   public GTRecipeType recipeType;
   public int duration = 100;
   public boolean perTick;
   public int chance = ChanceLogic.getMaxChancedValue();
   public int maxChance = ChanceLogic.getMaxChancedValue();
   public int tierChanceBoost = 0;
   private boolean itemMaterialInfo = false;
   private boolean fluidMaterialInfo = false;
   private boolean removePreviousMatInfo = false;
   public GTRecipeCategory recipeCategory;
   @Nullable
   public BiConsumer<GTRecipeBuilder, RecipeOutput> onSave;
   private final Collection<GTRecipeBuilder.ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
   private boolean generatingRecipes = true;
   private List<ItemStack> tempItemStacks = new ArrayList<>();
   private List<MaterialStack> tempItemMaterialStacks = new ArrayList<>();
   private List<MaterialStack> tempFluidStacks = new ArrayList<>();

   public GTRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
      this.id = id;
      this.recipeType = recipeType;
      this.recipeCategory = recipeType.getCategory();
   }

   public GTRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
      this.id = toCopy.id;
      this.recipeType = recipeType;
      toCopy.inputs.forEach((k, v) -> this.input.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      toCopy.outputs.forEach((k, v) -> this.output.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      toCopy.tickInputs.forEach((k, v) -> this.tickInput.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      toCopy.tickOutputs.forEach((k, v) -> this.tickOutput.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      this.inputChanceLogic.putAll(toCopy.inputChanceLogics);
      this.outputChanceLogic.putAll(toCopy.outputChanceLogics);
      this.tickInputChanceLogic.putAll(toCopy.tickInputChanceLogics);
      this.tickOutputChanceLogic.putAll(toCopy.tickOutputChanceLogics);
      this.conditions.addAll(toCopy.conditions);
      this.data = toCopy.data.copy();
      this.duration = toCopy.duration;
      this.recipeCategory = toCopy.recipeCategory;
   }

   public static GTRecipeBuilder of(ResourceLocation id, GTRecipeType recipeType) {
      return new GTRecipeBuilder(id, recipeType);
   }

   public static GTRecipeBuilder ofRaw() {
      return new GTRecipeBuilder(GTCEu.id("raw"), GTRecipeTypes.DUMMY_RECIPES);
   }

   public GTRecipeBuilder copy(String id) {
      return this.copy(GTCEu.id(id));
   }

   public GTRecipeBuilder copy(ResourceLocation id) {
      GTRecipeBuilder copy = new GTRecipeBuilder(id, this.recipeType);
      this.input.forEach((k, v) -> copy.input.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      this.output.forEach((k, v) -> copy.output.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      this.tickInput.forEach((k, v) -> copy.tickInput.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      this.tickOutput.forEach((k, v) -> copy.tickOutput.put((RecipeCapability<?>)k, new ArrayList<>(v)));
      copy.inputChanceLogic.putAll(this.inputChanceLogic);
      copy.outputChanceLogic.putAll(this.outputChanceLogic);
      copy.tickInputChanceLogic.putAll(this.tickInputChanceLogic);
      copy.tickOutputChanceLogic.putAll(this.tickOutputChanceLogic);
      copy.conditions.addAll(this.conditions);
      copy.data = this.data.copy();
      copy.duration = this.duration;
      copy.chance = this.chance;
      copy.maxChance = this.maxChance;
      copy.perTick = this.perTick;
      copy.tierChanceBoost = this.tierChanceBoost;
      copy.recipeCategory = this.recipeCategory;
      copy.onSave = this.onSave;
      return copy;
   }

   public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
      this.recipeType.setMinRecipeConditions(builder.conditions.size());
      return builder.copy(builder.id).onSave(null).recipeType(this.recipeType).category(this.recipeCategory);
   }

   protected Content makeContent(Object o) {
      return new Content(o, this.chance, this.maxChance, this.tierChanceBoost);
   }

   public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T obj) {
      Map<RecipeCapability<?>, List<Content>> t = this.perTick ? this.tickInput : this.input;
      this.warnTooManyIngredients(capability, true, t, 1);
      t.computeIfAbsent(capability, c -> new ArrayList<>()).add(this.makeContent(capability.of(obj)));
      return this;
   }

   public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
      Map<RecipeCapability<?>, List<Content>> t = this.perTick ? this.tickInput : this.input;
      this.warnTooManyIngredients(capability, true, t, obj.length);
      t.computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.<Object>stream(obj).map(capability::of).map(this::makeContent).toList());
      return this;
   }

   public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T obj) {
      Map<RecipeCapability<?>, List<Content>> t = this.perTick ? this.tickOutput : this.output;
      this.warnTooManyIngredients(capability, false, t, 1);
      t.computeIfAbsent(capability, c -> new ArrayList<>()).add(this.makeContent(capability.of(obj)));
      return this;
   }

   public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
      Map<RecipeCapability<?>, List<Content>> t = this.perTick ? this.tickOutput : this.output;
      this.warnTooManyIngredients(capability, false, t, obj.length);
      t.computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.<Object>stream(obj).map(capability::of).map(this::makeContent).toList());
      return this;
   }

   public GTRecipeBuilder addCondition(RecipeCondition<?> condition) {
      this.conditions.add(condition);
      this.recipeType.setMinRecipeConditions(this.conditions.size());
      return this;
   }

   public GTRecipeBuilder duration(int duration) {
      if (duration < 0) {
         GTCEu.LOGGER.error("Recipe duration must be non negative, id: {}", this.id);
      }

      this.duration = Math.max(duration, 0);
      return this;
   }

   public GTRecipeBuilder inputEU(long eu) {
      return this.inputEU(eu, 1L);
   }

   public GTRecipeBuilder inputEU(long voltage, long amperage) {
      return this.input(EURecipeCapability.CAP, new EnergyStack(voltage, amperage));
   }

   public GTRecipeBuilder EUt(long eu) {
      return this.EUt(eu, 1L);
   }

   public GTRecipeBuilder EUt(long voltage, long amperage) {
      if (voltage == 0L) {
         GTCEu.LOGGER.error("EUt can't be explicitly set to 0, id: {}", this.id);
      }

      if (amperage < 1L) {
         GTCEu.LOGGER.error("Amperage must be a positive integer, id: {}", this.id);
      }

      boolean lastPerTick = this.perTick;
      this.perTick = true;
      if (voltage > 0L) {
         this.tickInput.remove(EURecipeCapability.CAP);
         this.inputEU(voltage, amperage);
      } else if (voltage < 0L) {
         this.tickOutput.remove(EURecipeCapability.CAP);
         this.outputEU(-voltage, amperage);
      }

      this.perTick = lastPerTick;
      return this;
   }

   public GTRecipeBuilder outputEU(long eu) {
      return this.outputEU(eu, 1L);
   }

   public GTRecipeBuilder outputEU(long voltage, long amperage) {
      return this.output(EURecipeCapability.CAP, new EnergyStack(voltage, amperage));
   }

   public GTRecipeBuilder inputCWU(int cwu) {
      return this.input(CWURecipeCapability.CAP, cwu);
   }

   public GTRecipeBuilder CWUt(int cwu) {
      if (cwu == 0) {
         GTCEu.LOGGER.error("CWUt can't be explicitly set to 0, id: {}", this.id);
      }

      boolean lastPerTick = this.perTick;
      this.perTick = true;
      if (cwu > 0) {
         this.tickInput.remove(CWURecipeCapability.CAP);
         this.inputCWU(cwu);
      } else if (cwu < 0) {
         this.tickOutput.remove(CWURecipeCapability.CAP);
         this.outputCWU(-cwu);
      }

      this.perTick = lastPerTick;
      return this;
   }

   public GTRecipeBuilder totalCWU(int cwu) {
      this.durationIsTotalCWU(true);
      this.hideDuration(true);
      this.duration(cwu);
      return this;
   }

   public GTRecipeBuilder outputCWU(int cwu) {
      return this.output(CWURecipeCapability.CAP, cwu);
   }

   public GTRecipeBuilder inputItems(Object input) {
      Objects.requireNonNull(input);

      return switch (input) {
         case Item itemx -> this.inputItems(itemx);
         case Supplier supplier when supplier.get() instanceof ItemLike item -> this.inputItems(item.asItem());
         case ItemStack stack -> this.inputItems(stack);
         case Ingredient ingredient -> this.inputItems(ingredient);
         case SizedIngredient ingredientx -> this.inputItems(ingredientx);
         case MaterialEntry entry -> this.inputItems(entry);
         case TagKey tag -> this.inputItems((TagKey<Item>)tag);
         case MachineDefinition machine -> this.inputItems(machine);
         case IntProviderIngredient ingredientxx -> this.inputItems(
         new SizedIngredient(ingredientxx.toVanilla(), ingredientxx.getCountProvider().getMaxValue())
      );
         default -> {
            GTCEu.LOGGER
               .error(
                  "Input item is not one of:\nItem, Supplier<Item>, ItemStack, Ingredient, SizedIngredient, MaterialEntry, TagKey<Item>, MachineDefinition\nid: {}",
                  this.id
               );
            yield this;
         }
      };
   }

   public GTRecipeBuilder inputItems(Object input, int count) {
      Objects.requireNonNull(input);

      return switch (input) {
         case Item itemx -> this.inputItems(itemx, count);
         case Supplier supplier when supplier.get() instanceof ItemLike item -> this.inputItems(item.asItem(), count);
         case ItemStack stack -> this.inputItems(stack.copyWithCount(count));
         case Ingredient ingredient -> this.inputItems(ingredient, count);
         case SizedIngredient ingredientx -> this.inputItems(ingredientx.ingredient(), count);
         case MaterialEntry entry -> this.inputItems(entry, count);
         case TagKey tag -> this.inputItems((TagKey<Item>)tag, count);
         case MachineDefinition machine -> this.inputItems(machine, count);
         case IntProviderIngredient ingredientxx -> this.inputItems(
         new SizedIngredient(ingredientxx.toVanilla(), ingredientxx.getCountProvider().getMaxValue())
      );
         default -> {
            GTCEu.LOGGER
               .error(
                  "Input item is not one of:\nItem, Supplier<Item>, ItemStack, Ingredient, SizedIngredient, MaterialEntry, TagKey<Item>, MachineDefinition\nid: {}",
                  this.id
               );
            yield this;
         }
      };
   }

   public GTRecipeBuilder inputItems(Ingredient input) {
      return this.missingIngredientError(0, true, ItemRecipeCapability.CAP, input::isEmpty)
         ? this
         : this.input(ItemRecipeCapability.CAP, new SizedIngredient(input, 1));
   }

   public GTRecipeBuilder inputItems(Ingredient input, int count) {
      return this.missingIngredientError(0, true, ItemRecipeCapability.CAP, input::isEmpty)
         ? this
         : this.input(ItemRecipeCapability.CAP, new SizedIngredient(input, count));
   }

   public GTRecipeBuilder inputItems(Ingredient... inputs) {
      List<SizedIngredient> ingredients = new ArrayList<>();

      for (int i = 0; i < inputs.length; i++) {
         Ingredient ingredient = inputs[i];
         if (this.missingIngredientError(i, true, ItemRecipeCapability.CAP, ingredient::isEmpty)) {
            return this;
         }

         ingredients.add(new SizedIngredient(ingredient, 1));
      }

      return this.input(ItemRecipeCapability.CAP, ingredients.toArray(SizedIngredient[]::new));
   }

   public GTRecipeBuilder inputItems(SizedIngredient input) {
      return this.missingIngredientError(0, true, ItemRecipeCapability.CAP, input.ingredient()::isEmpty) ? this : this.input(ItemRecipeCapability.CAP, input);
   }

   public GTRecipeBuilder inputItems(SizedIngredient... inputs) {
      List<SizedIngredient> ingredients = new ArrayList<>();

      for (int i = 0; i < inputs.length; i++) {
         SizedIngredient ingredient = inputs[i];
         if (this.missingIngredientError(i, true, ItemRecipeCapability.CAP, ingredient.ingredient()::isEmpty)) {
            return this;
         }

         ingredients.add(ingredient);
      }

      return this.input(ItemRecipeCapability.CAP, ingredients.toArray(SizedIngredient[]::new));
   }

   public GTRecipeBuilder inputItems(ItemStack input) {
      if (this.missingIngredientError(0, true, ItemRecipeCapability.CAP, input::isEmpty)) {
         return this;
      } else {
         this.gatherMaterialInfoFromStack(input);
         return this.input(ItemRecipeCapability.CAP, RecipeHelper.makeSizedIngredient(input));
      }
   }

   public GTRecipeBuilder inputItems(ItemStack... inputs) {
      List<SizedIngredient> ingredients = new ArrayList<>();

      for (int i = 0; i < inputs.length; i++) {
         ItemStack itemStack = inputs[i];
         if (this.missingIngredientError(i, true, ItemRecipeCapability.CAP, itemStack::isEmpty)) {
            return this;
         }

         this.gatherMaterialInfoFromStack(itemStack);
         ingredients.add(RecipeHelper.makeSizedIngredient(itemStack));
      }

      return this.input(ItemRecipeCapability.CAP, ingredients.toArray(SizedIngredient[]::new));
   }

   public GTRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
      return this.inputItems(SizedIngredient.of(tag, amount));
   }

   public GTRecipeBuilder inputItems(TagKey<Item> tag) {
      return this.inputItems(tag, 1);
   }

   public GTRecipeBuilder inputItems(Item input, int amount) {
      return this.inputItems(new ItemStack(input, amount));
   }

   public GTRecipeBuilder inputItems(Item input) {
      return this.inputItems(input, 1);
   }

   public GTRecipeBuilder inputItems(Supplier<? extends Item> input) {
      return this.inputItems(input.get());
   }

   public GTRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
      return this.inputItems(input.get(), amount);
   }

   public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
      return this.inputItems(orePrefix, material, 1);
   }

   public GTRecipeBuilder inputItems(MaterialEntry input) {
      return this.inputItems(input, 1);
   }

   public GTRecipeBuilder inputItems(MaterialEntry input, int count) {
      return this.inputItems(input.tagPrefix(), input.material(), count);
   }

   public GTRecipeBuilder inputItems(TagPrefix tagPrefix, @NotNull Material material, int count) {
      if (!tagPrefix.isEmpty() && !material.isNull()) {
         this.tempItemMaterialStacks.add(new MaterialStack(material, tagPrefix.getMaterialAmount(material) * (long)count));
         tagPrefix.secondaryMaterials().forEach(mat -> this.tempItemMaterialStacks.add(mat.multiply((long)count)));
         TagKey<Item> tag = ChemicalHelper.getTag(tagPrefix, material);
         if (tag != null) {
            return this.inputItems(tag, count);
         } else {
            ItemStack item = ChemicalHelper.get(tagPrefix, material, count);
            if (item.isEmpty()) {
               GTCEu.LOGGER
                  .error(
                     "Tried to set input item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}", this.id, tagPrefix, material, count
                  );
            }

            return this.input(ItemRecipeCapability.CAP, RecipeHelper.makeSizedIngredient(item));
         }
      } else {
         GTCEu.LOGGER
            .error("Tried to set input item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}", this.id, tagPrefix, material, count);
         return this;
      }
   }

   public GTRecipeBuilder inputItems(MachineDefinition machine) {
      return this.inputItems(machine, 1);
   }

   public GTRecipeBuilder inputItems(MachineDefinition machine, int count) {
      return this.inputItems(machine.asStack(count));
   }

   public GTRecipeBuilder inputItemRanged(IntProviderIngredient provider) {
      return this.inputItems(provider);
   }

   public GTRecipeBuilder inputItemsRanged(ItemStack input, IntProvider intProvider) {
      return this.inputItemRanged(IntProviderIngredient.of(input, intProvider));
   }

   public GTRecipeBuilder inputItemsRanged(Item input, IntProvider intProvider) {
      return this.inputItemsRanged(new ItemStack(input), intProvider);
   }

   public GTRecipeBuilder inputItemsRanged(Supplier<? extends ItemLike> input, IntProvider intProvider) {
      return this.inputItemsRanged(new ItemStack(input.get().asItem()), intProvider);
   }

   public GTRecipeBuilder inputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
      ItemStack item = ChemicalHelper.get(orePrefix, material, 1);
      if (item.isEmpty()) {
         GTCEu.LOGGER.error("Tried to set input ranged item stack that doesn't exist, TagPrefix: {}, Material: {}", orePrefix, material);
      }

      return this.inputItemsRanged(item, intProvider);
   }

   public GTRecipeBuilder inputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
      return this.inputItemsRanged(machine.asStack(), intProvider);
   }

   public GTRecipeBuilder outputItems(Object output) {
      Objects.requireNonNull(output);

      return switch (output) {
         case Item itemx -> this.outputItems(itemx);
         case Ingredient ingredient -> this.outputItems(ingredient);
         case Supplier supplier when supplier.get() instanceof ItemLike item -> this.outputItems(item.asItem());
         case ItemStack stack -> this.outputItems(stack);
         case MaterialEntry entry -> this.outputItems(entry);
         case MachineDefinition machine -> this.outputItems(machine);
         case IntProviderIngredient ingredientx -> this.outputItems(new SizedIngredient(ingredientx.toVanilla(), ingredientx.getCountProvider().getMaxValue()));
         default -> {
            GTCEu.LOGGER.error("Output item is not one of:\nItem, Supplier<Item>, ItemStack, MaterialEntry, MachineDefinition\nid: {}", this.id);
            yield this;
         }
      };
   }

   public GTRecipeBuilder outputItems(Object output, int count) {
      Objects.requireNonNull(output);

      return switch (output) {
         case Item itemx -> this.outputItems(itemx, count);
         case Ingredient ingredient -> this.outputItems(ingredient, count);
         case Supplier supplier when supplier.get() instanceof ItemLike item -> this.outputItems(item.asItem(), count);
         case ItemStack stack -> this.outputItems(stack.copyWithCount(count));
         case MaterialEntry entry -> this.outputItems(entry, count);
         case MachineDefinition machine -> this.outputItems(machine, count);
         case IntProviderIngredient ingredientx -> this.outputItems(new SizedIngredient(ingredientx.toVanilla(), ingredientx.getCountProvider().getMaxValue()));
         default -> {
            GTCEu.LOGGER.error("Output item is not one of:\nItem, Supplier<Item>, ItemStack, MaterialEntry, MachineDefinition\nid: {}", this.id);
            yield this;
         }
      };
   }

   public GTRecipeBuilder outputItems(Ingredient input) {
      return this.outputItems(input, 1);
   }

   public GTRecipeBuilder outputItems(Ingredient input, int count) {
      return this.output(ItemRecipeCapability.CAP, new SizedIngredient(input, count));
   }

   public GTRecipeBuilder outputItems(ItemStack output) {
      return this.missingIngredientError(0, false, ItemRecipeCapability.CAP, output::isEmpty)
         ? this
         : this.output(ItemRecipeCapability.CAP, RecipeHelper.makeSizedIngredient(output));
   }

   public GTRecipeBuilder outputItems(ItemStack... outputs) {
      List<SizedIngredient> ingredients = new ArrayList<>();

      for (int i = 0; i < outputs.length; i++) {
         ItemStack itemStack = outputs[i];
         if (this.missingIngredientError(i, false, ItemRecipeCapability.CAP, itemStack::isEmpty)) {
            return this;
         }

         ingredients.add(RecipeHelper.makeSizedIngredient(itemStack));
      }

      return this.output(ItemRecipeCapability.CAP, ingredients.toArray(SizedIngredient[]::new));
   }

   public GTRecipeBuilder outputItems(Item output, int amount) {
      return this.outputItems(new ItemStack(output, amount));
   }

   public GTRecipeBuilder outputItems(Item output) {
      return this.outputItems(new ItemStack(output));
   }

   public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
      return this.outputItems(new ItemStack(input.get().asItem()));
   }

   public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
      return this.outputItems(new ItemStack(input.get().asItem(), amount));
   }

   public GTRecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
      return this.outputItems(orePrefix, material, 1);
   }

   public GTRecipeBuilder outputItems(TagPrefix orePrefix, @NotNull Material material, int count) {
      if (!orePrefix.isEmpty() && !material.isNull()) {
         ItemStack item = ChemicalHelper.get(orePrefix, material, count);
         if (item.isEmpty()) {
            GTCEu.LOGGER
               .error("Tried to set output item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}", this.id, orePrefix, material, count);
            return this;
         } else {
            return this.outputItems(item);
         }
      } else {
         GTCEu.LOGGER
            .error("Tried to set output item stack that doesn't exist, id: {}, TagPrefix: {}, Material: {}, Count: {}", this.id, orePrefix, material, count);
         return this;
      }
   }

   public GTRecipeBuilder outputItems(MaterialEntry entry) {
      return this.outputItems(entry.tagPrefix(), entry.material());
   }

   public GTRecipeBuilder outputItems(MaterialEntry entry, int count) {
      return this.outputItems(entry.tagPrefix(), entry.material(), count);
   }

   public GTRecipeBuilder outputItems(MachineDefinition machine) {
      return this.outputItems(machine, 1);
   }

   public GTRecipeBuilder outputItems(MachineDefinition machine, int count) {
      return this.outputItems(machine.asStack(count));
   }

   protected GTRecipeBuilder outputItems(SizedIngredient ingredient) {
      return this.output(ItemRecipeCapability.CAP, ingredient);
   }

   public GTRecipeBuilder outputItemRanged(IntProviderIngredient provider) {
      return this.outputItems(provider);
   }

   public GTRecipeBuilder outputItemsRanged(ItemStack output, IntProvider intProvider) {
      return this.outputItemRanged(IntProviderIngredient.of(output, intProvider));
   }

   public GTRecipeBuilder outputItemsRanged(Item input, IntProvider intProvider) {
      return this.outputItemsRanged(new ItemStack(input), intProvider);
   }

   public GTRecipeBuilder outputItemsRanged(Supplier<? extends ItemLike> output, IntProvider intProvider) {
      return this.outputItemsRanged(new ItemStack(output.get().asItem()), intProvider);
   }

   public GTRecipeBuilder outputItemsRanged(TagPrefix orePrefix, Material material, IntProvider intProvider) {
      ItemStack item = ChemicalHelper.get(orePrefix, material, 1);
      if (item.isEmpty()) {
         GTCEu.LOGGER.error("Tried to set output ranged item stack that doesn't exist, TagPrefix: {}, Material: {}", orePrefix, material);
      }

      return this.outputItemsRanged(item, intProvider);
   }

   public GTRecipeBuilder outputItemsRanged(MachineDefinition machine, IntProvider intProvider) {
      return this.outputItemsRanged(machine.asStack(), intProvider);
   }

   public GTRecipeBuilder notConsumable(ItemStack itemStack) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(itemStack);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumable(Ingredient ingredient) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(ingredient);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumable(Item item) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(item);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumable(Supplier<? extends Item> item) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(item);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(orePrefix, material);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumable(TagPrefix orePrefix, Material material, int count) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputItems(orePrefix, material, count);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder notConsumableFluid(FluidStack fluid) {
      return this.notConsumableFluid(RecipeHelper.makeSizedFluidIngredient(fluid));
   }

   public GTRecipeBuilder notConsumableFluid(SizedFluidIngredient ingredient) {
      int lastChance = this.chance;
      this.chance = 0;
      this.inputFluids(ingredient);
      this.chance = lastChance;
      return this;
   }

   public GTRecipeBuilder circuitMeta(int configuration) {
      if (configuration < 0 || configuration > 32) {
         GTCEu.LOGGER.error("Circuit configuration must be in the bounds 0 - 32");
      }

      return this.notConsumable(IntCircuitIngredient.circuit(configuration));
   }

   public GTRecipeBuilder chancedInput(Ingredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.inputItems(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedInput(Ingredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.inputItems(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedInput(SizedIngredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.inputItems(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedInput(SizedIngredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.inputItems(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedInput(SizedFluidIngredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.inputFluids(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedInput(SizedFluidIngredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.inputFluids(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(Ingredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.outputItems(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(Ingredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.outputItems(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(SizedIngredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.outputItems(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(SizedIngredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.outputItems(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(SizedFluidIngredient stack, int chance) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         this.chance = chance;
         this.outputFluids(stack);
         this.chance = lastChance;
         return this;
      }
   }

   public GTRecipeBuilder chancedOutput(SizedFluidIngredient stack, int chance, int tierChanceBoost) {
      if (this.checkChanceAndPrintError(chance)) {
         return this;
      } else {
         int lastChance = this.chance;
         int lastTierChanceBoost = this.tierChanceBoost;
         this.chance = chance;
         this.tierChanceBoost = tierChanceBoost;

         try {
            this.outputFluids(stack);
         } finally {
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
         }

         return this;
      }
   }

   public GTRecipeBuilder chancedInput(ItemStack stack, int chance) {
      return this.chancedInput(SizedIngredient.of(stack.getItem(), stack.getCount()), chance);
   }

   public GTRecipeBuilder chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
      return this.chancedInput(SizedIngredient.of(stack.getItem(), stack.getCount()), chance, tierChanceBoost);
   }

   public GTRecipeBuilder chancedInput(FluidStack stack, int chance) {
      return this.chancedInput(SizedFluidIngredient.of(stack), chance);
   }

   public GTRecipeBuilder chancedInput(FluidStack stack, int chance, int tierChanceBoost) {
      return this.chancedInput(SizedFluidIngredient.of(stack), chance, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(ItemStack stack, int chance) {
      return this.chancedOutput(SizedIngredient.of(stack.getItem(), stack.getCount()), chance);
   }

   public GTRecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
      return this.chancedOutput(SizedIngredient.of(stack.getItem(), stack.getCount()), chance, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(FluidStack stack, int chance) {
      return this.chancedOutput(SizedFluidIngredient.of(stack), chance);
   }

   public GTRecipeBuilder chancedOutput(FluidStack stack, int chance, int tierChanceBoost) {
      return this.chancedOutput(SizedFluidIngredient.of(stack), chance, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance) {
      return this.chancedOutput(ChemicalHelper.get(tag, mat), chance);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance) {
      return this.chancedOutput(ChemicalHelper.get(tag, mat, count), chance);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
      return this.chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(ItemStack stack, String fraction) {
      if (stack.isEmpty()) {
         return this;
      } else {
         String[] split = fraction.split("/");
         if (split.length != 2) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".", fraction, new Throwable());
            return this;
         } else {
            int chance;
            int maxChance;
            try {
               chance = Integer.parseInt(split[0]);
               maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException var9) {
               GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".", fraction, new Throwable());
               return this;
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
               GTCEu.LOGGER
                  .error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", ChanceLogic.getMaxChancedValue(), chance, new Throwable());
               return this;
            } else if (chance < maxChance && maxChance <= ChanceLogic.getMaxChancedValue()) {
               int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
               chance *= scalar;
               maxChance *= scalar;
               int lastChance = this.chance;
               int lastMaxChance = this.maxChance;
               this.chance = chance;
               this.maxChance = maxChance;
               this.outputItems(stack);
               this.chance = lastChance;
               this.maxChance = lastMaxChance;
               return this;
            } else {
               GTCEu.LOGGER
                  .error(
                     "Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.", ChanceLogic.getMaxChancedValue(), maxChance, new Throwable()
                  );
               return this;
            }
         }
      }
   }

   public GTRecipeBuilder chancedOutput(ItemStack stack, String fraction, int tierChanceBoost) {
      int lastTierChanceBoost = this.tierChanceBoost;
      this.tierChanceBoost = tierChanceBoost;

      try {
         this.chancedOutput(stack, fraction);
      } finally {
         this.tierChanceBoost = lastTierChanceBoost;
      }

      return this;
   }

   public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, int count, String fraction) {
      return this.chancedOutput(ChemicalHelper.get(prefix, material, count), fraction);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, int count, String fraction, int tierChanceBoost) {
      return this.chancedOutput(ChemicalHelper.get(prefix, material, count), fraction, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, String fraction) {
      return this.chancedOutput(prefix, material, 1, fraction);
   }

   public GTRecipeBuilder chancedOutput(TagPrefix prefix, Material material, String fraction, int tierChanceBoost) {
      return this.chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(Item item, int count, String fraction) {
      return this.chancedOutput(new ItemStack(item, count), fraction);
   }

   public GTRecipeBuilder chancedOutput(Item item, int count, String fraction, int tierChanceBoost) {
      return this.chancedOutput(new ItemStack(item, count), fraction, tierChanceBoost);
   }

   public GTRecipeBuilder chancedOutput(Item item, String fraction) {
      return this.chancedOutput(item, 1, fraction);
   }

   public GTRecipeBuilder chancedOutput(Item item, String fraction, int tierChanceBoost) {
      return this.chancedOutput(item, 1, fraction, tierChanceBoost);
   }

   public GTRecipeBuilder chancedFluidOutput(FluidStack stack, String fraction, int tierChanceBoost) {
      if (stack.isEmpty()) {
         return this;
      } else {
         String[] split = fraction.split("/");
         if (split.length != 2) {
            GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".", fraction, new Throwable());
            return this;
         } else {
            int chance;
            int maxChance;
            try {
               chance = Integer.parseInt(split[0]);
               maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException var15) {
               GTCEu.LOGGER.error("Fraction was not parsed correctly! Expected format is \"1/3\". Actual: \"{}\".", fraction, new Throwable());
               return this;
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
               GTCEu.LOGGER
                  .error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", ChanceLogic.getMaxChancedValue(), chance, new Throwable());
               return this;
            } else if (chance < maxChance && maxChance <= ChanceLogic.getMaxChancedValue()) {
               int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
               chance *= scalar;
               maxChance *= scalar;
               int lastChance = this.chance;
               int lastMaxChance = this.maxChance;
               int lastTierChanceBoost = this.tierChanceBoost;
               this.chance = chance;
               this.maxChance = maxChance;
               this.tierChanceBoost = tierChanceBoost;

               try {
                  this.outputFluids(stack);
               } finally {
                  this.chance = lastChance;
                  this.maxChance = lastMaxChance;
                  this.tierChanceBoost = lastTierChanceBoost;
               }

               return this;
            } else {
               GTCEu.LOGGER
                  .error(
                     "Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.", ChanceLogic.getMaxChancedValue(), maxChance, new Throwable()
                  );
               return this;
            }
         }
      }
   }

   public GTRecipeBuilder chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
      this.outputChanceLogic.put(cap, logic);
      return this;
   }

   public GTRecipeBuilder chancedItemOutputLogic(ChanceLogic logic) {
      return this.chancedOutputLogic(ItemRecipeCapability.CAP, logic);
   }

   public GTRecipeBuilder chancedFluidOutputLogic(ChanceLogic logic) {
      return this.chancedOutputLogic(FluidRecipeCapability.CAP, logic);
   }

   public GTRecipeBuilder chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
      this.inputChanceLogic.put(cap, logic);
      return this;
   }

   public GTRecipeBuilder chancedItemInputLogic(ChanceLogic logic) {
      return this.chancedInputLogic(ItemRecipeCapability.CAP, logic);
   }

   public GTRecipeBuilder chancedFluidInputLogic(ChanceLogic logic) {
      return this.chancedInputLogic(FluidRecipeCapability.CAP, logic);
   }

   public GTRecipeBuilder chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
      this.tickOutputChanceLogic.put(cap, logic);
      return this;
   }

   public GTRecipeBuilder chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
      this.tickInputChanceLogic.put(cap, logic);
      return this;
   }

   public GTRecipeBuilder inputFluids(@NotNull Material material, int amount) {
      return this.inputFluids(material.getFluid(amount));
   }

   public GTRecipeBuilder inputFluids(FluidStack input) {
      if (this.missingIngredientError(0, true, FluidRecipeCapability.CAP, input::isEmpty)) {
         return this;
      } else {
         Material matStack = ChemicalHelper.getMaterial(input.getFluid());
         if (!matStack.isNull() && this.chance != 0 && this.chance == this.maxChance) {
            this.tempFluidStacks.add(new MaterialStack(matStack, (long)input.getAmount() * 3628800L / 144L));
         }

         return this.input(FluidRecipeCapability.CAP, RecipeHelper.makeSizedFluidIngredient(input));
      }
   }

   public GTRecipeBuilder inputFluids(FluidStack... inputs) {
      List<SizedFluidIngredient> ingredients = new ArrayList<>();

      for (int i = 0; i < inputs.length; i++) {
         FluidStack fluid = inputs[i];
         if (this.missingIngredientError(i, true, FluidRecipeCapability.CAP, fluid::isEmpty)) {
            return this;
         }

         Material matStack = ChemicalHelper.getMaterial(fluid.getFluid());
         if (!matStack.isNull() && this.chance == this.maxChance && this.chance != 0) {
            this.tempFluidStacks.add(new MaterialStack(matStack, (long)fluid.getAmount() * 3628800L / 144L));
         }

         ingredients.add(RecipeHelper.makeSizedFluidIngredient(fluid));
      }

      return this.input(FluidRecipeCapability.CAP, ingredients.toArray(SizedFluidIngredient[]::new));
   }

   public GTRecipeBuilder inputFluidsRanged(IntProviderFluidIngredient provider) {
      return this.inputFluids(provider);
   }

   protected GTRecipeBuilder inputFluidsRanged(FluidIngredient input, IntProvider intProvider) {
      return this.inputFluidsRanged(IntProviderFluidIngredient.of(input, intProvider));
   }

   public GTRecipeBuilder inputFluidsRanged(FluidStack input, IntProvider intProvider) {
      return this.inputFluidsRanged(FluidIngredient.of(new FluidStack[]{input}), intProvider);
   }

   public GTRecipeBuilder inputFluids(SizedFluidIngredient... inputs) {
      return this.input(FluidRecipeCapability.CAP, inputs);
   }

   public GTRecipeBuilder inputFluids(IntProviderFluidIngredient... inputs) {
      return this.input(
         FluidRecipeCapability.CAP,
         Arrays.stream(inputs)
            .map(ingredient -> new SizedFluidIngredient(ingredient, ingredient.getCountProvider().getMaxValue()))
            .toList()
            .toArray(new SizedFluidIngredient[0])
      );
   }

   public GTRecipeBuilder outputFluids(FluidStack output) {
      return this.output(FluidRecipeCapability.CAP, SizedFluidIngredient.of(output));
   }

   public GTRecipeBuilder outputFluids(FluidStack... outputs) {
      return this.output(FluidRecipeCapability.CAP, Arrays.stream(outputs).map(SizedFluidIngredient::of).toArray(SizedFluidIngredient[]::new));
   }

   public GTRecipeBuilder outputFluids(SizedFluidIngredient... outputs) {
      return this.output(FluidRecipeCapability.CAP, outputs);
   }

   public GTRecipeBuilder outputFluids(IntProviderFluidIngredient... outputs) {
      return this.output(
         FluidRecipeCapability.CAP,
         Arrays.stream(outputs)
            .map(ingredient -> new SizedFluidIngredient(ingredient, ingredient.getCountProvider().getMaxValue()))
            .toList()
            .toArray(new SizedFluidIngredient[0])
      );
   }

   public GTRecipeBuilder outputFluidsRanged(IntProviderFluidIngredient provider) {
      return this.outputFluids(provider);
   }

   protected GTRecipeBuilder outputFluidsRanged(FluidIngredient output, IntProvider intProvider) {
      return this.outputFluidsRanged(IntProviderFluidIngredient.of(output, intProvider));
   }

   public GTRecipeBuilder outputFluidsRanged(FluidStack output, IntProvider intProvider) {
      return this.outputFluidsRanged(FluidIngredient.of(new FluidStack[]{output}), intProvider);
   }

   public GTRecipeBuilder addData(String key, Tag data) {
      this.data.put(key, data);
      return this;
   }

   public GTRecipeBuilder addData(String key, int data) {
      this.data.putInt(key, data);
      return this;
   }

   public GTRecipeBuilder addData(String key, long data) {
      this.data.putLong(key, data);
      return this;
   }

   public GTRecipeBuilder addData(String key, String data) {
      this.data.putString(key, data);
      return this;
   }

   public GTRecipeBuilder addData(String key, float data) {
      this.data.putFloat(key, data);
      return this;
   }

   public GTRecipeBuilder addData(String key, boolean data) {
      this.data.putBoolean(key, data);
      return this;
   }

   public GTRecipeBuilder blastFurnaceTemp(int blastTemp) {
      return this.addData("ebf_temp", blastTemp);
   }

   public GTRecipeBuilder explosivesAmount(int explosivesAmount) {
      return this.inputItems(new ItemStack(Blocks.TNT, explosivesAmount));
   }

   public GTRecipeBuilder explosivesType(ItemStack explosivesType) {
      return this.inputItems(explosivesType);
   }

   public GTRecipeBuilder solderMultiplier(int multiplier) {
      return this.addData("solder_multiplier", multiplier);
   }

   public GTRecipeBuilder disableDistilleryRecipes(boolean flag) {
      return this.addData("disable_distillery", flag);
   }

   public GTRecipeBuilder fusionStartEU(long eu) {
      return this.addData("eu_to_start", eu);
   }

   public GTRecipeBuilder researchScan(boolean isScan) {
      return this.addData("scan_for_research", isScan);
   }

   public GTRecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
      return this.addData("duration_is_total_cwu", durationIsTotalCWU);
   }

   public GTRecipeBuilder hideDuration(boolean hideDuration) {
      return this.addData("hide_duration", hideDuration);
   }

   public GTRecipeBuilder cleanroom(CleanroomType cleanroomType) {
      return this.addCondition(new CleanroomCondition(cleanroomType));
   }

   public GTRecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
      return this.dimension(ResourceKey.create(Registries.DIMENSION, dimension), reverse);
   }

   public GTRecipeBuilder dimension(ResourceLocation dimension) {
      return this.dimension(dimension, false);
   }

   public GTRecipeBuilder dimension(ResourceKey<Level> dimension, boolean reverse) {
      return this.addCondition(new DimensionCondition(dimension).setReverse(reverse));
   }

   public GTRecipeBuilder dimension(ResourceKey<Level> dimension) {
      return this.dimension(dimension, false);
   }

   public GTRecipeBuilder biome(ResourceKey<Biome> biome, boolean reverse) {
      return this.addCondition(new BiomeCondition(biome).setReverse(reverse));
   }

   public GTRecipeBuilder biome(ResourceKey<Biome> biome) {
      return this.biome(biome, false);
   }

   public GTRecipeBuilder biomeTag(TagKey<Biome> biome, boolean reverse) {
      return this.addCondition(new BiomeTagCondition(biome).setReverse(reverse));
   }

   public GTRecipeBuilder biomeTag(TagKey<Biome> biome) {
      return this.biomeTag(biome, false);
   }

   public GTRecipeBuilder rain(float level, boolean reverse) {
      return this.addCondition(new RainingCondition(level).setReverse(reverse));
   }

   public GTRecipeBuilder rain(float level) {
      return this.rain(level, false);
   }

   public GTRecipeBuilder thunder(float level, boolean reverse) {
      return this.addCondition(new ThunderCondition(level).setReverse(reverse));
   }

   public GTRecipeBuilder thunder(float level) {
      return this.thunder(level, false);
   }

   public GTRecipeBuilder posY(int min, int max, boolean reverse) {
      return this.addCondition(new PositionYCondition(min, max).setReverse(reverse));
   }

   public GTRecipeBuilder posY(int min, int max) {
      return this.posY(min, max, false);
   }

   public GTRecipeBuilder environmentalHazard(MedicalCondition condition, boolean reverse) {
      return this.addCondition(new EnvironmentalHazardCondition(condition).setReverse(reverse));
   }

   public GTRecipeBuilder environmentalHazard(MedicalCondition condition) {
      return this.environmentalHazard(condition, false);
   }

   public final GTRecipeBuilder adjacentFluids(Fluid... fluids) {
      return this.adjacentFluids(false, fluids);
   }

   public final GTRecipeBuilder adjacentFluids(boolean isReverse, Fluid... fluids) {
      if (fluids.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent fluid condition has too many fluids, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(AdjacentFluidCondition.fromFluids(fluids).setReverse(isReverse));
      }
   }

   @SafeVarargs
   public final GTRecipeBuilder adjacentFluids(TagKey<Fluid>... tags) {
      return this.adjacentFluids(false, tags);
   }

   @SafeVarargs
   public final GTRecipeBuilder adjacentFluids(boolean isReverse, TagKey<Fluid>... tags) {
      if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent fluid condition has too many fluids, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(AdjacentFluidCondition.fromTags(tags).setReverse(isReverse));
      }
   }

   public GTRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids) {
      return this.adjacentFluids(fluids, false);
   }

   public GTRecipeBuilder adjacentFluids(Collection<HolderSet<Fluid>> fluids, boolean isReverse) {
      if (fluids.size() > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent fluid condition has too many fluids, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(new AdjacentFluidCondition(isReverse, List.copyOf(fluids)));
      }
   }

   public GTRecipeBuilder adjacentBlocks(Block... blocks) {
      return this.adjacentBlocks(false, blocks);
   }

   public GTRecipeBuilder adjacentBlocks(boolean isReverse, Block... blocks) {
      if (blocks.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent block condition has too many blocks, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(AdjacentBlockCondition.fromBlocks(blocks).setReverse(isReverse));
      }
   }

   @SafeVarargs
   public final GTRecipeBuilder adjacentBlocks(TagKey<Block>... tags) {
      return this.adjacentBlocks(false, tags);
   }

   @SafeVarargs
   public final GTRecipeBuilder adjacentBlocks(boolean isReverse, TagKey<Block>... tags) {
      if (tags.length > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent block condition has too many blocks, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(AdjacentBlockCondition.fromTags(tags).setReverse(isReverse));
      }
   }

   public GTRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks) {
      return this.adjacentBlocks(blocks, false);
   }

   public GTRecipeBuilder adjacentBlocks(Collection<HolderSet<Block>> blocks, boolean isReverse) {
      if (blocks.size() > GTUtil.NON_CORNER_NEIGHBOURS.size()) {
         GTCEu.LOGGER.error("Adjacent block condition has too many blocks, not adding to recipe. id: {}", this.id);
         return this;
      } else {
         return this.addCondition(new AdjacentBlockCondition(isReverse, List.copyOf(blocks)));
      }
   }

   public GTRecipeBuilder daytime(boolean isNight) {
      return this.addCondition(new DaytimeCondition().setReverse(isNight));
   }

   public GTRecipeBuilder daytime() {
      return this.daytime(false);
   }

   public GTRecipeBuilder heraclesQuest(String questId, boolean isReverse) {
      if (!GTCEu.Mods.isHeraclesLoaded()) {
         GTCEu.LOGGER.error("Heracles not loaded!");
         return this;
      } else if (questId.isEmpty()) {
         GTCEu.LOGGER.error("Quest ID cannot be empty for recipe {}", this.id);
         return this;
      } else {
         return this.addCondition(new HeraclesQuestCondition(isReverse, questId));
      }
   }

   public GTRecipeBuilder heraclesQuest(String questId) {
      return this.heraclesQuest(questId, false);
   }

   public GTRecipeBuilder ftbQuest(String questId, boolean isReverse) {
      if (!GTCEu.Mods.isFTBQuestsLoaded()) {
         GTCEu.LOGGER.error("FTBQuests is not loaded!");
         return this;
      } else if (questId.isEmpty()) {
         GTCEu.LOGGER.error("Quest ID cannot be empty for recipe {}", this.id);
         return this;
      } else {
         long qID = QuestObjectBase.parseCodeString(questId);
         if (qID == 0L) {
            GTCEu.LOGGER.error("Quest {} not found for recipe {}", questId, this.id);
            return this;
         } else {
            return this.addCondition(new FTBQuestCondition(isReverse, qID));
         }
      }
   }

   public GTRecipeBuilder ftbQuest(String questId) {
      return this.ftbQuest(questId, false);
   }

   private boolean applyResearchProperty(ResearchData.ResearchEntry researchEntry) {
      if (!ConfigHolder.INSTANCE.machines.enableResearch) {
         return false;
      } else if (researchEntry == null) {
         GTCEu.LOGGER.error("Research Entry cannot be empty.", new IllegalArgumentException());
         return false;
      } else if (!this.generatingRecipes) {
         GTCEu.LOGGER.error("Cannot generate recipes when using researchWithoutRecipe()", new IllegalArgumentException());
         return false;
      } else {
         ResearchCondition condition = this.conditions
            .stream()
            .filter(ResearchCondition.class::isInstance)
            .findAny()
            .map(ResearchCondition.class::cast)
            .orElse(null);
         if (condition != null) {
            condition.data.add(researchEntry);
         } else {
            condition = new ResearchCondition();
            condition.data.add(researchEntry);
            this.addCondition(condition);
         }

         return true;
      }
   }

   public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
      return this.researchWithoutRecipe(researchId, ResearchManager.getDefaultScannerItem());
   }

   public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
      this.applyResearchProperty(new ResearchData.ResearchEntry(researchId, dataStack));
      this.generatingRecipes = false;
      return this;
   }

   public GTRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
      GTRecipeBuilder.ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder()).build();
      if (this.applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
         this.researchRecipeEntries.add(entry);
      }

      return this;
   }

   public GTRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
      return this.scannerResearch(
         (UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder>)(b -> (ResearchRecipeBuilder.ScannerRecipeBuilder)b.researchStack(researchStack))
      );
   }

   public GTRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
      GTRecipeBuilder.ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder()).build();
      if (this.applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
         this.researchRecipeEntries.add(entry);
      }

      return this;
   }

   public GTRecipeBuilder category(@NotNull GTRecipeCategory category) {
      this.recipeCategory = category;
      return this;
   }

   public GTRecipeBuilder addMaterialInfo(boolean item) {
      this.itemMaterialInfo = item;
      return this;
   }

   public GTRecipeBuilder addMaterialInfo(boolean item, boolean fluid) {
      this.itemMaterialInfo = item;
      this.fluidMaterialInfo = fluid;
      return this;
   }

   public GTRecipeBuilder removePreviousMaterialInfo() {
      this.removePreviousMatInfo = true;
      return this;
   }

   public GTRecipeBuilder setTempItemMaterialStacks(List<MaterialStack> stacks) {
      this.tempItemMaterialStacks = stacks;
      return this;
   }

   public GTRecipeBuilder setTempFluidMaterialStacks(List<MaterialStack> stacks) {
      this.tempFluidStacks = stacks;
      return this;
   }

   public GTRecipeBuilder setTempItemStacks(List<ItemStack> stacks) {
      this.tempItemStacks = stacks;
      return this;
   }

   public void toJson(Provider registryAccess, JsonObject json) {
      RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
      JsonObject serialized = ((JsonElement)CodecUtils.encodeMap(this.build(), GTRecipeSerializer.CODEC, ops).getOrThrow()).getAsJsonObject();

      for (String key : serialized.keySet()) {
         json.add(key, serialized.get(key));
      }
   }

   public void save(RecipeOutput output) {
      if (this.onSave != null) {
         this.onSave.accept(this, output);
      }

      ResearchCondition condition = this.conditions
         .stream()
         .filter(ResearchCondition.class::isInstance)
         .findAny()
         .map(ResearchCondition.class::cast)
         .orElse(null);
      if (condition != null) {
         for (ResearchData.ResearchEntry entry : condition.data) {
            this.recipeType.addDataStickEntry(entry.researchId(), this.build());
         }
      }

      if (this.recipeType != null) {
         if (this.recipeCategory == null) {
            GTCEu.LOGGER.error("Recipes must have a category", new IllegalArgumentException());
         } else if (this.recipeCategory != GTRecipeCategory.DEFAULT && this.recipeCategory.getRecipeType() != this.recipeType) {
            GTCEu.LOGGER.error("Cannot apply Category with incompatible RecipeType", new IllegalArgumentException());
         }
      }

      if (this.removePreviousMatInfo) {
         this.removeExistingMaterialInfo();
      }

      if (this.itemMaterialInfo || this.fluidMaterialInfo) {
         this.addOutputMaterialInfo();
      }

      this.tempItemStacks = null;
      this.tempItemMaterialStacks = null;
      this.tempFluidStacks = null;

      assert this.recipeType != null;

      output.accept(this.id.withPrefix(this.recipeType.registryName.getPath() + "/"), this.build(), null);
   }

   private void gatherMaterialInfoFromStack(ItemStack input) {
      ItemMaterialInfo matInfo = ItemMaterialData.getMaterialInfo(input.getItem());
      List<ItemStack> unresolvedMatInfo = ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.get(input);
      if (this.chance == this.maxChance && this.chance != 0) {
         if (unresolvedMatInfo != null) {
            this.tempItemStacks.add(input);
         }

         if (matInfo != null) {
            for (MaterialStack matStack : matInfo.getMaterials()) {
               this.tempItemMaterialStacks.add(matStack.multiply((long)input.getCount()));
            }
         } else if (unresolvedMatInfo == null) {
            this.tempItemStacks.add(input);
         }
      }
   }

   private void addOutputMaterialInfo() {
      List<Content> itemOutputs = this.output.getOrDefault(ItemRecipeCapability.CAP, new ArrayList<>());
      List<Content> itemInputs = this.input.getOrDefault(ItemRecipeCapability.CAP, new ArrayList<>());
      if (itemOutputs.size() == 1 && (!itemInputs.isEmpty() || !this.tempFluidStacks.isEmpty())) {
         SizedIngredient currOutput = ItemRecipeCapability.CAP.of(itemOutputs.getFirst().content());
         Item out = null;
         int outputCount = 0;
         if (currOutput.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProvider) {
            ItemStack[] items = intProvider.getInner().getItems();
            if (items.length > 0) {
               out = items[0].getItem();
               outputCount = intProvider.getCountProvider().getMaxValue();
            }
         } else if (!currOutput.ingredient().hasNoItems()) {
            ItemStack[] items = currOutput.getItems();
            if (items.length > 0) {
               out = items[0].getItem();
               outputCount = items[0].getCount();
            }
         }

         if (out == null || out == Items.AIR) {
            return;
         }

         Reference2LongOpenHashMap<Material> matStacks = new Reference2LongOpenHashMap();
         if (this.itemMaterialInfo) {
            for (MaterialStack input : this.tempItemMaterialStacks) {
               long am = input.amount() / (long)outputCount;
               matStacks.addTo(input.material(), am);
            }
         }

         if (this.fluidMaterialInfo) {
            for (MaterialStack input : this.tempFluidStacks) {
               long am = input.amount() / (long)outputCount;
               matStacks.addTo(input.material(), am);
            }
         }

         if (outputCount != 0 && !this.tempItemStacks.isEmpty()) {
            ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.put(new ItemStack(out, outputCount), this.tempItemStacks);
         }

         if (!matStacks.isEmpty()) {
            ItemMaterialData.registerMaterialInfo(out, new ItemMaterialInfo(matStacks));
         }
      }
   }

   private void removeExistingMaterialInfo() {
      List<Content> itemOutputs = this.output.get(ItemRecipeCapability.CAP);
      if (itemOutputs.size() == 1) {
         SizedIngredient currOutput = ItemRecipeCapability.CAP.of(itemOutputs.getFirst().content());
         Item out = null;
         int outputCount = 0;
         if (currOutput.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProvider) {
            ItemStack[] items = intProvider.getInner().getItems();
            if (items.length > 0) {
               out = items[0].getItem();
               outputCount = intProvider.getCountProvider().getMaxValue();
            }
         } else if (!currOutput.ingredient().hasNoItems()) {
            ItemStack[] items = currOutput.getItems();
            if (items.length > 0) {
               out = items[0].getItem();
               outputCount = items[0].getCount();
            }
         }

         if (out == null || out == Items.AIR) {
            return;
         }

         if (outputCount != 0) {
            ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.remove(new ItemStack(out, outputCount));
         }

         ItemMaterialInfo existingItemInfo = ItemMaterialData.getMaterialInfo(out);
         if (existingItemInfo != null) {
            ItemMaterialData.clearMaterialInfo(out);
         }
      }
   }

   public GTRecipe build() {
      return new GTRecipe(
         this.recipeType,
         this.id.withPrefix(this.recipeType.registryName.getPath() + "/"),
         this.input,
         this.output,
         this.tickInput,
         this.tickOutput,
         this.inputChanceLogic,
         this.outputChanceLogic,
         this.tickInputChanceLogic,
         this.tickOutputChanceLogic,
         this.conditions,
         List.of(),
         this.data,
         this.duration,
         this.recipeCategory,
         -1
      );
   }

   protected void warnTooManyIngredients(RecipeCapability<?> capability, boolean isInput, Map<RecipeCapability<?>, List<Content>> table, int addedEntries) {
      Object2IntSortedMap<RecipeCapability<?>> recipeCapabilityMax = isInput ? this.recipeType.maxInputs : this.recipeType.maxOutputs;
      if (recipeCapabilityMax.containsKey(capability)) {
         int max = recipeCapabilityMax.getInt(capability);
         if (table.getOrDefault(capability, List.of()).size() + addedEntries > max) {
            String io = isInput ? "inputs" : "outputs";
            GTCEu.LOGGER.warn("Recipe {} is trying to add more {} than its recipe type can support, Max {} {}: {}", this.id, io, capability.id, io, max);
         }
      }
   }

   protected boolean missingIngredientError(int index, boolean isInput, RecipeCapability<?> cap, BooleanSupplier empty) {
      if (empty.getAsBoolean()) {
         String io = isInput ? "Input" : "Output";
         if (this.perTick) {
            io = "Tick " + io.toLowerCase(Locale.ROOT);
         }

         int size = (this.perTick ? this.tickOutput : this.output).getOrDefault(cap, List.of()).size();
         GTCEu.LOGGER.error("{} {} {} of recipe {} is empty", io, cap.id, size + index, this.id);
         return true;
      } else {
         return false;
      }
   }

   protected boolean checkChanceAndPrintError(int chance) {
      if (0 < chance && chance <= ChanceLogic.getMaxChancedValue()) {
         return false;
      } else {
         GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", ChanceLogic.getMaxChancedValue(), chance, new Throwable());
         return true;
      }
   }

   public EnergyStack EUt() {
      if (!this.tickInput.containsKey(EURecipeCapability.CAP)) {
         return EnergyStack.EMPTY;
      } else {
         return this.tickInput.get(EURecipeCapability.CAP).isEmpty()
            ? EnergyStack.EMPTY
            : EURecipeCapability.CAP.of(this.tickInput.get(EURecipeCapability.CAP).getFirst().content());
      }
   }

   public int getSolderMultiplier() {
      return this.data.contains("solderMultiplier") ? Math.max(1, this.data.getInt("solderMultiplier")) : Math.max(1, this.data.getInt("solder_multiplier"));
   }

   @NotNull
   @Generated
   public GTRecipeBuilder id(ResourceLocation id) {
      this.id = id;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder recipeType(GTRecipeType recipeType) {
      this.recipeType = recipeType;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder perTick(boolean perTick) {
      this.perTick = perTick;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder chance(int chance) {
      this.chance = chance;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder maxChance(int maxChance) {
      this.maxChance = maxChance;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder tierChanceBoost(int tierChanceBoost) {
      this.tierChanceBoost = tierChanceBoost;
      return this;
   }

   @NotNull
   @Generated
   public GTRecipeBuilder onSave(@Nullable BiConsumer<GTRecipeBuilder, RecipeOutput> onSave) {
      this.onSave = onSave;
      return this;
   }

   @Generated
   public Collection<GTRecipeBuilder.ResearchRecipeEntry> researchRecipeEntries() {
      return this.researchRecipeEntries;
   }

   public static record ResearchRecipeEntry(
      @NotNull String researchId,
      @NotNull ItemStack researchItem,
      @NotNull FluidStack researchFluid,
      @NotNull ItemStack dataStack,
      int duration,
      EnergyStack EUt,
      int CWUt
   ) {
   }
}
