package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public enum ActiveCameraReleaseC2SP implements Packet {
    INSTANCE;
    public static final Type<ActiveCameraReleaseC2SP> TYPE = new Type<>(Exposure.resource("active_camera_release"));
    public static final StreamCodec<FriendlyByteBuf, ActiveCameraReleaseC2SP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

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
