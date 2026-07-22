package io.github.mortuusars.exposure.world.item.camcom;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.data.*;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShutterOpenedS2CP;
import io.github.mortuusars.exposure.network.packet.serverbound.OpenCameraAttachmentsInCreativePacketC2SP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.*;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CameraItemInteraction {

    public static boolean overrideOtherStackedOnMe(CameraItem item, ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) return false;

        if (item.getShutter().isOpen(stack)) {
            player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
            player.sendSystemMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED));
            return true;
        }

        if (otherStack.isEmpty() && Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get()) {
            if (!(slot.container instanceof Inventory)) {
                return false; // Cannot open when not in player's inventory
            }

            if (player.isCreative() && player.level().isClientSide()) {
                Packets.sendToServer(new OpenCameraAttachmentsInCreativePacketC2SP(slot.getContainerSlot()));
                return true;
            }

            openCameraAttachments(item, player, slot.getContainerSlot(), true);
            return true;
        }

        if (Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get()) {
            if (hotswap(item, player, stack, otherStack, access) != InteractionResult.PASS) {
                return true;
            }
        }

        return false;
    }

    public static InteractionResult handleStandSneakInteraction(CameraItem item, CameraStandEntity stand, Player player, InteractionHand hand, ItemStack cameraStack) {
        ItemStack itemInHand = player.getItemInHand(hand);
        int slot = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot();
        SlotAccess access = SlotAccess.forPlayer(player, slot);
        return hotswap(item, stand, cameraStack, itemInHand, access);
    }

    public static InteractionResult hotswap(CameraItem item, CameraHolder holder, ItemStack stack, ItemStack otherStack, SlotAccess access) {
        for (Attachment<?> attachment : item.getAttachments()) {
            StoredItemStack storedStack = attachment.get(stack);
            int maxCount = attachment.maxCount().get();

            // Remove
            if (otherStack.isEmpty()) {
                if (storedStack.isEmpty()) return InteractionResult.FAIL;

                access.set(storedStack.getCopy());
                attachment.set(stack, ItemStack.EMPTY);
                attachment.playRemoveSoundSided(holder.asHolderEntity());
                return InteractionResult.SUCCESS;
            }

            if (attachment.matches(otherStack)) {
                // Insertion
                if (storedStack.isEmpty() || ItemStack.isSameItemSameComponents(storedStack.getForReading(), otherStack)) {
                    int availableCount = Math.max(0, maxCount - storedStack.getForReading().getCount());
                    if (availableCount == 0) {
                        holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                        return InteractionResult.FAIL; // No space
                    }

                    ItemStack insertedStack = otherStack.split(availableCount);
                    insertedStack.setCount(insertedStack.getCount() + storedStack.getForReading().getCount());
                    attachment.set(stack, insertedStack);
                    access.set(otherStack);
                    attachment.playInsertSoundSided(holder.asHolderEntity());
                    return InteractionResult.SUCCESS;
                }

                // Swap

                if (otherStack.getCount() > maxCount) {
                    holder.asHolderEntity().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 0.9f, 1f);
                    return InteractionResult.FAIL; // Cannot swap when holding more than can be inserted
                }

                ItemStack returnedStack = storedStack.getCopy();
                attachment.set(stack, otherStack);
                access.set(returnedStack);
                attachment.playInsertSoundSided(holder.asHolderEntity());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult openCameraAttachments(CameraItem item, @NotNull Player player, ItemStack stack, boolean openedFromGUI) {
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem, "%s is not a CameraItem.", stack);

        int cameraSlot = CameraItemCapture.getMatchingSlotInInventory(item, player.getInventory(), stack);
        if (cameraSlot < 0) {
            Exposure.LOGGER.error("Cannot open camera attachments: slot index is not found for item '{}'.", stack);
            return InteractionResult.FAIL;
        }

        return openCameraAttachments(item, player, cameraSlot, openedFromGUI);
    }

    public static InteractionResult openCameraAttachments(CameraItem item, @NotNull Player player, int slotIndex, boolean openedFromGUI) {
        Preconditions.checkArgument(slotIndex >= 0,
                "slotIndex '%s' is invalid. Should be larger than 0", slotIndex);
        ItemStack stack = player.getInventory().getItem(slotIndex);
        Preconditions.checkArgument(stack.getItem() instanceof CameraItem,
                "Item in slotIndex '%s' is not a CameraItem but '%s'.", slotIndex, stack);

        if (item.getShutter().isOpen(stack)) {
            player.sendSystemMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        item.getOrCreateId(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            item.getTimer().stop(stack);

            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return stack.get(DataComponents.CUSTOM_NAME) != null
                            ? stack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraInHandAttachmentsMenu(containerId, playerInventory, slotIndex, openedFromGUI);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> {
                buffer.writeInt(slotIndex);
                buffer.writeBoolean(openedFromGUI);
            });
        }

        item.setDisassembled(stack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    public static void inventoryTick(CameraItem item, ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof CameraHolder holder)) return;

        tick(item, holder, stack);

        if (level.isClientSide() && entity instanceof Player player) {
            boolean matchesActive = player.getActiveExposureCameraOptional()
                    .map(camera -> camera.idMatches(item.getOrCreateId(stack)))
                    .orElse(false);
            if (item.isActive(stack) && !matchesActive) {
                item.setActive(stack, false);
            }
        }
    }

    // === Delegate methods moved from CameraItem ===

    public static void actionPerformed(CameraItem item, ItemStack stack, CameraHolder holder) {
        item.setLastActionTime(stack, holder.asHolderEntity().level().getGameTime());
        holder.asHolderEntity().gameEvent(GameEvent.ITEM_INTERACT_FINISH);
    }

    public static @NotNull InteractionResult activateInHand(CameraItem item, Player player, ItemStack stack, @NotNull InteractionHand hand) {
        player.setActiveExposureCamera(new CameraInHand(player, item.getOrCreateId(stack), hand));
        if (player.level().isClientSide())
            Minecrft.releaseUseButton();
        return activate(item, player, stack);
    }

    public static @NotNull InteractionResult activateOnStand(CameraItem item, Player player, ItemStack stack, CameraStandEntity cameraStand) {
        player.setActiveExposureCamera(new CameraOnStand(player, cameraStand, item.getOrCreateId(stack)));
        if (player.level().isClientSide())
            Minecrft.releaseUseButton();
        return activate(item, player, stack);
    }

    public static @NotNull InteractionResult activate(CameraItem item, Entity entity, ItemStack stack) {
        item.setActive(stack, true);
        item.setDisassembled(stack, false);
        Sound.play(entity, item.getViewfinderOpenSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }

    public static @NotNull InteractionResult deactivate(CameraItem item, Entity entity, ItemStack stack) {
        item.setActive(stack, false);
        CameraSettings.SELFIE_MODE.set(stack, false);
        Sound.play(entity, item.getViewfinderCloseSound(), entity.getSoundSource(), 0.35f, 0.9f, 0.2f);
        entity.gameEvent(GameEvent.EQUIP);
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }

    public static int calculateCooldownAfterShot(CameraItem item, ItemStack stack, CaptureParameters captureParameters) {
        if (captureParameters.projection().isPresent()) return CameraItem.PROJECT_COOLDOWN;
        if (captureParameters.getFlash()) return item.getFlash().getCooldown();
        return CameraItem.BASE_COOLDOWN;
    }

    public static boolean tick(CameraItem item, CameraHolder holder, ItemStack stack) {
        Level level = holder.asHolderEntity().level();
        if (!(level instanceof ServerLevel serverLevel)) return false;

        boolean shutterStateChanged = item.getShutter().tick(holder, serverLevel, stack);
        boolean timerChanged = item.getTimer().tick(holder, serverLevel, stack);

        if (Config.Server.TIMER_ATTRACTS_MOB_ATTENTION.get()
                && item.getTimer().isTicking(holder, stack) || item.getTimer().getTicksSinceLastRelease(holder, stack) < 10) {
            grabAttentionOfNearbyMobs(item, holder, stack);
        }

        boolean projectionChanged = CameraInstances.getOptional(stack).map(instance -> {
            CameraInstance.ProjectionState state = instance.getProjectionState(level);
            switch (state) {
                case SUCCESSFUL, FAILED, TIMED_OUT -> {
                    CameraItemCapture.handleProjectionResult(item, serverLevel, holder, stack, state, instance.getProjectionError(level));
                    instance.stopWaitingForProjection();
                    return true;
                }
            }
            return false;
        }).orElse(false);

        if (ExposureServer.debugHighlightEntitiesInFrame && item.isActive(stack)) {
            CameraItemCapture.testEntitiesInFrame(item, stack, level, holder);
        }

        return shutterStateChanged || timerChanged || projectionChanged;
    }

    public static void grabAttentionOfNearbyMobs(CameraItem item, CameraHolder holder, ItemStack stack) {
        Entity holderEntity = holder.asHolderEntity();
        Vec3 pos = item.isInSelfieMode(stack)
                ? holderEntity.getEyePosition().add(holderEntity.getLookAngle().scale(Config.Server.SELFIE_CAMERA_DISTANCE.get()))
                : holderEntity.getEyePosition();

        holderEntity.level().getEntities(holderEntity, new AABB(holderEntity.blockPosition())
                        .inflate(Config.Server.TIMER_ATTENTION_RADIUS.get()))
                .stream()
                .filter(entity -> entity instanceof Mob)
                .map(entity -> ((Mob) entity))
                .filter(mob -> canGrabAttentionOf(item, holder, mob))
                .forEach(mob -> {
                    // Each entity has slightly different delay until looking
                    long startLookingTick = item.getTimer().getStartTick(stack) + (mob.getId() % 15);
                    if (mob.level().getGameTime() > startLookingTick) {
                        mob.lookAt(EntityAnchorArgument.Anchor.EYES, pos);
                    }
                });
    }

    public static boolean canGrabAttentionOf(CameraItem item, CameraHolder holder, Mob mob) {
        return mob.isAlive()
                && !mob.isDeadOrDying()
                && !mob.isSleeping()
                && !mob.getType().is(Exposure.Tags.Entities.IGNORES_CAMERA)
                && (mob.getTarget() == null || mob.getTarget().equals(holder))
                && !mob.hasEffect(MobEffects.BLINDNESS)
                && mob.hasLineOfSight(holder.asHolderEntity());
    }

    public static @NotNull InteractionResult use(CameraItem item, @NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().getItem() instanceof CameraItem offhandCameraItem
                && offhandCameraItem.isActive(player.getOffhandItem())) {
            return InteractionResult.PASS;
        }

        if (!item.isActive(stack)) {
            return player.isSecondaryUseActive()
                    ? openCameraAttachments(item, player, stack, false)
                    : item.activateInHand(player, stack, hand);
        }

        return release(item, player, stack);
    }

    public static boolean canTakePhoto(CameraItem item, CameraHolder holder, ItemStack stack) {
        return !isOnCooldown(item, holder, stack)
                && !item.getTimer().isTicking(holder, stack)
                && !item.getShutter().isOpen(stack)
                && Attachment.FILM.map(stack, FilmRollItem::canAddFrame).orElse(false)
                && CameraInstances.canReleaseShutter(CameraId.ofStack(stack));
    }

    public static boolean isOnCooldown(CameraItem item, CameraHolder holder, ItemStack stack) {
        if (holder.asHolderEntity() instanceof Player player) {
            return player.getCooldowns().isOnCooldown(item);
        } else if (holder instanceof CameraStandEntity stand) {
            return stand.isOnCooldown();
        }
        return false;
    }

    public static float getCooldownPercent(CameraItem item, CameraHolder holder, ItemStack stack) {
        if (holder.asHolderEntity() instanceof Player player) {
            return player.getCooldowns().isOnCooldown(stack.getItem())
                    ? player.getCooldowns().getCooldownPercent(stack.getItem(), 0)
                    : 0;
        } else if (holder instanceof CameraStandEntity stand) {
            return stand.isOnCooldown()
                    ? stand.getCooldownPercent()
                    : 0;
        }
        return 0;
    }

    public static @NotNull InteractionResult release(CameraItem item, CameraHolder holder, ItemStack stack) {
        Entity entity = holder.asHolderEntity();
        Level level = entity.level();

        Sound.playSided(entity, item.getReleaseButtonSound(), entity.getSoundSource(), 0.3f, 1f, 0.1f);

        if (level.isClientSide() || !canTakePhoto(item, holder, stack)) {
            return InteractionResult.CONSUME.heldItemTransformedTo(stack);
        }

        if (item.getTimer().getEndTick(stack) != level.getGameTime()) {
            SelfTimer selfTimer = CameraSettings.SELF_TIMER.getOrDefault(stack);
            if (selfTimer != SelfTimer.OFF) {
                item.getTimer().set(holder, stack, selfTimer.getTicks());
                return InteractionResult.CONSUME.heldItemTransformedTo(stack);
            }
        }

        holder.getServerPlayerExecutingExposure().ifPresentOrElse(
                player -> CameraItemCapture.takePhoto(item, holder, player, stack),
                () -> Exposure.LOGGER.error("Cannot start capture: photographer '{}' does not have valid executing player.", holder));

        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }
}
