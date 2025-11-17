package io.github.mortuusars.exposure.forge.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class ExposureKubeJSPlugin extends KubeJSPlugin {
   /* @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(ExposureJSEvents.GROUP);
    }

    @Override
    public void init() {
        subscribeToNeoForgeEvents();
    }

    private void subscribeToNeoForgeEvents() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postAddEntityInFrameDataEvent);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postModifyFrameDataEvent);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::postFrameAddedEvent);
    }

    // --

    private void postAddEntityInFrameDataEvent(ModifyEntityInFrameDataEvent event) {
        ExposureJSEvents.ADD_ENTITY_IN_FRAME_DATA.post(ScriptType.SERVER,
                new ModifyEntityInFrameExtraDataEventJS(event.getCameraHolder(), event.getCamera(), event.getEntityInFrame(), event.getData()));
    }

    private void postModifyFrameDataEvent(ModifyFrameExtraDataEvent event) {
        ExposureJSEvents.MODIFY_FRAME_DATA.post(ScriptType.SERVER,
                new ModifyFrameExtraDataEventJS(event.getCameraHolder(), event.getCamera(), event.getCaptureProperties(),
                        event.getPositionsInFrame(), event.getEntitiesInFrame(), event.getData()));
    }

    private void postFrameAddedEvent(FrameAddedEvent event) {
        ExposureJSEvents.FRAME_ADDED.post(ScriptType.SERVER,
                new FrameAddedEventJS(event.getCameraHolder(), event.getCamera(), event.getFrame(), event.getPositionsInFrame(), event.getEntitiesInFrame()));
    }*/
}