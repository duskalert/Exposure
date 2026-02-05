package io.github.mortuusars.exposure.world.item;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class InterplanarProjectorItem extends Item {
    public InterplanarProjectorItem(Properties properties) {
        super(properties);
    }

    public DitherMode getMode(ItemStack stack) {
        return Exposure.DataComponents.getInterplanarProjectorMode(stack, DitherMode.DITHERED);
    }

    public void setMode(ItemStack stack, DitherMode mode) {
        Exposure.DataComponents.setInterplanarProjectorMode(stack, mode);
    }

    public boolean isConsumable(ItemStack stack) {
        return isAllowed();
    }

    protected boolean isAllowed() {
        return Config.Server.CAN_PROJECT.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> components, TooltipFlag tooltipFlag) {
        if (!isAllowed()) {
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.disabled"));
        }

        if (getProjection(stack).isPresent()) {
            components.add(getMode(stack).translate());
        }

        if (Screen.hasShiftDown()) {
            if (isConsumable(stack)) {
                components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.consumed_info"));
            }
            components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.info"));
            if (getProjection(stack).isPresent()) {
                components.add(Component.translatable("item.exposure.interplanar_projector.tooltip.change_mode_info"));
            }
        } else {
            components.add(Component.translatable("tooltip.exposure.hold_for_details"));
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (other.isEmpty() && action == ClickAction.SECONDARY && getProjection(stack).isPresent()) {
            setMode(stack, getMode(stack).cycle());
            slot.setChanged();
            if (player.level().isClientSide) {
                player.playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), 0.6f, 1f);
            }
            return true;
        }

        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    public Optional<String> getPath(ItemStack stack) {
        @Nullable Component customName = stack.getHoverName();
        return Optional.of(customName.getString());
    }

    public Optional<Projection> getProjection(ItemStack stack) {
        return isAllowed() ? getPath(stack).map(filepath -> new Projection(filepath, getMode(stack))) : Optional.empty();
    }
}
