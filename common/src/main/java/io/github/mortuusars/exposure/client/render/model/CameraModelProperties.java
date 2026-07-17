package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;

public class CameraModelProperties {
    public static void bootstrap() {
        SelectItemModelProperties.ID_MAPPER.put(Exposure.resource("camera_mode"), CameraModeProperty.TYPE);
    }
}
