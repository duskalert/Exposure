package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class Slider extends AbstractWidget {
    // TODO: MC 26.1 - Widget API redesigned. Stubbed.

    public static final Identifier SLIDER_SPRITE = Identifier.withDefaultNamespace("widget/slider");
    public static final Identifier HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_highlighted");
    public static final Identifier SLIDER_HANDLE_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle");
    public static final Identifier SLIDER_HANDLE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");

    protected static final int TEXT_MARGIN = 2;
    protected static final int HANDLE_WIDTH = 8;
    protected static final int HANDLE_HALF_WIDTH = 4;

    protected double position;
    protected boolean canChangeValue;

    protected final double defaultValue;
    protected final double min;
    protected final double max;
    protected final DecimalFormat displayFormat;
    protected final String name;
    protected final Consumer<Double> onChanged;

    @Nullable
    protected Pair<Integer, Integer> horizontalGradient;

    public Slider(int x, int y, int width, int height,
                  double defaultValue, double min, double max, int displayedPrecision, String name, Consumer<Double> onChanged) {
        this(x, y, width, height, CommonComponents.EMPTY, defaultValue, min, max, displayedPrecision, name, onChanged);
    }

    public Slider(int x, int y, int width, int height, Component message,
                  double defaultValue, double min, double max, int displayedPrecision, String name, Consumer<Double> onChanged) {
        super(x, y, width, height, message);
        this.position = (defaultValue - min) / (max - min);
        this.defaultValue = Mth.clamp(defaultValue, min, max);
        this.min = min;
        this.max = max;
        String zeros = "#".repeat(displayedPrecision);
        this.displayFormat = new DecimalFormat("#" + (zeros.isEmpty() ? "" : "." + zeros));
        this.name = name;
        this.onChanged = onChanged;
        updateMessage();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor gr, int mx, int my, float pt) {
        // TODO: MC 26.1 - abstract method stub
    }

    public Slider setHorizontalGradient(int leftColor, int rightColor) {
        this.horizontalGradient = Pair.of(leftColor, rightColor);
        return this;
    }

    public void removeHorizontalGradient() {
        this.horizontalGradient = null;
    }

    protected Identifier getSprite() {
        return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    protected Identifier getHandleSprite() {
        return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    public double getPosition() {
        return this.position;
    }

    public void setPosition(double position) {
        double d = this.position;
        this.position = Mth.clamp(position, 0.0, 1.0);
        if (d != this.position) {
            this.applyValue();
        }
        this.updateMessage();
    }

    public Double getValue() {
        return Mth.clampedLerp(min, max, position);
    }

    public void setValue(double value) {
        double positionFromValue = (value - min) / (max - min);
        setPosition(positionFromValue);
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    protected void updateMessage() {
        setMessage(Component.literal(name + ": " + displayFormat.format(getValue())));
    }

    protected void applyValue() {
        onChanged.accept(getValue());
    }

    // TODO: MC 26.1 - onClick signature changed
    public void onClick(double mouseX, double mouseY) {
        // Stubbed
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.canChangeValue = false;
        } else {
            InputType inputType = Minecraft.getInstance().getLastInputType();
            if (inputType == InputType.MOUSE || inputType == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }
    }

    protected void setPositionFromMouse(double mouseX) {
        this.setPosition((mouseX - (double)(this.getX() + HANDLE_HALF_WIDTH)) / (double)(this.width - HANDLE_WIDTH));
    }

    // TODO: MC 26.1 - onRelease signature changed
    public void onRelease(double mouseX, double mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - onDrag signature changed
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderWidget signature changed
    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - fillHorizontalGradient stubbed
    private void fillHorizontalGradient(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        // Stubbed
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }
    }
}
