package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.util.ExtraData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * We cannot use {@link net.minecraft.advancements.criterion.NbtPredicate} as it checks against CompoundTag.class.
 * This is basically a copy of its functionality to make ExtraData work.
 */
public record ExtraDataPredicate(ExtraData data) {
    public static final Codec<ExtraDataPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(
            tag -> new ExtraDataPredicate(new ExtraData(tag)),
            predicate -> predicate.data.toTag());
    public static final StreamCodec<ByteBuf, ExtraDataPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
            tag -> new ExtraDataPredicate(new ExtraData(tag)),
            predicate -> predicate.data.toTag());

    public boolean matches(@Nullable Tag tag) {
        return tag != null && compareNbt(tag);
    }

    private boolean compareNbt(@Nullable Tag other) {
        if (data == null || other == null) {
            return data == other;
        }

        CompoundTag thisTag = data.toTag();
        if (other instanceof CompoundTag otherTag) {
            if (otherTag.size() < thisTag.size()) {
                return false;
            }
            for (String key : thisTag.keySet()) {
                Tag tag2 = thisTag.get(key);
                if (!NbtUtils.compareNbt(tag2, otherTag.get(key), true)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
