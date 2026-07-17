package io.github.mortuusars.exposure.neoforge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.ExposureClientReloadListener;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;

@SuppressWarnings("unused")
public class NeoForgeClientEvents {
    @EventBusSubscriber(modid = Exposure.ID, value = {Dist.CLIENT})
    public static class ModBus {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ExposureClient::init);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
            event.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
            event.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
            event.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
            event.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
            event.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(PhotographTooltip.class, PhotographClientTooltip::new);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            KeyboardHandler.registerKeymappings(key -> {
                event.register(key);
                return key;
            });
        }
    }

    public static class GameBus {
        public static void register() {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(GameBus::onRenderGuiPost);
        }

        private static void onRenderGuiPost(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
            CameraStandTooltip.render(event.getGuiGraphicsExtractor(), event.getPartialTick());
        }
    }
}
