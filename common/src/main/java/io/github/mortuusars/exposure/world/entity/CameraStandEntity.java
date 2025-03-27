package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.Config;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
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
    protected static final EntityDataAccessor<Boolean> DATA_ID_MALFUNCTIONED =
            SynchedEntityData.defineId(CameraStandEntity.class, EntityDataSerializers.BOOLEAN);

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
        builder.define(DATA_ID_MALFUNCTIONED, false);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Cooldown", getCooldown());
        tag.putBoolean("Malfunctioned", isMalfunctioned());
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
        setCooldown(tag.getInt("Cooldown"));
        setMalfunctioned(tag.getBoolean("Malfunctioned"));
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

    public Optional<? extends Player> getClosestPlayerInRange() {
        return level().players().stream()
                .min(Comparator.comparingDouble(player -> player.distanceTo(this)))
                .filter(this::isInRangeForPhoto);
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

    public boolean isMalfunctioned() {
        return getEntityData().get(DATA_ID_MALFUNCTIONED);
    }

    public void setMalfunctioned(boolean malfunctioned) {
        getEntityData().set(DATA_ID_MALFUNCTIONED, malfunctioned);
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

    public boolean isInRangeForPhoto(Player player) {
        return player.distanceTo(this) < Config.Server.CAMERA_STAND_WORKING_RANGE.get();
    }

    public Optional<Player> getPlayerToMakePhoto() {
        if (Config.Server.CAMERA_STAND_FALLBACK_TO_OTHER_PLAYERS.get()) {
            return getOwnerPlayer().filter(this::isInRangeForPhoto).or(this::getClosestPlayerInRange);
        } else {
            return getOwnerPlayer().filter(this::isInRangeForPhoto);
        }
    }

    @Override
    public @NotNull Player getPlayerExecutingExposure() {
        return getPlayerToMakePhoto().orElseThrow();
    }

    @Override
    public Optional<Player> getPlayerAwardedForExposure() {
        return getOwnerPlayer();
    }

    @Override
    public @NotNull Entity getExposureAuthorEntity() {
        return getOwnerPlayer().or(this::getClosestPlayerInRange).map(pl -> (Entity) pl).orElse(this);
    }

    @Override
    public Optional<CameraOperator> getExposureCameraOperator() {
        return getOperator();
    }

    // -- Interact

    public boolean isInInteractionRange(LivingEntity entity) {
        double range = entity.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return this.getBoundingBox().distanceToSqr(entity.getEyePosition()) < range * range;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
        if (!canUse(player)) {
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (isMalfunctioned()) {
            if (!level().isClientSide) {
                setMalfunctioned(false);
                player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.malfunction_fixed"), true);
                playSound(SoundEvents.SMITHING_TABLE_USE, 0.9f, 1.3f);
                showRepairingParticles();
            }

            return InteractionResult.SUCCESS;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack cameraStack = getCamera();

        if (handStack.is(Items.DEBUG_STICK) && !cameraStack.isEmpty()) {
            malfunction();
            return InteractionResult.SUCCESS;
        }

        if (cameraStack.isEmpty() && handStack.getItem() instanceof CameraItem) {
            setCamera(handStack);
            player.setItemInHand(hand, ItemStack.EMPTY);

            if (!level().isClientSide) {
                playCameraSetSound();
            }

            // Set initial camera direction.
            if (getYRot() == 0 && getXRot() == 0) { // If rotations are at 0 - player likely hasn't chosen a direction yet.
                setYRot(player.getYRot());
            }

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
            player.displayClientMessage(Component.translatable("gui.exposure.camera_stand.error.in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ItemStack cameraStack = getCamera();

        if (cameraStack.isEmpty() || !(cameraStack.getItem() instanceof CameraItem cameraItem))
            return InteractionResult.FAIL;

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

            PlatformHelper.openMenu(serverPlayer, menuProvider,
                    buffer -> buffer.writeInt(getId()));
        }

        cameraItem.setDisassembled(cameraStack, true);
        Sound.play(player, Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get(), SoundSource.PLAYERS, 0.9f, 0.9f, 0.2f);

        return InteractionResult.SUCCESS;
    }

    public void startControlling(Player player, CameraItem cameraItem) {
        cameraItem.activateOnStand(player, getCamera(), this);
        setOperator(player);

        if (getOwnerPlayerId().equals(Util.NIL_UUID)) {
            setOwnerPlayer(player);
        }

        if (player.level().isClientSide) {
            CameraClient.setCameraEntity(this);
            Minecrft.stopPlayerMovement();
        }
    }

    public void stopControlling() {
        if (getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
            cameraItem.deactivate(this, getCamera());
        }
        if (operator() instanceof Player player && level().isClientSide) {
            CameraClient.setCameraEntity(player);
        }
        setOperator(null);
    }

    public void release() {
        if (!isMalfunctioned() && !isOnCooldown() && getCamera().getItem() instanceof CameraItem cameraItem) {
            getPlayerToMakePhoto().ifPresentOrElse(
                    player -> cameraItem.release(this, getCamera()),
                    this::malfunction);
        }
    }

    protected void malfunction() {
        if (getCamera().isEmpty()) return;
        if (Config.Server.CAMERA_STAND_RANGE_MALFUNCTION.isFalse()) return;
        stopControlling();
        setMalfunctioned(true);
        playSound(Exposure.SoundEvents.CAMERA_GENERIC_CLICK.get());
    }

    // --

    @Override
    public void tick() {
        super.tick();
        travel();
        checkForBoats();

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
            stopControlling();
            return;
        }

        @Nullable CameraOperator operator = operator();
        if (operator == null) {
            if (!level().isClientSide && getCamera().getItem() instanceof CameraItem cameraItem && cameraItem.isActive(getCamera())) {
                cameraItem.deactivate(this, getCamera());
            }
            return;
        }

        if (!isInInteractionRange(operator.asOperatorEntity())) {
            stopControlling();
        }
    }

    protected void travel() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.applyGravity();
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.1F) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.1F) {
            this.setUnderLavaMovement();
        }

        if (this.level().isClientSide) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f = 0.98F;
            if (this.onGround()) {
                f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
            if (this.onGround()) {
                Vec3 vec32 = this.getDeltaMovement();
                if (vec32.y < 0.0) {
                    this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
                }
            }
        }
    }

    protected void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.9F, vec3.y * 0.23F, vec3.z * 0.9F);
    }

    protected void setUnderLavaMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.5F, vec3.y * 0.1F, vec3.z * 0.5F);
    }

    protected void checkForBoats() {
        if (!level().isClientSide && !isPassenger()) {
            List<Entity> boats = level().getEntities(this, getBoundingBox().inflate(0.4F, 0.2F, 0.4F), e -> e instanceof Boat);
            for (Entity entity : boats) {
                Boat boat = ((Boat) entity);
                if (boat.getPassengers().size() < 2 && boat.hasEnoughSpaceFor(this)) {
                    this.startRiding(boat);
                }
            }
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
                playCameraRemoveSound();
            }
            setCamera(ItemStack.EMPTY);

            if (source.isCreativePlayer()) {
                return true; // Prevent discard at the same hit as removing the camera.
            }

            amount = 1.0f; // Prevent one-hit harvesting.
        }

        if (!level().isClientSide) {
            setHurtDir(-getHurtDir());
            setHurtTime(10);
            markHurt();
            setDamage(getDamage() + amount * 10.0F);
            gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            playHitSound();

            if (source.isCreativePlayer()) {
                discard();
                showBreakingParticles();
                playBreakSound();
            } else if (getDamage() > 10.0F) {
                destroy(source);
                showBreakingParticles();
                playBreakSound();
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
            this.spawnAtLocation(itemStack, 0.5f);
        }
    }

    protected void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level())
                    .sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
                            this.getX(),
                            this.getY(0.25f),
                            this.getZ(),
                            6,
                            (this.getBbWidth() / 6.0F),
                            (this.getBbHeight() / 6.0F),
                            (this.getBbWidth() / 6.0F),
                            0.05
                    );
        }
    }

    protected void showRepairingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel) this.level())
                    .sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HOPPER.defaultBlockState()),
                            this.getX(),
                            this.getY(0.75f),
                            this.getZ(),
                            4,
                            (this.getBbWidth() / 8.0F),
                            (this.getBbHeight() / 8.0F),
                            (this.getBbWidth() / 8.0F),
                            0.05
                    );
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

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getDropItem());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    public void playPlaceSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_PLACE.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playHitSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_HIT.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playBreakSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_BREAK.get(), this.getSoundSource(), 1.0F, 1.0F);
    }

    public void playCameraSetSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_SET_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
    }

    public void playCameraRemoveSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                Exposure.SoundEvents.CAMERA_STAND_REMOVE_CAMERA.get(), this.getSoundSource(), 0.8F, 1.0F);
    }
}
