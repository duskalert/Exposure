package io.github.mortuusars.exposure.client.gui.screen.element.textbox;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.util.Pos2i;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextBox extends AbstractWidget {
    // TODO: MC 26.1 - Widget/Rendering API redesigned. Method bodies stubbed.

    public final Font font;
    public Supplier<String> textGetter;
    public Consumer<String> textSetter;
    public Predicate<String> textValidator = text -> text != null
            && height >= getFont().lineHeight; // TODO: MC 26.1 - wordWrapHeight now takes FormattedText

    public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    public int fontColor = 0xFF000000;
    public int fontUnfocusedColor = 0xFF000000;
    public int selectionColor = 0xFF0000FF;
    public int selectionUnfocusedColor = 0x880000FF;

    public final TextFieldHelper textFieldHelper;
    protected DisplayCache displayCache = new DisplayCache();
    protected int frameTick;
    protected long lastClickTime;
    protected int lastIndex = -1;

    public TextBox(@NotNull Font font, int x, int y, int width, int height,
                   Supplier<String> textGetter, Consumer<String> textSetter) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.textGetter = textGetter;
        this.textSetter = textSetter;
        textFieldHelper = new TextFieldHelper(this::getText, this::setText,
                TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()),
                this::validateText);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gr, int mx, int my, float pt) {
        // TODO: MC 26.1 - abstract method stub
    }

    public void tick() {
        ++frameTick;
    }

    public Font getFont() {
        return font;
    }

    public @NotNull String getText() {
        return textGetter.get();
    }

    public TextBox setText(@NotNull String text) {
        textSetter.accept(text);
        clearDisplayCache();
        return this;
    }

    protected boolean validateText(String text) {
        return textValidator.test(text);
    }

    public void setHeight(int height) {
        this.height = height;
        clearDisplayCache();
    }

    public int getCurrentFontColor() {
        return isFocused() ? fontColor : fontUnfocusedColor;
    }

    public TextBox setFontColor(int fontColor, int fontUnfocusedColor) {
        this.fontColor = fontColor;
        this.fontUnfocusedColor = fontUnfocusedColor;
        clearDisplayCache();
        return this;
    }

    public TextBox setFontColor(int fontColor) {
        this.fontColor = fontColor;
        this.fontUnfocusedColor = fontColor;
        clearDisplayCache();
        return this;
    }

    public TextBox setSelectionColor(int selectionColor, int selectionUnfocusedColor) {
        this.selectionColor = selectionColor;
        this.selectionUnfocusedColor = selectionUnfocusedColor;
        clearDisplayCache();
        return this;
    }

    public void setCursorToEnd() {
        textFieldHelper.setCursorToEnd();
        clearDisplayCache();
    }

    public void refresh() {
        clearDisplayCache();
    }

    protected DisplayCache getDisplayCache() {
        if (displayCache.needsRebuilding)
            displayCache.rebuild(font, getText(), textFieldHelper.getCursorPos(), textFieldHelper.getSelectionPos(),
                    getX(), getY(), getWidth(), getHeight(), horizontalAlignment);
        return displayCache;
    }

    protected void clearDisplayCache() {
        displayCache.needsRebuilding = true;
    }

    protected Pos2i convertLocalToScreen(Pos2i pos) {
        return new Pos2i(getX() + pos.x, getY() + pos.y);
    }

    protected Pos2i convertScreenToLocal(Pos2i screenPos) {
        return new Pos2i(screenPos.x - getX(), screenPos.y - getY());
    }

    // TODO: MC 26.1 - renderWidget signature changed
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed - drawString, fill, pose API changed
    }

    // TODO: MC 26.1 - renderHighlight stubbed
    protected void renderHighlight(GuiGraphicsExtractor guiGraphics, Rect2i[] highlightAreas) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderCursor stubbed
    protected void renderCursor(GuiGraphicsExtractor guiGraphics, Pos2i cursorPos, boolean isEndOfText) {
        // Stubbed
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    public @NotNull Component getMessage() {
        return Component.literal(getText());
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent, hasControlDown/hasShiftDown removed from Screen
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - handleKeyPressed stubbed
    protected boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - charTyped now takes CharacterEvent
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - mouseDragged now takes MouseButtonEvent,double,double
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    // TODO: MC 26.1 - selectWord
    protected void selectWord(int index) {
        // Stubbed
    }

    // TODO: MC 26.1 - changeLine
    protected void changeLine(int yChange) {
        // Stubbed
    }

    // TODO: MC 26.1 - keyHome
    protected void keyHome() {
        // Stubbed
    }

    // TODO: MC 26.1 - keyEnd
    protected void keyEnd() {
        // Stubbed
    }
}
