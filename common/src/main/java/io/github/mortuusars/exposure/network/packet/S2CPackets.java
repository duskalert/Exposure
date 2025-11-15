package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.clientbound.*;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class S2CPackets {
    public static <T extends  Packet> Map<Class<T>, Function<FriendlyByteBuf,T>> getDefinitions() {
        Map<Class<T>, Function<FriendlyByteBuf, T>> map = new HashMap<>();
        map.put((Class<T>)ActiveCameraRemoveS2CP.class, buf -> (T)ActiveCameraRemoveS2CP.fromPacket(buf));
                map.put((Class<T>)ActiveCameraInHandSetS2CP.class, buf -> (T)ActiveCameraInHandSetS2CP.fromPacket(buf));
                map.put((Class<T>)ActiveCameraOnStandSetS2CP.class, buf -> (T)ActiveCameraOnStandSetS2CP.fromPacket(buf));
                map.put((Class<T>)CameraStandSetRotationsS2CP.class,buf -> (T)CameraStandSetRotationsS2CP.fromPacket(buf));
                map.put((Class<T>)CameraStandStopControllingS2CP.class, buf -> (T)CameraStandStopControllingS2CP.fromPacket(buf));
                map.put((Class<T>)ShaderApplyS2CP.class, buf -> (T)ShaderApplyS2CP.fromPacket(buf));
                map.put((Class<T>) ActionS2CP.class, buf -> (T) ActionS2CP.fromPacket(buf));
                map.put((Class<T>)CreateChromaticExposureS2CP.class,buf ->  (T)CreateChromaticExposureS2CP.fromPacket(buf));
                map.put((Class<T>)ExposureDataChangedS2CP.class, buf -> (T)ExposureDataChangedS2CP.fromPacket(buf));
                map.put((Class<T>)UniqueSoundPlayS2CP.class,buf -> (T)UniqueSoundPlayS2CP.fromPacket(buf));
                map.put((Class<T>)UniqueSoundPlayShutterTickingS2CP.class, buf -> (T)UniqueSoundPlayShutterTickingS2CP.fromPacket(buf));
                map.put((Class<T>)UniqueSoundStopS2CP.class,buf -> (T)UniqueSoundStopS2CP.fromPacket(buf));
                map.put((Class<T>)ShowExposureCommandS2CP.class, buf -> (T)ShowExposureCommandS2CP.fromPacket(buf));
                map.put((Class<T>)ExposureDataResponseS2CP.class, buf -> (T)ExposureDataResponseS2CP.fromPacket(buf));
                map.put((Class<T>)CaptureStartS2CP.class, buf -> (T)CaptureStartS2CP.fromPacket(buf));
                map.put((Class<T>)CaptureStartDebugRGBS2CP.class,buf -> (T)CaptureStartDebugRGBS2CP.fromPacket(buf));
                map.put((Class<T>)ExportS2CP.class, buf -> (T)ExportS2CP.STREAM_CODEC);
                map.put((Class<T>)ExportStopS2CP.class, buf -> (T)ExportStopS2CP.STREAM_CODEC);
        return map;
    }
}
