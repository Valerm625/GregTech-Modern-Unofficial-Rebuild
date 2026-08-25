package com.gregtechceu.gtceu.integration.ftbquests;

import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.widgets.ButtonWidget;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FTBQuestUIIntegration {
   private static final ResourceLocation QUEST_BOOK_ID = ResourceLocation.fromNamespaceAndPath("ftbquests", "book");
   private static Runnable clientQuestBookOpener = () -> {
   };

   private FTBQuestUIIntegration() {
   }

   public static void registerClientQuestBookOpener(Runnable opener) {
      clientQuestBookOpener = Objects.requireNonNull(opener);
   }

   public static ButtonWidget<?> createQuestBookButton() {
      ItemStack questBook = BuiltInRegistries.ITEM.get(QUEST_BOOK_ID).getDefaultInstance();
      return new ButtonWidget<>()
         .size(18)
         .overlay(new ItemDrawable(questBook))
         .onMousePressed((context, button) -> {
            if (button != 0) {
               return false;
            } else {
               clientQuestBookOpener.run();
               return true;
            }
         })
         .tooltip(tooltip -> tooltip.add(Component.translatable("item.ftbquests.book")));
   }
}
