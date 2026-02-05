package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record ShowExposureCommandS2CP(List<Frame> frames,
                                      boolean negative) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("show_exposure_command");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeCollection(frames,(buf1, frame) -> frame.toPacket(buf1));
        buf.writeBoolean(negative);
    }

    public static ShowExposureCommandS2CP fromPacket(FriendlyByteBuf buf) {
        return new ShowExposureCommandS2CP(buf.readList(Frame::fromPacket),buf.readBoolean());
    }

    public static ShowExposureCommandS2CP identifier(ExposureIdentifier identifier, boolean negative) {
        Frame frame = Frame.create().setIdentifier(identifier).toImmutable();
        return new ShowExposureCommandS2CP(List.of(frame), negative);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.showExposure(this);
        return true;
    }
}
