package io.github.mortuusars.exposure.client.render.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class TextureRenderer {
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, Color color) {
        render(poseStack, bufferSource, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, 0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                              float x, float y, float width, float height, int packedLight, int r, int g, int b, int a) {
        render(poseStack, bufferSource, texture, x, y, x + width, y + height,
                0, 0, 1, 1, packedLight, r, g, b, a);
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture,
                               float minX, float minY, float maxX, float maxY,
                               float minU, float minV, float maxU, float maxV, int packedLight, int r, int g, int b, int a) {
        // TODO: MC 26.1 - RenderType.text() and RenderSystem API changed
    }
}
