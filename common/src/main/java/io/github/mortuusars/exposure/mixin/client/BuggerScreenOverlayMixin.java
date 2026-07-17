package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.util.bugger.Bugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractorExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class BuggerScreenOverlayMixin {
    @SuppressWarnings("deprecation")
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphicsExtractor GuiGraphicsExtractor, CallbackInfo ci) {
        if (!PlatformHelper.isInDevEnv()) return;

        if (Bugger.page == 0) {
            Minecraft.getInstance().getProfiler().push("bugger_main");
            GuiGraphicsExtractor.drawManaged(() -> Bugger.renderMainPage(GuiGraphicsExtractor));
            Minecraft.getInstance().getProfiler().pop();
            ci.cancel();
        }

        if (Bugger.page == 1) {
            Minecraft.getInstance().getProfiler().push("bugger_tag");
            GuiGraphicsExtractor.drawManaged(() -> Bugger.renderTagPage(GuiGraphicsExtractor));
            Minecraft.getInstance().getProfiler().pop();
            ci.cancel();
        }

        String str = "[<-] and [->] to switch pages";
        int strWidth = Minecraft.getInstance().font.width(str);
        int x = GuiGraphicsExtractor.guiWidth() / 2 - strWidth / 2;
        GuiGraphicsExtractor.fill(x - 1, 1, x + strWidth + 1, 10, -1873784752);
        GuiGraphicsExtractor.drawString(Minecraft.getInstance().font, str, x, 2, 0xFFFFFFFF, false);
    }
}
