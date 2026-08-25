package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.client.model.item.CustomItemRendererWrapperModel;
import com.gregtechceu.gtceu.client.model.item.FacadeUnbakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModelLoader;
import com.gregtechceu.gtceu.client.particle.GTParticleManager;
import com.gregtechceu.gtceu.client.particle.HazardParticle;
import com.gregtechceu.gtceu.client.particle.MufflerParticle;
import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.SurfaceRockRenderer;
import com.gregtechceu.gtceu.client.renderer.entity.GTExplosiveRenderer;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTComponentItemDecorator;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTLampItemOverlayRenderer;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTTankItemFluidPreview;
import com.gregtechceu.gtceu.client.renderer.item.decorator.GTToolBarRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.gregtechceu.gtceu.client.renderer.machine.impl.BoilerMultiPartRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.CentralMonitorRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.FluidAreaRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.FusionRingRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.GrowingPlantRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender;
import com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumTankFluidRender;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;
import com.gregtechceu.gtceu.common.CommonEventListener;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.gregtechceu.gtceu.common.item.QuantumTankMachineItem;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;
import com.gregtechceu.gtceu.data.pack.event.RegisterDynamicResourcesEvent;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksPlugin;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.OreRenderLayer;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class ClientProxy {
   public static void init(IEventBus modBus) {
      modBus.register(ClientProxy.class);
      if (GTCEu.Mods.isFTBQuestsLoaded()) {
         FTBQuestClientIntegration.init();
      }

      if (!GTCEu.isDataGen()) {
         ClientCacheManager.registerClientCache(GTClientCache.instance, "gtceu");
         Layers.registerLayer(OreRenderLayer::new, "ore_veins");
         Layers.registerLayer(FluidRenderLayer::new, "bedrock_fluids");
         CommonEventListener.registerCapes(new RegisterGTCapesEvent());
      }

      initializeDynamicRenders();
      ModelEventHelper.initInternalAssetReloadListeners();
      NeoForge.EVENT_BUS.register(GTParticleManager.INSTANCE);
      GTGuiTextures.init();
      ModLoadingContext.get().getActiveContainer().getEventBus().addListener(GTGuiTheme::onReloadThemes);
   }

   @SubscribeEvent
   public static void onRegisterEntityRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)GTEntityTypes.DYNAMITE.get(), ThrownItemRenderer::new);
      event.registerEntityRenderer((EntityType)GTEntityTypes.POWDERBARREL.get(), GTExplosiveRenderer::new);
      event.registerEntityRenderer((EntityType)GTEntityTypes.INDUSTRIAL_TNT.get(), GTExplosiveRenderer::new);
   }

   @SubscribeEvent
   public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof IComponentItem) {
            event.register(item, GTComponentItemDecorator.INSTANCE);
         }

         if (item instanceof IGTTool) {
            event.register(item, GTToolBarRenderer.INSTANCE);
         }

         if (item instanceof LampBlockItem) {
            event.register(item, GTLampItemOverlayRenderer.INSTANCE);
         }

         if (item instanceof DrumMachineItem) {
            event.register(item, GTTankItemFluidPreview.DRUM);
         }

         if (item instanceof QuantumTankMachineItem) {
            event.register(item, GTTankItemFluidPreview.QUANTUM_TANK);
         }
      }
   }

   @SubscribeEvent
   public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
      SyncedKeyMapping.onRegisterKeyBinds(event);
   }

   @SubscribeEvent
   public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
      event.registerAboveAll(GTCEu.id("hud"), new HudGuiOverlay());
   }

   @SubscribeEvent
   public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)GTParticleTypes.HAZARD_PARTICLE.get(), HazardParticle.Provider::new);
      event.registerSpriteSet((ParticleType)GTParticleTypes.MUFFLER_PARTICLE.get(), MufflerParticle.Provider::new);
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      if (ConfigHolder.INSTANCE.compat.minimap.toggle.ftbChunksIntegration && GTCEu.isModLoaded("ftbchunks")) {
         FTBChunksPlugin.addEventListeners();
      }
   }

   private static void initializeDynamicRenders() {
      DynamicRenderManager.register(GTCEu.id("quantum_tank_fluid"), QuantumTankFluidRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("quantum_chest_item"), QuantumChestItemRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("fusion_ring"), FusionRingRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("boiler_multi_parts"), BoilerMultiPartRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("fluid_area"), FluidAreaRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("growing_plant"), GrowingPlantRender.TYPE);
      DynamicRenderManager.register(GTCEu.id("central_monitor"), CentralMonitorRender.TYPE);
   }

   @SubscribeEvent
   public static void registerModelLoaders(RegisterGeometryLoaders event) {
      event.register(MachineModelLoader.ID, MachineModelLoader.INSTANCE);
      event.register(PipeModelLoader.ID, PipeModelLoader.INSTANCE);
      event.register(GTCEu.id("facade"), FacadeUnbakedModel.Loader.INSTANCE);
      event.register(CustomItemRendererWrapperModel.ID, CustomItemRendererWrapperModel.Loader.INSTANCE);
   }

   @SubscribeEvent
   public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      event.registerFluidType(new IClientFluidTypeExtensions() {
         private static final ResourceLocation TEXTURE = GTCEu.id("block/fluids/fluid.potion");

         @NotNull
         public ResourceLocation getStillTexture() {
            return TEXTURE;
         }

         @NotNull
         public ResourceLocation getFlowingTexture() {
            return TEXTURE;
         }

         public int getTintColor(@NotNull FluidStack stack) {
            return ((PotionContents)stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).getColor() | 0xFF000000;
         }
      }, new FluidType[]{GTFluids.POTION.getType()});
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void preRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
      PipeModel.DYNAMIC_MODELS.clear();
   }

   @SubscribeEvent
   public static void registerDynamicAssets(RegisterDynamicResourcesEvent event) {
      for (BlockEntry<CableBlock> block : GTMaterialBlocks.CABLE_BLOCKS.values()) {
         if (block != null) {
            ((CableBlock)block.get()).createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
         }
      }

      for (BlockEntry<FluidPipeBlock> blockx : GTMaterialBlocks.FLUID_PIPE_BLOCKS.values()) {
         if (blockx != null) {
            ((FluidPipeBlock)blockx.get()).createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
         }
      }

      for (BlockEntry<ItemPipeBlock> blockxx : GTMaterialBlocks.ITEM_PIPE_BLOCKS.values()) {
         if (blockxx != null) {
            ((ItemPipeBlock)blockxx.get()).createPipeModel(RuntimeBlockstateProvider.INSTANCE).dynamicModel();
         }
      }

      MaterialBlockRenderer.reinitModels();
      TagPrefixItemRenderer.reinitModels();
      OreBlockRenderer.reinitModels();
      ToolItemRenderer.reinitModels();
      ArmorItemRenderer.reinitModels();
      SurfaceRockRenderer.reinitModels();
      GTModels.registerMaterialFluidModels();
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void postRegisterDynamicAssets(RegisterDynamicResourcesEvent event) {
      PipeModel.initDynamicModels();
      if (GTCEu.Mods.isKubeJSLoaded()) {
         GregTechKubeJSPlugin.generateMachineBlockModels();
      }

      RuntimeBlockstateProvider.INSTANCE.run();
      PipeModelBuilder.clearRestrictorModelCache();
   }
}
