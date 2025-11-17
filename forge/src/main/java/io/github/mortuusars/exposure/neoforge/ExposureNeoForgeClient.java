package io.github.mortuusars.exposure.neoforge;

import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import net.minecraftforge.fml.ModContainer;

public class ExposureNeoForgeClient {
    public static void init(ModContainer modContainer) {
       // modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ModCompatibilityClient.handle();
    }
}
