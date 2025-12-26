package io.github.mortuusars.exposure.client.gui;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ModWidgetSprites;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Widgets {
    public static final ModWidgetSprites PREVIOUS_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/previous_button"));
    public static final ModWidgetSprites NEXT_BUTTON_SPRITES =
            threeStateSprites(Exposure.resource("widgets/next_button"));

    public static ResourceLocation empty() {
        return Exposure.resource("empty");
    }

    public static ModWidgetSprites threeStateSprites(ResourceLocation base) {
        return ModWidgetSprites.withPrefix(base,
                new ResourceLocation(base.getNamespace(), base.getPath() + "_disabled"),
                new ResourceLocation(base.getNamespace(), base.getPath() + "_highlighted"),15,18);
    }

    public static <T> Map<T, ModWidgetSprites> createMap(List<T> values, Function<T, ModWidgetSprites> convertFunc) {
        Preconditions.checkArgument(!values.isEmpty(), "values list must not be empty.");
        Map<T, ModWidgetSprites> map = new HashMap<>();
        for (T value : values) {
            map.put(value, convertFunc.apply(value));
        }
        return map;
    }
}
