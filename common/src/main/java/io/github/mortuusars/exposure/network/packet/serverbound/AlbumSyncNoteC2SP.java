package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record AlbumSyncNoteC2SP(int pageIndex, String text) implements Packet {
    public static final ResourceLocation ID = Exposure.resource("album_update_note");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        buf.writeInt(pageIndex);
        buf.writeUtf(text);
    }

    public static AlbumSyncNoteC2SP fromPacket(FriendlyByteBuf buf){
        return new AlbumSyncNoteC2SP(buf.readInt(),buf.readUtf());
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        Preconditions.checkState(player != null, "Cannot handle packet: Player was null");

        if (!(player.containerMenu instanceof AlbumMenu albumMenu)) {
            throw new IllegalStateException("Player receiving this packet should have AlbumMenu open. " +
                    "Current menu: " + player.containerMenu);
        }

        albumMenu.updatePage(pageIndex, page -> page.setNote(text));
        return true;
    }
}
