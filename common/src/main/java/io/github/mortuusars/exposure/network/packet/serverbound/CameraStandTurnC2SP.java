package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record CameraStandTurnC2SP(int entityId, double yRot, double xRot) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("camera_stand_turn");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(yRot);
        buf.writeDouble(xRot);
    }

    public static CameraStandTurnC2SP fromPacket(FriendlyByteBuf buf) {
        return new CameraStandTurnC2SP(buf.readInt(),buf.readDouble(),buf.readDouble());
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        if (!(player.level().getEntity(entityId) instanceof CameraStandEntity stand)) return false;
        if (player.equals(stand.operator()) || stand.getOwnerPlayer().map(pl -> pl.equals(player)).orElse(false)) {
            stand.turn(yRot, xRot);
        }
        return true;
    }
}