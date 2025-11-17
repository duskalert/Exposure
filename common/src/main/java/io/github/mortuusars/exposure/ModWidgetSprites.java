package io.github.mortuusars.exposure;

import net.minecraft.resources.ResourceLocation;

public record ModWidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
    public ModWidgetSprites(ResourceLocation p_295225_, ResourceLocation p_294772_) {
        this(p_295225_, p_295225_, p_294772_, p_294772_);
    }

    public ModWidgetSprites(ResourceLocation p_296152_, ResourceLocation p_296020_, ResourceLocation p_296073_) {
        this(p_296152_, p_296020_, p_296073_, p_296020_);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
