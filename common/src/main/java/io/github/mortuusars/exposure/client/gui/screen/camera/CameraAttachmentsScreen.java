package io.github.mortuusars.exposure.client.gui.screen.camera;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.screen.ItemListScreen;
import io.github.mortuusars.exposure.client.gui.screen.element.ToggleImageButton;
import io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast;
import io.github.mortuusars.exposure.client.gui.toast.ToastIcon;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.supporter.Supporters;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.world.inventory.AbstractCameraAttachmentsMenu;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class CameraAttachmentsScreen extends AbstractContainerScreen<AbstractCameraAttachmentsMenu> {
    // TODO: MC 26.1 - ContainerScreen/Rendering API redesigned. Stubbed.

    public static final Identifier TEXTURE = Exposure.resource("textures/gui/camera_attachments.png");

    public static final WidgetSprites SKIN_REGULAR_BUTTON_SPRITES = Widgets.threeStateSprites(Exposure.resource("camera_attachments/regular"));
    public static final WidgetSprites SKIN_GOLD_BUTTON_SPRITES = Widgets.threeStateSprites(Exposure.resource("camera_attachments/gold"));

    protected final Player player;

    protected Map<Integer, Rect2i> slotPlaceholders = Map.of(
            0, new Rect2i(238, 0, 18, 18),
            1, new Rect2i(238, 18, 18, 18),
            2, new Rect2i(238, 36, 18, 18),
            3, new Rect2i(238, 54, 18, 18)
    );

    protected final HoveredElement flash = new HoveredElement(List.of(new Rect2i(96, 11, 28, 27)),
            () -> getMenu().getSlot(1).hasItem());
    protected final HoveredElement filterOnLens = new HoveredElement(List.of(new Rect2i(114, 57, 13, 6),
            new Rect2i(110, 63, 17, 24)), () -> getMenu().getSlot(2).hasItem());
    protected final HoveredElement lens = new HoveredElement(List.of(new Rect2i(93, 48, 33, 34)),
            () -> getMenu().getSlot(2).hasItem());
    protected final HoveredElement filter = new HoveredElement(List.of(new Rect2i(110, 55, 13, 6),
            new Rect2i(106, 61, 17, 24)), () -> !getMenu().getSlot(2).hasItem());
    protected final HoveredElement lensBuiltIn = new HoveredElement(List.of(new Rect2i(93, 48, 29, 32)),
            () -> !getMenu().getSlot(2).hasItem());
    protected final HoveredElement selfTimer = new HoveredElement(List.of(new Rect2i(92, 77, 6, 7)), () -> true);
    protected final HoveredElement viewfinder = new HoveredElement(List.of(new Rect2i(65, 25, 30, 12),
            new Rect2i(72, 31, 39, 11), new Rect2i(80, 42, 24, 5)), () -> true);
    protected final HoveredElement film = new HoveredElement(List.of(new Rect2i(48, 33, 15, 38),
            new Rect2i(52, 24, 16, 11)), () -> true);
    protected final HoveredElement shutterSpeedKnob = new HoveredElement(List.of(new Rect2i(68, 49, 21, 26)), () -> true);

    protected long openedAt = System.currentTimeMillis();
    protected boolean hasHoveredOverPart;

    public CameraAttachmentsScreen(AbstractCameraAttachmentsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    protected void changeSkin(boolean gold) {
        // Stubbed
    }

    // TODO: MC 26.1 - render/rendering API redesigned
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBg signature changed, blit/RenderSystem API changed
    protected void renderBg(@NotNull GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderAttachments stubbed
    protected void renderAttachments(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderFilter stubbed
    protected void renderFilter(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, Filter filter, int filterX, int filterY) {
        // Stubbed
    }

    protected boolean isMouseOver(HoveredElement element, int mouseX, int mouseY) {
        if (!element.isEnabled.get()) {
            return false;
        }
        mouseX -= leftPos;
        mouseY -= topPos;
        for (Rect2i area : element.hoverArea) {
            if (mouseX >= area.getX() && mouseX < area.getX() + area.getWidth() &&
                    mouseY >= area.getY() && mouseY < area.getY() + area.getHeight()) {
                return true;
            }
        }
        return false;
    }

    // TODO: MC 26.1 - renderSlotPlaceholders stubbed
    protected void renderSlotPlaceholders(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip signature changed, renderTooltip calls changed
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        // Stubbed
    }

    public @NotNull List<Component> getTooltipFromContainerItem(ItemStack stack) {
        return super.getTooltipFromContainerItem(stack);
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_F1) {
            return true;
        }
        return false;
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void onClose() {
        super.onClose();
    }

    protected MutableComponent translate(String key) {
        return Component.translatable("gui.exposure.camera_attachments." + key);
    }

    protected MutableComponent translate(String key, Object... args) {
        return Component.translatable("gui.exposure.camera_attachments." + key, args);
    }

    protected MutableComponent translate(String key, ChatFormatting formatting) {
        return Component.translatable("gui.exposure.camera_attachments." + key).withStyle(formatting);
    }

    protected MutableComponent translateKey(KeyMapping mapping, ChatFormatting formatting) {
        return Component.literal(mapping.getTranslatedKeyMessage().getString()).withStyle(formatting);
    }

    protected List<FormattedCharSequence> getTooltipLines(Component component, int width) {
        return font.split(component, width);
    }

    protected List<FormattedCharSequence> getTooltipLines(Component component) {
        return font.split(component, getMaxTooltipWidth());
    }

    protected int getMaxTooltipWidth() {
        return 250;
    }

    public record HoveredElement(List<Rect2i> hoverArea, Supplier<Boolean> isEnabled) {
    }
}
