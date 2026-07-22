package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemListScreen extends Screen {
    // TODO: MC 26.1 - Screen API redesigned. Stubbed.

    public static final Identifier TEXTURE = Exposure.resource("textures/gui/item_list.png");

    protected final Screen parent;
    protected final List<ItemStack> items;
    protected final int rowsCount;
    protected final Animation openingAnimation;

    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX = 8;
    protected int titleLabelY = 6;
    protected int leftPos;
    protected int topPos;

    @Nullable
    protected Slot hoveredSlot;
    protected List<Slot> slots = new ArrayList<>();

    protected long openedAt;

    public ItemListScreen(Screen parent, Component title, List<ItemStack> items) {
        super(title);
        this.parent = parent;
        this.items = items;
        List<List<ItemStack>> rows = Lists.partition(items, 9);
        this.rowsCount = rows.size();
        this.openingAnimation = new Animation(200, EasingFunction.EASE_OUT_EXPO);
        this.openedAt = Util.getMillis();
    }

    // TODO: MC 26.1
    public boolean isPauseScreen() {
        return false;
    }

    // TODO: MC 26.1
    protected void init() {
        // Stubbed
    }

    // TODO: MC 26.1 - render signature changed, pushPose/translate/scale changed
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBg stubbed
    protected void renderBg(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderLabels stubbed
    protected void renderLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderSlot stubbed
    protected void renderSlot(GuiGraphicsExtractor guiGraphics, Slot slot) {
        // Stubbed
    }

    public static void renderSlotHighlight(GuiGraphicsExtractor guiGraphics, int x, int y, int blitOffset) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip stubbed
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // Stubbed
    }

    protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        return (mouseX -= (double) i) >= (double) (x - 1) && mouseX < (double) (x + width + 1) && (mouseY -= (double) j) >= (double) (y - 1) && mouseY < (double) (y + height + 1);
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1
    public void onClose() {
        Minecrft.get().setScreen(parent);
    }
}
