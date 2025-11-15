package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ExposureDataChangedS2CP(String id) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_changed");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
    }

    public static ExposureDataChangedS2CP fromPacket(FriendlyByteBuf buf) {
        return new ExposureDataChangedS2CP(buf.readUtf());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.exposureDataChanged(this);
        return true;
    }
}
