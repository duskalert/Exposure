package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record CameraStandSetRotationsS2CP(int entityId, float yRot, float xRot) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("camera_stand_set_rotations");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(yRot);
        buf.writeFloat(xRot);
    }

    public static CameraStandSetRotationsS2CP fromPacket(FriendlyByteBuf buf) {
        return new CameraStandSetRotationsS2CP(buf.readInt(),buf.readFloat(),buf.readFloat());
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (player.level().getEntity(entityId) instanceof CameraStandEntity stand) {
            stand.setYRot(yRot);
            stand.setXRot(xRot);
        }
        return true;
    }
}