package io.github.mortuusars.exposure.world.item.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraSetSettingC2SP;
import io.github.mortuusars.exposure.util.NBTAbstraction;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CameraSetting<T>(NBTAbstraction.Named<T> function, T defaultValue, Optional<SoundEffect> sound) {
    public static final Codec<CameraSetting<?>> CODEC = ResourceLocation.CODEC.xmap(CameraSettings::byId, CameraSettings::idOf);

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(CameraSettings.idOf(this));
    }

    public static CameraSetting<?> fromPacket(FriendlyByteBuf buf) {
        return CameraSettings.byId(buf.readResourceLocation());
    }

    public CameraSetting(NBTAbstraction.Named<T> function, T defaultValue, SoundEffect sound) {
        this(function, defaultValue, Optional.ofNullable(sound));
    }

    public CameraSetting(NBTAbstraction.Named<T> function, T defaultValue) {
        this(function, defaultValue, Optional.empty());
    }

    // --

    public @Nullable T get(ItemStack stack) {
        return function.read(stack);
    }

    public Optional<T> getOptional(ItemStack stack) {
        return Optional.ofNullable(get(stack));
    }

    public T getOrDefault(ItemStack stack) {
        T t = get(stack);
        return t == null ? defaultValue : t;
    }

    public T getOrElse(ItemStack stack, T defaultValue) {
        T t = get(stack);
        return t == null ? defaultValue : t;
    }

    public @Nullable T get(Camera camera) {
        return function.read(camera.getItemStack());
    }

    public Optional<T> getOptional(Camera camera) {
        return Optional.ofNullable(get(camera));
    }

    public T getOrDefault(Camera camera) {
        T t = function.read(camera.getItemStack());
        return t == null ? defaultValue : t;
    }

    public T getOrElse(Camera camera, T defaultValue) {
        T t = function.read(camera.getItemStack());
        return t == null ? defaultValue : t;
    }

    // --

    public boolean set(ItemStack stack, T value) {
        if (stack.isEmpty() || getOrDefault(stack).equals(value)) return false;

        if (value instanceof Boolean bool && !bool) {
            stack.removeTagKey(function.key());
        } else {
            function.write(stack,value);
        }
        return true;
    }

    public boolean set(CameraHolder holder, ItemStack stack, T value) {
        if (stack.getItem() instanceof CameraItem cameraItem && set(stack, value)) {
            cameraItem.actionPerformed(stack, holder);
            sound.ifPresent(sound ->
                    Sound.playSided(holder.asHolderEntity(), sound.sound().get(), SoundSource.PLAYERS,
                            sound.volume(), sound.pitch(), sound.pitchVariability()));
            return true;
        }
        return false;
    }

    public boolean set(Camera camera, T value) {
        return camera.map((item, stack) -> set(camera.getHolder(), stack, value)).orElse(false);
    }

    public boolean setAndSync(Camera camera, T value) {
        return camera.map((item, stack) -> {
            if (set(camera.getHolder(), stack, value)) {
                byte[] bytes = encodeValue(value);
                Packets.sendToServer(new ActiveCameraSetSettingC2SP(this, bytes));
                return true;
            }
            return false;
        }).orElse(false);
    }

    public boolean decodeAndSet(ItemStack stack, byte[] bytes) {
        T value = decodeValue(bytes);
        return set(stack, value);
    }

    public boolean decodeAndSet(CameraHolder holder, ItemStack stack, byte[] bytes) {
        T value = decodeValue(bytes);
        return set(holder, stack, value);
    }

    // --

    public byte[] encodeValue(T value) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            function.abstraction().packetWriter().accept(buffer, value);
            return buffer.array().clone();
        } finally {
            buffer.release();
        }
    }

    public T decodeValue(byte[] bytes) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeBytes(bytes);
            return function.abstraction().packetReader().apply(buffer);
        } finally {
            buffer.release();
        }
    }
}
