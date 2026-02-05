package io.github.mortuusars.exposure.network.forge;

import io.github.mortuusars.exposure.network.packet.*;
import io.github.mortuusars.exposure.network.packet.clientbound.*;
import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import io.github.mortuusars.exposure.network.packet.serverbound.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PacketsImpl {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
          new ResourceLocation("exposure:packets"),
          () -> PROTOCOL_VERSION,
          PROTOCOL_VERSION::equals,
          PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(ActiveCameraDeactivateCommonPacket.class, id++)
              .encoder(ActiveCameraDeactivateCommonPacket::toPacket)
              .decoder(friendlyByteBuf -> ActiveCameraDeactivateCommonPacket.INSTANCE)
              .consumerMainThread(PacketsImpl::handlePacket)
              .add();

        registerC2S(AlbumSignC2SP.class, AlbumSignC2SP::toPacket, AlbumSignC2SP::fromPacket);
        registerC2S(AlbumSyncNoteC2SP.class, AlbumSyncNoteC2SP::toPacket, AlbumSyncNoteC2SP::fromPacket);
        registerC2S(ActiveCameraSetSettingC2SP.class, ActiveCameraSetSettingC2SP::toPacket, ActiveCameraSetSettingC2SP::fromPacket);
        registerC2S(OpenCameraAttachmentsInCreativePacketC2SP.class, OpenCameraAttachmentsInCreativePacketC2SP::toPacket, OpenCameraAttachmentsInCreativePacketC2SP::fromPacket);
        registerC2S(ExposureRequestC2SP.class, ExposureRequestC2SP::toPacket, ExposureRequestC2SP::fromPacket);
        registerC2S(ActiveCameraReleaseC2SP.class, ActiveCameraReleaseC2SP::toPacket, ActiveCameraReleaseC2SP::fromPacket);
        registerC2S(InterplanarProjectionFinishedC2SP.class, InterplanarProjectionFinishedC2SP::toPacket, InterplanarProjectionFinishedC2SP::fromPacket);
        registerC2S(ExposureDataC2SP.class, ExposureDataC2SP::toPacket, ExposureDataC2SP::fromPacket);
        registerC2S(ExposureDataChunkHeaderC2SP.class, ExposureDataChunkHeaderC2SP::toPacket, ExposureDataChunkHeaderC2SP::fromPacket);
        registerC2S(ExposureDataChunkBytesC2SP.class, ExposureDataChunkBytesC2SP::toPacket, ExposureDataChunkBytesC2SP::fromPacket);
        registerC2S(CameraStandTurnC2SP.class, CameraStandTurnC2SP::toPacket, CameraStandTurnC2SP::fromPacket);

        registerS2C(ActiveCameraRemoveS2CP.class, ActiveCameraRemoveS2CP::toPacket, ActiveCameraRemoveS2CP::fromPacket);
        registerS2C(ActiveCameraInHandSetS2CP.class, ActiveCameraInHandSetS2CP::toPacket, ActiveCameraInHandSetS2CP::fromPacket);
        registerS2C(ActiveCameraOnStandSetS2CP.class, ActiveCameraOnStandSetS2CP::toPacket, ActiveCameraOnStandSetS2CP::fromPacket);
        registerS2C(CameraStandSetRotationsS2CP.class, CameraStandSetRotationsS2CP::toPacket, CameraStandSetRotationsS2CP::fromPacket);
        registerS2C(CameraStandStopControllingS2CP.class, CameraStandStopControllingS2CP::toPacket, CameraStandStopControllingS2CP::fromPacket);
        registerS2C(ShaderApplyS2CP.class, ShaderApplyS2CP::toPacket, ShaderApplyS2CP::fromPacket);
        registerS2C(ActionS2CP.class, ActionS2CP::toPacket, ActionS2CP::fromPacket);
        registerS2C(CreateChromaticExposureS2CP.class, CreateChromaticExposureS2CP::toPacket, CreateChromaticExposureS2CP::fromPacket);
        registerS2C(ExposureDataChangedS2CP.class, ExposureDataChangedS2CP::toPacket, ExposureDataChangedS2CP::fromPacket);
        registerS2C(UniqueSoundPlayS2CP.class, UniqueSoundPlayS2CP::toPacket, UniqueSoundPlayS2CP::fromPacket);
        registerS2C(UniqueSoundPlayShutterTickingS2CP.class, UniqueSoundPlayShutterTickingS2CP::toPacket, UniqueSoundPlayShutterTickingS2CP::fromPacket);
        registerS2C(UniqueSoundStopS2CP.class, UniqueSoundStopS2CP::toPacket, UniqueSoundStopS2CP::fromPacket);
        registerS2C(ShowExposureCommandS2CP.class, ShowExposureCommandS2CP::toPacket, ShowExposureCommandS2CP::fromPacket);
        registerS2C(ExposureDataResponseS2CP.class, ExposureDataResponseS2CP::toPacket, ExposureDataResponseS2CP::fromPacket);
        registerS2C(CaptureStartS2CP.class, CaptureStartS2CP::toPacket, CaptureStartS2CP::fromPacket);
        registerS2C(CaptureStartDebugRGBS2CP.class, CaptureStartDebugRGBS2CP::toPacket, CaptureStartDebugRGBS2CP::fromPacket);
        registerS2C(ExportS2CP.class, ExportS2CP::toPacket, ExportS2CP::fromPacket);
    }

    private static <T extends Packet> void registerS2C(Class<T> type,
                                                       BiConsumer<T, FriendlyByteBuf> encoder,
                                                       Function<FriendlyByteBuf, T> decoder) {
        CHANNEL.messageBuilder(type, id++, NetworkDirection.PLAY_TO_CLIENT)
              .encoder(encoder)
              .decoder(decoder)
              .consumerMainThread(PacketsImpl::handlePacket)
              .add();
    }

    private static <T extends Packet> void registerC2S(Class<T> type,
                                                       BiConsumer<T, FriendlyByteBuf> encoder,
                                                       Function<FriendlyByteBuf, T> decoder) {
        CHANNEL.messageBuilder(type, id++, NetworkDirection.PLAY_TO_SERVER)
              .encoder(encoder)
              .decoder(decoder)
              .consumerMainThread(PacketsImpl::handlePacket)
              .add();
    }

    public static void sendToServer(Packet packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToClient(Packet packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllClients(Packet packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToPlayersNear(Packet packet, @NotNull ServerLevel level, @javax.annotation.Nullable ServerPlayer excluded,
                                         double x, double y, double z, double radius) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension())), packet);
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

    private static <T extends Packet> void handlePacket(T packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        packet.handle(direction(context.getDirection()), context.getSender());
    }

    private static PacketFlow direction(NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_SERVER)
            return PacketFlow.SERVERBOUND;
        else if (direction == NetworkDirection.PLAY_TO_CLIENT)
            return PacketFlow.CLIENTBOUND;
        else
            throw new IllegalStateException("Can only convert direction for Client/Server, not others.");
    }
}