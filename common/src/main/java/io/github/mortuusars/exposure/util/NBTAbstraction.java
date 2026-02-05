package io.github.mortuusars.exposure.util;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record NBTAbstraction<T>(BiFunction<ItemStack,String,T> reader, TriConsumer<ItemStack,String,T> writer,
                                FriendlyByteBuf.Reader<T> packetReader, FriendlyByteBuf.Writer<T> packetWriter){



    public static final NBTAbstraction<Boolean> BOOLEAN = new NBTAbstraction<>(Exposure.DataComponents::getBoolean, Exposure.DataComponents::setBoolean
    ,FriendlyByteBuf::readBoolean,FriendlyByteBuf::writeBoolean);
    public static final NBTAbstraction<Float> FLOAT = new NBTAbstraction<>(Exposure.DataComponents::getFloat, Exposure.DataComponents::setFloat,
            FriendlyByteBuf::readFloat,FriendlyByteBuf::writeFloat);
    public static final NBTAbstraction<Double> DOUBLE = new NBTAbstraction<>(Exposure.DataComponents::getDouble, Exposure.DataComponents::setDouble,
            FriendlyByteBuf::readDouble,FriendlyByteBuf::writeDouble);

    public static final NBTAbstraction<ShutterSpeed> SHUTTER_SPEED = new NBTAbstraction<>(Exposure.DataComponents::getShutterSpeed, Exposure.DataComponents::setShutterSpeed,
            ShutterSpeed::fromPacket,(buf, shutterSpeed) -> shutterSpeed.toPacket(buf));

    public static final NBTAbstraction<CompositionGuide> COMPOSITION_GUIDE = new NBTAbstraction<>(Exposure.DataComponents::getCompositionGuide,
            Exposure.DataComponents::setCompositionGuide,
            CompositionGuide::fromPacket,(buf, shutterSpeed) -> shutterSpeed.toPacket(buf));

    public static final NBTAbstraction<SelfTimer> SELF_TIMER = new NBTAbstraction<>(Exposure.DataComponents::getSelfTimer,
            Exposure.DataComponents::setSelfTimer,
            buf -> buf.readEnum(SelfTimer.class), FriendlyByteBuf::writeEnum);

    public static final NBTAbstraction<FlashMode> FLASH_MODE = new NBTAbstraction<>(Exposure.DataComponents::getFlashMode,
            Exposure.DataComponents::setFlashMode,
            buf -> buf.readEnum(FlashMode.class), FriendlyByteBuf::writeEnum);

    public static <T> NBTAbstraction.Named<T> named(String key,NBTAbstraction<T> abstraction) {
        return new Named<>(key, abstraction);
    }

    public record Named<T>(String key,NBTAbstraction<T> abstraction) {
        public T read(ItemStack stack) {
            return abstraction.reader.apply(stack,key);
        }

        public void write(ItemStack stack,T value) {
            abstraction.writer.accept(stack,key,value);
        }
    }
}
