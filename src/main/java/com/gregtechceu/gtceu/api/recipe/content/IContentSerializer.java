package com.gregtechceu.gtceu.api.recipe.content;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IContentSerializer<T> {
   default void toNetwork(RegistryFriendlyByteBuf buf, T content) {
      buf.writeJsonWithCodec(this.codec(), content);
   }

   default T fromNetwork(RegistryFriendlyByteBuf buf) {
      return (T)buf.readJsonWithCodec(this.codec());
   }

   default T fromJson(JsonElement json, Provider provider) {
      return (T)this.codec().parse(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
   }

   default JsonElement toJson(T content, Provider provider) {
      return (JsonElement)this.codec().encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), content).getOrThrow();
   }

   T of(Object var1);

   T defaultValue();

   default void toNetworkContent(RegistryFriendlyByteBuf buf, Content content) {
      T inner = (T)content.content();
      this.toNetwork(buf, inner);
      buf.writeVarInt(content.chance());
      buf.writeVarInt(content.maxChance());
      buf.writeVarInt(content.tierChanceBoost());
   }

   default Content fromNetworkContent(RegistryFriendlyByteBuf buf) {
      T inner = this.fromNetwork(buf);
      int chance = buf.readVarInt();
      int maxChance = buf.readVarInt();
      int tierChanceBoost = buf.readVarInt();
      return Content.fromSerialized(inner, chance, maxChance, tierChanceBoost);
   }

   Class<T> contentClass();

   Codec<T> codec();

   default Tag toNbt(T content, Provider provider) {
      return (Tag)this.codec().encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), content).getOrThrow();
   }

   default T fromNbt(Tag tag, Provider provider) {
      return (T)this.codec().parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
   }
}
