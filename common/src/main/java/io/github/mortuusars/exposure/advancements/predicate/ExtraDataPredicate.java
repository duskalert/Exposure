package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.ExtraData;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

/**
 * We cannot use {@link net.minecraft.advancements.critereon.NbtPredicate} as it checks against CompoundTag.class.
 * This is basically a copy of its functionality to make ExtraData work.
 */
public record ExtraDataPredicate(ExtraData data) {
    public static final Codec<ExtraDataPredicate> CODEC = ExtraData.CODEC.xmap(ExtraDataPredicate::new, ExtraDataPredicate::data);/*TagParser.LENIENT_CODEC.xmap(
            tag -> new ExtraDataPredicate(new ExtraData(tag)),
            predicate -> predicate.data);*/
    public void toPacket(FriendlyByteBuf buf) {
        data.toPacket(buf);
    }

    public static ExtraDataPredicate fromPacket(FriendlyByteBuf buf) {
        return new ExtraDataPredicate(ExtraData.fromPacket(buf));
    }

    public boolean matches(@Nullable Tag tag) {
        return tag != null && compareNbt(tag);
    }

    private boolean compareNbt(@Nullable Tag other) {
        if (data == other) {
            return true;
        } else if (data == null) {
            return true;
        } else if (other == null) {
            return false;
        } else if (data instanceof CompoundTag compoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)other;
            if (compoundTag2.size() < compoundTag.size()) {
                return false;
            } else {
                for (String string : compoundTag.getAllKeys()) {
                    Tag tag2 = compoundTag.get(string);
                    if (!NbtUtils.compareNbt(tag2, compoundTag2.get(string), true)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return data.equals(other);
        }
    }
}
