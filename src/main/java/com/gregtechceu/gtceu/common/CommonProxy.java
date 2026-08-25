package com.gregtechceu.gtceu.common;

import brachy.modularui.factory.GuiManager;
import com.google.common.collect.Multimaps;
import com.google.common.collect.UnmodifiableIterator;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.compat.EUToFEProvider;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.IndicatorGenerators;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerators;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.misc.forge.QuantumFluidHandlerItemStack;
import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.mui.factory.MachineUIFactory;
import com.gregtechceu.gtceu.api.multiblock.error.GTPatternErrors;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.IntersectionMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.CustomFluidMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidDataComponentMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid.FluidTagMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.CustomItemMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemDataComponentMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemTagMapIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.block.DuctPipeBlock;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.block.LaserPipeBlock;
import com.gregtechceu.gtceu.common.block.OpticalPipeBlock;
import com.gregtechceu.gtceu.common.data.GTAttachmentTypes;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTCommandArguments;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTDimensionMarkers;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.GTMobEffects;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;
import com.gregtechceu.gtceu.common.data.GTPlaceholders;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.GTToolTiers;
import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.fluid.potion.BottleItemFluidHandler;
import com.gregtechceu.gtceu.common.fluid.potion.PotionItemFluidHandler;
import com.gregtechceu.gtceu.common.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.item.GTBucketItem;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.tool.rotation.CustomBlockRotations;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTheme;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.compat.atm10.EmiGregStarVisibilityCompat;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.registrate.AbstractRegistrateAccessor;
import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.gregtechceu.gtceu.data.lang.MaterialLangGenerator;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;
import com.gregtechceu.gtceu.integration.cctweaked.CCTweakedPlugin;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.MaterialModificationEventJS;
import com.gregtechceu.gtceu.integration.kjs.helpers.KubeGTRegistryEventHandler;
import com.gregtechceu.gtceu.integration.map.WaypointManager;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;

public class CommonProxy {
   private static IEventBus modBus;
   private static boolean didRunRegistration = false;

   public static void init(IEventBus modBus) {
      CommonProxy.modBus = modBus;
      if (GTCEu.Mods.isKubeJSLoaded()) {
         modBus.register(KubeGTRegistryEventHandler.class);
      }

      modBus.register(CommonProxy.class);
      GuiManager.registerFactory(MachineUIFactory.INSTANCE);
      GuiManager.registerFactory(CoverUIFactory.INSTANCE);
      GTGuiTheme.registerThemes();
      GregTechDatagen.initPre();
      GTRegistries.init(modBus);
      GTDataComponents.DATA_COMPONENTS.register(modBus);
      GTRegistration.REGISTRATE.registerEventListeners(modBus);
      GTCreativeModeTabs.init();
      GTAttachmentTypes.ATTACHMENT_TYPES.register(modBus);
      FusionReactorMachine.registerFusionTier(6, "MKI");
      FusionReactorMachine.registerFusionTier(7, "MKII");
      FusionReactorMachine.registerFusionTier(8, "MKIII");
      AddonFinder.getAddonList().forEach(IGTAddon::gtInitComplete);
   }

   @SubscribeEvent
   public static void onRegister(RegisterEvent event) {
      if (!didRunRegistration) {
         didRunRegistration = true;
         GTElements.init();
         MaterialIconSet.init();
         MaterialIconType.init();
         initMaterials();
         GTMedicalConditions.init();
         TagPrefix.init();
         GTSoundEntries.init();
         GTDamageTypes.init();
         GTPlaceholders.init();
         if (ConfigHolder.INSTANCE.compat.createCompat && GTCEu.Mods.isCreateLoaded()) {
            GTCreateIntegration.init();
         }

         GTCovers.init();
         GTCreativeModeTabs.init();
         GTBlocks.init();
         GTFluids.init();
         GTDimensionMarkers.init();
         GTRecipeCapabilities.init();
         GTRecipeConditions.init();
         ChanceLogic.init();
         GTRecipeTypes.init();
         GTRecipeCategories.init();
         GTPatternErrors.init();
         GTFoods.init();
         GTToolTiers.init();
         GTToolBehaviors.init();
         GTArmorMaterials.ARMOR_MATERIALS.register(modBus);
         GTItems.init();
         GTMachineUtils.init();
         GTCovers.init();
         GTMachines.init();
         GTEntityTypes.init();
         GTIngredientTypes.ITEM_INGREDIENT_TYPES.register(modBus);
         GTIngredientTypes.FLUID_INGREDIENT_TYPES.register(modBus);
         GTRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
         GTCommandArguments.COMMAND_ARGUMENT_TYPES.register(modBus);
         GTMobEffects.MOB_EFFECTS.register(modBus);
         GTParticleTypes.PARTICLE_TYPES.register(modBus);
         WorldGenLayers.init();
         GregTechDatagen.initPost();
         GTValueProviderTypes.init(modBus);
         GTFeatures.register(modBus);
         VeinGenerators.registerAddonGenerators();
         IndicatorGenerators.registerAddonGenerators();
         WaypointManager.init();
         CustomBlockRotations.init();
         SyncedKeyMappings.init();
         MachineOwner.init();
         ChestGenHooks.init();
      }
   }

   @Internal
   public static void initMaterials() {
      GTCEu.LOGGER.info("Registering GTCEu Materials");
      GTMaterials.init();
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onRegisterLowest(RegisterEvent event) {
      if (event.getRegistryKey() == GTRegistries.Keys.MATERIAL) {
         GTCEu.LOGGER.info("Firing material register late event");
         GTRegistries.MATERIALS.close();
         ModLoader.postEventWrapContainerInModOrder(new PostMaterialEvent());
         if (GTCEu.Mods.isKubeJSLoaded()) {
            CommonProxy.KJSEventWrapper.materialModification();
         }

         GTRegistries.MATERIALS
            .getUsedNamespaces()
            .forEach(
               namespace -> {
                  GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(namespace);
                  AbstractRegistrateAccessor accessor = (AbstractRegistrateAccessor)registrate;
                  if ((Boolean)accessor.getDoDatagen().get()) {
                     List<NonNullConsumer<? extends RegistrateProvider>> providers = (List<NonNullConsumer<? extends RegistrateProvider>>)Multimaps.asMap(
                           accessor.getDatagens()
                        )
                        .get(ProviderType.LANG);
                     providers.addFirst(provider -> MaterialLangGenerator.generate((RegistrateLangProvider)provider, namespace));
                  }
               }
            );
      } else if (event.getRegistryKey() == GTRegistries.Keys.MACHINE) {
         for (MachineDefinition machine : GTRegistries.MACHINES) {
            UnmodifiableIterator var3 = machine.getStateDefinition().getPossibleStates().iterator();

            while (var3.hasNext()) {
               MachineRenderState renderState = (MachineRenderState)var3.next();
               MachineDefinition.RENDER_STATE_REGISTRY.add(renderState);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void registerMaterialContent(RegisterEvent event) {
      if (event.getRegistryKey() == Registries.BLOCK) {
         GTRegistration.REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_BLOCK);
         GTMaterialBlocks.generateMaterialBlocks();
         GTMaterialBlocks.generateOreBlocks();
         GTMaterialBlocks.generateOreIndicators();
         GTMaterialBlocks.buildMaterialBlockTable();
         GTRegistration.REGISTRATE.creativeModeTab(GTCreativeModeTabs.MATERIAL_PIPE);
         GTMaterialBlocks.generateCableBlocks();
         GTMaterialBlocks.generateFluidPipeBlocks();
         GTMaterialBlocks.generateItemPipeBlocks();
      } else if (event.getRegistryKey() == Registries.ITEM) {
         GTMaterialItems.generateMaterialItems();
         GTMaterialItems.generateTools();
         GTMaterialItems.generateArmors();
      } else if (event.getRegistryKey() == Registries.FLUID) {
         GTFluids.registerMaterialFluids();
      } else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE) {
         GTBlockEntities.init();
      }
   }

   @SubscribeEvent
   public static void registerRegistries(NewRegistryEvent event) {
      GTRegistries.getRegistries().forEach(event::register);
   }

   @SubscribeEvent
   public static void registerDataPackRegistries(NewRegistry event) {
      event.dataPackRegistry(GTRegistries.Keys.ORE_VEIN, GTOreDefinition.DIRECT_CODEC, GTOreDefinition.DIRECT_CODEC);
      event.dataPackRegistry(GTRegistries.Keys.BEDROCK_FLUID, BedrockFluidDefinition.DIRECT_CODEC, BedrockFluidDefinition.DIRECT_CODEC);
      event.dataPackRegistry(GTRegistries.Keys.BEDROCK_ORE, BedrockOreDefinition.DIRECT_CODEC, BedrockOreDefinition.DIRECT_CODEC);
   }

   @SubscribeEvent
   public static void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(
         () -> {
            MapIngredientTypeManager.registerMapIngredient(SizedFluidIngredient.class, ingredient -> {
               FluidIngredient inner = ingredient.ingredient();
               return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(IntProviderFluidIngredient.class, ingredient -> {
               FluidIngredient inner = ingredient.getInner();
               return MapIngredientTypeManager.getFrom(inner, FluidRecipeCapability.CAP);
            });
            MapIngredientTypeManager.registerMapIngredient(CompoundFluidIngredient.class, ingredient -> {
               List<AbstractMapIngredient> list = new ObjectArrayList();

               for (FluidIngredient child : ingredient.children()) {
                  list.addAll(MapIngredientTypeManager.getFrom(child, FluidRecipeCapability.CAP));
               }

               return list;
            });
            MapIngredientTypeManager.registerMapIngredient(DataComponentFluidIngredient.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidIngredient.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(SingleFluidIngredient.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionFluidIngredient.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, FluidDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(FluidStack.class, CustomFluidMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(
               SizedIngredient.class,
               ingredient -> {
                  Ingredient inner = ingredient.ingredient();
                  return inner.isCustom()
                     ? MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP)
                     : MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
               }
            );
            MapIngredientTypeManager.registerMapIngredient(
               IntProviderIngredient.class,
               ingredient -> {
                  Ingredient inner = ingredient.getInner();
                  return inner.isCustom()
                     ? MapIngredientTypeManager.getFrom(inner.getCustomIngredient(), ItemRecipeCapability.CAP)
                     : MapIngredientTypeManager.getFrom(inner, ItemRecipeCapability.CAP);
               }
            );
            MapIngredientTypeManager.registerMapIngredient(DataComponentIngredient.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(Ingredient.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(IntersectionIngredient.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemStackMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemTagMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, ItemDataComponentMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, IntersectionMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(ItemStack.class, CustomItemMapIngredient::from);
            MapIngredientTypeManager.registerMapIngredient(
               IntCircuitIngredient.class, custom -> List.of(new ItemStackMapIngredient(GTItems.PROGRAMMED_CIRCUIT.asStack(), custom.toVanilla()))
            );
            if (GTCEu.Mods.isCCTweakedLoaded()) {
               GTCEu.LOGGER.info("CC: Tweaked found. Enabling integration...");
               CCTweakedPlugin.init();
            }
         }
      );
   }

   @SubscribeEvent
   public static void loadComplete(FMLLoadCompleteEvent event) {
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerItem(FluidHandler.ITEM, BottleItemFluidHandler::new, new ItemLike[]{Items.GLASS_BOTTLE});
      Stream<MachineDefinition> quantumTanks = Stream.of(GTMachines.SUPER_TANK, GTMachines.QUANTUM_TANK).flatMap(Arrays::stream);
      quantumTanks = Stream.concat(quantumTanks, Stream.of(GTMachines.CREATIVE_FLUID));
      event.registerItem(FluidHandler.ITEM, (stack, ctx) -> {
         if (stack.getItem() instanceof MetaMachineItem machineItem) {
            long capacity = QuantumTankMachine.TANK_CAPACITY.getLong(machineItem.getDefinition());
            return capacity == -1L ? null : new QuantumFluidHandlerItemStack(stack, capacity);
         } else {
            return null;
         }
      }, (ItemLike[])quantumTanks.filter(Objects::nonNull).map(MachineDefinition::getItem).toArray(Item[]::new));

      for (Block block : BuiltInRegistries.BLOCK) {
         if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE && event.isBlockRegistered(EnergyStorage.BLOCK, block)) {
            event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
               IEnergyStorage forgeEnergy = (IEnergyStorage)level.getCapability(EnergyStorage.BLOCK, pos, state, blockEntity, side);
               return forgeEnergy != null ? new EUToFEProvider(forgeEnergy) : null;
            }, new Block[]{block});
         }

         if (block instanceof FluidPipeBlock fluidPipe) {
            fluidPipe.attachCapabilities(event);
         } else if (block instanceof CableBlock cable) {
            cable.attachCapabilities(event);
         } else if (block instanceof ItemPipeBlock itemPipe) {
            itemPipe.attachCapabilities(event);
         } else if (block instanceof LaserPipeBlock laserPipe) {
            laserPipe.attachCapabilities(event);
         } else if (block instanceof DuctPipeBlock duct) {
            duct.attachCapabilities(event);
         } else if (block instanceof MetaMachineBlock machine) {
            machine.attachCapabilities(event);
         } else if (block instanceof OpticalPipeBlock optical) {
            optical.attachCapabilities(event);
         }
      }

      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof IComponentItem componentItem) {
            componentItem.attachCapabilities(event);
         } else if (item instanceof IGTTool tool) {
            tool.attachCapabilities(event);
         } else if (item instanceof DrumMachineItem drum) {
            drum.attachCapabilities(event);
         } else if (item instanceof GTBucketItem) {
            event.registerItem(FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), new ItemLike[]{item});
         } else if (item instanceof PotionItem) {
            event.registerItem(FluidHandler.ITEM, PotionItemFluidHandler::new, new ItemLike[]{item});
         }
      }
   }

   @SubscribeEvent
   public static void registerPackFinders(AddPackFindersEvent event) {
      if (event.getPackType() == PackType.CLIENT_RESOURCES) {
         GTDynamicResourcePack.clearClient();
         EmiGregStarVisibilityCompat.addDynamicResources();
         event.addRepositorySource(new GTPackSource("gtceu:dynamic_assets", event.getPackType(), Position.BOTTOM, GTDynamicResourcePack::new));
      } else if (event.getPackType() == PackType.SERVER_DATA) {
         GTDynamicDataPack.clearServer();
         event.addRepositorySource(new GTPackSource("gtceu:dynamic_data", event.getPackType(), Position.BOTTOM, GTDynamicDataPack::new));
      }
   }

   @SubscribeEvent
   public static void addValidBlocksToBETypes(BlockEntityTypeAddBlocksEvent event) {
      event.modify(
         BlockEntityType.SIGN,
         new Block[]{
            (Block)GTBlocks.RUBBER_SIGN.get(),
            (Block)GTBlocks.RUBBER_WALL_SIGN.get(),
            (Block)GTBlocks.TREATED_WOOD_SIGN.get(),
            (Block)GTBlocks.TREATED_WOOD_WALL_SIGN.get()
         }
      );
      event.modify(
         BlockEntityType.HANGING_SIGN,
         new Block[]{
            (Block)GTBlocks.RUBBER_HANGING_SIGN.get(),
            (Block)GTBlocks.RUBBER_WALL_HANGING_SIGN.get(),
            (Block)GTBlocks.TREATED_WOOD_HANGING_SIGN.get(),
            (Block)GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN.get()
         }
      );
   }

   public static final class KJSEventWrapper {
      public static void materialModification() {
         GTCEuStartupEvents.MATERIAL_MODIFICATION.post(new MaterialModificationEventJS());
      }
   }
}
