package io.github.mortuusars.exposure.network.fabric;

import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.clientbound.*;
import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;

import java.util.function.Function;

public class ClientPackets {
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ActiveCameraDeactivateCommonPacket.ID, new ClientHandler(ActiveCameraDeactivateCommonPacket::fromBuffer));

        ClientPlayNetworking.registerGlobalReceiver(ActiveCameraRemoveS2CP.ID, new ClientHandler(ActiveCameraRemoveS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ActiveCameraInHandSetS2CP.ID, new ClientHandler(ActiveCameraInHandSetS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ActiveCameraOnStandSetS2CP.ID, new ClientHandler(ActiveCameraOnStandSetS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(CameraStandSetRotationsS2CP.ID, new ClientHandler(CameraStandSetRotationsS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(CameraStandStopControllingS2CP.ID, new ClientHandler(CameraStandStopControllingS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ShaderApplyS2CP.ID, new ClientHandler(ShaderApplyS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ActionS2CP.ID, new ClientHandler(ActionS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(CreateChromaticExposureS2CP.ID, new ClientHandler(CreateChromaticExposureS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataChangedS2CP.ID, new ClientHandler(ExposureDataChangedS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(UniqueSoundPlayS2CP.ID, new ClientHandler(UniqueSoundPlayS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(UniqueSoundPlayShutterTickingS2CP.ID, new ClientHandler(UniqueSoundPlayShutterTickingS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(UniqueSoundStopS2CP.ID, new ClientHandler(UniqueSoundStopS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ShowExposureCommandS2CP.ID, new ClientHandler(ShowExposureCommandS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataResponseS2CP.ID, new ClientHandler(ExposureDataResponseS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataChunkResponseHeaderS2CP.ID, new ClientHandler(ExposureDataChunkResponseHeaderS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ExposureDataChunkResponseBytesS2CP.ID, new ClientHandler(ExposureDataChunkResponseBytesS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(CaptureStartS2CP.ID, new ClientHandler(CaptureStartS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(CaptureStartDebugRGBS2CP.ID, new ClientHandler(CaptureStartDebugRGBS2CP::fromPacket));
        ClientPlayNetworking.registerGlobalReceiver(ExportS2CP.ID, new ClientHandler(ExportS2CP::fromPacket));
    }

    private record ClientHandler(Function<FriendlyByteBuf, Packet> decoder) implements ClientPlayNetworking.PlayChannelHandler {
        @Override
        public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
            Packet packet = decoder.apply(buf);
            Minecrft.execute(() -> packet.handle(PacketFlow.CLIENTBOUND, Minecrft.player()));
        }
    }
}