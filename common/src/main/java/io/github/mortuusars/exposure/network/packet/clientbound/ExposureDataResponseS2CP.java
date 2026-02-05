package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.level.storage.RequestedPalettedExposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ExposureDataResponseS2CP(String id, RequestedPalettedExposure result) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_response");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        result.toPacket(buf);
    }

    public static ExposureDataResponseS2CP fromPacket(FriendlyByteBuf buf) {
        return new ExposureDataResponseS2CP(buf.readUtf(),RequestedPalettedExposure.fromPacket(buf));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureStore().receive(id, result);
        return true;
    }
}
