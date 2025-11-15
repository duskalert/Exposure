package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record CaptureStartDebugRGBS2CP(ResourceLocation templateId, List<CaptureParameters> captureProperties) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("capture_start_debug_rgb");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeResourceLocation(templateId);
        buf.writeCollection(captureProperties,(buf1, captureParameters) -> captureParameters.toPacket(buf1));
    }

    public static CaptureStartDebugRGBS2CP fromPacket(FriendlyByteBuf buf) {
        return new CaptureStartDebugRGBS2CP(buf.readResourceLocation(),buf.readList(CaptureParameters::fromPacket));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.startDebugRGBCapture(this);
        return true;
    }
}
