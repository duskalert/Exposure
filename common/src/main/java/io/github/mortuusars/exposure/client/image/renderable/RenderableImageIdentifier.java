package io.github.mortuusars.exposure.client.image.renderable;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public record RenderableImageIdentifier(String base, String variant) {
    public RenderableImageIdentifier(String base) {
        this(base, "");
    }

    public RenderableImageIdentifier appendVariant(String appendedVariant) {
        if (appendedVariant.isBlank()) return this;
        if (variant.isBlank()) return new RenderableImageIdentifier(base, appendedVariant);
        else return new RenderableImageIdentifier(base, variant + "_" + appendedVariant);
    }

    public ResourceLocation toResourceLocation() {
        String path = variant.isBlank() ? base : base + "/" + variant;
        String validPath = Util.sanitizeName(path, ResourceLocation::validPathChar);
        return Exposure.resource(validPath);
    }
}
