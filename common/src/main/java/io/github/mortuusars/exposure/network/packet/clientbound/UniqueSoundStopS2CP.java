package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public record UniqueSoundStopS2CP(String id, SoundEvent sound) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_stop");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        sound.writeToNetwork(buf);
    }

    public static UniqueSoundStopS2CP fromPacket(FriendlyByteBuf buf){
        return new UniqueSoundStopS2CP(buf.readUtf(),SoundEvent.readFromNetwork(buf));
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        UniqueSoundManager.stop(id, sound);
        return true;
    }
}
