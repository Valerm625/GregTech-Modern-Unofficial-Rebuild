package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.core.mixins.registrate.RegistrateDataProviderAccessor;
import com.gregtechceu.gtceu.data.datamap.DataMapsHandler;
import com.gregtechceu.gtceu.data.lang.GTLangProvider;
import com.gregtechceu.gtceu.data.model.BlockstateModelLoader;
import com.gregtechceu.gtceu.data.tags.BlockTagLoader;
import com.gregtechceu.gtceu.data.tags.EntityTypeTagLoader;
import com.gregtechceu.gtceu.data.tags.FluidTagLoader;
import com.gregtechceu.gtceu.data.tags.ItemTagLoader;

import net.minecraft.data.DataProvider;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    // we only register this so the class gets loaded. the key gets overwritten in #initPre.
    private static final ProviderType<GTBlockstateProvider> BLOCKSTATE_PROVIDER = ProviderType.registerProvider(
            "ex_blockstate",
            GTBlockstateProvider::new);
    // same trick for the language provider: GTLangProvider applies LangHandler after the
    // per-entry language data instead of before it, which is what keeps runData from crashing
    // with "Duplicate translation key block.gtceu.creosote".
    private static final ProviderType<GTLangProvider> LANG_PROVIDER = ProviderType.registerProvider(
            "ex_lang",
            GTLangProvider::new);

    public static void initPre() {
        DataProvider.INDENT_WIDTH.set(4);
        // replace some default providers with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("blockstate", BLOCKSTATE_PROVIDER);
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("lang", LANG_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE,
                p -> BlockstateModelLoader.init((GTBlockstateProvider) p));
    }

    public static void initPost() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, DataMapsHandler::init);
    }
}
