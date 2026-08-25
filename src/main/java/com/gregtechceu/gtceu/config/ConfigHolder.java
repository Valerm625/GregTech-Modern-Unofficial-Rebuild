package com.gregtechceu.gtceu.config;

import brachy.modularui.screen.RichTooltip.Pos;
import com.gregtechceu.gtceu.client.bloom.BloomType;
import com.gregtechceu.gtceu.compat.atm10.Atm10Environment;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import dev.toma.configuration.config.Configurable.Comment;
import dev.toma.configuration.config.Configurable.DecimalRange;
import dev.toma.configuration.config.Configurable.Range;
import dev.toma.configuration.config.Configurable.StringPattern;
import dev.toma.configuration.config.Configurable.Synchronized;
import dev.toma.configuration.config.Configurable.UpdateRestriction;
import dev.toma.configuration.config.Configurable.Gui.ColorValue;
import dev.toma.configuration.config.format.ConfigFormats;
import org.jetbrains.annotations.ApiStatus.Internal;

@Config(
   id = "gtceu"
)
public class ConfigHolder {
   public static ConfigHolder INSTANCE;
   private static final Object LOCK = new Object();
   @Internal
   public static dev.toma.configuration.config.ConfigHolder<ConfigHolder> INTERNAL_INSTANCE;
   @Configurable
   public ConfigHolder.RecipeConfigs recipes = new ConfigHolder.RecipeConfigs();
   @Configurable
   public ConfigHolder.WorldGenConfigs worldgen = new ConfigHolder.WorldGenConfigs();
   @Configurable
   public ConfigHolder.MachineConfigs machines = new ConfigHolder.MachineConfigs();
   @Configurable
   public ConfigHolder.ClientConfigs client = new ConfigHolder.ClientConfigs();
   @Configurable
   @Comment({"Config options for Tools and Armor"})
   public ConfigHolder.ToolConfigs tools = new ConfigHolder.ToolConfigs();
   @Configurable
   @Comment({"Config options for Game Mechanics"})
   public ConfigHolder.GameplayConfigs gameplay = new ConfigHolder.GameplayConfigs();
   @Configurable
   @Comment({"Config options for Mod Compatibility"})
   public ConfigHolder.CompatibilityConfigs compat = new ConfigHolder.CompatibilityConfigs();
   @Configurable
   public ConfigHolder.DeveloperConfigs dev = new ConfigHolder.DeveloperConfigs();

   public static void init() {
      synchronized (LOCK) {
         if (INSTANCE == null || INTERNAL_INSTANCE == null) {
            INTERNAL_INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.YAML);
            INSTANCE = (ConfigHolder)INTERNAL_INSTANCE.getConfigInstance();
         }
      }
   }

   public static ConfigHolder getInstance() {
      init();
      return INSTANCE;
   }

   public static class ClientConfigs {
      @Configurable
      @Comment({"Whether or not to display all temperatures in Celsius instead of Kelvin"})
      public boolean temperaturesInCelsius = false;
      @Configurable
      @Comment({"Whether or not to enable Emissive Textures for GregTech Machines.", "Default: true"})
      public boolean machinesEmissiveTextures = true;
      @Configurable
      @Comment({"Whether most machines will have block entity renderers, mainly used for rendering certain covers. (Restart required)", "Disable if experiencing performance issues.", "Default: true"})
      public boolean machinesHaveBERsByDefault = true;
      @Configurable
      @Comment({"Whether or not sounds should be played when using tools outside of crafting.", "Default: true"})
      public boolean toolUseSounds = true;
      @Configurable
      @Comment({"Whether or not sounds should be played when crafting with tools.", "Default: true"})
      public boolean toolCraftingSounds = true;
      @Configurable
      @Comment({"The default color to overlay onto machines.", "#FFFFFF is no coloring (default).", "#D2DCFF is the classic blue from GT5."})
      @StringPattern("#[0-9a-fA-F]{1,6}")
      @ColorValue
      public String defaultPaintingColor = "#FFFFFF";
      @Configurable
      @Comment({"Use VBO cache for multiblock preview.", "Disable if you have issues with rendering multiblocks.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean useVBO = true;
      @Configurable
      @Comment({"Duration of the multiblock in-world preview (in seconds)", "Default: 10"})
      @Range(
         min = 1L,
         max = 999L
      )
      public int inWorldPreviewDuration = 10;
      @Configurable
      public ConfigHolder.ClientConfigs.ArmorHud armorHud = new ConfigHolder.ClientConfigs.ArmorHud();
      @Configurable
      public ConfigHolder.ClientConfigs.RendererOptions renderer = new ConfigHolder.ClientConfigs.RendererOptions();
      @Configurable
      @Comment({"Config options for bloom and other post-processing effects"})
      public ConfigHolder.ClientConfigs.BloomOptions bloom = new ConfigHolder.ClientConfigs.BloomOptions();
      @Configurable
      public ConfigHolder.ClientConfigs.TankItemFluidPreview tankItemFluidPreview = new ConfigHolder.ClientConfigs.TankItemFluidPreview();
      @Configurable
      public ConfigHolder.ClientConfigs.UIConfigs ui = new ConfigHolder.ClientConfigs.UIConfigs();

      public int getDefaultPaintingColor() {
         return Long.decode(this.defaultPaintingColor).intValue() | 0xFF000000;
      }

      public static class ArmorHud {
         @Configurable
         @Comment({"Sets HUD location", "1 - left-upper corner", "2 - right-upper corner", "3 - left-bottom corner", "4 - right-bottom corner", "Default: 1"})
         @Range(
            min = 1L,
            max = 4L
         )
         public int hudLocation = 1;
         @Configurable
         @Comment({"Horizontal offset of HUD.", "Default: 0"})
         @Range(
            min = 0L,
            max = 100L
         )
         public int hudOffsetX = 0;
         @Configurable
         @Comment({"Vertical offset of HUD.", "Default: 0"})
         @Range(
            min = 0L,
            max = 100L
         )
         public int hudOffsetY = 0;
      }

      public static class BloomOptions {
         @Configurable
         @Comment({"Bloom Algorithm", "Requires reloading all chunks ", "UNITY - Unity-like Bloom (rescale)", "UNREAL - Unreal-like Bloom (gaussian blur)", "DISABLED - No bloom", "Default: UNREAL"})
         public BloomType type = BloomType.DISABLED;
         @Configurable
         @Comment({"Whether or not to add bloom to emissive textures", "Default: true"})
         public boolean emissiveTexturesHaveBloom = true;
         @Configurable
         @Comment({"The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights (e.g., daytime)", "This value should be greater than minBrightness.", "OUTPUT = BACKGROUND + BLOOM * strength * (base + min + (1 - BACKGROUND_BRIGHTNESS) * ({max} - min)))", "Default: 0.5"})
         @DecimalRange(
            min = 0.0
         )
         public float maxBrightness = 0.5F;
         @Configurable
         @Comment({"The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts (e.g., night/caves)", "This value should be lower than maxBrightness.", "OUTPUT = BACKGROUND + BLOOM * strength * (base + {min} + (1 - BACKGROUND_BRIGHTNESS) * (max - {min})))", "Default: 0.2"})
         @DecimalRange(
            min = 0.0
         )
         public float minBrightness = 0.2F;
         @Configurable
         @Comment({"The base brightness of the bloom.", "It is similar to strength", "This value should be lower than maxBrightness.", "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + min + (1 - BACKGROUND_BRIGHTNESS) * (max - min)))", "Default: 0.1"})
         @DecimalRange(
            min = 0.0
         )
         public float baseBrightness = 0.1F;
         @Configurable
         @Comment({"Bloom Strength", "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + min + (1 - BACKGROUND_BRIGHTNESS) * (max - min)))", "Default: 1.5"})
         @DecimalRange(
            min = 0.0
         )
         public float strength = 1.5F;
         @Configurable
         @Comment({"Blur Step (bloom range)", "Default: 1"})
         @DecimalRange(
            min = 0.0
         )
         public float step = 1.0F;
      }

      public static class RendererOptions {
         @Configurable
         @Comment({"Render fluids in multiblocks that support them?", "Default: true"})
         public boolean renderFluids = true;
         @Configurable
         @Comment({"Render growing plants in multiblocks that support them?", "Default: true"})
         public boolean renderGrowingPlants = true;
         @Configurable
         @Comment({"Whether or not to color material/ore block highlights in the material color", "Default: true"})
         public boolean coloredMaterialBlockOutline = true;
         @Configurable
         @Comment({"Whether or not to color tiered machine highlights in the tier color", "Default: true"})
         public boolean coloredTieredMachineOutline = true;
         @Configurable
         @Comment({"Whether or not to color wire/cable highlights based on voltage tier or material color", "Default: true"})
         public boolean coloredWireOutline = true;
      }

      public static class TankItemFluidPreview {
         @Configurable
         @Comment({"Set true to render the including fluid icons to GT Drums", "Default: false"})
         public boolean drum = false;
         @Configurable
         @Comment({"Set true to render the including fluid icons to Super and Quantum Tanks", "Default: false"})
         public boolean quantumTank = false;
      }

      public static class UIConfigs {
         @Configurable
         @Comment({"If progress bar should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)", "Default: true"})
         public boolean smoothProgressBar = true;
         @Configurable
         @Comment({"Duration of UI animations in ms.", "Default: 100"})
         @Range(
            min = 1L,
            max = 500L
         )
         public int animationTime = 100;
         @Configurable
         @Comment({"Default tooltip position around the widget or its panel.", "Default: VERTICAL"})
         public Pos tooltipPos = Pos.NEXT_TO_MOUSE;
         @Configurable
         @Comment({"The default color to overlay onto Machine (and other) UIs.", "#FFFFFF is no coloring (like GTCE) (default).", "#D2DCFF is the classic blue from GT5."})
         @StringPattern("#[0-9a-fA-F]{1,6}")
         @ColorValue
         public String defaultUIColor = "#FFFFFF";
         @Configurable
         @Comment({"If true, pressing the ESC key in a text field will restore the last text instead of confirming current one.", "Default: fakse"})
         public boolean escRestoresLastText = false;
         @Configurable
         @Comment({"If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme.", "Default: false"})
         public boolean useDarkThemeByDefault = false;
         @Configurable
         @Comment({"If true, vanilla tooltips will be replaced with MUI's RichTooltip", "Default: false"})
         public boolean replaceVanillaTooltips = false;

         public int getDefaultUIColor() {
            return Long.decode(ConfigHolder.INSTANCE.client.ui.defaultUIColor).intValue() | 0xFF000000;
         }
      }
   }

   public static class CompatibilityConfigs {
      @Configurable
      @Comment({"Config options regarding GTEU compatibility with other energy systems"})
      public ConfigHolder.CompatibilityConfigs.EnergyCompatConfig energy = new ConfigHolder.CompatibilityConfigs.EnergyCompatConfig();
      @Configurable
      @Comment({"Config options regarding GTCEu compatibility with AE2"})
      public ConfigHolder.CompatibilityConfigs.AE2CompatConfig ae2 = new ConfigHolder.CompatibilityConfigs.AE2CompatConfig();
      @Configurable
      @Comment({"Config options regarding GTCEu compatibility with minimap mods"})
      public ConfigHolder.CompatibilityConfigs.MinimapCompatConfig minimap = new ConfigHolder.CompatibilityConfigs.MinimapCompatConfig();
      @Configurable
      @Comment({"Whether to hide facades of all blocks in JEI and creative search menu.", "Default: true"})
      public boolean hideFacadesInRecipeViewer = true;
      @Configurable
      @Comment({"Whether to hide filled cells in JEI and creative search menu.", "Default: true"})
      public boolean hideFilledCellsInRecipeViewer = true;
      @Configurable
      @Comment({"Whether to hide the ore processing diagrams in JEI", "Default: false"})
      public boolean hideOreProcessingDiagrams = false;
      @Configurable
      @Comment({"Whether Gregtech should remove smelting recipes from the vanilla furnace for ingots requiring the Electric Blast Furnace.", "Default: true"})
      public boolean removeSmeltingForEBFMetals = true;
      @Configurable
      @Comment({"Whether dimension markers should show the dimension tier value.", "Default: false"})
      public boolean showDimensionTier = false;
      @Configurable
      @Comment({"Whether Create compatibility will be available.", "Default: true"})
      public boolean createCompat = true;

      public static class AE2CompatConfig {
         @Configurable
         @Comment({"The interval between ME Hatch/Bus interact ME network.", "It may cause lag if the interval is too small.", "Default: 2 sec"})
         @Range(
            min = 1L
         )
         public int updateIntervals = 40;
         @Configurable
         @Comment({"The energy consumption of ME Hatch/Bus.", "Default: 4.0AE/t"})
         @DecimalRange(
            min = 0.0,
            max = 2.147483647E9
         )
         @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
         public double meHatchEnergyUsage = 4.0;
      }

      public static class EnergyCompatConfig {
         @Configurable
         @Comment({"Enable Native GTEU to Forge Energy (RF and alike) on GT Cables and Wires.", "This does not enable nor disable Converters.", "Default: true"})
         public boolean nativeEUToFE = true;
         @Configurable
         @Comment({"Enable GTEU to FE (and vice versa) Converters.", "Default: false"})
         @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
         public boolean enableFEConverters = false;
         @Configurable
         @Comment({"Forge Energy to GTEU ratio for converting FE to EU.", "Only affects converters.", "Default: 4 FE == 1 EU"})
         @Range(
            min = 1L,
            max = 2147483647L
         )
         public int feToEuRatio = 4;
         @Configurable
         @Comment({"GTEU to Forge Energy ratio for converting EU to FE.", "Affects native conversion and Converters.", "Default: 4 FE == 1 EU"})
         @Range(
            min = 1L,
            max = 2147483647L
         )
         public int euToFeRatio = 4;
      }

      public static class MinimapCompatConfig {
         @Configurable
         @Comment({"Toggle specific map mod integration on/off"})
         @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
         public ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Toggle toggle = new ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Toggle();
         @Configurable
         @Comment({"The radius, in blocks, that picking up a surface rock will search for veins in.", "0 to disable.", "Default: 24"})
         @Range(
            min = 0L
         )
         public int surfaceRockProspectRange = 24;
         @Configurable
         @Comment({"The radius, in blocks, that clicking an ore block will search for veins in.", "0 to disable", "Default: 24"})
         @Range(
            min = 0L
         )
         public int oreBlockProspectRange = 24;
         @Configurable
         @Comment({"The map scale at which displayed ores will stop scaling."})
         @DecimalRange(
            min = 0.1,
            max = 16.0
         )
         public float oreScaleStop = 1.0F;
         @Configurable
         @Comment({"The size, in pixels, of ore icons on the map"})
         @Range(
            min = 4L
         )
         public int oreIconSize = 32;
         @Configurable
         @Comment({"The string prepending ore names in the ore vein tooltip"})
         public String oreNamePrefix = "- ";
         @Configurable
         @Comment({"The color to draw a box around the ore icon with.", "Accepts either an ARGB hex color prefixed with # or the string 'material' to use the ore's material color"})
         public String borderColor = "#00000000";
         @Configurable
         @Comment({"Which part of the screen to anchor buttons to", "Default: BOTTOM_LEFT"})
         public ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Anchor buttonAnchor = ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Anchor.BOTTOM_LEFT;
         @Configurable
         @Comment({"Which direction the buttons will go", "Default: VERTICAL"})
         public ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction direction = ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL;
         @Configurable
         @Comment({"How horizontally far away from the anchor to place the buttons", "Default: 20"})
         public int xOffset = 20;
         @Configurable
         @Comment({"How vertically far away from the anchor to place the buttons", "Default: 0"})
         public int yOffset = 0;

         public int getBorderColor(int materialColor) {
            if (this.borderColor.equals("material")) {
               return materialColor;
            } else {
               try {
                  long tmp = Long.decode(this.borderColor);
                  if (tmp > 2147483647L) {
                     tmp -= 4294967296L;
                  }

                  return (int)tmp;
               } catch (NumberFormatException var4) {
                  return 0;
               }
            }
         }

         public static enum Anchor {
            TOP_LEFT,
            TOP_CENTER,
            TOP_RIGHT,
            RIGHT_CENTER,
            BOTTOM_RIGHT,
            BOTTOM_CENTER,
            BOTTOM_LEFT,
            LEFT_CENTER;

            public boolean isCentered() {
               return this == TOP_CENTER || this == RIGHT_CENTER || this == BOTTOM_CENTER || this == LEFT_CENTER;
            }

            public ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction usualDirection() {
               return switch (this) {
                  case TOP_CENTER, BOTTOM_CENTER -> ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.HORIZONTAL;
                  default -> null;
                  case RIGHT_CENTER, LEFT_CENTER -> ConfigHolder.CompatibilityConfigs.MinimapCompatConfig.Direction.VERTICAL;
               };
            }
         }

         public static enum Direction {
            VERTICAL,
            HORIZONTAL;
         }

         public static class Toggle {
            @Configurable
            @Comment({"FTB Chunks integration enabled"})
            public boolean ftbChunksIntegration = false;
            @Configurable
            @Comment({"Journey Map integration enabled"})
            public boolean journeyMapIntegration = true;
            @Configurable
            @Comment({"Xaerox's map integration enabled"})
            public boolean xaerosMapIntegration = true;
         }
      }
   }

   public static class DeveloperConfigs {
      @Configurable
      @Comment({"Debug general events? (will print recipe conficts etc. to server's debug.log)", "Default: false"})
      public boolean debug = false;
      @Configurable
      @Comment({"Debug ore vein placement? (will print placed veins to server's debug.log)", "Default: false (no placement printout in debug.log)"})
      public boolean debugWorldgen = false;
      @Configurable
      @Comment({"Generate ores in superflat worlds?", "Default: false"})
      public boolean doSuperflatOres = false;
      @Configurable
      @Comment({"Dump all registered GT recipes?", "Default: false"})
      public boolean dumpRecipes = false;
      @Configurable
      @Comment({"Dump all registered GT models/blockstates/etc?", "Default: false"})
      public boolean dumpAssets = false;
      @Configurable
      @Comment({"Executes ./gradlew :processResources when F3+T is pressed", "Only works in a development environment", "Default: false"})
      public boolean autoRebuildResources = false;
   }

   public static class GameplayConfigs {
      @Configurable
      @Comment({"Enable hazardous materials", "Default: true"})
      @Synchronized
      public boolean hazardsEnabled = true;
      @Configurable
      @Comment({"Whether hazards are applied to all valid items, or just GT's.", "true = all, false = GT only.", "Default: true"})
      @Synchronized
      public boolean universalHazards = true;
      @Configurable
      @Comment({"Whether environmental hazards like pollution or radiation are active", "Default: false"})
      @Synchronized
      public boolean environmentalHazards = false;
      @Configurable
      @Comment({"How much environmental hazards decay per chunk, per tick.", "Default: 0.001"})
      public float environmentalHazardDecayRate = 0.001F;
      @Configurable
      @Comment({"List of domains that are allowed in the image module"})
      public String[] allowedImageDomains = new String[]{"imgur.com", "discord.com", "github.com", "raw.githubusercontent.com"};
   }

   public static class MachineConfigs {
      @Configurable
      @Comment({"Whether to require a Wrench, Wirecutter, or other GregTech tools to break machines, casings, wires, and more.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean requireGTToolsForBlocks = true;
      @Configurable
      @Comment({"Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava", "Default: false"})
      @Synchronized
      public boolean shouldWeatherOrTerrainExplosion = false;
      @Configurable
      @Comment({"Energy use multiplier for electric items.", "Default: 100"})
      @Synchronized
      public int energyUsageMultiplier = 100;
      @Configurable
      @Comment({"Energy use multiplier for prospectors.", "Default: 100"})
      @Synchronized
      public int prospectorEnergyUseMultiplier = 100;
      @Configurable
      @Comment({"Whether machines or boilers damage the terrain when they explode.", "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.", "Default: true"})
      @Synchronized
      public boolean doesExplosionDamagesTerrain = true;
      @Configurable
      @Comment({"Enables Safe Active Transformers, removing their ability to explode if unformed while transmitting/receiving power.", "Default: false"})
      @Synchronized
      public boolean harmlessActiveTransformers = false;
      @Configurable
      @Comment({"Whether to play machine sounds while machines are active.", "Default: true"})
      public boolean machineSounds = true;
      @Configurable
      @Comment({"Duration in ticks that batching will try to reach.", "Default: 100"})
      public int batchDuration = 100;
      @Configurable
      @Comment({"Whether Steam Multiblocks should use Steel instead of Bronze.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean steelSteamMultiblocks = false;
      @Configurable
      @Comment({"Whether to enable the cleanroom, required for various recipes.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean enableCleanroom = true;
      @Configurable
      @Comment({"Whether multiblocks should ignore all cleanroom requirements.", "This does nothing if enableCleanroom is false.", "Default: false"})
      public boolean cleanMultiblocks = false;
      @Configurable
      @Comment({"Whether the miner should attempt to replace the block mined with a cobbled version of the ore", "Default: true"})
      public boolean replaceWithCobbleVersion = true;
      @Configurable
      @Comment({"Block to replace mined ores with in the miner and multiblock miner.", "Default: minecraft:cobblestone"})
      public String replaceMinedBlocksWith = "minecraft:cobblestone";
      @Configurable
      @Comment({"Whether to enable Assembly Line research for recipes.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean enableResearch = true;
      @Configurable
      @Comment({"Whether to enable the Maintenance Hatch, required for Multiblocks.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean enableMaintenance = true;
      @Configurable
      @Comment({"How often to check for maintenance, rolling a 1/6000 chance every X ticks (before secondary effects like Configurable Maintenance Hatch).", "In default settings, this equates to a 5% chance every hour of a machine running.", "Default: 1000 (ticks)"})
      public int maintenanceCheckRate = 1000;
      @Configurable
      @Comment({"Whether to enable World Accelerators, which accelerate ticks for surrounding Tile Entities, Crops, etc.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean enableWorldAccelerators = true;
      @Configurable
      @Comment({"List of TileEntities that the World Accelerator should not accelerate.", "GregTech TileEntities are always blocked.", "Entries must be in a fully qualified format. For example: appeng.tile.networking.TileController", "Default: none"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public String[] worldAcceleratorBlacklist = new String[0];
      @Configurable
      @Comment({"Whether to use GT6-style pipe and cable connections, meaning they will not auto-connect unless placed directly onto another pipe or cable.", "Default: true"})
      @Synchronized
      public boolean gt6StylePipesCables = true;
      @Configurable
      @Comment({"Whether the machine's circuit slot need to be inserted a real circuit."})
      @Synchronized
      public boolean ghostCircuit = true;
      @Configurable
      @Comment({"Whether to add a \"Bedrock Ore Miner\" (also enables bedrock ore generation)", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean doBedrockOres = false;
      @Configurable
      @Comment({"What Kind of material should the bedrock ore miner output?", "Default: \"raw\""})
      public String bedrockOreDropTagPrefix = "raw";
      @Configurable
      @Range(
         min = 120L,
         max = 800L
      )
      @Comment({"The base amount of ticks per block for electric singleblock ore miners", "Default: 320"})
      public int minerSpeed = 320;
      @Configurable
      @Comment({"Makes nearly every GCYM Multiblock require blocks which set their maximum voltages.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean enableTieredCasings = false;
      @Configurable
      @Comment({"Minimum distance between Long Distance Item Pipe Endpoints", "Default: 50"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int ldItemPipeMinDistance = 50;
      @Configurable
      @Comment({"Minimum distance betweeb Long Distance Fluid Pipe Endpoints", "Default: 50"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int ldFluidPipeMinDistance = 50;
      @Configurable
      @Comment({"Whether ONLY owners can open a machine gui", "Default: false"})
      @Synchronized
      public boolean onlyOwnerGUI = false;
      @Configurable
      @Comment({"Whether ONLY owners can break a machine", "Default: false"})
      @Synchronized
      public boolean onlyOwnerBreak = false;
      @Configurable
      @Comment({"Minimum op level to bypass the ownership checks", "Default: 2"})
      @Range(
         min = 0L,
         max = 4L
      )
      @Synchronized
      public int ownerOPBypass = 2;
      @Configurable
      @Comment({"If High Tier (>UV-tier) GT content should be registered.", "Items and Machines enabled with this config will have missing recipes by default.", "This is intended for modpack developers only, and is not playable without custom tweaks or addons.", "Other mods can override this to true, regardless of the config file.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean highTierContent = false;
      @Configurable
      @Comment({"Whether the Assembly Line should require the item inputs to be in order.", "Default: true"})
      public boolean orderedAssemblyLineItems = true;
      @Configurable
      @Comment({"Whether the Assembly Line should require the fluid inputs to be in order.", "(Requires Ordered Assembly Line Item Inputs to be enabled.)", "Default: true"})
      public boolean orderedAssemblyLineFluids = true;
      @Configurable
      @Comment({"Default maximum parallel of steam multiblocks", "Default: 8"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int steamMultiParallelAmount = 8;
      @Configurable
      @Comment({"Small Steam Boiler Options"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public ConfigHolder.MachineConfigs.SmallBoilers smallBoilers = new ConfigHolder.MachineConfigs.SmallBoilers();
      @Configurable
      @Comment({"Large Steam Boiler Options"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public ConfigHolder.MachineConfigs.LargeBoilers largeBoilers = new ConfigHolder.MachineConfigs.LargeBoilers();

      public static class LargeBoilers {
         @Configurable
         @Comment({"The conversion rate between water and steam in Large Boilers.", "Default: 160"})
         public int steamPerWater = 160;
         @Configurable
         @Comment({"The max temperature of the Large Bronze Boiler.", "Default: 800"})
         public int bronzeBoilerMaxTemperature = 800;
         @Configurable
         @Comment({"The heat speed of the Large Bronze Boiler.", "Default: 1"})
         public int bronzeBoilerHeatSpeed = 1;
         @Configurable
         @Comment({"The max temperature of the Large Steel Boiler.", "Default: 1800"})
         public int steelBoilerMaxTemperature = 1800;
         @Configurable
         @Comment({"The heat speed of the Large Steel Boiler.", "Default: 1"})
         public int steelBoilerHeatSpeed = 1;
         @Configurable
         @Comment({"The max temperature of the Large Titanium Boiler.", "Default: 3200"})
         public int titaniumBoilerMaxTemperature = 3200;
         @Configurable
         @Comment({"The heat speed of the Large Titanium Boiler.", "Default: 1"})
         public int titaniumBoilerHeatSpeed = 1;
         @Configurable
         @Comment({"The max temperature of the Large Tungstensteel Boiler.", "Default: 6400"})
         public int tungstensteelBoilerMaxTemperature = 6400;
         @Configurable
         @Comment({"The heat speed of the Large Tungstensteel Boiler.", "Default: 2"})
         public int tungstensteelBoilerHeatSpeed = 2;
      }

      public static class SmallBoilers {
         @Configurable
         @Comment({"The amount of steam a Steam Solid Boiler produces per second at max temperature.", "Default: 120"})
         public int solidBoilerBaseOutput = 120;
         @Configurable
         @Comment({"The amount of steam a High Pressure Steam Solid Boiler produces per second at max temperature.", "Default: 300"})
         public int hpSolidBoilerBaseOutput = 300;
         @Configurable
         @Comment({"The amount of steam a Steam Liquid Boiler produces per second at max temperature.", "Default: 240"})
         public int liquidBoilerBaseOutput = 240;
         @Configurable
         @Comment({"The amount of steam a High Pressure Steam Liquid Boiler produces per second at max temperature.", "Default: 600"})
         public int hpLiquidBoilerBaseOutput = 600;
         @Configurable
         @Comment({"The amount of steam a Steam Solar Boiler produces per second at max temperature.", "Default: 120"})
         public int solarBoilerBaseOutput = 120;
         @Configurable
         @Comment({"The amount of steam a High Pressure Steam Solar Boiler produces per second at max temperature.", "Default: 360"})
         public int hpSolarBoilerBaseOutput = 360;
      }
   }

   public static class RecipeConfigs {
      @Configurable
      @Comment({"Whether to generate Flawed and Chipped Gems for materials and recipes involving them.", "Useful for mods like TerraFirmaCraft.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public boolean generateLowQualityGems = false;
      @Configurable
      @Comment({"Whether to remove Block/Ingot compression and decompression in the Crafting Table.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean disableManualCompression = true;
      @Configurable
      @Comment({"Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean harderRods = true;
      @Configurable
      @Comment({"Whether to make crafting recipes for Bricks, Firebricks, Nether Bricks, and Coke Bricks harder.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean harderBrickRecipes = false;
      @Configurable
      @Comment({"Whether to nerf Wood crafting to 2 Planks from 1 Log, and 2 Sticks from 2 Planks.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean nerfWoodCrafting = false;
      @Configurable
      @Comment({"Whether to make Wood related recipes harder.", "Excludes sticks and planks.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardWoodRecipes = false;
      @Configurable
      @Comment({"Recipes for Buckets, Cauldrons, Hoppers, and Iron Bars require Iron Plates, Rods, and more.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardIronRecipes = true;
      @Configurable
      @Comment({"Whether to make Redstone related recipes harder.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardRedstoneRecipes = false;
      @Configurable
      @Comment({"Whether to make Vanilla Tools and Armor recipes harder.", "Excludes Flint and Steel, and Buckets.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardToolArmorRecipes = false;
      @Configurable
      @Comment({"Whether to make miscellaneous recipes harder.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardMiscRecipes = false;
      @Configurable
      @Comment({"Whether to make Glass related recipes harder. Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardGlassRecipes = true;
      @Configurable
      @Comment({"Whether to nerf the Paper crafting recipe.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean nerfPaperCrafting = true;
      @Configurable
      @Comment({"Recipes for items like Iron Doors, Trapdoors, Anvil require Iron Plates, Rods, and more.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardAdvancedIronRecipes = true;
      @Configurable
      @Comment({"Whether to make coloring blocks like Concrete or Glass harder.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardDyeRecipes = false;
      @Configurable
      @Comment({"Whether to remove charcoal smelting recipes from the vanilla furnace.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean harderCharcoalRecipe = true;
      @Configurable
      @Comment({"Whether to make the Flint and Steel recipe require steel parts.", "Default: true."})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean flintAndSteelRequireSteel = true;
      @Configurable
      @Comment({"Whether to remove Vanilla Block Recipes from the Crafting Table.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean removeVanillaBlockRecipes = false;
      @Configurable
      @Comment({"Whether to remove Vanilla TNT Recipe from the Crafting Table.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean removeVanillaTNTRecipe = true;
      @Configurable
      @Comment({"Restore vanilla recipe definitions when the ATM10 marker pair is loaded.", "This keeps the original furnace, crafting, smelting, and compression recipes available while", "leaving GregTech machine recipes and processing chains intact.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean restoreVanillaRecipesForAtm10 = true;
      @Configurable
      @Comment({"How many Multiblock Casings to make per craft. Either 1, 2, or 3.", "Default: 2"})
      @Range(
         min = 1L,
         max = 3L
      )
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public int casingsPerCraft = 2;
      @Configurable
      @Comment({"Whether to nerf the output amounts of the first circuit in a set to 1 (from 2) and SoC to 2 (from 4).", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean harderCircuitRecipes = false;
      @Configurable
      @Comment({"Whether to nerf machine controller recipes.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean hardMultiRecipes = false;
      @Configurable
      @Comment({"Whether tools should have enchants or not. Like the flint sword getting fire aspect.", "Default: false"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean enchantedTools = false;
      @Configurable
      @Comment({"Whether to enable macerator decomposition recycling", "Default: true"})
      public boolean enableMaceratorRecycling = true;
      @Configurable
      @Comment({"Percentage yield of macerator decomposition recycling outputs, 1.0 means 100%", "Default: 1.0f"})
      @DecimalRange(
         min = 0.0,
         max = 1.0
      )
      public float maceratorRecyclingYield = 1.0F;
      @Configurable
      @Comment({"Whether to enable arc furnace decomposition recycling", "Default: true"})
      public boolean enableArcRecycling = true;
      @Configurable
      @Comment({"Percentage yield of arc furnace decomposition recycling outputs, 1.0 means 100%", "Default: 1.0f"})
      @DecimalRange(
         min = 0.0,
         max = 1.0
      )
      public float arcRecyclingYield = 1.0F;
      @Configurable
      @Comment({"Whether to enable extractor decomposition recycling", "Default: true"})
      public boolean enableExtractorRecycling = true;
      @Configurable
      @Comment({"Percentage yield of extractor decomposition recycling outputs, 1.0 means 100%", "Default: 1.0f"})
      @DecimalRange(
         min = 0.0,
         max = 1.0
      )
      public float extractorRecyclingYield = 1.0F;

      public boolean isAtm10VanillaRecipeRestorationEnabled() {
         return this.restoreVanillaRecipesForAtm10 && Atm10Environment.isDetected();
      }

      public ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope beginAtm10VanillaRecipeRestoration() {
         return this.isAtm10VanillaRecipeRestorationEnabled()
            ? new ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope(this)
            : ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope.NOOP;
      }

      public static final class VanillaRecipeRestorationScope implements AutoCloseable {
         private static final ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope NOOP = new ConfigHolder.RecipeConfigs.VanillaRecipeRestorationScope();
         private final ConfigHolder.RecipeConfigs owner;
         private final boolean disableManualCompression;
         private final boolean harderBrickRecipes;
         private final boolean nerfWoodCrafting;
         private final boolean hardWoodRecipes;
         private final boolean hardIronRecipes;
         private final boolean hardRedstoneRecipes;
         private final boolean hardToolArmorRecipes;
         private final boolean hardMiscRecipes;
         private final boolean hardGlassRecipes;
         private final boolean nerfPaperCrafting;
         private final boolean hardAdvancedIronRecipes;
         private final boolean hardDyeRecipes;
         private final boolean harderCharcoalRecipe;
         private final boolean flintAndSteelRequireSteel;
         private final boolean removeVanillaBlockRecipes;
         private final boolean removeVanillaTNTRecipe;
         private boolean closed;

         private VanillaRecipeRestorationScope() {
            this.owner = null;
            this.disableManualCompression = false;
            this.harderBrickRecipes = false;
            this.nerfWoodCrafting = false;
            this.hardWoodRecipes = false;
            this.hardIronRecipes = false;
            this.hardRedstoneRecipes = false;
            this.hardToolArmorRecipes = false;
            this.hardMiscRecipes = false;
            this.hardGlassRecipes = false;
            this.nerfPaperCrafting = false;
            this.hardAdvancedIronRecipes = false;
            this.hardDyeRecipes = false;
            this.harderCharcoalRecipe = false;
            this.flintAndSteelRequireSteel = false;
            this.removeVanillaBlockRecipes = false;
            this.removeVanillaTNTRecipe = false;
            this.closed = true;
         }

         private VanillaRecipeRestorationScope(ConfigHolder.RecipeConfigs owner) {
            this.owner = owner;
            this.disableManualCompression = owner.disableManualCompression;
            this.harderBrickRecipes = owner.harderBrickRecipes;
            this.nerfWoodCrafting = owner.nerfWoodCrafting;
            this.hardWoodRecipes = owner.hardWoodRecipes;
            this.hardIronRecipes = owner.hardIronRecipes;
            this.hardRedstoneRecipes = owner.hardRedstoneRecipes;
            this.hardToolArmorRecipes = owner.hardToolArmorRecipes;
            this.hardMiscRecipes = owner.hardMiscRecipes;
            this.hardGlassRecipes = owner.hardGlassRecipes;
            this.nerfPaperCrafting = owner.nerfPaperCrafting;
            this.hardAdvancedIronRecipes = owner.hardAdvancedIronRecipes;
            this.hardDyeRecipes = owner.hardDyeRecipes;
            this.harderCharcoalRecipe = owner.harderCharcoalRecipe;
            this.flintAndSteelRequireSteel = owner.flintAndSteelRequireSteel;
            this.removeVanillaBlockRecipes = owner.removeVanillaBlockRecipes;
            this.removeVanillaTNTRecipe = owner.removeVanillaTNTRecipe;
            owner.disableManualCompression = false;
            owner.harderBrickRecipes = false;
            owner.nerfWoodCrafting = false;
            owner.hardWoodRecipes = false;
            owner.hardIronRecipes = false;
            owner.hardRedstoneRecipes = false;
            owner.hardToolArmorRecipes = false;
            owner.hardMiscRecipes = false;
            owner.hardGlassRecipes = false;
            owner.nerfPaperCrafting = false;
            owner.hardAdvancedIronRecipes = false;
            owner.hardDyeRecipes = false;
            owner.harderCharcoalRecipe = false;
            owner.flintAndSteelRequireSteel = false;
            owner.removeVanillaBlockRecipes = false;
            owner.removeVanillaTNTRecipe = false;
         }

         @Override
         public void close() {
            if (!this.closed && this.owner != null) {
               this.owner.disableManualCompression = this.disableManualCompression;
               this.owner.harderBrickRecipes = this.harderBrickRecipes;
               this.owner.nerfWoodCrafting = this.nerfWoodCrafting;
               this.owner.hardWoodRecipes = this.hardWoodRecipes;
               this.owner.hardIronRecipes = this.hardIronRecipes;
               this.owner.hardRedstoneRecipes = this.hardRedstoneRecipes;
               this.owner.hardToolArmorRecipes = this.hardToolArmorRecipes;
               this.owner.hardMiscRecipes = this.hardMiscRecipes;
               this.owner.hardGlassRecipes = this.hardGlassRecipes;
               this.owner.nerfPaperCrafting = this.nerfPaperCrafting;
               this.owner.hardAdvancedIronRecipes = this.hardAdvancedIronRecipes;
               this.owner.hardDyeRecipes = this.hardDyeRecipes;
               this.owner.harderCharcoalRecipe = this.harderCharcoalRecipe;
               this.owner.flintAndSteelRequireSteel = this.flintAndSteelRequireSteel;
               this.owner.removeVanillaBlockRecipes = this.removeVanillaBlockRecipes;
               this.owner.removeVanillaTNTRecipe = this.removeVanillaTNTRecipe;
               this.closed = true;
            }
         }
      }
   }

   public static class ToolConfigs {
      @Configurable
      @Comment({"Random chance for electric tools to take actual damage", "Default: 10%"})
      @Range(
         min = 0L,
         max = 100L
      )
      public int rngDamageElectricTools = 10;
      @Configurable
      @Comment({"Amount of blocks that can be spray painted at once", "Default: 16"})
      @Range(
         min = 1L,
         max = 512L
      )
      public int sprayCanChainLength = 16;
      @Configurable
      @Comment({"Delay in ticks between each log being broken when tree felling", "Default: 2"})
      @Range(
         min = 1L,
         max = 400L
      )
      public int treeFellingDelay = 2;
      @Configurable
      @Comment({"NanoSaber Options"})
      public ConfigHolder.ToolConfigs.NanoSaber nanoSaber = new ConfigHolder.ToolConfigs.NanoSaber();
      @Configurable
      @Comment({"NightVision Goggles Voltage Tier. Default: 1 (LV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierNightVision = 1;
      @Configurable
      @Comment({"NanoSuit Voltage Tier. Default: 3 (HV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierNanoSuit = 3;
      @Configurable
      @Comment({"Advanced NanoSuit Chestplate Voltage Tier.", "Default: 3 (HV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierAdvNanoSuit = 3;
      @Configurable
      @Comment({"QuarkTech Suit Voltage Tier.", "Default: 5 (IV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierQuarkTech = 5;
      @Configurable
      @Comment({"Advanced QuarkTech Suit Chestplate Voltage Tier.", "Default: 6 (LuV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierAdvQuarkTech = 6;
      @Configurable
      @Comment({"Electric Impeller Jetpack Voltage Tier.", "Default: 2 (MV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierImpeller = 2;
      @Configurable
      @Comment({"Advanced Electric Jetpack Voltage Tier.", "Default: 3 (HV)"})
      @Range(
         min = 0L,
         max = 14L
      )
      @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
      public int voltageTierAdvImpeller = 3;

      public static class NanoSaber {
         @Configurable
         @DecimalRange(
            min = 0.0,
            max = 100.0
         )
         @Comment({"The additional damage added when the NanoSaber is powered.", "Default: 20.0"})
         @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
         public double nanoSaberDamageBoost = 20.0;
         @Configurable
         @DecimalRange(
            min = 0.0,
            max = 100.0
         )
         @Comment({"The base damage of the NanoSaber.", "Default: 5.0"})
         @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
         public double nanoSaberBaseDamage = 5.0;
         @Configurable
         @Comment({"Should Zombies spawn with charged, active NanoSabers on hard difficulty?", "Default: true"})
         public boolean zombieSpawnWithSabers = true;
         @Configurable
         @Range(
            min = 1L,
            max = 512L
         )
         @Comment({"The EU/t consumption of the NanoSaber.", "Default: 64"})
         @UpdateRestriction(UpdateRestrictions.GAME_RESTART)
         public int energyConsumption = 64;
      }
   }

   public static class WorldGenConfigs {
      @Configurable
      @Comment({"Rubber Tree spawn chance (decimal % per chunk)", "Default: 0.5"})
      @DecimalRange(
         min = 0.0,
         max = 1.0
      )
      public float rubberTreeSpawnChance = 0.5F;
      @Configurable
      @Comment({"Should Sand-like ores fall?", "This includes gravel, sand, and red sand ores.", "Default: false (no falling ores)"})
      public boolean sandOresFall = false;
      @Configurable
      @Comment({"Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically.", "Default: true", "WARNING: Currently unimplemented."})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean increaseDungeonLoot = true;
      @Configurable
      @Comment({"Allow GregTech to add additional GregTech Items as loot in various structures.", "Default: true"})
      @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
      public boolean addLoot = true;
      @Configurable
      public ConfigHolder.WorldGenConfigs.OreVeinConfigs oreVeins = new ConfigHolder.WorldGenConfigs.OreVeinConfigs();

      public static class OreVeinConfigs {
         @Configurable
         @Range(
            min = 1L,
            max = 32L
         )
         @Comment({"The grid size (in chunks) for ore vein generation", "Default: 3"})
         public int oreVeinGridSize = 3;
         @Configurable
         @Range(
            min = 0L,
            max = 512L
         )
         @Comment({"The maximum random offset (in blocks) from the grid for generating an ore vein.", "Default: 12"})
         public int oreVeinRandomOffset = 12;
         @Configurable
         @Comment({"Prevents regular vanilla ores from being generated outside GregTech ore veins", "Default: true"})
         @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
         public boolean removeVanillaOreGen = true;
         @Configurable
         @Comment({"Prevents vanilla's large ore veins from being generated", "Default: true"})
         @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
         public boolean removeVanillaLargeOreVeins = true;
         @Configurable
         @Comment({"Distance between bedrock ore veins in chunks, if enabled.", "Default: 16"})
         public int bedrockOreDistance = 16;
         @Configurable
         @Comment({"Make bedrock ore/fluid veins infinite?", "Default: false"})
         public boolean infiniteBedrockOresFluids = false;
         @Configurable
         @Comment({"Generate ores indicators above ore veins", "Default: true"})
         public boolean oreIndicators = true;
         @Configurable
         @Comment({"Sets the maximum number of chunks that may be cached for ore vein generation.", "Higher values may improve world generation performance, but at the cost of more RAM usage.", "If you substantially increase the ore vein grid size, random vein offset, or have very large (custom) veins, you may need to increase this value as well.", "Default: 512"})
         @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
         public int oreGenerationChunkCacheSize = 512;
         @Configurable
         @Comment({"Sets the maximum number of chunks for which ore indicators may be cached.", "If you register any custom veins with very large indicator ranges (or modify existing ones that way), you may need to increase this value.", "Default: 2048"})
         @UpdateRestriction(UpdateRestrictions.MAIN_MENU)
         public int oreIndicatorChunkCacheSize = 2048;
      }
   }
}
