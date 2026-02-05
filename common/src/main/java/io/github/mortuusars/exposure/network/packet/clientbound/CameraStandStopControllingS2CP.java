package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record CameraStandStopControllingS2CP(int standId) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("camera_stand_stop_controlling");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(standId);
    }

    public static CameraStandStopControllingS2CP fromPacket(FriendlyByteBuf buf) {
        return new CameraStandStopControllingS2CP(buf.readInt());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.stopControllingCameraStand(this);
        return true;
    }
}
