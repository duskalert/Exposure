package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExtraData {
    public static final Codec<ExtraData> CODEC = CompoundTag.CODEC.xmap(ExtraData::new, ExtraData::toTag);
    public static final StreamCodec<ByteBuf, ExtraData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(ExtraData::new, ExtraData::toTag);

    public static final ExtraData EMPTY = new ExtraData(new CompoundTag());

    private final CompoundTag tag;

    protected ExtraData(Map<String, Tag> tags) {
        this.tag = new CompoundTag(tags);
    }

    public ExtraData() {
        this.tag = new CompoundTag();
    }

    public ExtraData(CompoundTag tag) {
        this.tag = tag.copy();
    }

    public CompoundTag toTag() {
        return tag.copy();
    }

    public <T> Optional<T> get(@NotNull ExtraData.Type<T> type) {
        if (!tag.contains(type.key())) return Optional.empty();
        try {
            return Optional.ofNullable(type.getter().apply(this, type.key()));
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public <T> T getOrDefault(@NotNull ExtraData.Type<T> type, T defaultValue) {
        if (!tag.contains(type.key())) return defaultValue;
        try {
            @Nullable T value = type.getter().apply(this, type.key());
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", e.getMessage());
            return defaultValue;
        }
    }

    public <T> void put(Type<T> type, @NotNull T value) {
        Preconditions.checkNotNull(value, "value");
        type.setter().accept(this, type.key(), value);
    }

    public <T> void remove(Type<T> type) {
        tag.remove(type.key());
    }

    // Delegate methods for CompoundTag operations used by Types
    public String getString(String key) { return tag.getStringOr(key, ""); }
    public void putString(String key, String value) { tag.putString(key, value); }
    public boolean getBoolean(String key) { return tag.getBoolean(key).orElse(false); }
    public void putBoolean(String key, boolean value) { tag.putBoolean(key, value); }
    public int getInt(String key) { return tag.getIntOr(key, 0); }
    public void putInt(String key, int value) { tag.putInt(key, value); }
    public long getLong(String key) { return tag.getLongOr(key, 0L); }
    public void putLong(String key, long value) { tag.putLong(key, value); }
    public float getFloat(String key) { return tag.getFloatOr(key, 0.0f); }
    public void putFloat(String key, float value) { tag.putFloat(key, value); }
    public double getDouble(String key) { return tag.getDoubleOr(key, 0.0); }
    public void putDouble(String key, double value) { tag.putDouble(key, value); }
    public ListTag getList(String key, int type) { return tag.getListOrEmpty(key); }
    public void put(String key, Tag value) { tag.put(key, value); }
    public Set<String> keySet() { return tag.keySet(); }
    public Tag get(String key) { return tag.get(key); }
    public CompoundTag getCompound(String key) { return tag.getCompoundOrEmpty(key); }
    public boolean contains(String key) { return tag.contains(key); }
    public boolean contains(String key, int type) { return tag.contains(key); }

    @Override
    public @NotNull ExtraData clone() {
        return new ExtraData(tag.copy());
    }

    public @NotNull ExtraData copy() {
        return new ExtraData(tag.copy());
    }

    public @NotNull ExtraData merge(CompoundTag other) {
        for (String key : other.keySet()) {
            Tag value = other.get(key);
            if (value != null) {
                if (value.getId() == 10) {
                    if (tag.contains(key)) {
                        tag.getCompound(key).merge((CompoundTag) value);
                    } else {
                        tag.put(key, value.copy());
                    }
                } else {
                    tag.put(key, value.copy());
                }
            }
        }
        return this;
    }

    public record Type<T>(String key, BiFunction<ExtraData, String, @Nullable T> getter,
                          TriConsumer<ExtraData, String, T> setter) {
        public static Type<String> string(String key) {
            return new Type<>(key, ExtraData::getString, ExtraData::putString);
        }

        public static Type<Boolean> bool(String key) {
            return new Type<>(key, ExtraData::getBoolean, ExtraData::putBoolean);
        }

        public static Type<Integer> intVal(String key) {
            return new Type<>(key, ExtraData::getInt, ExtraData::putInt);
        }

        public static Type<Long> longVal(String key) {
            return new Type<>(key, ExtraData::getLong, ExtraData::putLong);
        }

        public static Type<Float> floatVal(String key) {
            return new Type<>(key, ExtraData::getFloat, ExtraData::putFloat);
        }

        public static Type<Double> doubleVal(String key) {
            return new Type<>(key, ExtraData::getDouble, ExtraData::putDouble);
        }

        public static <T extends StringRepresentable> Type<T> stringRepresentable(String key, Function<String, @Nullable T> deserializeFunction) {
            return new Type<>(key,
                    (data, k) -> deserializeFunction.apply(data.getStringOr(k, "")),
                    (data, k, value) -> data.putString(k, value.getSerializedName()));
        }

        public static Type<Vec3> vec3(String key) {
            return new Type<>(key,
                    (data, k) -> {
                        ListTag pos = data.getList(k, DoubleTag.TAG_DOUBLE);
                        return new Vec3(pos.getDoubleOr(0, 0.0), pos.getDoubleOr(1, 0.0), pos.getDoubleOr(2, 0.0));
                    },
                    (data, k, value) -> {
                        ListTag pos = new ListTag();
                        pos.add(DoubleTag.valueOf(value.x()));
                        pos.add(DoubleTag.valueOf(value.y()));
                        pos.add(DoubleTag.valueOf(value.z()));
                        data.put(k, pos);
                    });
        }

        public static Type<Identifier> Identifier(String key) {
            return new Type<>(key,
                    (data, k) -> Identifier.parse(data.getStringOr(k, "")),
                    (data, k, value) -> data.putString(k, value.toString()));
        }

        public static <T> Type<List<T>> list(String key, int tagType, Function<Tag, T> extractFunc, Function<T, Tag> packFunc) {
            return new Type<>(key,
                    (data, k) -> data.getListOrEmpty(k).stream()
                            .map(extractFunc)
                            .toList(),
                    (data, k, value) -> {
                        ListTag list = new ListTag();
                        list.addAll(value.stream()
                                .map(packFunc)
                                .toList());
                        data.put(k, list);
                    });
        }

        public static <T> Type<List<T>> stringBasedList(String key, Function<String, T> extractFunc, Function<T, String> packFunc) {
            return list(key, Tag.TAG_STRING, tag -> extractFunc.apply(tag.getAsString()), value -> StringTag.valueOf(packFunc.apply(value)));
        }
    }
}
