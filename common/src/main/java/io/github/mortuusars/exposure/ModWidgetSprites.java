package io.github.mortuusars.exposure;

import net.minecraft.resources.ResourceLocation;

public record ModWidgetSprites(ResourceLocation enabled,
                               ResourceLocation disabled,
                               ResourceLocation enabledFocused,
                               ResourceLocation disabledFocused,
                               int width,
                               int height) {
    ModWidgetSprites(ResourceLocation enabled, ResourceLocation disabled, int width, int height) {
        this(enabled, enabled, disabled, disabled, width, height);
    }

    ModWidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused,
                     int width, int height) {
        this(enabled, disabled, enabledFocused, disabled, width, height);
    }

    public static ModWidgetSprites withPrefix(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused,
                                              int width, int height) {
        return new ModWidgetSprites(enabled.withPrefix("textures/gui/sprites/").withSuffix(".png"),
              disabled.withPrefix("textures/gui/sprites/").withSuffix(".png"),
              enabledFocused.withPrefix("textures/gui/sprites/").withSuffix(".png"), width, height);
    }

    public static ModWidgetSprites withPrefix(ResourceLocation enabled, ResourceLocation disabled, int width, int height) {
        return new ModWidgetSprites(enabled.withPrefix("textures/gui/sprites/").withSuffix(".png"),
              disabled.withPrefix("textures/gui/sprites/").withSuffix(".png"), width, height);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
