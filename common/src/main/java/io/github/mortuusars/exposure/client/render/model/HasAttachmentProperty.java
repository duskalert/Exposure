package io.github.mortuusars.exposure.client.render.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.camcom.Attachment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HasAttachmentProperty implements SelectItemModelProperty<Boolean> {
    public static final MapCodec<HasAttachmentProperty> CODEC = MapCodec.unit(HasAttachmentProperty::new);
    private final String attachmentName;

    public HasAttachmentProperty() {
        this.attachmentName = "";
    }

    @Nullable
    public HasAttachmentProperty withAttachment(String name) {
        var prop = new HasAttachmentProperty();
        // Attachment name is set via JSON field
        return prop;
    }

    @Override
    public Boolean get(ItemStack stack, ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return false;
    }

    @Override
    public Codec<Boolean> valueCodec() {
        return Codec.BOOL;
    }

    @Override
    public Type<? extends SelectItemModelProperty<Boolean>, Boolean> type() {
        return CameraModelProperties.HAS_FLASH;
    }
}
