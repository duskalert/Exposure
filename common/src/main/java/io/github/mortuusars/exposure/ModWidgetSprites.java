package io.github.mortuusars.exposure;

import net.minecraft.resources.ResourceLocation;

public record ModWidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused
,int width,int height) {
    ModWidgetSprites(ResourceLocation p_295225_, ResourceLocation p_294772_,int width,int height) {
        this(p_295225_, p_295225_, p_294772_, p_294772_,width,height);
    }

    ModWidgetSprites(ResourceLocation p_296152_, ResourceLocation p_296020_, ResourceLocation p_296073_, int width, int height) {
        this(p_296152_, p_296020_, p_296073_, p_296020_,width,height);
    }

    public static ModWidgetSprites withPrefix(ResourceLocation p_296152_, ResourceLocation p_296020_, ResourceLocation p_296073_,int width,int height) {
        return new ModWidgetSprites(p_296152_.withPrefix("textures/gui/sprites/").withSuffix(".png"),
                p_296020_.withPrefix("textures/gui/sprites/").withSuffix(".png"),
                p_296073_.withPrefix("textures/gui/sprites/").withSuffix(".png"),width,height);
    }

    public static ModWidgetSprites withPrefix(ResourceLocation p_296152_, ResourceLocation p_296020_,int width,int height) {
        return new ModWidgetSprites(p_296152_.withPrefix("textures/gui/sprites/").withSuffix(".png"),
                p_296020_.withPrefix("textures/gui/sprites/").withSuffix(".png"),width,height);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
