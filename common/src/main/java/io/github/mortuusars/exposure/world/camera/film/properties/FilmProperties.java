package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FilmProperties(Optional<Integer> size,
                             Float sensitivity,
                             Float contrast,
                             Levels levels,
                             HSB hsb,
                             ColorBalance colorBalance,
                             Float saturation,
                             Float noise,
                             Optional<Holder<ColorPalette>> colorPalette) {

    public FilmProperties {
        size.ifPresent(value -> Preconditions.checkArgument(value > 0,
                "size must be >0. Got: " + value));
        Preconditions.checkArgument(sensitivity >= -10f && sensitivity <= 10f,
                "sensitivity must be >=-10 and <= 10. Got: " + sensitivity);
        Preconditions.checkArgument(contrast >= -1f && contrast <= 1f,
                "contrast must be >=-1 and <= 1. Got: " + contrast);
        Preconditions.checkArgument(saturation >= -1f && saturation <= 1f,
                "saturation must be >=-1 and <= 1. Got: " + saturation);
        Preconditions.checkArgument(noise >= 0f && noise <= 1f,
                "noise must be >=0 and <=1. Got: " + noise);
    }

    public static final Codec<FilmProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.intRange(1, 2048).optionalFieldOf("frame_size").forGetter(FilmProperties::size),
            Codecs.floatRange(-10f, 10f).optionalFieldOf("sensitivity", 0f).forGetter(FilmProperties::sensitivity),
            Codecs.floatRange(-1f, 1f).optionalFieldOf("contrast", 0f).forGetter(FilmProperties::contrast),
            Levels.CODEC.optionalFieldOf("levels", Levels.EMPTY).forGetter(FilmProperties::levels),
            HSB.CODEC.optionalFieldOf("hsb", HSB.EMPTY).forGetter(FilmProperties::hsb),
            ColorBalance.CODEC.optionalFieldOf("color_balance", ColorBalance.EMPTY).forGetter(FilmProperties::colorBalance),
            Codecs.floatRange(-1f, 1f).optionalFieldOf("saturation", 0f).forGetter(FilmProperties::saturation),
            Codecs.floatRange(0f, 1f).optionalFieldOf("noise", 0f).forGetter(FilmProperties::noise),
            ColorPalette.HOLDER_CODEC.optionalFieldOf("color_palette").forGetter(FilmProperties::colorPalette)
    ).apply(instance, FilmProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilmProperties> STREAM_CODEC = new StreamCodec<>() {
        public @NotNull FilmProperties decode(RegistryFriendlyByteBuf buffer) {
            return new FilmProperties(
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    Levels.STREAM_CODEC.decode(buffer),
                    HSB.STREAM_CODEC.decode(buffer),
                    ColorBalance.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.FLOAT.decode(buffer),
                    ByteBufCodecs.optional(ColorPalette.STREAM_CODEC).decode(buffer));
        }

        public void encode(RegistryFriendlyByteBuf buffer, FilmProperties data) {
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.size);
            ByteBufCodecs.FLOAT.encode(buffer, data.sensitivity);
            ByteBufCodecs.FLOAT.encode(buffer, data.contrast);
            Levels.STREAM_CODEC.encode(buffer, data.levels);
            HSB.STREAM_CODEC.encode(buffer, data.hsb);
            ColorBalance.STREAM_CODEC.encode(buffer, data.colorBalance);
            ByteBufCodecs.FLOAT.encode(buffer, data.saturation);
            ByteBufCodecs.FLOAT.encode(buffer, data.noise);
            ByteBufCodecs.optional(ColorPalette.STREAM_CODEC).encode(buffer, data.colorPalette);
        }
    };

    public static final FilmProperties EMPTY = new FilmProperties(
            Optional.empty(),
            0f,
            0f,
            Levels.EMPTY,
            HSB.EMPTY,
            ColorBalance.EMPTY,
            0f,
            0f,
            Optional.empty());

    public static FilmProperties create() {
        return EMPTY;
    }

    public FilmProperties withSize(@Nullable Integer size) {
        return new FilmProperties(Optional.ofNullable(size), sensitivity, contrast,
                levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withSensitivity(@Nullable Float sensitivity) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withContrast(@Nullable Float contrast) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withLevels(@Nullable Levels levels) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withHSB(@Nullable HSB hsb) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withColorBalance(@Nullable ColorBalance colorBalance) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withSaturation(@Nullable Float saturation) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withNoise(@Nullable Float noise) {
        return new FilmProperties(size, sensitivity, contrast, levels, hsb, colorBalance, saturation, noise, colorPalette);
    }

    public FilmProperties withColorPalette(@Nullable Holder<ColorPalette> colorPalette) {
        return new FilmProperties(size, sensitivity, contrast,
                levels, hsb, colorBalance, saturation, noise, Optional.ofNullable(colorPalette));
    }
}