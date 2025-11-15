package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public record ShaderApplyS2CP(Optional<ResourceLocation> shaderLocation) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("shader_apply");

    public ShaderApplyS2CP(ResourceLocation location) {
        this(Optional.of(location));
    }

    public static final ShaderApplyS2CP REMOVE = new ShaderApplyS2CP(Optional.empty());

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeOptional(shaderLocation,FriendlyByteBuf::writeResourceLocation);
    }

    public static ShaderApplyS2CP fromPacket(FriendlyByteBuf buf) {
        return new ShaderApplyS2CP(buf.readOptional(FriendlyByteBuf::readResourceLocation));
    }

    @Override
    public boolean handle(PacketFlow direction, Player player) {
        ClientPacketsHandler.applyShader(this);
        return true;
    }
}
