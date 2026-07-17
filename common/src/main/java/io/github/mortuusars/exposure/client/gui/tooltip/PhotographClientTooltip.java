package io.github.mortuusars.exposure.client.gui.tooltip;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractorExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotographClientTooltip implements ClientTooltipComponent {
    public static final int SIZE = 72;

    protected final PhotographTooltip tooltip;
    protected final List<ItemAndStack<PhotographItem>> photographs;

    public PhotographClientTooltip(PhotographTooltip tooltip) {
        this.tooltip = tooltip;
        this.photographs = tooltip.photographs();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE + 2; // 2px bottom margin
    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, GuiGraphicsExtractor GuiGraphicsExtractor) {
        int photographsCount = photographs.size();
        int additionalPhotographs = Math.min(2, photographsCount - 1);

        GuiGraphicsExtractor.pose().pushPose();
        GuiGraphicsExtractor.pose().translate(mouseX, mouseY, 5);
        float scale = SIZE;
        float nextPhotographOffset = ExposureClient.photographRenderer().getStackedPhotographOffset();
        scale *= 1f - (additionalPhotographs * nextPhotographOffset);
        GuiGraphicsExtractor.pose().scale(scale, scale, 1f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        ExposureClient.photographRenderer().renderStackedPhotographs(photographs, GuiGraphicsExtractor.pose(), bufferSource,
                Lightmap.FULL_BRIGHT, 255, 255, 255, 255);

        bufferSource.endBatch();

        GuiGraphicsExtractor.pose().popPose();

        // Stack count:
        if (photographsCount > 1) {
            GuiGraphicsExtractor.pose().pushPose();
            String count = Integer.toString(photographsCount);
            int fontWidth = Minecraft.getInstance().font.width(count);
            float fontScale = 1.6f;
            GuiGraphicsExtractor.pose().translate(
                    mouseX + scale - 2 - fontWidth * fontScale,
                    mouseY + scale - 2 - 8 * fontScale,
                    10);
            GuiGraphicsExtractor.pose().scale(fontScale, fontScale, fontScale);
            GuiGraphicsExtractor.drawString(font, count, 0, 0, 0xFFFFFFFF);
            GuiGraphicsExtractor.pose().popPose();
        }
    }
}
