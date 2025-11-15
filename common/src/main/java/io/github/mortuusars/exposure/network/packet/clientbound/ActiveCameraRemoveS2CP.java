package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ActiveCameraRemoveS2CP(int operatorEntityId) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("active_camera_remove");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(operatorEntityId);
    }

    public static ActiveCameraRemoveS2CP fromPacket(FriendlyByteBuf buf) {
        return new ActiveCameraRemoveS2CP(buf.readInt());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player.level().getEntity(operatorEntityId) instanceof CameraOperator operator) {
            operator.removeActiveExposureCamera();
        }
        return true;
    }
}
