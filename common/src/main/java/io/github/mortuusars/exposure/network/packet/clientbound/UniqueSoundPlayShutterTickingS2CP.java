package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record UniqueSoundPlayShutterTickingS2CP(int entityId,
                                                CameraId cameraId,
                                                float volume,
                                                float pitch,
                                                int durationTicks) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_play_shutter_ticking");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        cameraId.toPacket(buf);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeInt(durationTicks);
    }

    public static UniqueSoundPlayShutterTickingS2CP fromPacket(FriendlyByteBuf buf) {
        return new UniqueSoundPlayShutterTickingS2CP(buf.readInt(),CameraId.fromPacket(buf),buf.readFloat(),buf.readFloat(),buf.readInt());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.playShutterTickingSound(this);
        return true;
    }
}
