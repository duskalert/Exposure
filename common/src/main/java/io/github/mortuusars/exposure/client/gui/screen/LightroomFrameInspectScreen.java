package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class LightroomFrameInspectScreen extends FilmFrameInspectScreen {
    // TODO: MC 26.1 - Screen API redesigned. Stubbed.

    private final LightroomScreen lightroomScreen;

    public LightroomFrameInspectScreen(LightroomScreen lightroomScreen) {
        super(lightroomScreen.getMenu().getExposedFrames(), lightroomScreen.getMenu().getSelectedFrame());
        this.lightroomScreen = lightroomScreen;
        this.pager.setChangeSound(null);
    }

    // TODO: MC 26.1
    protected void pageChanged(int oldPage, int newPage) {
        // Stubbed
    }

    // TODO: MC 26.1 - render signature changed
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1
    public void onClose() {
        zoom.setTarget(0f);
    }
}
