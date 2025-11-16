package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record OpenCameraAttachmentsInCreativePacketC2SP(int cameraSlotIndex) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("open_camera_attachments");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(cameraSlotIndex);
    }

    public static OpenCameraAttachmentsInCreativePacketC2SP fromPacket(FriendlyByteBuf buf) {
        return new OpenCameraAttachmentsInCreativePacketC2SP(buf.readInt());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.server.execute(() -> {
                ItemStack stack = player.getInventory().getItem(cameraSlotIndex);
                if (stack.getItem() instanceof CameraItem cameraItem)
                    cameraItem.openCameraAttachments(player, cameraSlotIndex, true);
            });
        }

        return true;
    }


}
