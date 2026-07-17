package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.ARGB;

public class NegativeEffect implements PixelEffect {
    @Override
    public String getIdentifier() {
        return "negative";
    }

    public int modify(int ARGB) {
        int alpha = ARGB.alpha(ARGB);
        int red = ARGB.red(ARGB);
        int green = ARGB.green(ARGB);
        int blue = ARGB.blue(ARGB);

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return ARGB.color(alpha, red, green, blue);
    }
}
