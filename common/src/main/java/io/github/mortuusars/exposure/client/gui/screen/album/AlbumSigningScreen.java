package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class AlbumSigningScreen extends Screen {
    // TODO: MC 26.1 - Screen API redesigned. Method bodies stubbed.

    public static final WidgetSprites CANCEL_BUTTON_SPRITE = new WidgetSprites(
            Exposure.resource("album/cancel"), Exposure.resource("album/cancel_disabled"), Exposure.resource("album/cancel_highlighted"));

    public static final int SELECTION_COLOR = 0xFF8888FF;
    public static final int SELECTION_UNFOCUSED_COLOR = 0xFFBBBBFF;

    protected final AlbumScreen parentScreen;

    protected int imageWidth, imageHeight, leftPos, topPos;

    protected TextBox titleTextBox;
    protected ImageButton signButton;
    protected ImageButton cancelSigningButton;

    protected String titleText = "";

    public AlbumSigningScreen(AlbumScreen parent) {
        super(Component.empty());
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        this.imageWidth = 149;
        this.imageHeight = 188;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        titleTextBox = new TextBox(font, leftPos + 21, topPos + 73, 108, 9,
                () -> titleText, text -> titleText = text)
                .setFontColor(Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR))
                .setSelectionColor(SELECTION_COLOR, SELECTION_UNFOCUSED_COLOR);
        titleTextBox.horizontalAlignment = HorizontalAlignment.CENTER;
        addRenderableWidget(titleTextBox);

        signButton = new ImageButton(leftPos + 46, topPos + 110, 22, 22,
                AlbumScreen.SIGN_BUTTON_SPRITES, b -> signAlbum(), Component.translatable("gui.exposure.album.sign"));
        MutableComponent component = Component.translatable("gui.exposure.album.sign")
                .append("\n").append(Component.translatable("gui.exposure.album.sign.warning").withStyle(ChatFormatting.GRAY));
        signButton.setTooltip(Tooltip.create(component));
        addRenderableWidget(signButton);

        cancelSigningButton = new ImageButton(leftPos + 83, topPos + 111, 22, 22,
                CANCEL_BUTTON_SPRITE, b -> cancelSigning(), Component.translatable("gui.exposure.album.cancel_signing"));
        cancelSigningButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.cancel_signing")));
        addRenderableWidget(cancelSigningButton);

        setInitialFocus(titleTextBox);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // TODO: MC 26.1 - tick signature
    public void tick() {
        // Stubbed
    }

    private void updateButtons() {
        signButton.active = canSign();
    }

    protected boolean canSign() {
        return !titleText.isEmpty();
    }

    // TODO: MC 26.1 - render signature changed (GuiGraphicsExtractor -> GuiGraphicsExtractor src)
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderLabels stubbed
    private void renderLabels(GuiGraphicsExtractor guiGraphics) {
        // Stubbed
    }

    protected void signAlbum() {
        if (canSign()) {
            parentScreen.getMenu().setTitle(titleText);
            parentScreen.getMenu().signAlbum(Minecrft.player());
            this.onClose();
        }
    }

    protected void cancelSigning() {
        Minecrft.get().setScreen(parentScreen);
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB) {
            // return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == InputConstants.KEY_ESCAPE) {
            cancelSigning();
            return true;
        }
        if (titleTextBox.isFocused()) {
            return titleTextBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }
}
