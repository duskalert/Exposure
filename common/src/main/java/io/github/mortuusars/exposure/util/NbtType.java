package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record NbtType<T>(String key, BiFunction<CompoundTag, String, @Nullable T> getter,
                         TriConsumer<CompoundTag, String, T> setter) {
    public static NbtType<String> string(String key) {
        return new NbtType<>(key, CompoundTag::getString, CompoundTag::putString);
    }

    public static NbtType<Boolean> bool(String key) {
        return new NbtType<>(key, CompoundTag::getBoolean, CompoundTag::putBoolean);
    }

    public static NbtType<Integer> intVal(String key) {
        return new NbtType<>(key, CompoundTag::getInt, CompoundTag::putInt);
    }

    public static NbtType<Long> longVal(String key) {
        return new NbtType<>(key, CompoundTag::getLong, CompoundTag::putLong);
    }

    public static NbtType<Float> floatVal(String key) {
        return new NbtType<>(key, CompoundTag::getFloat, CompoundTag::putFloat);
    }

    public static NbtType<Double> doubleVal(String key) {
        return new NbtType<>(key, CompoundTag::getDouble, CompoundTag::putDouble);
    }

    public static <T extends StringRepresentable> NbtType<T> stringRepresentable(String key, Function<String, @Nullable T> deserializeFunction) {
        return new NbtType<>(key,
                (data, k) -> deserializeFunction.apply(data.getString(k)),
                (data, k, value) -> data.putString(k, value.getSerializedName()));
    }

    public static NbtType<Vec3> vec3(String key) {
        return new NbtType<>(key,
                (data, k) -> {
                    ListTag pos = data.getList(k, DoubleTag.TAG_DOUBLE);
                    return new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
                },
                (data, k, value) -> {
                    ListTag pos = new ListTag();
                    pos.add(DoubleTag.valueOf(value.x()));
                    pos.add(DoubleTag.valueOf(value.y()));
                    pos.add(DoubleTag.valueOf(value.z()));
                    data.put(k, pos);
                });
    }

    public static NbtType<ResourceLocation> resourceLocation(String key) {
        return new NbtType<>(key,
                (data, k) -> new ResourceLocation(data.getString(k)),
                (data, k, value) -> data.putString(k, value.toString()));
    }

    public static <T> NbtType<List<T>> list(String key, int tagType, Function<Tag, T> extractFunc, Function<T, Tag> packFunc) {
        return new NbtType<>(key,
                (data, k) -> data.getList(k, tagType).stream()
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

    public static <T> NbtType<List<T>> stringBasedList(String key, Function<String, T> extractFunc, Function<T, String> packFunc) {
        return list(key, Tag.TAG_STRING, tag -> extractFunc.apply(tag.getAsString()), value -> StringTag.valueOf(packFunc.apply(value)));
    }


    public static <T> Optional<T> get(CompoundTag tag,@NotNull NbtType<T> type) {
        if (!tag.contains(type.key())) return Optional.empty();
        try {
            return Optional.ofNullable(type.getter().apply(tag, type.key()));
        } catch (Exception e) {
            Exposure.LOGGER.error("Cannot get CompoundTag entry: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static <T> void put(CompoundTag tag,NbtType<T> type, @NotNull T value) {
        Preconditions.checkNotNull(value, "value");
        type.setter().accept(tag, type.key(), value);
    }

}
