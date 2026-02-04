package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public enum ActionS2CP implements Packet {
    CLEAR_RENDERING_CACHE,SHUTTER_OPENED,EXPORT_STOPPED;

    public static final ResourceLocation ID = Exposure.resource("clear_rendering_cache");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeEnum(this);
    }

    public static ActionS2CP fromPacket(FriendlyByteBuf buf) {
        return buf.readEnum(ActionS2CP.class);
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        switch (this){
            case CLEAR_RENDERING_CACHE -> ClientPacketsHandler.clearRenderingCache();
            case SHUTTER_OPENED -> ClientPacketsHandler.shutterOpened();
            case EXPORT_STOPPED -> ClientPacketsHandler.stopExportTask();
        }
        return true;
    }
}