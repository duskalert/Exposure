package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.client.sound.UniqueSoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record UniqueSoundPlayS2CP(String id, int entityId, SoundEvent sound, SoundSource source,
                                  float volume, float pitch) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("unique_sound_play");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeInt(entityId);
        sound.writeToNetwork(buf);
        buf.writeEnum(source);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    public static UniqueSoundPlayS2CP fromPacket(FriendlyByteBuf buf) {
        return new UniqueSoundPlayS2CP(
              buf.readUtf(),
              buf.readInt(),
              SoundEvent.readFromNetwork(buf),
              buf.readEnum(SoundSource.class),
              buf.readFloat(),
              buf.readFloat());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        @Nullable Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            UniqueSoundManager.play(id, entity, sound, source, volume, pitch);
        }
        return true;
    }
}
