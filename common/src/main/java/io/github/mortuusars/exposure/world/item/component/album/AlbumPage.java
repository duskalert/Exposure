package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record AlbumPage(ItemStack photograph, String note) {
    public static final Codec<AlbumPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(AlbumPage::photograph),
            Codec.STRING.optionalFieldOf("note", "").forGetter(AlbumPage::note)
    ).apply(instance, AlbumPage::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeItem(photograph);
        buf.writeUtf(note);
    }

    public static AlbumPage fromPacket(FriendlyByteBuf buf) {
        return new AlbumPage(buf.readItem(),buf.readUtf());
    }

    public static final AlbumPage EMPTY = new AlbumPage(ItemStack.EMPTY, "");

    public boolean isEmpty() {
        return photograph().isEmpty() && note().isEmpty();
    }

    public AlbumPage setPhotograph(ItemStack stack) {
        return new AlbumPage(stack, note);
    }

    public AlbumPage setNote(String note) {
        return new AlbumPage(photograph, note);
    }

    public SignedAlbumPage convertToSigned() {
        return new SignedAlbumPage(photograph, Component.literal(note));
    }
}
