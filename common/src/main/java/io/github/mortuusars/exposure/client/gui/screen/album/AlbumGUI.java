package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ModWidgetSprites;
import net.minecraft.resources.ResourceLocation;

public class AlbumGUI {
    public static final ModWidgetSprites PREVIOUS_PAGE_BUTTON_SPRITES = new ModWidgetSprites(
            Exposure.resource("album/previous_page"), Exposure.resource("album/previous_page_highlighted"));
    public static final ModWidgetSprites NEXT_PAGE_BUTTON_SPRITES = new ModWidgetSprites(
            Exposure.resource("album/next_page"), Exposure.resource("album/next_page_highlighted"));

    public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/album.png");
}
