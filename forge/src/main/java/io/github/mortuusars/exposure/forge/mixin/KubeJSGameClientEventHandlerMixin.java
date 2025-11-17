package io.github.mortuusars.exposure.forge.mixin;

/**
 * Fixes only sky rendering with KubeJS installed.
 */
//@Mixin(KubeJSGameClientEventHandler.class)
public class KubeJSGameClientEventHandlerMixin {
   /* @Inject(method = "worldRender", at = @At("RETURN"))
    private static void onWorldRender(RenderLevelStageEvent event, CallbackInfo ci) {
        if (BackgroundScreenshotCaptureTask.isCapturing()) {
            BackgroundScreenshotCaptureTask.getRenderTarget().bindWrite(false);
        }
    }*/
}
