package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.inventory.LecternAlbumMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LecternAlbumScreen extends AlbumViewScreen implements MenuAccess<LecternAlbumMenu> {
    // TODO: MC 26.1 - Screen API redesigned. Stubbed.

    private final LecternAlbumMenu menu;

    private final ContainerListener listener = new ContainerListener() {
        public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
            LecternAlbumScreen.this.bookChanged();
        }

        public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
            if (dataSlotIndex == 0) {
                LecternAlbumScreen.this.pageChanged();
            }
        }
    };

    public LecternAlbumScreen(LecternAlbumMenu menu, Inventory playerInventory, Component title) {
        super(AlbumAccess.fromItem(menu.getBook()));
        this.menu = menu;
        this.pager.setChangeSound(null);
    }

    public @NotNull LecternAlbumMenu getMenu() {
        return this.menu;
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    // TODO: MC 26.1 - onSpreadChanged
    protected void onSpreadChanged(int oldSpread, int newSpread) {
        // Stubbed
    }

    protected void pageChanged() {
        pager.changePage(menu.getPage() / 2);
    }

    protected void bookChanged() {
        ItemStack itemStack = this.menu.getBook();
        this.setAlbumAccess(AlbumAccess.fromItem(itemStack));
    }

    // TODO: MC 26.1 - forcePage
    protected void forcePage(int pageIndex) {
        // Stubbed
    }

    protected void sendButtonClick(int buttonId) {
        Minecrft.gameMode().handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - drawPageNumbers stubbed, drawString changed
    protected void drawPageNumbers(GuiGraphicsExtractor guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip stubbed
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // Stubbed
    }

    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        mouseX -= this.leftPos;
        mouseY -= this.topPos;
        return mouseX >= (double)(x - 1) && mouseX < (double)(x + width + 1) && mouseY >= (double)(y - 1) && mouseY < (double)(y + height + 1);
    }
}
