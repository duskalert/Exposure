package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.world.inventory.ItemRenameMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemRenameScreen extends AbstractContainerScreen<ItemRenameMenu> {
    // TODO: MC 26.1 - ContainerScreen API redesigned. Stubbed.

    public static final Identifier TEXTURE = Exposure.resource("textures/gui/item_rename.png");
    protected EditBox name;

    public ItemRenameScreen(ItemRenameMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    protected void confirm() {
        onClose();
    }

    protected void cancel() {
        onClose();
    }

    // TODO: MC 26.1 - resize signature
    public void resize(Minecraft minecraft, int width, int height) {
        // Stubbed
    }

    // TODO: MC 26.1 - render signature changed
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip signature changed
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBg signature changed, blit signature changed
    protected void renderBg(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Stubbed
    }

    protected void onNameChanged(String name) {
        // Stubbed
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
