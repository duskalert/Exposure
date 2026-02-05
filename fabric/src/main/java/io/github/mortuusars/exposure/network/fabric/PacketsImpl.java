package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.fabric.ExposureFabric;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import io.github.mortuusars.exposure.network.packet.serverbound.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class PacketsImpl {
    @Nullable
    private static MinecraftServer server;

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ActiveCameraDeactivateCommonPacket.ID, new ServerHandler(ActiveCameraDeactivateCommonPacket::fromBuffer));

        ServerPlayNetworking.registerGlobalReceiver(AlbumSignC2SP.ID, new ServerHandler(AlbumSignC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(AlbumSyncNoteC2SP.ID, new ServerHandler(AlbumSyncNoteC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ActiveCameraSetSettingC2SP.ID, new ServerHandler(ActiveCameraSetSettingC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(OpenCameraAttachmentsInCreativePacketC2SP.ID, new ServerHandler(OpenCameraAttachmentsInCreativePacketC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ExposureRequestC2SP.ID, new ServerHandler(ExposureRequestC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ActiveCameraReleaseC2SP.ID, new ServerHandler(ActiveCameraReleaseC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(InterplanarProjectionFinishedC2SP.ID, new ServerHandler(InterplanarProjectionFinishedC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ExposureDataC2SP.ID, new ServerHandler(ExposureDataC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ExposureDataChunkHeaderC2SP.ID, new ServerHandler(ExposureDataChunkHeaderC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(ExposureDataChunkBytesC2SP.ID, new ServerHandler(ExposureDataChunkBytesC2SP::fromPacket));
        ServerPlayNetworking.registerGlobalReceiver(CameraStandTurnC2SP.ID, new ServerHandler(CameraStandTurnC2SP::fromPacket));
    }

    public static void registerS2CPackets() {
        ClientPackets.registerS2CPackets();
    }

    // --

    public static void sendToServer(Packet packet) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        packet.toPacket(buffer);
        ClientPlayNetworking.send(packet.getId(), buffer);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        packet.toPacket(buffer);
        ServerPlayNetworking.send(player, packet.getId(), buffer);
    }

    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        if (ExposureFabric.server == null) {
            Exposure.LOGGER.error("Cannot send a packet to players. Server is not available.");
            return;
        }

        for (ServerPlayer player : ExposureFabric.server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(packet, player);
            }
        }
    }

    public static void sendToAllClients(Packet packet) {
        if (ExposureFabric.server == null) {
            Exposure.LOGGER.error("Cannot send a packet to all players. Server is not available.");
            return;
        }

        for (ServerPlayer player : ExposureFabric.server.getPlayerList().getPlayers()) {
            sendToClient(packet, player);
        }
    }

    public static void sendToPlayersNear(Packet packet, @NotNull ServerLevel level, @Nullable ServerPlayer excludedPlayer,
                                         double x, double y, double z, double radius) {
        sendToClients(packet, player -> {
            if (player != excludedPlayer && player.level().dimension() == level.dimension()) {
                double d0 = x - player.getX();
                double d1 = y - player.getY();
                double d2 = z - player.getZ();
                return d0 * d0 + d1 * d1 + d2 * d2 < radius * radius;
            }

            return false;
        });
    }

    private record ServerHandler(Function<FriendlyByteBuf, Packet> decoder) implements ServerPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
            Packet packet = decoder.apply(buf);
            server.execute(() -> packet.handle(PacketFlow.SERVERBOUND, player));
        }
    }
}