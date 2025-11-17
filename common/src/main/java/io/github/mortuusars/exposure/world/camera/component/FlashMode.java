package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FlashMode implements StringRepresentable {
    OFF("off"),
    ON("on"),
    AUTO("auto");

    public static final Codec<FlashMode> CODEC = StringRepresentable.fromEnum(FlashMode::values);

    private final String name;

    FlashMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".flash_mode." + name);
    }
}
