package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.Camera;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public enum ActiveCameraReleaseC2SP implements Packet {
    INSTANCE;

    public static final ResourceLocation ID = Exposure.resource("active_camera_release");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {

    }

    public static ActiveCameraReleaseC2SP fromPacket(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.getActiveExposureCameraOptional().ifPresentOrElse(
                Camera::release,
                () -> Exposure.LOGGER.error("Cannot release shutter: '{}' does not have an active camera.", player));

        return true;
    }
}
