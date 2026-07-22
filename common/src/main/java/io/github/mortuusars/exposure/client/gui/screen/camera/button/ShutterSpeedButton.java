package io.github.mortuusars.exposure.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class ShutterSpeedButton extends CycleButton<ShutterSpeed> {
    // TODO: MC 26.1 - Widget rendering API redesigned. Stubbed.

    protected final int secondaryFontColor;
    protected final int mainFontColor;

    public ShutterSpeedButton(int x, int y, int width, int height, List<ShutterSpeed> values, @NotNull ShutterSpeed startingValue,
                              Function<ShutterSpeed, WidgetSprites> spritesFunc, OnCycle<ShutterSpeed> onCycle) {
        super(x, y, width, height, values, startingValue, spritesFunc, onCycle);
        mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
        secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
    }

    public ShutterSpeedButton(int x, int y, int width, int height, List<ShutterSpeed> values, @NotNull ShutterSpeed startingValue,
                              Function<ShutterSpeed, WidgetSprites> spritesFunc) {
        super(x, y, width, height, values, startingValue, spritesFunc, (button, newValue) -> {});
        mainFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_MAIN_COLOR);
        secondaryFontColor = Config.getColor(Config.Client.VIEWFINDER_FONT_SECONDARY_COLOR);
    }

    // TODO: MC 26.1 - renderWidget signature changed, drawString changed
    public void renderWidget(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - playDownSound
    public void playDownSound(SoundManager handler) {
        if (clickSound != null) {
            handler.play(SimpleSoundInstance.forUI(clickSound,
                    ThreadLocalRandom.current().nextFloat() * 0.05f + 0.9f + currentIndex * 0.01f, 0.7f));
        }
    }
}
