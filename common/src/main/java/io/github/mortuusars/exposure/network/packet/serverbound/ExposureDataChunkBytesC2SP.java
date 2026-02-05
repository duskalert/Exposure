package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ExposureDataChunkBytesC2SP(String id, int offset, byte[] bytes) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_chunk_bytes");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeInt(offset);
        buffer.writeByteArray(bytes);
    }

    public static ExposureDataChunkBytesC2SP fromPacket(FriendlyByteBuf buffer) {
        return new ExposureDataChunkBytesC2SP(buffer.readUtf(), buffer.readInt(), buffer.readByteArray());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            Exposure.LOGGER.error("Cannot receive '{}' packet. Player is not ServerPlayer.", ID);
            return false;
        }

        ExposureServer.exposureRepository().receiveChunkedClientUploadChunk(serverPlayer, id, offset, bytes);
        return true;
    }
}
