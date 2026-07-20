package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

// TODO: MC 26.1 - GameRenderer, RenderTarget, PostChain APIs redesigned. Needs full rewrite.
@Deprecated
public class BackgroundScreenshotCaptureTask extends Task<Result<Image>> {
    private static @Nullable RenderTarget renderTarget = null;

    public static boolean isCapturing() { return false; }

    public static @NotNull RenderTarget getRenderTarget() {
        throw new UnsupportedOperationException("Not implemented in MC 26.1");
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        Exposure.LOGGER.error("BackgroundScreenshotCaptureTask not implemented in MC 26.1");
        return CompletableFuture.completedFuture(Result.error(Capture.ERROR_FAILED_GENERIC));
    }
}
