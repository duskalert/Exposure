package io.github.mortuusars.exposure.world.item.camcom;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.util.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class CameraItem extends Item {
    public static final int BASE_COOLDOWN = 2;
    public static final int PROJECT_COOLDOWN = 20;

    protected final Shutter shutter;
    protected final Timer timer;
    protected final Flash flash;
    protected final List<Attachment<?>> attachments;
    protected final List<ShutterSpeed> availableShutterSpeeds;

    public CameraItem(Properties properties) {
        super(properties);
        this.shutter = createShutter();
        this.timer = createTimer();
        this.flash = createFlash();
        this.attachments = defineAttachments();
        this.availableShutterSpeeds = defineShutterSpeeds();

        shutter.onOpen(this::onShutterOpen);
        shutter.onClosed(this::onShutterClosed);
    }

    protected Shutter createShutter() {
        return new Shutter();
    }

    protected Timer createTimer() {
        return new Timer();
    }

    protected Flash createFlash() {
        return new Flash();
    }

    protected @NotNull List<Attachment<?>> defineAttachments() {
        return List.of(Attachment.FILM, Attachment.FLASH, Attachment.LENS, Attachment.FILTER);
    }

    protected List<ShutterSpeed> defineShutterSpeeds() {
        return List.of(
                new ShutterSpeed("1/500"),
                new ShutterSpeed("1/250"),
                new ShutterSpeed("1/125"),
                new ShutterSpeed("1/60"),
                new ShutterSpeed("1/30"),
                new ShutterSpeed("1/15"),
                new ShutterSpeed("1/8"),
                new ShutterSpeed("1/4"),
                new ShutterSpeed("1/2"),
                new ShutterSpeed("1\"")
        );
    }

    public boolean hasAttachmentsMenu() {
        return true;
    }

    // --

    public Shutter getShutter() {
        return shutter;
    }

    public Timer getTimer() {
        return timer;
    }

    public Flash getFlash() {
        return flash;
    }

    public List<ShutterSpeed> getAvailableShutterSpeeds() {
        return availableShutterSpeeds;
    }

    public List<Attachment<?>> getAttachments() {
        return attachments;
    }

    public Attachment<?> getFilmAttachment() {
        return Attachment.FILM;
    }

    public @NotNull FilmProperties getFilmProperties(ItemStack stack) {
        ItemStack filmStack = getFilmAttachment().get(stack).getForReading();
        if (!(filmStack.getItem() instanceof SensitiveFilmItem filmItem)) {
            throw new IllegalStateException("Cannot take a photo without SensitiveFilmItem in the camera. stack: " + stack);
        }
        return filmItem.getFilmProperties(filmStack);
    }

    public SoundEvent getViewfinderOpenSound() {
        return Exposure.SoundEvents.VIEWFINDER_OPEN.get();
    }

    public SoundEvent getViewfinderCloseSound() {
        return Exposure.SoundEvents.VIEWFINDER_CLOSE.get();
    }

    public SoundEvent getReleaseButtonSound() {
        return Exposure.SoundEvents.CAMERA_RELEASE_BUTTON_CLICK.get();
    }

    public Identifier getCaptureType(ItemStack stack) {
        return CaptureType.CAMERA;
    }

    public double getSelfieCameraDistance(ItemStack stack) {
        return Config.Server.SELFIE_CAMERA_DISTANCE.get();
    }

    public double getYPositionOffset(ItemStack stack) {
        return Config.Server.WAIST_LEVEL_VIEWFINDER.get() ? -0.35 : 0.0;
    }

    public float getScaleOnStand() { return CameraItemGetters.getScaleOnStand(this); }

    public float getCropFactor() { return CameraItemGetters.getCropFactor(this); }

    public FocalRange getFocalRange(RegistryAccess registryAccess, ItemStack stack) { return CameraItemGetters.getFocalRange(this, registryAccess, stack); }

    public double getFov(Level level, ItemStack stack) { return CameraItemGetters.getFov(this, level, stack); }

    /**
     * Fov of what's seen when looking through viewfinder.
     */
    public double getViewfinderFov(Level level, ItemStack stack) { return CameraItemGetters.getViewfinderFov(this, level, stack); }

    // --

    public PointOfView getPointOfView(CameraHolder holder, ItemStack stack) { return CameraItemCapture.getPointOfView(this, holder, stack); }

    public Optional<Filter> getFilter(RegistryAccess registryAccess, ItemStack stack) { return CameraItemGetters.getFilter(this, registryAccess, stack); }

    public Optional<Identifier> getFilterShaderLocation(RegistryAccess registryAccess, ItemStack stack) { return CameraItemGetters.getFilterShaderLocation(this, registryAccess, stack); }

    protected Optional<ColorChannel> getChromaticChannel(ItemStack stack) { return CameraItemGetters.getChromaticChannel(this, stack); }

    protected Optional<Projection> getProjection(ItemStack stack) { return CameraItemGetters.getProjection(this, stack); }

    // --

    public CameraId getOrCreateId(ItemStack stack) { return CameraItemGetters.getOrCreateId(this, stack); }

    public boolean isInSelfieMode(ItemStack stack) { return CameraItemGetters.isInSelfieMode(this, stack); }

    public boolean isActive(ItemStack stack) { return CameraItemGetters.isActive(this, stack); }

    public void setActive(ItemStack stack, boolean active) { CameraItemGetters.setActive(this, stack, active); }

    public boolean isDisassembled(ItemStack stack) { return CameraItemGetters.isDisassembled(this, stack); }

    public void setDisassembled(ItemStack stack, boolean disassembled) { CameraItemGetters.setDisassembled(this, stack, disassembled); }

    public long getLastActionTime(ItemStack stack) { return CameraItemGetters.getLastActionTime(this, stack); }

    public void setLastActionTime(ItemStack stack, long lastActionTime) { CameraItemGetters.setLastActionTime(this, stack, lastActionTime); }

    public void actionPerformed(ItemStack stack, CameraHolder holder) { CameraItemInteraction.actionPerformed(this, stack, holder); }
    public @NotNull InteractionResult activateInHand(Player player, ItemStack stack, @NotNull InteractionHand hand) { return CameraItemInteraction.activateInHand(this, player, stack, hand); }
    public @NotNull InteractionResult activateOnStand(Player player, ItemStack stack, CameraStandEntity cameraStand) { return CameraItemInteraction.activateOnStand(this, player, stack, cameraStand); }
    public @NotNull InteractionResult activate(Entity entity, ItemStack stack) { return CameraItemInteraction.activate(this, entity, stack); }
    public @NotNull InteractionResult deactivate(Entity entity, ItemStack stack) { return CameraItemInteraction.deactivate(this, entity, stack); }
    public int calculateCooldownAfterShot(ItemStack stack, CaptureParameters captureParameters) { return CameraItemInteraction.calculateCooldownAfterShot(this, stack, captureParameters); }

    // --

    public boolean isBarVisible(@NotNull ItemStack stack) {
        return Config.Client.CAMERA_SHOW_FILM_BAR_ON_ITEM.get()
                && Attachment.FILM.map(stack, FilmRollItem::isBarVisible).orElse(false);
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarWidth).orElse(0);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        return Attachment.FILM.map(stack, FilmRollItem::getBarColor).orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if (Config.Client.CAMERA_SHOW_FILM_FRAMES_IN_TOOLTIP.get()) {
            Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
                int exposed = filmItem.getStoredFramesCount(filmStack);
                int max = filmItem.getMaxFrameCount(filmStack);
                components.add(Component.translatable("item.exposure.camera.tooltip.film_roll_frames", exposed, max));
            });
        }

        if (Config.Client.CAMERA_SHOW_TOOLTIP_DETAILS.get()) {
            boolean rClickAttachments = Config.Server.CAMERA_GUI_RIGHT_CLICK_OPEN_ATTACHMENTS.get();
            boolean rClickHotswap = Config.Server.CAMERA_GUI_RIGHT_CLICK_HOTSWAP.get();

            if (rClickAttachments || rClickHotswap) {
                if (Screen.hasShiftDown()) {
                    if (rClickAttachments)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_attachments_screen"));
                    if (rClickHotswap)
                        components.add(Component.translatable("item.exposure.camera.tooltip.details_hotswap"));
                } else
                    components.add(Component.translatable("tooltip.exposure.hold_for_details"));
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) { return CameraItemInteraction.overrideOtherStackedOnMe(this, stack, otherStack, slot, action, player, access); }
    public InteractionResult handleStandSneakInteraction(CameraStandEntity stand, Player player, InteractionHand hand, ItemStack cameraStack) { return CameraItemInteraction.handleStandSneakInteraction(this, stand, player, hand, cameraStack); }
    protected InteractionResult hotswap(CameraHolder holder, ItemStack stack, ItemStack otherStack, SlotAccess access) { return CameraItemInteraction.hotswap(this, holder, stack, otherStack, access); }
    public InteractionResult openCameraAttachments(@NotNull Player player, ItemStack stack, boolean openedFromGUI) { return CameraItemInteraction.openCameraAttachments(this, player, stack, openedFromGUI); }
    public InteractionResult openCameraAttachments(@NotNull Player player, int slotIndex, boolean openedFromGUI) { return CameraItemInteraction.openCameraAttachments(this, player, slotIndex, openedFromGUI); }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) { CameraItemInteraction.inventoryTick(this, stack, level, entity, slotId, isSelected); }
    public boolean tick(CameraHolder holder, ItemStack stack) { return CameraItemInteraction.tick(this, holder, stack); }
    protected void grabAttentionOfNearbyMobs(CameraHolder holder, ItemStack stack) { CameraItemInteraction.grabAttentionOfNearbyMobs(this, holder, stack); }
    protected boolean canGrabAttentionOf(CameraHolder holder, Mob mob) { return CameraItemInteraction.canGrabAttentionOf(this, holder, mob); }
    @Override public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) { return CameraItemInteraction.use(this, level, player, hand); }
    public boolean canTakePhoto(CameraHolder holder, ItemStack stack) { return CameraItemInteraction.canTakePhoto(this, holder, stack); }
    public boolean isOnCooldown(CameraHolder holder, ItemStack stack) { return CameraItemInteraction.isOnCooldown(this, holder, stack); }
    public float getCooldownPercent(CameraHolder holder, ItemStack stack) { return CameraItemInteraction.getCooldownPercent(this, holder, stack); }
    public @NotNull InteractionResult release(CameraHolder holder, ItemStack stack) { return CameraItemInteraction.release(this, holder, stack); }
    protected void takePhoto(CameraHolder holder, ServerPlayer executingPlayer, ItemStack stack) { CameraItemCapture.takePhoto(this, holder, executingPlayer, stack); }
    protected void onShutterOpen(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) { CameraItemCapture.onShutterOpen(this, holder, serverLevel, stack); }
    protected void onShutterClosed(CameraHolder holder, ServerLevel serverLevel, ItemStack stack) { CameraItemCapture.onShutterClosed(this, holder, serverLevel, stack); }
    public void addNewFrame(ServerLevel level, CameraHolder holder, ItemStack stack, CaptureParameters captureParameters) { CameraItemCapture.addNewFrame(this, level, holder, stack, captureParameters); }
    public Frame createFrame(CameraHolder holder, ServerLevel level, ItemStack stack, CaptureParameters captureParameters, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) { return CameraItemCapture.createFrame(this, holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame); }
    protected void addFrameExtraData(CameraHolder holder, ServerLevel level, ItemStack camera, CaptureParameters params, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) { CameraItemCapture.addFrameExtraData(this, holder, level, camera, params, positionsInFrame, entitiesInFrame, data); }
    public List<BlockPos> getPositionsInFrame(CameraHolder cameraHolder, PointOfView pov, double fov) { return CameraItemCapture.getPositionsInFrame(this, cameraHolder, pov, fov); }
    public void addFrameToFilm(ItemStack stack, Frame frame) { CameraItemCapture.addFrameToFilm(this, stack, frame); }
    public void onFrameAdded(CameraHolder holder, ServerLevel level, ItemStack stack, Frame frame, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) { CameraItemCapture.onFrameAdded(this, holder, level, stack, frame, positionsInFrame, entitiesInFrame); }
    protected void entityCaptured(CameraHolder cameraHolder, ItemStack stack, LivingEntity entity) { CameraItemCapture.entityCaptured(this, cameraHolder, stack, entity); }
    public void handleProjectionResult(ServerLevel level, CameraHolder holder, ItemStack stack, CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error) { CameraItemCapture.handleProjectionResult(this, level, holder, stack, projectionState, error); }
    protected int getMatchingSlotInInventory(Inventory inventory, ItemStack stack) { return CameraItemCapture.getMatchingSlotInInventory(this, inventory, stack); }
    protected void testEntitiesInFrame(ItemStack stack, Level level, CameraHolder holder) { CameraItemCapture.testEntitiesInFrame(this, stack, level, holder); }
    protected void testPositionsInFrame(ItemStack stack, Level level, Player player) { CameraItemCapture.testPositionsInFrame(this, stack, level, player); }
    public static int getGlassTintColor(ItemStack stack, int tintIndex) { return CameraItemCapture.getGlassTintColor(stack, tintIndex); }



}