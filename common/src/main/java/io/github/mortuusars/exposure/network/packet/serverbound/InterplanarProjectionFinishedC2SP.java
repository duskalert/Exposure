package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public record InterplanarProjectionFinishedC2SP(CameraId cameraId,
                                                boolean successful,
                                                Optional<TranslatableError> error) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("interplanar_projection_finished");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        cameraId.toPacket(buf);
        buf.writeBoolean(successful);
        buf.writeOptional(error,(buf1, translatableError) -> translatableError.toPacket(buf1));
    }

    public static InterplanarProjectionFinishedC2SP fromPacket(FriendlyByteBuf buf) {
        return new InterplanarProjectionFinishedC2SP(CameraId.fromPacket(buf),buf.readBoolean(),buf.readOptional(TranslatableError::fromPacket));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        CameraInstances.ifPresent(cameraId, cameraInstance -> cameraInstance.setProjectionResult(player.level(), successful, error));
        return true;
    }
}
