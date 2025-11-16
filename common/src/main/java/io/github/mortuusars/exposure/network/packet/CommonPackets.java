package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommonPackets {
    public static <T extends  Packet> Map<Class<T>, Function<FriendlyByteBuf,T>> getDefinitions() {
        Map<Class<T>, Function<FriendlyByteBuf, T>> map = new HashMap<>();
        map.put((Class<T>) ActiveCameraDeactivateCommonPacket.class, buf -> (T)ActiveCameraDeactivateCommonPacket.INSTANCE);
        return map;
    }
}
