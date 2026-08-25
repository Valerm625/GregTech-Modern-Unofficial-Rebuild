package com.gregtechceu.gtceu.integration.map.journeymap;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.ButtonState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import journeymap.api.v2.client.event.FullscreenDisplayEvent.AddonButtonDisplayEvent;
import journeymap.api.v2.client.event.FullscreenDisplayEvent.CustomToolbarEvent;
import journeymap.api.v2.client.fullscreen.CustomToolBarBuilder;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import journeymap.api.v2.client.fullscreen.IThemeToolBar;
import journeymap.api.v2.client.fullscreen.ThemeButtonDisplay;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.client.io.ThemeLoader;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.Theme.Container.Toolbar;
import journeymap.client.ui.theme.Theme.Container.Toolbar.ToolbarSpec;
import net.minecraft.client.gui.screens.Screen;

public class JourneymapEventListener {
   public static void init() {
      FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe("gtceu", JourneymapEventListener::onFullscreenAddonButton);
      FullscreenEventRegistry.CUSTOM_TOOLBAR_UPDATE_EVENT.subscribe("gtceu", JourneymapEventListener::onFullscreenToolbarEvent);
   }

   protected static void onFullscreenAddonButton(AddonButtonDisplayEvent event) {
      if (ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) {
         if (ConfigHolder.INSTANCE.compat.minimap.direction == ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL) {
            ThemeButtonDisplay display = event.getThemeButtonDisplay();
            List<ButtonState.Button> states = ButtonState.getAllButtons();
            syncButtonStatesFromOptions(states);
            ArrayList<IThemeButton> buttons = new ArrayList<>(states.size());

            for (ButtonState.Button state : states) {
               buttons.add(
                  display.addThemeToggleButton(
                     "gtceu.button." + state.name, GTCEu.id("textures/gui/widget/button_" + state.name + ".png"), state.enabled, b -> {
                        ButtonState.toggleButton(state);
                        syncThemeButtons(buttons, states);
                     }
                  )
               );
            }
         }
      }
   }

   protected static void onFullscreenToolbarEvent(CustomToolbarEvent event) {
      if (ConfigHolder.INSTANCE.compat.minimap.toggle.journeyMapIntegration) {
         if (ConfigHolder.INSTANCE.compat.minimap.direction != ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL) {
            CustomToolBarBuilder builder = event.getCustomToolBarBuilder();
            List<ButtonState.Button> allButtons = ButtonState.getAllButtons();
            syncButtonStatesFromOptions(allButtons);
            IThemeButton[] buttons = new IThemeButton[allButtons.size()];

            for (int i = 0; i < allButtons.size(); i++) {
               ButtonState.Button state = allButtons.get(i);
               buttons[i] = builder.getThemeToggleButton("gtceu.button." + state.name, GTCEu.id("textures/gui/widget/button_" + state.name + ".png"), b -> {
                  ButtonState.toggleButton(state);
                  syncThemeButtons(Arrays.asList(buttons), allButtons);
               });
               buttons[i].setToggled(state.enabled);
            }

            IThemeToolBar toolbar = builder.getNewToolbar(buttons);
            Theme theme = ThemeLoader.getCurrentTheme();
            Screen screen = event.getFullscreen().getScreen();
            Toolbar toolbarTheme = theme.container.toolbar;
            ToolbarSpec toolbarSpec = toolbarTheme.horizontal;
            int margin = toolbarSpec.margin;
            int padding = toolbarSpec.padding;
            int height = toolbarSpec.inner.height;
            toolbar.setLayoutHorizontal(margin, screen.height - (height + margin), padding, true);
         }
      }
   }

   private static void syncButtonStatesFromOptions(List<ButtonState.Button> states) {
      JourneymapOptions options = GTJourneyMapPlugin.getOptions();
      if (options != null) {
         for (ButtonState.Button state : states) {
            state.enabled = options.showLayer(state.name);
         }
      }
   }

   private static void syncThemeButtons(List<IThemeButton> buttons, List<ButtonState.Button> states) {
      int count = Math.min(buttons.size(), states.size());

      for (int i = 0; i < count; i++) {
         IThemeButton button = buttons.get(i);
         if (button != null && button.getToggled() != states.get(i).enabled) {
            button.toggle();
         }
      }
   }
}
