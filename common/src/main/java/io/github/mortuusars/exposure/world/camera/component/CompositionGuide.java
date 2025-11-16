package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public record CompositionGuide(String name) {
    public static final Codec<CompositionGuide> CODEC = Codec.STRING.xmap(CompositionGuides::byNameOrNone, CompositionGuide::name);

    public void toPacket(FriendlyByteBuf buf) {
        buf.writeUtf(name);
    }

    public static CompositionGuide fromPacket(FriendlyByteBuf buf) {
        return CompositionGuides.byNameOrNone(buf.readUtf());
    }

    public MutableComponent translate() {
        return Component.translatable("gui." + Exposure.ID + ".composition_guide." + name);
    }

    public ResourceLocation overlayTextureLocation() {
        return Exposure.resource("textures/gui/viewfinder/composition_guide/" + name + ".png");
    }

    public ResourceLocation buttonSpriteLocation() {
        return Exposure.resource("camera_controls/composition_guide/" + name);
    }
}
