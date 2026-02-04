package io.github.mortuusars.exposure.forge;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.ModelHooks;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ExposureForgeClient {

    public static IGuiOverlay VIEWFINDER = (forgeGui, arg, f, i, j) -> {
        ModelHooks.renderGui(arg, f);
    };

    public static void init(IEventBus bus) {
        // modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ModCompatibilityClient.handle();
        bus.addListener(ExposureForgeClient::registerOverlay);
        MinecraftForge.EVENT_BUS.addListener(ExposureForgeClient::renderOverlay);
    }

    static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("viewfinder", VIEWFINDER);
    }

    static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        Viewfinder viewfinder = CameraClient.viewfinder();
        if (viewfinder != null && viewfinder.isLookingThrough() && overlay.overlay() != VIEWFINDER && Config.Client.HIDE_HUD_WHILE_IN_VIEWFINDER.get()) {
            event.setCanceled(true);
            return;
        }

        if (overlay == VanillaGuiOverlay.CROSSHAIR.type()) {
            event.setCanceled(ModelHooks.renderCrosshair());
        }
    }
}
