package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class NegativeFilmEffect implements PixelEffect {
    @Override
    public String getIdentifier() {
        return "negative-film";
    }

    public int modify(int ARGB) {
        int alpha = ARGB.alpha(ARGB);
        int red = ARGB.red(ARGB);
        int green = ARGB.green(ARGB);
        int blue = ARGB.blue(ARGB);

        // Modify opacity to make lighter colors transparent, like in real film.
        int lightness = (red + green + blue) / 3;
        int opacity = (int) Mth.clamp(lightness * 1.5f, 0, 255);
        alpha = (alpha * opacity) / 255;

        // Invert
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return ARGB.color(alpha, red, green, blue);
    }
}
