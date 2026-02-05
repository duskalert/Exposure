package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FilmProperties(ExposureType type,
                             Optional<Integer> size,
                             ResourceKey<ColorPalette> colorPalette,
                             DitherMode ditherMode,
                             FilmStyle style) {
    public FilmProperties {
        size.ifPresent(s -> Preconditions.checkArgument(s > 0 && s <= 2048,
                "size must be 1-2048: " + size));
    }

    public static final Codec<FilmProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(FilmProperties::type),
            ExtraCodecs.intRange(0, 2048).optionalFieldOf("frame_size").forGetter(FilmProperties::size),
            ResourceKey.codec(Exposure.Registries.COLOR_PALETTE).optionalFieldOf("color_palette", ColorPalettes.DEFAULT).forGetter(FilmProperties::colorPalette),
            DitherMode.CODEC.optionalFieldOf("dither_mode", DitherMode.DITHERED).forGetter(FilmProperties::ditherMode),
            FilmStyle.CODEC.optionalFieldOf("style", FilmStyle.EMPTY).forGetter(FilmProperties::style)
    ).apply(instance, FilmProperties::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeOptional(size,FriendlyByteBuf::writeInt);
        buf.writeResourceKey(colorPalette);
        buf.writeEnum(ditherMode);
        style.toPacket(buf);
    }

    public static FilmProperties fromPacket(FriendlyByteBuf buf){
        return new FilmProperties(buf.readEnum(ExposureType.class),buf.readOptional(FriendlyByteBuf::readInt),buf.readResourceKey(Exposure.Registries.COLOR_PALETTE),
                buf.readEnum(DitherMode.class),FilmStyle.fromPacket(buf)
        );
    }

    public static final FilmProperties EMPTY = new FilmProperties(
            ExposureType.COLOR,
            Optional.empty(),
            ColorPalettes.DEFAULT,
            DitherMode.DITHERED,
            FilmStyle.EMPTY);

    public FilmProperties withType(ExposureType type) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withSize(@Nullable Integer size) {
        return new FilmProperties(type, Optional.ofNullable(size), colorPalette, ditherMode, style);
    }

    public FilmProperties withColorPalette(@NotNull ResourceKey<ColorPalette> colorPalette) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withDitherMode(DitherMode ditherMode) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    public FilmProperties withStyle(@NotNull FilmStyle style) {
        return new FilmProperties(type, size, colorPalette, ditherMode, style);
    }

    // --

    public int getSize() {
        return size.orElse(Config.Server.DEFAULT_FRAME_SIZE.get());
    }

    public Holder<ColorPalette> getColorPalette(RegistryAccess access) {
        return ColorPalettes.get(access, colorPalette);
    }
}