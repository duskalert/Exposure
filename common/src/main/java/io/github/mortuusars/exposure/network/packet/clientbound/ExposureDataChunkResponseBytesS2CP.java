package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ExposureDataChunkResponseBytesS2CP(String id, int offset, byte[] bytes) implements Packet {
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

    public static ExposureDataChunkResponseBytesS2CP fromPacket(FriendlyByteBuf buffer) {
        return new ExposureDataChunkResponseBytesS2CP(buffer.readUtf(), buffer.readInt(), buffer.readByteArray());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureStore().receiveChunkedResponseChunk(id, offset, bytes);
        return true;
    }
}
