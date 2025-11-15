package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public record Projection(String path, DitherMode mode) {
    public static final Codec<Projection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("path").forGetter(Projection::path),
            DitherMode.CODEC.optionalFieldOf("mode", DitherMode.DITHERED).forGetter(Projection::mode)
    ).apply(instance, Projection::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(path);
        buf.writeEnum(mode);
    }

    public static Projection fromPacket(FriendlyByteBuf buf) {
        return new Projection(buf.readUtf(),buf.readEnum(DitherMode.class));
    }
}
