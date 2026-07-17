package io.github.mortuusars.exposure.client.render;

import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class PhotographFrameEntityRenderer<T extends PhotographFrameEntity> extends EntityRenderer<T, EntityRenderState> {
    public static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/block/oak_planks.png");

    public PhotographFrameEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(T entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void extractRenderState(T entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }
}
