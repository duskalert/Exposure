package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class CameraStandEntity extends Entity implements CameraHolder {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<ItemStack> DATA_ID_CAMERA =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<Integer> DATA_ID_COOLDOWN =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.INT);

    protected CameraStandRedstoneControl redstoneControl = new CameraStandRedstoneControl(this);
    protected UUID ownerPlayerId = Util.NIL_UUID;

    @Nullable
    protected CameraOperator operator;

    public CameraStandEntity(EntityType<? extends CameraStandEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT, 0);
        builder.define(DATA_ID_HURTDIR, 1);
        builder.define(DATA_ID_DAMAGE, 0.0F);
        builder.define(DATA_ID_CAMERA, ItemStack.EMPTY);
        builder.define(DATA_ID_COOLDOWN, 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!getCamera().isEmpty()) {
            tag.put("Camera", getCamera().save(registryAccess()));
        }

        redstoneControl.save(tag);

        if (!ownerPlayerId.equals(Util.NIL_UUID)) {
            tag.putUUID("Owner", ownerPlayerId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setCamera(ItemStack.parseOptional(registryAccess(), tag.getCompound("Camera")));

        redstoneControl.load(tag);

        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            ownerPlayerId = tag.getUUID("Owner");
        }
    }

    // -- Camera

    public ItemStack getCamera() {
        return getEntityData().get(DATA_ID_CAMERA);
    }

    public void setCamera(ItemStack cameraStack) {
        getEntityData().set(DATA_ID_CAMERA, cameraStack);
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

    public int getCooldown() {
        return getEntityData().get(DATA_ID_COOLDOWN);
    }

    public void setCooldown(int cooldown) {
        getEntityData().set(DATA_ID_COOLDOWN, cooldown);
    }

    public boolean isOnCooldown() {
        return getCooldown() > 0;
    }

    // -- Operator

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

    // -- Interact

    public boolean isInInteractionRange(LivingEntity entity) {
        // Slightly smaller than entity's interaction range because it's mismatched
        // (probably because interactions are counting bounding box, not position, idk)
        return entity.distanceTo(this) <= entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - 0.5f;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (!canUse(player)) {
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

        startControlling(player, cameraItem);
        return InteractionResult.CONSUME; // To not cause arm swing, which causes viewfinder use/attack animation.
    }

    protected InteractionResult handleSneakInteraction(Player player, InteractionHand hand, CameraItem cameraItem, ItemStack cameraStack, ItemStack handStack) {
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

                return InteractionResult.FAIL;
            }
        }

        return openAttachmentsMenu(player, hand);
    }

    public boolean canUse(Player player) {
        return (operator() == null || player.equals(operator())) || level().players().stream()
                .filter(pl -> !pl.equals(player))
                .noneMatch(pl -> pl.containerMenu instanceof CameraOnStandAttachmentsMenu);
    }

    public InteractionResult openAttachmentsMenu(Player player, InteractionHand hand) {
        if (!canUse(player)) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use"), true);
            return InteractionResult.FAIL;
        }

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

    public void startControlling(Player player, CameraItem cameraItem) {
        cameraItem.activateOnStand(player, getCamera(), this);
        setOperator(player);
        if (player.level().isClientSide) {
            CameraClient.setCameraEntity(this);
            Minecrft.stopPlayerMovement();
        }
    }

    public void stopControlling(@Nullable CameraOperator operator) {
        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
            cameraItem.deactivate(this, getCamera());
        }
        if (operator instanceof Player player && level().isClientSide) {
            CameraClient.setCameraEntity(player);
        }
        setOperator(null);
    }

    public void release() {
        if (!isOnCooldown() && getCamera().getItem() instanceof CameraItem cameraItem) {
            cameraItem.release(this, getCamera());
        }
    }

    // --

    @Override
    public void tick() {
        super.tick();

        move();

        if (getHurtTime() > 0) {
            setHurtTime(getHurtTime() - 1);
        }

        if (getDamage() > 0.0F) {
            setDamage(getDamage() - 1.0F);
        }

        int cooldown = getCooldown();
        if (cooldown > 0) {
            setCooldown(cooldown - 1);
        }

        redstoneControl.tick();

        if (getCamera().getItem() instanceof CameraItem cameraItem) {
            cameraItem.tick(this, getCamera());
        }

        if (!isCameraActive() && operator() != null) {
            stopControlling(operator());
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

    protected void move() {
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
    public void push(Entity entity) {
        if (entity instanceof Boat) {
            this.startRiding(entity);
        }
    }

    // -- Hurt

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isRemoved()) return true;
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

            if (source.isCreativePlayer()) {
                return true; // Prevent discard at the same time as removing the camera.
            }
        }

        if (!level().isClientSide) {
            setHurtDir(-getHurtDir());
            setHurtTime(10);
            markHurt();
            setDamage(getDamage() + amount * 10.0F);
            gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

            if (source.isCreativePlayer()) {
                discard();
            } else if (getDamage() > 10.0F) {
                destroy(source);
            }
        }

        return true;
    }

    public void setHurtTime(int hurtTime) {
        this.entityData.set(DATA_ID_HURT, hurtTime);
    }

    public void setHurtDir(int hurtDir) {
        this.entityData.set(DATA_ID_HURTDIR, hurtDir);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_ID_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(DamageSource source) {
        this.destroy(this.getDropItem());
    }

    public void destroy(Item dropItem) {
        this.kill();
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = new ItemStack(dropItem);
            itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            this.spawnAtLocation(itemStack);
        }
    }

    protected Item getDropItem() {
        return Exposure.Items.CAMERA_STAND.get();
    }

    // --

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}
