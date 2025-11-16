package io.github.mortuusars.exposure.world.item.camera;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.NBTAbstraction;
import io.github.mortuusars.exposure.world.camera.component.*;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CameraSettings {
    private static final Map<ResourceLocation, CameraSetting<?>> REGISTRY = new HashMap<>();

    public static <T> CameraSetting<T> register(ResourceLocation id, CameraSetting<T> setting) {
        Preconditions.checkArgument(!REGISTRY.containsKey(id), "Setting with id '%s' is already registered.", id);
        REGISTRY.put(id, setting);
        return setting;
    }

    public static CameraSetting<?> byId(ResourceLocation id) {
        @Nullable CameraSetting<?> setting = REGISTRY.get(id);
        if (setting == null) {
            throw new IllegalStateException("Setting with id '" + id + "' is not registered.");
        }
        return setting;
    }

    public static ResourceLocation idOf(CameraSetting<?> setting) {
        for (Map.Entry<ResourceLocation, CameraSetting<?>> entry : REGISTRY.entrySet()) {
            if (entry.getValue().equals(setting)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Setting is not registered.");
    }

    public static final CameraSetting<Boolean> SELFIE_MODE = register(Exposure.resource("selfie"),
            new CameraSetting<>(NBTAbstraction.named("selfie_mode",NBTAbstraction.BOOLEAN), false, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Float> ZOOM = register(Exposure.resource("zoom"),
            new CameraSetting<>(NBTAbstraction.named("camera_zoom",NBTAbstraction.FLOAT), 0f, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Double> SELFIE_ROTATION_X = register(Exposure.resource("selfie_rotation_x"),
            new CameraSetting<>(NBTAbstraction.named("camera_selfie_rotation_x",NBTAbstraction.DOUBLE), 0.0, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<Double> SELFIE_ROTATION_Y = register(Exposure.resource("selfie_rotation_y"),
            new CameraSetting<>(NBTAbstraction.named("camera_selfie_rotation_y",NBTAbstraction.DOUBLE), 0.0, new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK)));
    public static final CameraSetting<ShutterSpeed> SHUTTER_SPEED = register(Exposure.resource("shutter_speed"),
            new CameraSetting<>(NBTAbstraction.named("camera_shutter_speed",NBTAbstraction.SHUTTER_SPEED), ShutterSpeed.DEFAULT, new SoundEffect(Exposure.SoundEvents.CAMERA_DIAL_CLICK)));
    public static final CameraSetting<CompositionGuide> COMPOSITION_GUIDE = register(Exposure.resource("composition_guide"),
            new CameraSetting<>(NBTAbstraction.named("camera_composition_guide",NBTAbstraction.COMPOSITION_GUIDE), CompositionGuides.NONE, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
    public static final CameraSetting<SelfTimer> SELF_TIMER = register(Exposure.resource("self_timer"),
            new CameraSetting<>(NBTAbstraction.named("camera_self_timer",NBTAbstraction.SELF_TIMER), SelfTimer.OFF, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
    public static final CameraSetting<FlashMode> FLASH_MODE = register(Exposure.resource("flash_mode"),
            new CameraSetting<>(NBTAbstraction.named("camera_flash_mode",NBTAbstraction.FLASH_MODE), FlashMode.OFF, new SoundEffect(Exposure.SoundEvents.CAMERA_BUTTON_CLICK)));
}
