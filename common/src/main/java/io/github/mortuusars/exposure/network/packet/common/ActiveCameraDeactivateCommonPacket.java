package io.github.mortuusars.exposure.network.packet.common;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public enum ActiveCameraDeactivateCommonPacket implements Packet {
    INSTANCE;

    public static final ResourceLocation ID = Exposure.resource("active_camera_deactivate");

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        player.getActiveExposureCameraOptional().ifPresent(camera -> {
            camera.map((item, stack) -> item.deactivate(camera.getHolder().asHolderEntity(), stack));
            player.removeActiveExposureCamera();
        });
        return true;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {

    }
}
