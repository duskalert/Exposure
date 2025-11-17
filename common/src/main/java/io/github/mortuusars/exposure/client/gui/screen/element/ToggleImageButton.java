package io.github.mortuusars.exposure.client.gui.screen.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

public class ToggleImageButton extends ImageButton {
    protected final Consumer<Boolean> onToggled;
    protected boolean state;

    public ToggleImageButton(int x, int y, int width, int height, ResourceLocation texture,
                             Consumer<Boolean> onToggled) {
        super(x, y, width, height, 0,0,texture, b -> {});
        this.onToggled = onToggled;
    }

    public boolean isOn() {
        return state;
    }

    public boolean isOff() {
        return !isOn();
    }

    public void toggle() {
        this.state = !state;
        onToggled.accept(this.state);
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public <T> T mapState(Function<Boolean, T> mappingFunction) {
        return mappingFunction.apply(state);
    }

    @Override
    public void onPress() {
        toggle();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //WidgetSprites sprites = isOn() ? onSprites : this.sprites;
        guiGraphics.blit(resourceLocation, this.getX(), this.getY(),0,0, this.width, this.height);
    }
}
