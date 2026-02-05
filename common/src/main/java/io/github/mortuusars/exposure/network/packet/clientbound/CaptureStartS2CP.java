package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record CaptureStartS2CP(ResourceLocation templateId, CaptureParameters captureParameters) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("capture_start");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(templateId);
        captureParameters.toPacket(buf);
    }

    public static CaptureStartS2CP fromPacket(FriendlyByteBuf buf) {
        return new CaptureStartS2CP(buf.readResourceLocation(),CaptureParameters.fromPacket(buf));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startCapture(this);
        return true;
    }
}
