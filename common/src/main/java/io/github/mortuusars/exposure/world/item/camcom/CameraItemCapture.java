package io.github.mortuusars.exposure.world.item.camcom;

import io.github.mortuusars.exposure.*;
import io.github.mortuusars.exposure.data.*;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShutterOpenedS2CP;
import io.github.mortuusars.exposure.server.CameraInstance;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.util.*;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.camera.*;
import io.github.mortuusars.exposure.world.camera.capture.Projection;
import io.github.mortuusars.exposure.world.camera.capture.CaptureType;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.frame.*;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import io.github.mortuusars.exposure.world.item.component.StoredItemStack;
import io.github.mortuusars.exposure.world.level.LevelUtil;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CameraItemCapture {

    public static void takePhoto(CameraItem item, CameraHolder holder, ServerPlayer executingPlayer, ItemStack stack) {
        ServerLevel level = (ServerLevel) executingPlayer.level();
        Entity entity = holder.asHolderEntity();

        ShutterSpeed shutterSpeed = CameraSettings.SHUTTER_SPEED.getOrDefault(stack);

        item.getShutter().open(holder, level, stack, shutterSpeed);

        CameraId cameraId = item.getOrCreateId(stack);
        String exposureId = ExposureIdentifier.createId(executingPlayer);
        int lightLevel = LevelUtil.getLightLevelAt(level, entity.blockPosition());
        boolean flash = item.getFlash().isAvailable(stack)
                && item.getFlash().shouldFire(stack, lightLevel)
                && item.getFlash().fire(holder, level, stack);

        CaptureParameters captureParameters = new CaptureParameters.Builder(exposureId)
                .setCameraID(cameraId)
                .setCameraHolder(holder)
                .setFov(item.getFov(level, stack))
                .setCropFactor(item.getCropFactor())
                .setFilter(item.getFilterShaderLocation(level.registryAccess(), stack).orElse(null))
                .setProjection(item.getProjection(stack))
                .setChromaticChannel(item.getChromaticChannel(stack))
                .setFilmProperties(item.getFilmProperties(stack))
                .extraData(CaptureParameters.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                .extraData(CaptureParameters.FLASH, flash)
                .extraData(CaptureParameters.LIGHT_LEVEL, lightLevel)
                .build();

        if (shutterSpeed.shouldCauseTickingSound() || captureParameters.projection().isPresent()) {
            int duration = Math.max(shutterSpeed.getDurationTicks(), captureParameters.projection()
                    .map(l -> Config.Server.PROJECT_TIMEOUT_TICKS.get()).orElse(0));
            Sound.playShutterTicking(entity, cameraId, duration);
        }

        CameraInstances.createOrUpdate(cameraId, instance -> {
            int cooldown = item.calculateCooldownAfterShot(stack, captureParameters);
            instance.setDeferredCooldown(cooldown);

            captureParameters.projection().ifPresent(fileLoading -> {
                instance.waitForProjection(level.getGameTime() + Config.Server.PROJECT_TIMEOUT_TICKS.get());
            });
        });

        addNewFrame(item, level, holder, stack, captureParameters);

        ExposureServer.exposureRepository().expect(executingPlayer, exposureId);
        Packets.sendToClient(new CaptureStartS2CP(item.getCaptureType(stack), captureParameters), executingPlayer);
    }

    public static void onShutterOpen(CameraItem item, CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
        holder.getExposureCameraOperator().ifPresent(operator -> {
            if (operator instanceof ServerPlayer player) {
                Packets.sendToClient(ShutterOpenedS2CP.INSTANCE, player);
            }
        });
    }

    public static void onShutterClosed(CameraItem item, CameraHolder holder, ServerLevel serverLevel, ItemStack stack) {
        if (holder instanceof Player player) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(CameraItem.BASE_COOLDOWN);
            player.getCooldowns().addCooldown(stack, cooldown);
        } else if (holder instanceof CameraStandEntity stand) {
            int cooldown = CameraInstances.getOptional(stack).map(CameraInstance::getDeferredCooldown).orElse(CameraItem.BASE_COOLDOWN);
            stand.startCooldown(cooldown);
        }

        Attachment.FILM.ifPresent(stack, (filmItem, filmStack) -> {
            SoundEvent sound = filmItem.isFull(filmStack)
                    ? Exposure.SoundEvents.FILM_ADVANCE_LAST.get()
                    : Exposure.SoundEvents.FILM_ADVANCE.get();

            float fullness = filmItem.getFullness(filmStack);
            String id = holder.asHolderEntity().getId() + item.getOrCreateId(stack).uuid().toString();
            Sound.playUnique(id, holder.asHolderEntity(), sound, SoundSource.PLAYERS, 1f, 0.85f + 0.2f * fullness);
        });
    }

    public static void addNewFrame(CameraItem item, ServerLevel level, CameraHolder holder, ItemStack stack, CaptureParameters captureParameters) {
        boolean projecting = captureParameters.projection().isPresent();

        PointOfView pov = item.getPointOfView(holder, stack);
        double fov = item.getViewfinderFov(level, stack);

        List<BlockPos> positionsInFrame = !projecting ? getPositionsInFrame(item, holder, pov, fov) : Collections.emptyList();
        List<LivingEntity> entitiesInFrame = !projecting ? EntitiesInFrame.get(holder, pov, fov) : Collections.emptyList();

        Frame frame = createFrame(item, holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame);
        addFrameToFilm(item, stack, frame);
        onFrameAdded(item, holder, level, stack, frame, positionsInFrame, entitiesInFrame);
    }

    public static Frame createFrame(CameraItem item, CameraHolder holder, ServerLevel level, ItemStack stack, CaptureParameters captureParameters,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        return Frame.create()
                .setIdentifier(ExposureIdentifier.id(captureParameters.exposureId()))
                .setType(captureParameters.filmProperties().type())
                .setPhotographer(new Photographer(holder))
                .setEntitiesInFrame(entitiesInFrame.stream()
                        .limit(Exposure.MAX_ENTITIES_IN_FRAME)
                        .map(entity -> EntityInFrame.of(holder.asHolderEntity(), entity, data -> {
                            PlatformHelper.postModifyEntityInFrameExtraDataEvent(holder, stack, entity, data);
                        }))
                        .toList())
                .addExtraData(Frame.SHUTTER_SPEED, CameraSettings.SHUTTER_SPEED.getOrDefault(stack))
                .addExtraData(Frame.TIMESTAMP, UnixTimestamp.Seconds.now())
                .updateExtraData(data -> addFrameExtraData(item, holder, level, stack, captureParameters, positionsInFrame, entitiesInFrame, data))
                .toImmutable();
    }

    public static void addFrameExtraData(CameraItem item, CameraHolder holder, ServerLevel level, ItemStack camera, CaptureParameters params,
                                     List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame, ExtraData data) {
        Entity cameraHolder = holder.asHolderEntity();
        boolean projecting = params.projection().isPresent();

        if (projecting) {
            data.put(Frame.PROJECTED, true);
            return;
        }

        if (params.getFlash()) {
            data.put(Frame.FLASH, true);
        }
        if (item.isInSelfieMode(camera)) {
            data.put(Frame.SELFIE, true);
        }
        if (holder instanceof CameraStandEntity) {
            data.put(Frame.ON_STAND, true);
        }

        double zoom = CameraSettings.ZOOM.getOrDefault(camera);
        FocalRange focalRange = item.getFocalRange(level.registryAccess(), camera);
        int focalLength = (int) focalRange.focalLengthFromZoom(zoom);
        data.put(Frame.FOCAL_LENGTH, focalLength);

        params.extraData().get(CaptureParameters.LIGHT_LEVEL)
                .ifPresent(lightLevel -> data.put(Frame.LIGHT_LEVEL, lightLevel));

        if (params.filmProperties().type() == ExposureType.BLACK_AND_WHITE) {
            params.singleChannel().ifPresent(channel ->
                    data.put(Frame.COLOR_CHANNEL, channel));
        }

        data.put(Frame.POSITION, cameraHolder.position());
        data.put(Frame.PITCH, cameraHolder.getXRot());
        data.put(Frame.YAW, cameraHolder.getYRot());

        data.put(Frame.DAY_TIME, (int) level.getDayTime());
        data.put(Frame.DIMENSION, level.dimension().location());

        BlockPos blockPos = cameraHolder.blockPosition();

        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, cameraHolder.getBlockX(), cameraHolder.getBlockZ());
        level.updateSkyBrightness();
        int skyLight = level.getBrightness(LightLayer.SKY, blockPos);

        if (cameraHolder.isUnderWater()) {
            data.put(Frame.UNDERWATER, true);
        }
        if (cameraHolder.getBlockY() < Math.min(level.getSeaLevel(), surfaceHeight) && skyLight == 0) {
            data.put(Frame.IN_CAVE, true);
        } else if (!cameraHolder.isUnderWater()) {
            Biome.Precipitation precipitation = level.getBiome(blockPos).value().getPrecipitationAt(blockPos);
            if (level.isThundering() && precipitation != Biome.Precipitation.NONE)
                data.put(Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snowstorm" : "Thunder");
            else if (level.isRaining() && precipitation != Biome.Precipitation.NONE)
                data.put(Frame.WEATHER, precipitation == Biome.Precipitation.SNOW ? "Snow" : "Rain");
            else
                data.put(Frame.WEATHER, "Clear");
        }

        // Most common biome:
        positionsInFrame.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .flatMap(pos -> level.getBiome(pos).unwrapKey().map(ResourceKey::location))
                .ifPresent(biome -> data.put(Frame.BIOME, biome));

        List<Identifier> structures = positionsInFrame.stream()
                .map(pos -> LevelUtil.getStructuresAt(level, pos))
                .flatMap(List::stream)
                .collect(Collectors.toSet()) // Remove duplicates
                .stream()
                .toList();
        if (!structures.isEmpty()) {
            data.put(Frame.STRUCTURES, structures);
        }

        PlatformHelper.postModifyFrameExtraDataEvent(holder, camera, params, positionsInFrame, entitiesInFrame, data);
    }

    /**
     * Fires 5 rays from camera and obtains positions where they landed. <br>
     * First ray is in the center (equals to look direction).<br>
     * Next are: top left, top right, bottom left, bottom right.
     * These 4 are roughly in positions where rule of thirds cross points are.
     */
    public static List<BlockPos> getPositionsInFrame(CameraItem item, CameraHolder cameraHolder, PointOfView pov, double fov) {
        // offset roughly corresponds to rule of thirds distance
        float offsetDegrees = (float) ((fov * item.getCropFactor()) / 4.3);

        return Vec3Util.getProbeVectors(pov.dir(), offsetDegrees).stream()
                .map(direction -> {
                    Vec3 endPos = pov.pos().add(direction.scale(100));
                    return cameraHolder.asHolderEntity().level().clip(
                            new ClipContext(pov.pos(), endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, cameraHolder.asHolderEntity()));
                })
                .filter(hit -> hit.getType() != HitResult.Type.MISS)
                .map(BlockHitResult::getBlockPos)
                .toList();
    }

    public static void addFrameToFilm(CameraItem item, ItemStack stack, Frame frame) {
        Attachment.FILM.ifPresentOrElse(stack, (filmItem, filmStack) -> {
            ItemStack updatedFilmStack = filmStack.copy();
            filmItem.addFrame(updatedFilmStack, frame);
            Attachment.FILM.set(stack, updatedFilmStack);
        }, () -> Exposure.LOGGER.error("Cannot add frame: no film attachment is present."));
    }

    public static void onFrameAdded(CameraItem item, CameraHolder holder, ServerLevel level, ItemStack stack, Frame frame,
                             List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
        Entity executor = holder.getPlayerExecutingExposure().map(pl -> (Entity) pl).orElse(holder.asHolderEntity());
        ExposureServer.frameHistory().add(executor, frame);

        entitiesInFrame.forEach(entity -> entityCaptured(item, holder, stack, entity));

        holder.getPlayerAwardedForExposure()
                .filter(player -> player instanceof ServerPlayer)
                .ifPresent(player -> {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.awardStat(Exposure.Stats.FILM_FRAMES_EXPOSED);
                    Exposure.CriteriaTriggers.FRAME_EXPOSED.get().trigger(
                            serverPlayer, holder, stack, frame, positionsInFrame, entitiesInFrame);
                });

        PlatformHelper.postFrameAddedEvent(holder, stack, frame, positionsInFrame, entitiesInFrame);
    }

    public static void entityCaptured(CameraItem item, CameraHolder cameraHolder, ItemStack stack, LivingEntity entity) {
        if (cameraHolder.asHolderEntity() instanceof ServerPlayer player && entity instanceof EnderMan enderMan) {
            boolean lookingAtAngryEnderMan = player.equals(enderMan.getTarget()) && enderMan.isLookingAtMe(player);

            if (lookingAtAngryEnderMan) {
                // I wanted to implement this in a predicate,
                // but it's tricky because EntitySubPredicates do not get the player in their 'match' method.
                // So it's just easier to hardcode it like this.
                Exposure.CriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.get().trigger(player);
            }
        }
    }

    public static void handleProjectionResult(CameraItem item, ServerLevel level, CameraHolder holder, ItemStack stack,
                                       CameraInstance.ProjectionState projectionState, Optional<TranslatableError> error) {
        StoredItemStack filter = Attachment.FILTER.get(stack);
        if (filter.isEmpty()) return;
        if (!(filter.getItem() instanceof InterplanarProjectorItem interplanarProjector)) return;
        if (!interplanarProjector.isConsumable(filter.getForReading())) return;

        Entity entity = holder.asHolderEntity();

        if (projectionState == CameraInstance.ProjectionState.FAILED || projectionState == CameraInstance.ProjectionState.TIMED_OUT) {
            ItemStack filterStack = filter.getCopy().transmuteCopy(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get());
            error.ifPresent(err -> filterStack.set(Exposure.DataComponents.INTERPLANAR_PROJECTOR_ERROR_CODE, err.code()));
            Attachment.FILTER.set(stack, filterStack);
            Sound.play(entity, Exposure.SoundEvents.BSOD.get());
            if (item.getShutter().isOpen(stack)) {
                item.getShutter().close(holder, level, stack);
            }
            return;
        }

        ItemStack filterStack = filter.getCopy();
        filterStack.shrink(1);
        Attachment.FILTER.set(stack, filterStack);

        if (projectionState == CameraInstance.ProjectionState.SUCCESSFUL) {
            holder.getServerPlayerAwardedForExposure()
                    .ifPresent(player -> Exposure.CriteriaTriggers.SUCCESSFULLY_PROJECT_IMAGE.get().trigger(player));
            Sound.play(entity, Exposure.SoundEvents.INTERPLANAR_PROJECT.get(), entity.getSoundSource(), 0.8f, 1.1f);
            for (int i = 0; i < 16; i++) {
                level.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1.2, entity.getZ(), 2,
                        entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, entity.getRandom().nextGaussian() * 0.3, 0.01);
            }
        }
    }

    public static int getMatchingSlotInInventory(CameraItem item, Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).equals(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static void testEntitiesInFrame(CameraItem item, ItemStack stack, Level level, CameraHolder holder) {
        PointOfView pov = item.getPointOfView(holder, stack);

        double fov = item.getViewfinderFov(level, stack);
        List<LivingEntity> entities = EntitiesInFrame.get(holder.asHolderEntity(), pov, fov);
        for (LivingEntity livingEntity : entities) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 2, 1, true, false, false));
        }
    }

    public static void testPositionsInFrame(CameraItem item, ItemStack stack, Level level, Player player) {
        if (level.isClientSide() && level.getGameTime() % 2 == 0) {
            List<BlockPos> positionsInFrame = getPositionsInFrame(item, player, item.getPointOfView(player, stack), item.getViewfinderFov(level, stack));
            for (BlockPos pos : positionsInFrame) {
                level.addAlwaysVisibleParticle(ParticleTypes.EXPLOSION, true, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    public static int getGlassTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
            boolean shutterOpen = stack.getItem() instanceof CameraItem cameraItem && cameraItem.getShutter().isOpen(stack);

            StoredItemStack filter = Attachment.FILTER.get(stack);
            if (filter.isEmpty()) return shutterOpen ? 0xFF333333 : -1;
            if (filter.getForReading().getItem() instanceof BlockItem item && item.getBlock() instanceof StainedGlassPaneBlock pane) {
                return shutterOpen
                        ? Color.argb(pane.getColor().getTextureDiffuseColor()).multiply(0.2f).withAlpha(255).getARGB()
                        : pane.getColor().getTextureDiffuseColor();
            }
            if (filter.getForReading().is(Exposure.Items.INTERPLANAR_PROJECTOR.get()))
                return shutterOpen ? 0xFF051A0F : 0xFF50B27E;
            if (filter.getForReading().is(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get()))
                return shutterOpen ? 0xFF003D76 : 0xFF54ADFF;
            return -1;
        }

        return -1;
    }

    public static PointOfView getPointOfView(CameraItem item, CameraHolder holder, ItemStack stack) {
        if (item.isInSelfieMode(stack)) {
            return PointOfView.of(holder)
                    .reverseDirection()
                    .limitMaxDistance(holder, item.getSelfieCameraDistance(stack))
                    .rotateX(-CameraSettings.SELFIE_ROTATION_X.getOrDefault(stack))
                    .rotateY(-CameraSettings.SELFIE_ROTATION_Y.getOrDefault(stack));
        } else {
            return PointOfView.of(holder)
                    .move(0, item.getYPositionOffset(stack), 0);
        }
    }

}
