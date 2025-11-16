package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraSetting;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ActiveCameraSetSettingC2SP(CameraSetting<?> setting, byte[] encodedValue) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_set_setting");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        setting.toPacket(buf);
        buf.writeByteArray(encodedValue);
    }

    public static ActiveCameraSetSettingC2SP fromPacket(FriendlyByteBuf buf) {
        return new ActiveCameraSetSettingC2SP(CameraSetting.fromPacket(buf), buf.readByteArray());
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        @Nullable Camera camera = player.getActiveExposureCamera();
        if (camera == null || camera.isEmpty()) return false;

        setting.decodeAndSet(camera.getHolder(), camera.getItemStack(), encodedValue);
        if (camera instanceof CameraOnStand cameraOnStand) {
            cameraOnStand.getStand().forceUpdate();
        }
        return true;
    }
}