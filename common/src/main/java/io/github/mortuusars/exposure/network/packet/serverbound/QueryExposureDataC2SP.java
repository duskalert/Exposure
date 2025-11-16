package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record QueryExposureDataC2SP(ExposureIdentifier identifier) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("query_exposure_data");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        identifier.toPacket(buf);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkArgument(player instanceof ServerPlayer, "Cannot handle packet: Player was is not available.");
       // PalettedExposure palettedExposure = ExposureServer.getExposure(identifier);
//        ExposureServer.exposureSender().sendTo(identifier, palettedExposure, ((ServerPlayer) player));
        return true;
    }
}
