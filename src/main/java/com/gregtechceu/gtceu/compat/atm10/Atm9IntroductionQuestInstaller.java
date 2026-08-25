package com.gregtechceu.gtceu.compat.atm10;

import com.gregtechceu.gtceu.GTCEu;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModFileInfo;

public final class Atm9IntroductionQuestInstaller {
   static final String ATM9_SOURCE_SHA256 = "0f28db7e2fdd2213421981d0c51bd92e0c557341f403bfdae51a4f65ac290133";
   static final String MIGRATED_SHA256 = "4841338c3675e1f7dea1a308268a772f2b8bb69a5182156a989335a6d81deb7f";
   static final String CHAPTER_RELATIVE_PATH = "chapters/an_introduction.snbt";
   private static final String RESOURCE_PATH = "gtceu_atm10_embedded/ftbquests/quests/chapters/an_introduction.snbt";

   private Atm9IntroductionQuestInstaller() {
   }

   public static void installIfNeeded() {
      if (!GTCEu.isDataGen() && GTCEu.Mods.isFTBQuestsLoaded() && GTCEu.isModLoaded("ftbfiltersystem") && Atm10Environment.isDetected()) {
         Path questRoot = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests").normalize();
         Path chapter = questRoot.resolve("chapters/an_introduction.snbt").normalize();
         if (!Files.exists(chapter)) {
            try {
               byte[] resource = readBundledChapter();
               Atm9IntroductionQuestInstaller.InstallResult result = installIfMissing(questRoot, resource);
               if (result == Atm9IntroductionQuestInstaller.InstallResult.INSTALLED) {
                  GTCEu.LOGGER
                     .info(
                        "Installed the mechanically migrated ATM9 introduction chapter (source_sha256={}, migrated_sha256={})",
                        "0f28db7e2fdd2213421981d0c51bd92e0c557341f403bfdae51a4f65ac290133",
                        "4841338c3675e1f7dea1a308268a772f2b8bb69a5182156a989335a6d81deb7f"
                     );
               }
            } catch (Exception var4) {
               GTCEu.LOGGER.error("Failed to install the ATM9 introduction chapter for ATM10", var4);
            }
         }
      }
   }

   static Atm9IntroductionQuestInstaller.InstallResult installIfMissing(Path questRoot, byte[] resource) throws IOException {
      Path root = questRoot.toAbsolutePath().normalize();
      Path target = root.resolve("chapters/an_introduction.snbt").normalize();
      if (!target.startsWith(root)) {
         throw new IOException("FTB Quests chapter path escaped its config root");
      } else if (Files.exists(target)) {
         return Atm9IntroductionQuestInstaller.InstallResult.ALREADY_PRESENT;
      } else if (!"4841338c3675e1f7dea1a308268a772f2b8bb69a5182156a989335a6d81deb7f".equals(sha256(resource))) {
         throw new IOException("Bundled ATM9 introduction chapter failed its SHA-256 gate");
      } else {
         Files.createDirectories(target.getParent());
         Path temporary = Files.createTempFile(target.getParent(), "an_introduction.", ".tmp");
         boolean moved = false;

         Atm9IntroductionQuestInstaller.InstallResult ignored;
         try {
            Files.write(temporary, resource);

            try {
               Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException var10) {
               Files.move(temporary, target);
            }

            moved = true;
            ignored = Atm9IntroductionQuestInstaller.InstallResult.INSTALLED;
         } finally {
            if (!moved) {
               Files.deleteIfExists(temporary);
            }
         }

         return ignored;
      }
   }

   static byte[] readBundledChapter() throws IOException {
      ClassLoader loader = Atm9IntroductionQuestInstaller.class.getClassLoader();

      try (InputStream input = loader.getResourceAsStream("gtceu_atm10_embedded/ftbquests/quests/chapters/an_introduction.snbt")) {
         if (input != null) {
            return input.readAllBytes();
         }
      }

      IModFileInfo modFile = ModList.get().getModFileById("gtceu");
      if (modFile != null) {
         Path resource = modFile.getFile().findResource(new String[]{"gtceu_atm10_embedded", "ftbquests", "quests", "chapters", "an_introduction.snbt"});
         if (Files.isRegularFile(resource)) {
            return Files.readAllBytes(resource);
         }
      }

      throw new IOException("Missing embedded resource gtceu_atm10_embedded/ftbquests/quests/chapters/an_introduction.snbt");
   }

   static String sha256(byte[] bytes) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
      } catch (NoSuchAlgorithmException var2) {
         throw new IllegalStateException("The JVM does not provide SHA-256", var2);
      }
   }

   static enum InstallResult {
      INSTALLED,
      ALREADY_PRESENT;
   }
}
