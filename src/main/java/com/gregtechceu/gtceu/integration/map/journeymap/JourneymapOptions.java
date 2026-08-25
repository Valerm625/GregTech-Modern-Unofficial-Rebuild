package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.display.Displayable;
import journeymap.api.v2.client.display.Overlay;
import journeymap.api.v2.client.option.BooleanOption;
import journeymap.api.v2.client.option.OptionCategory;

public class JourneymapOptions {
   private final Map<String, BooleanOption> layerOptions = new HashMap<>();

   public JourneymapOptions() {
      OptionCategory category = new OptionCategory("gtceu", "gtceu.journeymap.options.layers");

      for (String layerName : Layers.allKeys()) {
         BooleanOption layer = new BooleanOption(category, layerName, "gtceu.button." + layerName, false);
         this.layerOptions.put(layerName, layer);
      }
   }

   public boolean showLayer(String name) {
      BooleanOption option = this.layerOptions.get(name);
      return option != null && (Boolean)option.get();
   }

   public void toggleLayer(String name, boolean active) {
      BooleanOption option = this.layerOptions.get(name);
      if (option == null) {
         GTCEu.LOGGER.warn("[GT JourneyMap] Ignoring unknown layer toggle: {}", name);
      } else {
         option.set(active);
         IClientAPI api = GTJourneyMapPlugin.getJmApi();
         if (api == null) {
            GTCEu.LOGGER.warn("[GT JourneyMap] API unavailable while toggling layer {} to {}", name, active);
         } else {
            if (!active) {
               for (Entry<String, Overlay> entry : JourneymapRenderer.getMarkers().entrySet()) {
                  if (entry.getKey().startsWith(name + "@")) {
                     api.remove((Displayable)entry.getValue());
                  }
               }
            } else {
               for (Entry<String, Overlay> entryx : JourneymapRenderer.getMarkers().entrySet()) {
                  if (entryx.getKey().startsWith(name + "@")) {
                     try {
                        api.show((Displayable)entryx.getValue());
                     } catch (Exception var8) {
                        GTCEu.LOGGER.error("Failed to enable marker with name {}", name, var8);
                     }
                  }
               }
            }
         }
      }
   }
}
