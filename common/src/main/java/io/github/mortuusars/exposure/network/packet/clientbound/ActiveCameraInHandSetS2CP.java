package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record ActiveCameraInHandSetS2CP(int operatorEntityId, CameraId cameraId, InteractionHand hand) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_in_hand_set");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(operatorEntityId);
        cameraId.toPacket(buf);
        buf.writeEnum(hand);
    }

    public static ActiveCameraInHandSetS2CP fromPacket(FriendlyByteBuf buf) {
        return new ActiveCameraInHandSetS2CP(buf.readInt(), CameraId.fromPacket(buf), buf.readEnum(InteractionHand.class));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof LivingEntity entity
                && entity instanceof CameraOperator operator
                && entity instanceof CameraHolder holder) {
            operator.setActiveExposureCamera(new CameraInHand(holder, cameraId, hand));
        }
        return true;
    }
}
