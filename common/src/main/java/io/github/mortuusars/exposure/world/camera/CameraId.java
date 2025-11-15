package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record CameraId(UUID uuid) {
    public static final Codec<CameraId> CODEC = UUIDUtil.CODEC.xmap(CameraId::new, CameraId::uuid);

    public static CameraId create() {
        return new CameraId(UUID.randomUUID());
    }

    public static final CameraId NIL = new CameraId(Util.NIL_UUID);

    public static CameraId ofStack(ItemStack stack) {
        CameraId cameraId = Exposure.DataComponents.getCameraId(stack);
        return cameraId == null ? NIL : cameraId;
    }

    public boolean matches(ItemStack stack) {
        return equals(Exposure.DataComponents.getCameraId(stack));
    }

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    public static CameraId fromPacket(FriendlyByteBuf buf) {
        return new CameraId(buf.readUUID());
    }

    @Override
    public String toString() {
        return uuid().toString();
    }
}
