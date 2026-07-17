package io.github.mortuusars.exposure.world.item.camcom;

import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.data.*;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Function;

public class CameraItemGetters {

    public static float getScaleOnStand(CameraItem item) {
        return 0.9f;
    }

    public static float getCropFactor(CameraItem item) {
        return 0.875f;
    }

    public static FocalRange getFocalRange(CameraItem item, RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.LENS.map(stack, lensStack -> Lenses.getFocalRangeOrDefault(registryAccess, lensStack))
                .orElse(FocalRange.getDefault());
    }

    public static double getFov(CameraItem item, Level level, ItemStack stack) {
        double zoom = CameraSettings.ZOOM.getOrDefault(stack);
        FocalRange focalRange = item.getFocalRange(level.registryAccess(), stack);
        return focalRange.fovFromZoom(zoom);
    }

    public static double getViewfinderFov(CameraItem item, Level level, ItemStack stack) {
        return item.getFov(level, stack) * item.getCropFactor();
    }

    public static Optional<Filter> getFilter(CameraItem item, RegistryAccess registryAccess, ItemStack stack) {
        return Attachment.FILTER.map(stack, filter -> Filters.of(registryAccess, filter)).flatMap(Function.identity());
    }

    public static Optional<Identifier> getFilterShaderLocation(CameraItem item, RegistryAccess registryAccess, ItemStack stack) {
        return item.getFilter(registryAccess, stack).map(Filter::shader);
    }

    public static Optional<ColorChannel> getChromaticChannel(CameraItem item, ItemStack stack) {
        return Attachment.FILTER.map(stack, ColorChannel::fromFilterStack).orElse(Optional.empty());
    }

    public static Optional<Projection> getProjection(CameraItem item, ItemStack stack) {
        return Attachment.FILTER.map(stack, (filterItem, filterStack) ->
                        filterItem instanceof InterplanarProjectorItem projectorItem
                                ? projectorItem.getProjection(filterStack)
                                : Optional.<Projection>empty())
                .orElse(Optional.empty());
    }

    public static CameraId getOrCreateId(CameraItem item, ItemStack stack) {
        if (!stack.has(Exposure.DataComponents.CAMERA_ID)) {
            stack.set(Exposure.DataComponents.CAMERA_ID, CameraId.create());
        }
        return stack.get(Exposure.DataComponents.CAMERA_ID);
    }

    public static boolean isInSelfieMode(CameraItem item, ItemStack stack) {
        return CameraSettings.SELFIE_MODE.getOrDefault(stack);
    }

    public static boolean isActive(CameraItem item, ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_ACTIVE, false);
    }

    public static void setActive(CameraItem item, ItemStack stack, boolean active) {
        if (!active) {
            stack.remove(Exposure.DataComponents.CAMERA_ACTIVE);
        } else {
            stack.set(Exposure.DataComponents.CAMERA_ACTIVE, true);
        }
    }

    public static boolean isDisassembled(CameraItem item, ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_DISASSEMBLED, false);
    }

    public static void setDisassembled(CameraItem item, ItemStack stack, boolean disassembled) {
        if (!disassembled) {
            stack.remove(Exposure.DataComponents.CAMERA_DISASSEMBLED);
        } else {
            stack.set(Exposure.DataComponents.CAMERA_DISASSEMBLED, true);
        }
    }

    public static long getLastActionTime(CameraItem item, ItemStack stack) {
        return stack.getOrDefault(Exposure.DataComponents.CAMERA_LAST_ACTION_TIME, -1L);
    }

    public static void setLastActionTime(CameraItem item, ItemStack stack, long lastActionTime) {
        stack.set(Exposure.DataComponents.CAMERA_LAST_ACTION_TIME, lastActionTime);
    }

}
