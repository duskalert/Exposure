package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Stuff;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record SignedAlbumPage(ItemStack photograph, Component note) {
    public static final Codec<SignedAlbumPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(SignedAlbumPage::photograph),
            Stuff.COMPONENT_CODEC.optionalFieldOf("note",Component.empty()).forGetter(SignedAlbumPage::note)
    ).apply(instance, SignedAlbumPage::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignedAlbumPage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, SignedAlbumPage::photograph,
            ComponentSerialization.STREAM_CODEC, SignedAlbumPage::note,
            SignedAlbumPage::new
    );

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeItem(photograph);
        buf.writeComponent(note);
    }

    public static SignedAlbumPage fromPacket(FriendlyByteBuf buf) {
        return new SignedAlbumPage(buf.readItem(),buf.readComponent());
    }

    public static final SignedAlbumPage EMPTY = new SignedAlbumPage(ItemStack.EMPTY, Component.empty());

    public boolean isEmpty() {
        return this.equals(EMPTY) || (photograph().isEmpty() && note().getString().isEmpty());
    }
}
