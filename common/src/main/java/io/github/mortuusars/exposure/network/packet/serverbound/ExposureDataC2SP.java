package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ExposureDataC2SP(String id, ExposureData exposure) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        exposure.toPacket(buf);
    }

    public static ExposureDataC2SP fromPacket(FriendlyByteBuf buf) {
        return new ExposureDataC2SP(buf.readUtf(), ExposureData.fromPacket(buf));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureServer.exposureRepository().receiveClientUpload(((ServerPlayer) player), id, exposure);
        return true;
    }
}