package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ExposureDataChunkHeaderC2SP(String id, int width, int height,
                                          ResourceLocation palette, ExposureData.Tag tag) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_chunk_header");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeResourceLocation(palette);
        tag.toPacket(buffer);
    }

    public static ExposureDataChunkHeaderC2SP fromPacket(FriendlyByteBuf buffer) {
        return new ExposureDataChunkHeaderC2SP(buffer.readUtf(),
              buffer.readInt(),
              buffer.readInt(),
              buffer.readResourceLocation(),
              ExposureData.Tag.fromPacket(buffer));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            Exposure.LOGGER.error("Cannot receive '{}' packet. Player is not ServerPlayer.", ID);
            return false;
        }

        ExposureServer.exposureRepository().receiveChunkedClientUploadHeader(serverPlayer, id, width, height, palette, tag);
        return true;
    }

}
