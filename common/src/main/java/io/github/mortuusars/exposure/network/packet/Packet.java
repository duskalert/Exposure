package io.github.mortuusars.exposure.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;

public interface Packet {
    boolean handle(PacketFlow flow, Player player);
    void toPacket(FriendlyByteBuf buf);
}