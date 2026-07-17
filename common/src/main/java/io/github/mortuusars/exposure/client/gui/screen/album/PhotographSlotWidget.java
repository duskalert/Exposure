package io.github.mortuusars.exposure.client.gui.screen.album;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PhotographSlotWidget extends AbstractWidget {
    public static final WidgetSprites SPRITES = new WidgetSprites(
            Exposure.resource("album/photograph_slot"), Exposure.resource("album/photograph_slot_highlighted"));
    public static final WidgetSprites EMPTY_SPRITES = new WidgetSprites(
            Exposure.resource("album/photograph_slot_empty"), Exposure.resource("album/photograph_slot_empty_highlighted"));

    private final Screen parent;
    protected final Supplier<ItemStack> photographSupplier;

    protected boolean editable;
    protected Consumer<PhotographSlotWidget> primaryAction = slot -> {};
    protected Consumer<PhotographSlotWidget> secondaryAction = slot -> {};

    protected boolean hasPhotograph;

    public PhotographSlotWidget(Screen parent, int x, int y, int width, int height, Supplier<ItemStack> photographSupplier) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        this.photographSupplier = photographSupplier;
    }

    // --

    public PhotographSlotWidget editable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public PhotographSlotWidget primaryAction(Consumer<PhotographSlotWidget> primaryAction) {
        this.primaryAction = primaryAction;
        return this;
    }

    public PhotographSlotWidget secondaryAction(Consumer<PhotographSlotWidget> secondaryAction) {
        this.secondaryAction = secondaryAction;
        return this;
    }

    // --

    public boolean isEditable() {
        return editable;
    }

    public ItemStack getPhotograph() {
        return photographSupplier.get();
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        ItemStack photograph = getPhotograph();

        if (photograph.getItem() instanceof PhotographItem) {
            hasPhotograph = true;

            PhotographStyle photographStyle = PhotographStyle.of(photograph);

            // Paper
            GuiGraphicsExtractor.blit(photographStyle.albumPaperTexture(),
                    getX(), getY(), 0, 0, 0, width, height, width, height);

            // Exposure
            GuiGraphicsExtractor.pose().pushPose();
            float scale = 96;
            GuiGraphicsExtractor.pose().translate(getX() + 6, getY() + 6, 1);
            GuiGraphicsExtractor.pose().scale(scale, scale, scale);
            MultiBufferSource.BufferSource bufferSource = Minecrft.get().renderBuffers().bufferSource();
            ExposureClient.photographRenderer().render(photograph, false, false,
                    GuiGraphicsExtractor.pose(), bufferSource, Lightmap.FULL_BRIGHT);
            bufferSource.endBatch();
            GuiGraphicsExtractor.pose().popPose();

            // Paper overlay
            if (photographStyle.hasAlbumOverlayTexture()) {
                GuiGraphicsExtractor.pose().pushPose();
                GuiGraphicsExtractor.pose().translate(0, 0, 2);
                GuiGraphicsExtractor.blit(photographStyle.albumOverlayTexture(),
                        getX(), getY(), 0, 0, 0, width, height, width, height);
                GuiGraphicsExtractor.pose().popPose();
            }
        }
        else {
            hasPhotograph = false;
        }

        WidgetSprites sprites = hasPhotograph ? SPRITES : EMPTY_SPRITES;
        Identifier Identifier = sprites.get(isActive(), isHoveredOrFocused());
        if (!editable && !hasPhotograph) {
            Identifier = sprites.get(isActive(), false);
        }
        GuiGraphicsExtractor.blitSprite(Identifier, getX(), getY(), width, height);
    }

    public void renderTooltip(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY) {
        if (editable && !hasPhotograph) {
            GuiGraphicsExtractor.renderTooltip(Minecrft.get().font, Component.translatable("gui.exposure.album.add_photograph"), mouseX, mouseY);
            return;
        }

        ItemStack photograph = getPhotograph();
        if (photograph.isEmpty()) return;

        List<Component> itemTooltip = Screen.getTooltipFromItem(Minecrft.get(), photograph);
        itemTooltip.add(Component.translatable("gui.exposure.album.left_click_or_scroll_up_to_view"));
        if (editable) {
            itemTooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));
        }

        // Photograph image in tooltip is not rendered

        if (isFocused()) {
            GuiGraphicsExtractor.renderTooltip(Minecrft.get().font, Lists.transform(itemTooltip,
                    Component::getVisualOrderText), DefaultTooltipPositioner.INSTANCE, mouseX, mouseY);
        }
        else
            GuiGraphicsExtractor.renderTooltip(Minecrft.get().font, itemTooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !clicked(mouseX, mouseY)) return false;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            primaryAction.accept(this);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            secondaryAction.accept(this);
        } else return false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && clicked(mouseX, mouseY) && hasPhotograph) {
            primaryAction.accept(this);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible && CommonInputs.selected(keyCode)) {
            if (Screen.hasShiftDown()) {
                secondaryAction.accept(this);
            } else {
                primaryAction.accept(this);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack photograph = getPhotograph();
        if (!photograph.isEmpty()) {
            narrationElementOutput.add(NarratedElementType.TITLE, photograph.getHoverName());
        }
    }

    public boolean inspectPhotograph() {
        ItemStack photograph = getPhotograph();
        if (!(photograph.getItem() instanceof PhotographItem)) {
            return false;
        }

        Minecrft.get().setScreen(new ChildPhotographScreen(parent, List.of(new ItemAndStack<>(photograph))));
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                        Minecrft.level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
        return true;
    }
}
