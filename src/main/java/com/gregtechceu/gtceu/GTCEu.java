package com.gregtechceu.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.compat.atm10.Atm9IntroductionQuestInstaller;
import com.gregtechceu.gtceu.compat.atm10.GregStarQuestInstaller;
import com.gregtechceu.gtceu.compat.atm10.LegacyAllTheModiumItemsCompat;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.mojang.serialization.Codec;
import dev.emi.emi.config.EmiConfig;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Mod("gtceu")
public class GTCEu {
   public static final String MOD_ID = "gtceu";
   private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath("gtceu", "");
   public static final Codec<ResourceLocation> GTCEU_ID = Codec.STRING
      .comapFlatMap(str -> ResourceLocation.read(appendIdString(str)), s -> s.getNamespace().equals("gtceu") ? s.getPath() : s.toString());
   public static final String NAME = "GTCEu";
   public static final Logger LOGGER = LogManager.getLogger("GTCEu");
   public static final Path GTCEU_FOLDER = getGameDir().resolve("gtceu");
   @Internal
   public static IEventBus gtModBus;

   public GTCEu(IEventBus modBus, FMLModContainer container) {
      GTCEuAPI.instance = this;
      gtModBus = modBus;
      ConfigHolder.init();
      LegacyAllTheModiumItemsCompat.register(modBus);
      Atm9IntroductionQuestInstaller.installIfNeeded();
      GregStarQuestInstaller.installIfNeeded();
      GTCEuAPI.initializeHighTier();
      if (isDev()) {
         ConfigHolder.INSTANCE.recipes.generateLowQualityGems = true;
         ConfigHolder.INSTANCE.compat.energy.enableFEConverters = true;
      }

      CommonProxy.init(modBus);
      modBus.addListener(GTNetwork::registerPayloads);
   }

   public static ResourceLocation id(String path) {
      if (path.isBlank()) {
         return TEMPLATE_LOCATION;
      } else {
         int i = path.indexOf(58);
         if (i > 0) {
            return ResourceLocation.tryParse(path);
         } else {
            if (i == 0) {
               path = path.substring(i + 1);
            }

            if (FormattingUtil.hasUpperCase(path)) {
               LOGGER.warn(
                  "Resource location {} has uppercase characters, which are not allowed. Renaming resource location to {}",
                  path,
                  FormattingUtil.toLowerCaseUnderscore(path)
               );
               path = FormattingUtil.toLowerCaseUnderscore(path);
            }

            return TEMPLATE_LOCATION.withPath(path);
         }
      }
   }

   public static String appendIdString(String id) {
      int i = id.indexOf(58);
      if (i > 0) {
         return id;
      } else {
         return i == 0 ? "gtceu" + id : "gtceu:" + id;
      }
   }

   public static boolean isProd() {
      return FMLLoader.isProduction();
   }

   public static boolean isDev() {
      return !isProd();
   }

   public static boolean isDataGen() {
      return DatagenModLoader.isRunningDataGen();
   }

   public static MinecraftServer getMinecraftServer() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   public static boolean isModLoaded(String modId) {
      return ModList.get().isLoaded(modId);
   }

   public static boolean isClientThread() {
      return isClientSide() && Minecraft.getInstance() != null && Minecraft.getInstance().isSameThread();
   }

   public static boolean isClientSide() {
      return FMLEnvironment.dist.isClient();
   }

   public static boolean canGetServerLevel() {
      if (isClientSide()) {
         return Minecraft.getInstance().level != null;
      } else {
         MinecraftServer server = getMinecraftServer();
         return server != null && !server.isStopped() && !server.isShutdown() && server.isRunning() && !server.isCurrentlySaving();
      }
   }

   public static Path getGameDir() {
      return FMLPaths.GAMEDIR.get();
   }

   public static class Mods {
      public static boolean isAnyRecipeViewerLoaded() {
         return GTCEu.isModLoaded("emi") || GTCEu.isModLoaded("jei") || GTCEu.isModLoaded("roughlyenoughitems");
      }

      public static boolean isJEILoaded() {
         return !GTCEu.isModLoaded("emi") && !GTCEu.isModLoaded("roughlyenoughitems") && GTCEu.isModLoaded("jei");
      }

      public static boolean isEMILoaded() {
         return GTCEu.isModLoaded("emi") && (!GTCEu.isClientSide() || EmiConfig.enabled);
      }

      public static boolean isKubeJSLoaded() {
         return GTCEu.isModLoaded("kubejs");
      }

      public static boolean isIrisOculusLoaded() {
         return GTCEu.isModLoaded("iris") || GTCEu.isModLoaded("oculus");
      }

      public static boolean isSodiumEmbeddiumLoaded() {
         return GTCEu.isModLoaded("sodium") || GTCEu.isModLoaded("embeddium");
      }

      public static boolean isAE2Loaded() {
         return GTCEu.isModLoaded("ae2");
      }

      public static boolean isCuriosLoaded() {
         return GTCEu.isModLoaded("curios");
      }

      public static boolean isModernFixLoaded() {
         return GTCEu.isModLoaded("modernfix");
      }

      public static boolean isFTBTeamsLoaded() {
         return GTCEu.isModLoaded("ftbteams");
      }

      public static boolean isHeraclesLoaded() {
         return GTCEu.isModLoaded("heracles");
      }

      public static boolean isFTBQuestsLoaded() {
         return GTCEu.isModLoaded("ftbquests");
      }

      public static boolean isArgonautsLoaded() {
         return GTCEu.isModLoaded("argonauts");
      }

      public static boolean isGameStagesLoaded() {
         return GTCEu.isModLoaded("gamestages");
      }

      public static boolean isCCTweakedLoaded() {
         return GTCEu.isModLoaded("computercraft");
      }

      public static boolean isCreateLoaded() {
         return GTCEu.isModLoaded("create");
      }

      public static boolean isSableLoaded() {
         return GTCEu.isModLoaded("sable");
      }
   }
}
