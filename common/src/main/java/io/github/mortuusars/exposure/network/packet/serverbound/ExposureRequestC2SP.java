package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ExposureRequestC2SP(String id) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_request");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
    }

    public static ExposureRequestC2SP fromPacket(FriendlyByteBuf buf) {
        return new ExposureRequestC2SP(buf.readUtf());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureServer.exposureRepository().handleClientRequest((ServerPlayer) player, id);
        return true;
    }
}
