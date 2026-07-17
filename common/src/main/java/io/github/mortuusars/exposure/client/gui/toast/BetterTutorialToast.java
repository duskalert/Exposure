package io.github.mortuusars.exposure.client.gui.toast;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
//import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BetterTutorialToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/tutorial");
    public static final int DEFAULT_SHOW_DURATION_MS = 7000;

    public Identifier backgroundSprite = BACKGROUND_SPRITE;
    public Runnable onHide = () -> { };

    protected final ToastIcon icon;
    protected final Component title;
    @Nullable
    protected final Component message;
    protected Toast.Visibility visibility = Toast.Visibility.SHOW;

    protected final long shownAt = System.currentTimeMillis();
    protected final int showDurationMs;
    protected final Supplier<Boolean> hideIf;

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs, Supplier<Boolean> hideIf) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = showDurationMs;
        this.hideIf = hideIf;
    }

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, int showDurationMs) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = showDurationMs;
        this.hideIf = () -> false;
    }

    public BetterTutorialToast(ToastIcon icon, Component title, @Nullable Component message, Supplier<Boolean> hideIf) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.showDurationMs = -1;
        this.hideIf = hideIf;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

//    @Override
//    public @NotNull Visibility render(GuiGraphicsExtractor GuiGraphicsExtractor, ToastComponent toastComponent, long timeSinceLastVisible) {
//        if (visibility == Visibility.SHOW && hideIf.get() || (showDurationMs > 0 && System.currentTimeMillis() > shownAt + showDurationMs)) {
//            hide();
//            onHide.run();
//        }
//
//        GuiGraphicsExtractor.blitSprite(backgroundSprite, 0, 0, this.width(), this.height());
//        this.icon.render(GuiGraphicsExtractor, 6, 6);
//        if (this.message == null) {
//            GuiGraphicsExtractor.drawString(toastComponent.getMinecraft().font, this.title, 30, 12, 0xFF500050, false);
//        } else {
//            GuiGraphicsExtractor.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, 0xFF500050, false);
//            GuiGraphicsExtractor.drawString(toastComponent.getMinecraft().font, this.message, 30, 18, 0xFF000000, false);
//        }
//
//        return this.visibility;
//    }
}
