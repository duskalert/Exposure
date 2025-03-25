package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import io.github.mortuusars.exposure.world.inventory.CameraOnStandAttachmentsMenu;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class CameraStandEntity extends Entity implements CameraHolder {
    protected static final EntityDataAccessor<ItemStack> DATA_CAMERA =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.ITEM_STACK);

    protected UUID ownerPlayerId = Util.NIL_UUID;

    @Nullable
    protected CameraOperator operator;
    protected boolean receivedRedstonePulse = false;
    protected int redstoneReleaseDelay = 0;

    public CameraStandEntity(EntityType<? extends CameraStandEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return super.canRide(vehicle);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void push(Entity entity) {
        if (entity instanceof Boat) {
            this.startRiding(entity);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CAMERA, ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!getCamera().isEmpty()) {
            tag.put("Camera", getCamera().save(registryAccess()));
        }

        if (!ownerPlayerId.equals(Util.NIL_UUID)) {
            tag.putUUID("Owner", ownerPlayerId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setCamera(ItemStack.parseOptional(registryAccess(), tag.getCompound("Camera")));

        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            ownerPlayerId = tag.getUUID("Owner");
        }
    }

    // --

    public ItemStack getCamera() {
        return getEntityData().get(DATA_CAMERA);
    }

    public void setCamera(ItemStack cameraStack) {
        getEntityData().set(DATA_CAMERA, cameraStack);
    }

    public boolean isCameraActive() {
        return getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera());
    }

    public Optional<Player> getOwnerPlayer() {
        return Optional.ofNullable(level().getPlayerByUUID(ownerPlayerId));
    }

    public UUID getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public void setOwnerPlayer(Player player) {
        this.ownerPlayerId = player.getUUID();
    }

    public Optional<? extends Player> getClosestPlayer() {
        return level().players().stream().min(Comparator.comparingDouble(player -> player.distanceTo(this)));
    }

    // -- Interact

    public boolean isInInteractionRange(LivingEntity entity) {
        // Slightly smaller than entity's interaction range because it's mismatched
        // (probably because interactions are counting bounding box, not position, idk)
        return entity.distanceTo(this) <= entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - 0.5f;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (operator() != null) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use"), true);
            return InteractionResult.FAIL;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack cameraStack = getCamera();

        if (cameraStack.isEmpty() && handStack.getItem() instanceof CameraItem cameraItem) {
            setCamera(handStack);
            player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }

        if (cameraStack.isEmpty()) return InteractionResult.PASS;
        if (!(cameraStack.getItem() instanceof CameraItem cameraItem)) return InteractionResult.PASS;

        if (player.isSecondaryUseActive()) {
            return handleSneakInteraction(player, hand, cameraItem, cameraStack, handStack);
        }

        activate(player, cameraItem);
        return InteractionResult.CONSUME; // To not cause arm swing, which causes viewfinder use/attack animation.
    }

    protected InteractionResult handleSneakInteraction(Player player, InteractionHand hand, CameraItem cameraItem, ItemStack cameraStack, ItemStack handStack) {
        boolean isInUse = player.level().players().stream()
                .filter(pl -> !pl.equals(player))
                .anyMatch(pl -> pl.containerMenu instanceof CameraOnStandAttachmentsMenu);
        if (isInUse) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use"), true);
            return InteractionResult.FAIL;
        }

        if (handStack.isEmpty()) {
            return openAttachmentsMenu(player, hand);
        }

        for (Attachment<?> attachment : cameraItem.getAttachments()) {
            if (attachment.matches(handStack)) {

                if (attachment.get(cameraStack).isEmpty()) {
                    attachment.set(cameraStack, handStack.split(1));
                    attachment.playInsertSoundSided(player, this);
                    return InteractionResult.SUCCESS;
                }

                if (handStack.getCount() == 1) {
                    player.setItemInHand(hand, attachment.get(cameraStack).getCopy());
                    attachment.set(cameraStack, handStack);
                    attachment.playInsertSoundSided(player, this);
                    return InteractionResult.SUCCESS;
                }

                playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get());
                return InteractionResult.FAIL;
            }
        }

        return openAttachmentsMenu(player, hand);
    }

    public InteractionResult openAttachmentsMenu(Player player, InteractionHand hand) {
        ItemStack cameraStack = getCamera();

        if (cameraStack.isEmpty() || !(cameraStack.getItem() instanceof CameraItem cameraItem)) return InteractionResult.FAIL;

        if (cameraItem.getShutter().isOpen(cameraStack)) {
            player.displayClientMessage(Component.translatable("item.exposure.camera.camera_attachments.fail.shutter_open")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        cameraItem.getOrCreateID(cameraStack);

        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return cameraStack.get(DataComponents.CUSTOM_NAME) != null
                            ? cameraStack.getHoverName() : Component.translatable("container.exposure.camera");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                    return new CameraOnStandAttachmentsMenu(containerId, playerInventory, CameraStandEntity.this);
                }
            };

            PlatformHelper.openMenu(serverPlayer, menuProvider, buffer -> {
                buffer.writeInt(getId());
            });
        }

        cameraItem.setDisassembled(cameraStack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResult.SUCCESS;
    }

    public void activate(Player player, CameraItem cameraItem) {
        cameraItem.activateOnStand(player, getCamera(), this);
        setOperator(player);
        if (player.level().isClientSide) {
            CameraClient.setCameraEntity(this);
            Minecrft.stopPlayerMovement();
        }
    }

    // --

    @Override
    public void tick() {
        super.tick();

        move();

        if (redstoneReleaseDelay > 0) {
            redstoneReleaseDelay--;
        }
        int signalStrength = level().getBestNeighborSignal(blockPosition());
        boolean newPulse = !receivedRedstonePulse && signalStrength > 0;
        receivedRedstonePulse = signalStrength > 0;

        if (getCamera().getItem() instanceof CameraItem cameraItem) {
            cameraItem.tick(this, getCamera());

            if (newPulse && redstoneReleaseDelay <= 0) {
                cameraItem.release(this, getCamera());
            }
        }

        if (!isCameraActive() && operator() != null) {
            if (operator() instanceof Player player && level().isClientSide) {
                CameraClient.setCameraEntity(player);
            }

            setOperator(null);
            return;
        }

        @Nullable CameraOperator operator = operator();
        if (operator == null) {
            if (!level().isClientSide && getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
                cameraItem.deactivate(this, getCamera());
            }
            return;
        }

        LivingEntity operatorEntity = operator.asOperatorEntity();
        if (!isInInteractionRange(operatorEntity)) {
            stopControlling(operator);
        }
    }

    public void stopControlling(@NotNull CameraOperator operator) {
        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
            cameraItem.deactivate(this, getCamera());
        }
        if (operator instanceof Player player && level().isClientSide) {
            CameraClient.setCameraEntity(player);
        }
        setOperator(null);
    }

    private void move() {
        // Apply gravity
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0)); // Simulate gravity
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.9, 0, 0.9)); // Apply friction on ground
        }

        // Apply velocity (movement)
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Slow down over time like armor stand
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));

        // If speed is very low, stop movement completely
        if (this.getDeltaMovement().lengthSqr() < 0.0001) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        markHurt();

        if (!getCamera().isEmpty()) {
            if (!level().isClientSide) {
                @Nullable ItemEntity itemEntity = spawnAtLocation(getCamera(), getEyeHeight());
                if (itemEntity != null) {
                    itemEntity.setPickUpDelay(5);
                }
                playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM);
            }
            setCamera(ItemStack.EMPTY);
            return true;
        }

        if (!level().isClientSide) {
            if (!source.isCreativePlayer()) {
                spawnAtLocation(Exposure.Items.CAMERA_STAND.get());
            }
            remove(RemovalReason.KILLED);
            playSound(SoundEvents.ARMOR_STAND_BREAK);
        }

        return true;
    }

    // --

    public @Nullable CameraOperator operator() {
        return operator;
    }

    public Optional<CameraOperator> getOperator() {
        return Optional.ofNullable(operator);
    }

    public void setOperator(@Nullable CameraOperator operator) {
        this.operator = operator;
    }

    // -- Holder

    @Override
    public @NotNull Player getPlayerExecutingExposure() {
        //TODO: Disable closest player.
        //TODO: Check for players before calling this method.
        return getOwnerPlayer().or(this::getClosestPlayer).orElseThrow();
    }

    @Override
    public Optional<Player> getPlayerAwardedForExposure() {
        return getOwnerPlayer();
    }

    @Override
    public @NotNull Entity getExposureAuthorEntity() {
        return getOwnerPlayer().or(this::getClosestPlayer).map(pl -> (Entity)pl).orElse(this);
    }

    @Override
    public Optional<CameraOperator> getExposureCameraOperator() {
        return getOperator();
    }
}
