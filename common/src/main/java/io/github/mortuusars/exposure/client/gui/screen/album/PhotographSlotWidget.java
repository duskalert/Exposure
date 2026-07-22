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
    // TODO: MC 26.1 - Rendering API redesigned. Method bodies stubbed.

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

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gr, int mx, int my, float pt) {
        // TODO: MC 26.1 - abstract method stub
    }

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

    public boolean isEditable() {
        return editable;
    }

    public ItemStack getPhotograph() {
        return photographSupplier.get();
    }

    // TODO: MC 26.1 - renderWidget signature changed, blit/blitSprite need RenderPipeline, pose API changed
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed - rendering API redesigned in MC 26.1
    }

    public void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // TODO: MC 26.1 - renderTooltip signature changed
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent instead of int button
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Stubbed - event API redesigned in MC 26.1
        return false;
    }

    // TODO: MC 26.1 - mouseScrolled signature may have changed
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
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
        // TODO: MC 26.1 - Verify API compatibility for setScreen and SoundEvents
        Minecrft.get().setScreen(new ChildPhotographScreen(parent, List.of(new ItemAndStack<>(photograph))));
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                        Minecrft.level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
        return true;
    }
}
