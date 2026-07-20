package io.github.mortuusars.exposure.client.capture;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: MC 26.1 - PostChain API redesigned. Shader effects need full rewrite.
@Deprecated
public class CaptureShader {
    @Nullable
    private static PostChain shader = null;

    public static boolean hasShader() { return false; }
    public static void apply(Identifier shaderLocation) {}
    public static void resize(int width, int height) {}
    public static void process() {}
    public static void process(RenderTarget renderTarget) {}
    public static void process(@NotNull PostChain shader, @NotNull RenderTarget renderTarget) {}
    public static void remove() { shader = null; }
}
