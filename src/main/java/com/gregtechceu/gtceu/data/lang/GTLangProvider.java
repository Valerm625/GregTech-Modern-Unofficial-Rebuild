package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;

/**
 * Language provider that applies GT's hand-written entries and overrides after Registrate has
 * generated the per-entry keys.
 * <p>
 * Registrate runs {@code addDataGenerator(ProviderType.LANG, ...)} callbacks <em>before</em> the
 * per-entry language data, and NeoForge's {@code LanguageProvider.add} throws on duplicates. Running
 * {@link LangHandler} from such a callback therefore pre-inserted keys such as
 * {@code block.gtceu.creosote} and made the fluid block's own language data fail with
 * {@code IllegalStateException: Duplicate translation key}, aborting {@code runData}.
 */
public class GTLangProvider extends RegistrateLangProvider {

    public GTLangProvider(ProviderType.Context<GTLangProvider> context) {
        super(context.parent(), context.output());
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();
        LangHandler.init(this);
    }
}
