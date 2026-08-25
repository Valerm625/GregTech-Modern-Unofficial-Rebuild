package com.gregtechceu.gtceu.client.util;

import java.util.function.BooleanSupplier;

/** Guards optional ModularUI diagnostics from reading NeoForge config too early. */
public final class ModularUIConfigSafety {

    private ModularUIConfigSafety() {}

    public static boolean readBooleanOrFalse(BooleanSupplier value) {
        try {
            return value.getAsBoolean();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
