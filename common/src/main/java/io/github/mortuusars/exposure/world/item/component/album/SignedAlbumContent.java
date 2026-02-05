
package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collections;
import java.util.List;

public record SignedAlbumContent(String title, String author, List<SignedAlbumPage> pages) {
    public static final Codec<SignedAlbumContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("title").forGetter(SignedAlbumContent::title),
            Codec.STRING.fieldOf("author").forGetter(SignedAlbumContent::author),
            SignedAlbumPage.CODEC.listOf().fieldOf("pages").forGetter(SignedAlbumContent::pages)
    ).apply(instance, SignedAlbumContent::new));


    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(title);
        buf.writeUtf(author);
        buf.writeCollection(pages,(buf1, signedAlbumPage) -> signedAlbumPage.toPacket(buf1));
    }

    public static SignedAlbumContent fromPacket(FriendlyByteBuf buf) {
        return new SignedAlbumContent(buf.readUtf(),buf.readUtf(),buf.readList(SignedAlbumPage::fromPacket));
    }

    public static final SignedAlbumContent EMPTY = new SignedAlbumContent("", "", Collections.emptyList());

    public SignedAlbumContent {
        Preconditions.checkArgument(pages.size() <= AlbumContent.MAX_PAGES,
                "Too many pages for signed album. Max is " + AlbumContent.MAX_PAGES);
    }
}
