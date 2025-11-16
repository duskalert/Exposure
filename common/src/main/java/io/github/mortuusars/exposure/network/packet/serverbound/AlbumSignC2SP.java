package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record AlbumSignC2SP(int slot, String title, String author) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("album_sign");

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeUtf(title);
        buf.writeUtf(author);
    }

    public static AlbumSignC2SP fromPacket(FriendlyByteBuf buf) {
        return new AlbumSignC2SP(buf.readInt(),buf.readUtf(),buf.readUtf());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        ItemStack albumStack = player.getInventory().getItem(slot());
        if (albumStack.getItem() instanceof AlbumItem albumItem) {
            ItemStack signedAlbumStack = albumItem.sign(albumStack, title(), author());
            player.getInventory().setItem(slot(), signedAlbumStack);
            player.level().playSound(null, player, Exposure.SoundEvents.WRITE.get(), SoundSource.PLAYERS, 0.8f ,1f);
        }

        return true;
    }
}
