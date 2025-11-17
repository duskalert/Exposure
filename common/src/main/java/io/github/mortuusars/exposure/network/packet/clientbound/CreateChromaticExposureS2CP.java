package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record CreateChromaticExposureS2CP(String id, List<ExposureIdentifier> layers) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("create_chromatic_exposure");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeCollection(layers,(buf1, exposureIdentifier) -> exposureIdentifier.toPacket(buf1));
    }

    public static CreateChromaticExposureS2CP fromPacket(FriendlyByteBuf buf) {
        return new CreateChromaticExposureS2CP(buf.readUtf(),buf.readList(ExposureIdentifier::fromPacket));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketsHandler.createChromaticExposure(this);
        return true;
    }
}
