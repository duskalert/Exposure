package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ExposureDataChunkResponseHeaderS2CP(String id, int width, int height,
                                                  ResourceLocation palette, ExposureData.Tag tag) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("exposure_data_response_header");

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

    public static ExposureDataChunkResponseHeaderS2CP fromPacket(FriendlyByteBuf buffer) {
        return new ExposureDataChunkResponseHeaderS2CP(buffer.readUtf(),
              buffer.readInt(),
              buffer.readInt(),
              buffer.readResourceLocation(),
              ExposureData.Tag.fromPacket(buffer));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ExposureClient.exposureStore().receiveChunkedResponseHeader(id, width, height, palette, tag);
        return true;
    }
}
