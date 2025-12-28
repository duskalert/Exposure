package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;

import java.util.function.Function;

public class FabricS2CPackets {
    public static void register() {
        for (var definition : S2CPackets.getDefinitions().entrySet()) {
            Function<FriendlyByteBuf, Packet> value = definition.getValue();
            ClientPlayNetworking.registerGlobalReceiver(FabricC2SPackets.classToRL(definition.getKey()),(minecraft, clientPacketListener, friendlyByteBuf, packetSender) -> {
                Packet packet = value.apply(friendlyByteBuf);
                packet.handle(PacketFlow.CLIENTBOUND, minecraft.player);
            });
        }

        for (var definition : CommonPackets.getDefinitions().entrySet()) {
            Function<FriendlyByteBuf, Packet> value = definition.getValue();
            ClientPlayNetworking.registerGlobalReceiver(FabricC2SPackets.classToRL(definition.getKey()),(minecraft, clientPacketListener, friendlyByteBuf, packetSender) -> {
                Packet packet = value.apply(friendlyByteBuf);
                packet.handle(PacketFlow.CLIENTBOUND, minecraft.player);
            });
        }
    }
}
