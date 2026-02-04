package io.github.mortuusars.exposure.network.fabric;

public class FabricS2CPacketHandler {
    /*public static void register() {
        for (var definition : S2CPackets.getDefinitions()) {
            ClientPlayNetworking.registerGlobalReceiver(
                    (CustomPacketPayload.NbtType<Packet>) definition.type(), FabricS2CPacketHandler::handleClientboundPacket);
        }

        for (var definition : CommonPackets.getDefinitions()) {
            ClientPlayNetworking.registerGlobalReceiver(
                    (CustomPacketPayload.NbtType<Packet>) definition.type(), FabricS2CPacketHandler::handleClientboundPacket);
        }

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

    private static <T extends Packet> void handleClientboundPacket(T payload, ClientPlayNetworking.Context context) {
        payload.handle(PacketFlow.CLIENTBOUND, context.player());
    }*/
}
