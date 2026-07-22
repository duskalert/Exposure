package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographClientTooltip implements ClientTooltipComponent {
    // TODO: MC 26.1 - ClientTooltipComponent API redesigned. Stubbed.

    public static final int SIZE = 72;

    protected final PhotographTooltip tooltip;
    protected final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographClientTooltip(PhotographTooltip tooltip) {
        this.tooltip = tooltip;
        this.photographs = tooltip.photographs();
    }

    // TODO: MC 26.1
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    // TODO: MC 26.1 - getHeight now takes Font
    public int getHeight(@NotNull Font font) {
        return SIZE + 2;
    }

    // TODO: MC 26.1 - renderImage signature changed
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphicsExtractor guiGraphics) {
        // Stubbed - pushPose/translate/scale/drawString/Lightmap API changed
    }
}
