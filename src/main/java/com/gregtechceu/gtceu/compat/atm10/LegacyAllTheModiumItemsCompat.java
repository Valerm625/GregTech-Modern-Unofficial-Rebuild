package com.gregtechceu.gtceu.compat.atm10;

import com.gregtechceu.gtceu.GTCEu;
import java.lang.reflect.Constructor;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.Logger;

public final class LegacyAllTheModiumItemsCompat {
   private static final String MOD_ID = "allthemodium";

   private LegacyAllTheModiumItemsCompat() {
   }

   public static void register(IEventBus modBus) {
      if (GTCEu.isModLoaded("allthemodium")) {
         modBus.addListener(LegacyAllTheModiumItemsCompat::onRegister);
      }
   }

   private static void onRegister(RegisterEvent event) {
      if (event.getRegistryKey() == Registries.ITEM) {
         registerIfMissing(
            event, "allthemodium_bow", "com.thevortex.allthemodium.items.toolitems.tools.ATMBow", properties -> properties.fireResistant().rarity(Rarity.EPIC)
         );
         registerIfMissing(
            event,
            "unobtainium_crossbow",
            "com.thevortex.allthemodium.items.toolitems.tools.Unobow",
            properties -> properties.fireResistant().rarity(Rarity.EPIC)
         );
         registerIfMissing(
            event,
            "vibranium_shield",
            "com.thevortex.allthemodium.items.toolitems.armor.Vib_Shield",
            properties -> properties.fireResistant().stacksTo(1).rarity(Rarity.EPIC)
         );
      }
   }

   private static void registerIfMissing(RegisterEvent event, String path, String className, UnaryOperator<Properties> properties) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("allthemodium", path);
      if (!BuiltInRegistries.ITEM.containsKey(id)) {
         event.register(Registries.ITEM, id, () -> instantiate(className, properties.apply(new Properties())));
         GTCEu.LOGGER.info("Registered ATM10 legacy item ID {} through GTCEu", id);
      }
   }

   private static Item instantiate(String className, Properties properties) {
      try {
         Class<?> type = Class.forName(className, true, LegacyAllTheModiumItemsCompat.class.getClassLoader());
         Constructor<?> constructor = type.getConstructor(Properties.class);
         return (Item)constructor.newInstance(properties);
      } catch (ClassCastException | ReflectiveOperationException var4) {
         Logger logger = GTCEu.LOGGER;
         logger.error("Could not construct legacy AllTheModium item {}; using a generic compatibility item", className, var4);
         return new Item(properties);
      }
   }
}
