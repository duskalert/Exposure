package io.github.mortuusars.exposure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;

public class Stuff {

    public static final Codec<MinMaxBounds.Ints> INTS_CODEC = RecordCodecBuilder.create(
            intsInstance -> intsInstance.group(Codec.INT.fieldOf("min")
                    .forGetter(MinMaxBounds::getMin),
                    Codec.INT.fieldOf("max")
                            .forGetter(MinMaxBounds::getMax)
    ).apply(intsInstance,MinMaxBounds.Ints::between));

    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(Component.Serializer::fromJson, Component.Serializer::toJson);

}
