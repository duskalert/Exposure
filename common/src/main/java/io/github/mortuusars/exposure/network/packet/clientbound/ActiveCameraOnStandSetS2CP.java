package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ActiveCameraOnStandSetS2CP(int operatorEntityId, int cameraStandId, CameraId cameraId) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_on_stand_set");

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(operatorEntityId);
        buf.writeInt(cameraStandId);
        cameraId.toPacket(buf);
    }

    public static ActiveCameraOnStandSetS2CP fromPacket(FriendlyByteBuf buf) {
        return new ActiveCameraOnStandSetS2CP(buf.readInt(),buf.readInt(),CameraId.fromPacket(buf));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof CameraOperator operator
                && player.level().getEntity(cameraStandId) instanceof CameraStandEntity cameraStand) {
            operator.setActiveExposureCamera(new CameraOnStand(operator, cameraStand, cameraId));
        }
        return true;
    }
}
