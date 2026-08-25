package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.integration.ftbquests.FTBQuestUIIntegration;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FTBQuestClientIntegration {
   private FTBQuestClientIntegration() {
   }

   public static void init() {
      FTBQuestUIIntegration.registerClientQuestBookOpener(() -> FTBQuestsClient.openGui());
   }
}
