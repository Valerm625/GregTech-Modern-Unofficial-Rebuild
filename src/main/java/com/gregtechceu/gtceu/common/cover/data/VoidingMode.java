package com.gregtechceu.gtceu.common.cover.data;

public enum VoidingMode {
    VOID_ANY("cover.voiding.voiding_mode.void_any", "void_any", 1), VOID_OVERFLOW("cover.voiding.voiding_mode.void_overflow", "void_overflow", 1024);
    public final String tooltip;
    public final int maxStackSize;

    VoidingMode(String tooltip, String textureName, int maxStackSize) {
        this.tooltip = tooltip;
        this.maxStackSize = maxStackSize;
    }

    public String getTooltip() {
        return this.tooltip;
    }
}
