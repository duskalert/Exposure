package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.ModWidgetSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * backport of ImageButton from 1.21.1
 */
public class BetterImageButton  extends Button {
        protected final ModWidgetSprites sprites;

        public BetterImageButton(int x, int y, int width, int height, ModWidgetSprites sprites, Button.OnPress onPress) {
            this(x, y, width, height, sprites, onPress, CommonComponents.EMPTY);
        }

        public BetterImageButton(int x, int y, int width, int height, ModWidgetSprites sprites, Button.OnPress onPress, Component message) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.sprites = sprites;
        }

        public BetterImageButton(int width, int height, ModWidgetSprites sprites, Button.OnPress onPress, Component message) {
            this(0, 0, width, height, sprites, onPress, message);
        }

        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation resourceLocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blit(resourceLocation, this.getX(), this.getY(),0,0, this.width, this.height);
        }
    }
