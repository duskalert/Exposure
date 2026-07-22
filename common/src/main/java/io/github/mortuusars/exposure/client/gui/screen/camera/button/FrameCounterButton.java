package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.camcom.Attachment;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FrameCounterButton extends ImageButton {
    // TODO: MC 26.1 - Widget rendering API redesigned. Stubbed.

    protected final int secondaryFontColor;
    protected final int mainFontColor;

    public FrameCounterButton(int x, int y, int width, int height, WidgetSprites sprites) {
        super(x, y, width, height, sprites, button -> {});
        mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
        secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
    }

    // TODO: MC 26.1 - renderWidget signature changed, drawString changed
    public void renderWidget(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float pPartialTick) {
        // Stubbed
    }

    protected String createText() {
        // TODO: MC 26.1 - getActiveExposureCameraOptional() removed
        return "-";
    }

    protected boolean cameraHasFilmRoll() {
        // TODO: MC 26.1 - getActiveExposureCameraOptional() removed
        return false;
    }
}
