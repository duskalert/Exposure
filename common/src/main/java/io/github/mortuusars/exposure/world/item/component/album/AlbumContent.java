package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record AlbumContent(List<AlbumPage> pages) {
    public static final int MAX_PAGES = 16;

    public static final Codec<AlbumContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AlbumPage.CODEC.listOf().fieldOf("pages").forGetter(AlbumContent::pages)
    ).apply(instance, AlbumContent::new));

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeCollection(pages,(buf1, albumPage) -> albumPage.toPacket(buf1));
    }

    public static AlbumContent fromPacket(FriendlyByteBuf buf) {
        return new AlbumContent(buf.readList(AlbumPage::fromPacket));
    }

    public static final AlbumContent EMPTY = new AlbumContent(Collections.emptyList());

    public AlbumContent {
        Preconditions.checkArgument(pages.size() <= MAX_PAGES,
                "Too many pages for album. Max is " + MAX_PAGES);
    }

    public Optional<AlbumPage> getPage(int index) {
        return index < pages.size() ? Optional.ofNullable(pages.get(index)) : Optional.empty();
    }

    public boolean isEmpty() {
        return this.equals(EMPTY) || pages.stream().allMatch(AlbumPage::isEmpty);
    }

    public AlbumContent removeTrailingPages() {
        ArrayList<AlbumPage> pages = new ArrayList<>(this.pages);

        for (int i = pages.size() - 1; i >= 0; i--) {
            AlbumPage page = pages.get(i);

            if (page.isEmpty()) {
                pages.remove(i);
            } else {
                break;
            }
        }

        return new AlbumContent(pages);
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    public static class Mutable {
        private final ArrayList<AlbumPage> pages;

        public Mutable(AlbumContent content) {
            this.pages = new ArrayList<>(content.pages);
            while (pages.size() < MAX_PAGES) {
                pages.add(AlbumPage.EMPTY);
            }
        }

        public Mutable setPage(int index, AlbumPage page) {
            Preconditions.checkElementIndex(index, MAX_PAGES);
            pages.set(index, page);
            return this;
        }

        public AlbumContent toImmutable() {
            return new AlbumContent(pages);
        }
    }
}
