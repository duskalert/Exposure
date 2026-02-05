package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record ExportS2CP(List<String> ids, ExportSize size, ExportLook look) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("export");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeCollection(ids,FriendlyByteBuf::writeUtf);
        buf.writeEnum(size);
        buf.writeEnum(look);
    }

    public static ExportS2CP fromPacket(FriendlyByteBuf buf){
        return new ExportS2CP(buf.readList(FriendlyByteBuf::readUtf),buf.readEnum(ExportSize.class),buf.readEnum(ExportLook.class));
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.exportExposures(this);
        return true;
    }
}
