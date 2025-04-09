package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class ContrastEffect implements PixelEffect {
    protected final float contrast;

    /**
     * @param contrast 0 means no change.
     */
    public ContrastEffect(float contrast) {
        Preconditions.checkArgument(contrast >= -1 && contrast <= 1, "contrast must be in -1 to 1 range.");
        this.contrast = contrast;
    }

    @Override
    public String getIdentifier() {
        return "contrast-" + contrast;
    }

    public int modify(int colorARGB) {
        if (contrast == 0f) return colorARGB;

        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        int contrastValue = Math.round(127 * (1f + contrast));
        red = Mth.clamp((red - 127) * contrastValue / 127 + 127, 0, 255);
        green = Mth.clamp((green - 127) * contrastValue / 127 + 127, 0, 255);
        blue = Mth.clamp((blue - 127) * contrastValue / 127 + 127, 0, 255);

        return FastColor.ARGB32.color(alpha, red, green, blue);
    }

    @Override
    public String toString() {
        return "Contrast{contrast=" + contrast + "}";
    }
}
