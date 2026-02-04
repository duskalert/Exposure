package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.C2SPackets;
import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.function.Function;

public class FabricC2SPackets {
    @SuppressWarnings("unchecked")
    public static void register() {
        // This monstrosity is to avoid having to define packets for forge and fabric separately.
        for (var definition : C2SPackets.getDefinitions().entrySet()) {

            Function<FriendlyByteBuf, Packet> value = definition.getValue();
            ServerPlayNetworking.registerGlobalReceiver(classToRL(definition.getKey()), (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
                Packet packet = value.apply(friendlyByteBuf);
                packet.handle(PacketFlow.SERVERBOUND, serverPlayer);
            });

            /*PayloadTypeRegistry.playC2S().register(
                    (CustomPacketPayload.NbtType<CustomPacketPayload>) definition.type(),
                    (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) definition.codec().cast());
            ServerPlayNetworking.registerGlobalReceiver((CustomPacketPayload.NbtType<Packet>) definition.type(), FabricC2SPackets::handleServerboundPacket);*/
        }

        for (var definition : CommonPackets.getDefinitions().entrySet()) {
            Function<FriendlyByteBuf, Packet> value = definition.getValue();
            ServerPlayNetworking.registerGlobalReceiver(classToRL(definition.getKey()), (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
                Packet packet = value.apply(friendlyByteBuf);
                packet.handle(PacketFlow.SERVERBOUND, serverPlayer);
            });
        }
    }

    public static ResourceLocation classToRL(Class<?> clazz) {
        return Exposure.resource(clazz.getName().toLowerCase(Locale.ROOT));
    }

    public static void sendToServer(Packet packet) {
        ResourceLocation resourceLocation = classToRL(packet.getClass());
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toPacket(buf);
        ClientPlayNetworking.send(resourceLocation,buf);
    }
}
