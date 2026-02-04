package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.serverbound.*;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class C2SPackets {
    public static <T extends Packet> Map<Class<T>, Function<FriendlyByteBuf, T>> getDefinitions() {
        Map<Class<T>, Function<FriendlyByteBuf, T>> map = new HashMap<>();
        map.put((Class<T>) AlbumSignC2SP.class, buf -> (T) AlbumSignC2SP.fromPacket(buf));
        map.put((Class<T>) AlbumSyncNoteC2SP.class, buf -> (T) AlbumSyncNoteC2SP.fromPacket(buf));
        map.put((Class<T>) ActiveCameraSetSettingC2SP.class, buf -> (T) ActiveCameraSetSettingC2SP.fromPacket(buf));
        map.put((Class<T>) OpenCameraAttachmentsInCreativePacketC2SP.class, buf -> (T) OpenCameraAttachmentsInCreativePacketC2SP.fromPacket(buf));
        map.put((Class<T>) ExposureRequestC2SP.class, buf -> (T) ExposureRequestC2SP.fromPacket(buf));
        map.put((Class<T>) ActiveCameraReleaseC2SP.class, buf -> (T) ActiveCameraReleaseC2SP.fromPacket(buf));
        map.put((Class<T>) InterplanarProjectionFinishedC2SP.class, buf -> (T) InterplanarProjectionFinishedC2SP.fromPacket(buf));
        map.put((Class<T>) ExposureDataC2SP.class, buf -> (T) ExposureDataC2SP.fromPacket(buf));
        map.put((Class<T>) CameraStandTurnC2SP.class, buf -> (T) CameraStandTurnC2SP.fromPacket(buf));
        return map;
    }
}
