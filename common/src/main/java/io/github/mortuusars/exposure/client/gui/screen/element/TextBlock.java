package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TextBlock extends AbstractWidget {
    // TODO: MC 26.1 - Widget/Rendering API redesigned. Method bodies stubbed.

    public int fontColor = 0xFF000000;
    public boolean drawShadow = false;
    public HorizontalAlignment alignment = HorizontalAlignment.LEFT;

    private final Font font;
    private final Function<Style, Boolean> componentClickedHandler;

    private List<FormattedCharSequence> renderedLines;
    private List<FormattedCharSequence> tooltipLines;

    public TextBlock(Font font, int x, int y, int width, int height, Component message, Function<Style, Boolean> componentClickedHandler) {
        super(x, y, width, height, message);
        this.font = font;
        this.componentClickedHandler = componentClickedHandler;
        makeLines();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gr, int mx, int my, float pt) {
        // TODO: MC 26.1 - abstract method stub
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        makeLines();
    }

    protected void makeLines() {
        Component text = getMessage();
        List<FormattedCharSequence> lines = font.split(text, getWidth());
        int availableLines = Math.min(lines.size(), height / font.lineHeight);

        List<FormattedCharSequence> visibleLines = new ArrayList<>();
        for (int i = 0; i < availableLines; i++) {
            FormattedCharSequence line = lines.get(i);
            if (i == availableLines - 1 && availableLines < lines.size()) {
                line = FormattedCharSequence.composite(line,
                        Component.literal("...").withStyle(text.getStyle()).getVisualOrderText());
            }
            visibleLines.add(line);
        }

        List<FormattedCharSequence> hiddenLines = Collections.emptyList();
        if (availableLines < lines.size()) {
            hiddenLines = new ArrayList<>(lines.stream()
                    .skip(availableLines)
                    .toList());
            hiddenLines.set(0, FormattedCharSequence.composite(
                    FormattedCharSequence.forward("...", text.getStyle()), hiddenLines.get(0)));
        }

        this.renderedLines = visibleLines;
        this.tooltipLines = hiddenLines;
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return getMessage().copy();
    }

    // TODO: MC 26.1 - renderWidget signature changed, drawString/renderComponentHoverEffect/renderTooltip changed
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed - rendering API redesigned in MC 26.1
    }

    // TODO: MC 26.1 - getClickedComponentStyleAt stubbed
    public @Nullable Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        return null;
    }
}
