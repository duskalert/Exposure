package io.github.mortuusars.exposure.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record TranslatableError(String key, String code) {
    public static final TranslatableError GENERIC = new TranslatableError("error.exposure.generic", "ERR_GENERIC");
    public static final TranslatableError TIMED_OUT = new TranslatableError("error.exposure.timed_out", "ERR_TIMED_OUT");

    public static final Codec<TranslatableError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(TranslatableError::key),
            Codec.STRING.fieldOf("code").forGetter(TranslatableError::code)
    ).apply(instance, TranslatableError::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(key);
        buf.writeUtf(code);
    }

    public static TranslatableError fromPacket(FriendlyByteBuf buf) {
        return new TranslatableError(buf.readUtf(),buf.readUtf());
    }

    public MutableComponent technical() {
        return Component.translatable(key() + ".technical");
    }

    public MutableComponent casual() {
        return Component.translatable(key() + ".casual");
    }
}
