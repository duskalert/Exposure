package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.neoforge.PlatformHelperClientImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class PlatformHelperClient {
    public static BakedModel getModel(ModelResourceLocation model) {
        return PlatformHelperClientImpl.getModel(model);
    }
}
