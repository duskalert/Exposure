package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import net.minecraftforge.eventbus.api.IEventBus;

public class ExposureForgeClient {
    public static void init(IEventBus bus) {
       // modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ModCompatibilityClient.handle();
    }
}
