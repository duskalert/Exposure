package io.github.mortuusars.exposure.client.capture.saving;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureDataC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureDataChunkBytesC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureDataChunkHeaderC2SP;
import io.github.mortuusars.exposure.util.ByteArrayUtils;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class ExposureUploader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SINGLE_PACKET_DATA_LIMIT = 30_000;

    public static void upload(String id, ExposureData exposure) {
        Preconditions.checkArgument(!id.isBlank(), "Cannot upload exposure with null or empty id.");

        LOGGER.info("Sending exposure '{}' to server...", id);

        if (!Minecrft.get().isSingleplayer() && exposure.getPixels().length > SINGLE_PACKET_DATA_LIMIT) {
            uploadSplitted(id, exposure);
        } else {
            Packets.sendToServer(new ExposureDataC2SP(id, exposure));
        }
    }

    public static Consumer<ExposureData> upload(String id) {
        return exposure -> upload(id, exposure);
    }

    // --

    private static void uploadSplitted(String id, ExposureData exposure) {
        Packets.sendToServer(new ExposureDataChunkHeaderC2SP(id, exposure.getWidth(), exposure.getHeight(),
              exposure.getPaletteId(), exposure.getTag()));

        int offset = 0;

        for (byte[] chunk : ByteArrayUtils.splitToChunks(exposure.getPixels(), SINGLE_PACKET_DATA_LIMIT)) {
            Packets.sendToServer(new ExposureDataChunkBytesC2SP(id, offset, chunk));
            offset += chunk.length;
        }
    }
}
