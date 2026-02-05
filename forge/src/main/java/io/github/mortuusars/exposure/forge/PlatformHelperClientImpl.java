package io.github.mortuusars.exposure.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class PlatformHelperClientImpl {
    public static BakedModel getModel(ResourceLocation model) {
        return Minecraft.getInstance().getModelManager().getModel(model);
    }
}
