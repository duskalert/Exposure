package io.github.mortuusars.exposure.network.forge;


import io.github.mortuusars.exposure.forge.event.ForgeCommonEvents;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class PacketsImpl {

    public static void sendToServer(Packet packet) {
        ForgeCommonEvents.ModBus.registrar.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        ForgeCommonEvents.ModBus.registrar.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
        ForgeCommonEvents.ModBus.registrar.send(PacketDistributor.ALL.noArg(),packet);
        //PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendToPlayersNear(Packet packet, @NotNull ServerLevel level, @Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        ForgeCommonEvents.ModBus.registrar.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded,x,y,z,radius,level.dimension())),packet);
        //PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, packet);
    }
}