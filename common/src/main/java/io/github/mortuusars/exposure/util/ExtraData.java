package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Extension of CompoundTag to allow better type safety. <br>
 * {@link NbtType} is meant to be stored in a static final field in appropriate places.
 */
public class ExtraData extends CompoundTag {
    public static final Codec<ExtraData> CODEC = CompoundTag.CODEC.xmap(ExtraData::new, Function.identity());

    public static final ExtraData EMPTY = new ExtraData(Collections.emptyMap());
    private final Map<String, Tag> tags;

    protected ExtraData(Map<String, Tag> tags) {
        super(tags);
        this.tags = tags;
    }

    public ExtraData() {
        this(new HashMap<>());
    }

    public ExtraData(CompoundTag tag) {
        this(new HashMap<>());
        merge(tag);
    }

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeNbt(this);
    }

    public static ExtraData fromPacket(FriendlyByteBuf buf) {
        return new ExtraData(buf.readNbt());
    }

    public <T> Optional<T> get(@NotNull NbtType<T> type) {
        if (!contains(type.key())) return Optional.empty();
        try {
            return Optional.ofNullable(type.getter().apply(this, type.key()));
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public <T> T getOrDefault(@NotNull NbtType<T> type, T defaultValue) {
        if (!contains(type.key())) return defaultValue;
        try {
            @Nullable T value = type.getter().apply(this, type.key());
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return defaultValue;
        }
    }

    public <T> void put(NbtType<T> type, @NotNull T value) {
        Preconditions.checkNotNull(value, "value");
        type.setter().accept(this, type.key(), value);
    }

    public <T> void remove(NbtType<T> type) {
        remove(type.key());
    }

    // --

    @Override
    public @NotNull ExtraData copy() {
        Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new ExtraData(map);
    }

    @Override
    public @NotNull ExtraData merge(CompoundTag other) {
        for (String key : other.getAllKeys()) {
            Tag tag = other.get(key);
            assert tag != null;
            if (tag.getId() == 10) {
                if (this.contains(key, 10)) {
                    CompoundTag compoundTag = this.getCompound(key);
                    compoundTag.merge((CompoundTag) tag);
                } else {
                    this.put(key, tag.copy());
                }
            } else {
                this.put(key, tag.copy());
            }
        }

        return this;
    }

    // --

}
