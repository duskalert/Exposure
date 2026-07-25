package io.github.mortuusars.exposure.network.neoforge;


import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class PacketsImpl {
    public static void bind() {
        Packets.bind(new Packets.Service() {
            @Override public void sendToServer(Packet p) { PacketsImpl.sendToServer(p); }
            @Override public void sendToClient(Packet p, ServerPlayer pl) { PacketsImpl.sendToClient(p, pl); }
            @Override public void sendToClients(Packet p, Predicate<ServerPlayer> f) { PacketsImpl.sendToClients(p, f); }
            @Override public void sendToAllClients(Packet p) { PacketsImpl.sendToAllClients(p); }
            @Override public void sendToPlayersNear(Packet p, ServerLevel l, @Nullable ServerPlayer e, double x, double y, double z, double r) { PacketsImpl.sendToPlayersNear(p, l, e, x, y, z, r); }
        });
    }

    public static void handle(Packet packet, IPayloadContext context) {
        packet.handle(context.flow(), context.player());
    }

    public static void sendToServer(Packet packet) {
        ClientPacketDistributor.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToClients(Packet packet, Predicate<ServerPlayer> filter) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                "Cannot send clientbound payloads on the client");

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (filter.test(player)) {
                sendToClient(packet, player);
            }
        }
    }

    public static void sendToAllClients(Packet packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendToPlayersNear(Packet packet, @NotNull ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, packet);
    }
}