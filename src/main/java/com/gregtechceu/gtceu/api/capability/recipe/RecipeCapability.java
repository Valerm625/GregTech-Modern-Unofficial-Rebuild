package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.codec.DispatchedMapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public abstract class RecipeCapability<T> {
   private static final Map<ResourceLocation, ResourceLocation> LEGACY_IDS = Map.of(
      ResourceLocation.withDefaultNamespace("item"),
      GTCEu.id("item"),
      ResourceLocation.withDefaultNamespace("fluid"),
      GTCEu.id("fluid"),
      ResourceLocation.withDefaultNamespace("eu"),
      GTCEu.id("eu"),
      ResourceLocation.withDefaultNamespace("cwu"),
      GTCEu.id("cwu"),
      ResourceLocation.withDefaultNamespace("block_state"),
      GTCEu.id("block_state")
   );
   public static final Codec<RecipeCapability<?>> ID_CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
      ResourceLocation canonicalId = LEGACY_IDS.getOrDefault(id, id);
      RecipeCapability<?> capability = (RecipeCapability<?>)GTRegistries.RECIPE_CAPABILITIES.get(canonicalId);
      return capability == null ? DataResult.error(() -> "Unknown recipe capability: " + id) : DataResult.success(capability);
   }, capability -> {
      ResourceLocation registeredId = GTRegistries.RECIPE_CAPABILITIES.getKey(capability);
      return registeredId == null ? capability.id : registeredId;
   });
   public static final Codec<Map<RecipeCapability<?>, List<Content>>> CODEC = new DispatchedMapCodec<>(ID_CODEC, RecipeCapability::contentCodec);
   public static final Comparator<RecipeCapability<?>> COMPARATOR = Comparator.comparingInt(o -> o.sortIndex);
   public final ResourceLocation id;
   public final int color;
   public final boolean doRenderSlot;
   public final int sortIndex;
   public final IContentSerializer<T> serializer;

   protected RecipeCapability(ResourceLocation id, int color, boolean doRenderSlot, int sortIndex, IContentSerializer<T> serializer) {
      this.id = id;
      this.color = color;
      this.doRenderSlot = doRenderSlot;
      this.sortIndex = sortIndex;
      this.serializer = serializer;
   }

   @Deprecated(
      forRemoval = true,
      since = "8.0.0"
   )
   protected RecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex, IContentSerializer<T> serializer) {
      this(GTCEu.id(name), color, doRenderSlot, sortIndex, serializer);
   }

   public static Codec<List<Content>> contentCodec(RecipeCapability<?> capability) {
      return Content.codec(capability).listOf().xmap(ArrayList::new, list -> list);
   }

   public T copyInner(T content) {
      RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
         Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), ConnectionType.NEOFORGE
      );
      this.serializer.toNetwork(buf, content);
      return this.serializer.fromNetwork(buf);
   }

   public T copyWithModifier(T content, ContentModifier modifier) {
      return this.copyInner(content);
   }

   public final T copyContent(Object content) {
      return this.copyInner((T)content);
   }

   public final T copyContent(Object content, ContentModifier modifier) {
      return this.copyWithModifier((T)content, modifier);
   }

   public T of(Object o) {
      return this.serializer.of(o);
   }

   public T fromNbt(Tag tag, Provider provider) {
      return this.serializer.fromNbt(tag, provider);
   }

   public Tag toNbt(Object content, Provider provider) {
      return this.serializer.toNbt(this.of(content), provider);
   }

   public String slotName(IO io) {
      return "%s_%s".formatted(this.id, io.name().toLowerCase(Locale.ROOT));
   }

   public String slotName(IO io, int index) {
      return "%s_%s_%s".formatted(this.id, io.name().toLowerCase(Locale.ROOT), index);
   }

   public MutableComponent getName() {
      return Component.translatable("recipe.capability.%s.name".formatted(this.id.getPath()));
   }

   public MutableComponent getColoredName() {
      return this.getName().withStyle(style -> style.withColor(this.color));
   }

   public boolean isRecipeSearchFilter() {
      return false;
   }

   public List<Object> compressIngredients(@Unmodifiable Collection<Object> ingredients) {
      return new ArrayList<>(ingredients);
   }

   @Nullable
   public List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
      return null;
   }

   public boolean doMatchInRecipe() {
      return true;
   }

   public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int maxMultiplier, boolean tick) {
      return Integer.MAX_VALUE;
   }

   public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
      return Integer.MAX_VALUE;
   }

   public Object2IntMap<T> makeChanceCache() {
      return new Object2IntOpenHashMap();
   }

   public boolean isTickSlot(int index, IO io, GTRecipe recipe) {
      return index >= (io == IO.IN ? recipe.getInputContents(this) : recipe.getOutputContents(this)).size();
   }

   private static DataResult<Reference<RecipeCapability<?>>> safeReference(Holder<RecipeCapability<?>> value) {
      return value.getDelegate() instanceof Reference<RecipeCapability<?>> reference
         ? DataResult.success(reference)
         : DataResult.error(() -> "Unregistered holder in " + GTRegistries.Keys.RECIPE_CAPABILITY + ": " + value);
   }

   public boolean shouldBypassDistinct() {
      return true;
   }

   public boolean skipEmptyContentCheck() {
      return false;
   }

   public List<? extends NotifiableRecipeHandlerTrait<T>> getCapabilityHandlers(MetaMachine machine) {
      return List.of();
   }

   public List<? extends NotifiableRecipeHandlerTrait<T>> getCapabilityHandlers(MetaMachine machine, IO io) {
      return this.getCapabilityHandlers(machine).stream().filter(v -> v.getHandlerIO() == io).toList();
   }
}
