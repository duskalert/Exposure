package io.github.mortuusars.exposure.advancements.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancements.predicate.CameraPredicate;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FrameExposedTrigger extends SimpleCriterionTrigger<FrameExposedTrigger.TriggerInstance> {

    public static final ResourceLocation ID = Exposure.resource("frame_exposed");

    //@Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player,
                        CameraHolder cameraHolder,
                        ItemStack cameraStack,
                        Frame frame,
                        List<BlockPos> locationsInFrame,
                        List<LivingEntity> entitiesInFrame) {
        this.trigger(player, triggerInstance ->
                triggerInstance.matches(player, cameraHolder, cameraStack, frame, locationsInFrame, entitiesInFrame));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        CameraPredicate camera = CameraPredicate.fromJson(json.get("camera"));
        FramePredicate frame = FramePredicate.fromJson(json.get("location"));
        LocationPredicate location = LocationPredicate.fromJson(json.get("location"));
        return new TriggerInstance(predicate, camera, frame, location);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {
            public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                            CameraPredicate.CODEC.optionalFieldOf("camera").forGetter(TriggerInstance::camera),
                            FramePredicate.CODEC.optionalFieldOf("frame").forGetter(TriggerInstance::frame),
                            LocationPredicate.CODEC.optionalFieldOf("location_in_frame").forGetter(TriggerInstance::locationInFrame),
                            EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("entities_in_frame").forGetter(TriggerInstance::entitiesInFrame))
                    .apply(instance, TriggerInstance::new));
        private final ContextAwarePredicate player;
        private final CameraPredicate camera;
        private final FramePredicate frame;
        private final Optional<LocationPredicate> locationInFrame;
        private final Optional<List<ContextAwarePredicate>> entitiesInFrame;

        public TriggerInstance(ContextAwarePredicate player,
                               CameraPredicate camera,
                               Optional<FramePredicate> frame,
                               Optional<LocationPredicate> locationInFrame,
                               Optional<List<ContextAwarePredicate>> entitiesInFrame) {
            super();
            this.player = player;
            this.camera = camera;
            this.frame = frame;
            this.locationInFrame = locationInFrame;
            this.entitiesInFrame = entitiesInFrame;
        }

            public boolean matches(ServerPlayer player,
                                   CameraHolder cameraHolder,
                                   ItemStack cameraStack,
                                   Frame frame,
                                   List<BlockPos> locationsInFrame,
                                   List<LivingEntity> entitiesInFrame) {
                return (camera.isEmpty() || camera.get().matches(player.serverLevel(), cameraStack, cameraHolder.asHolderEntity().position()))
                        && (this.frame.isEmpty() || this.frame.get().matches(frame))
                        && locationsMatch(player, locationsInFrame)
                        && entitiesInFrameMatch(player, cameraHolder, entitiesInFrame);
            }

            private boolean locationsMatch(ServerPlayer player, List<BlockPos> locationsInFrame) {
                return locationInFrame.isEmpty() || locationsInFrame.stream().anyMatch(pos ->
                        locationInFrame.get().matches(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            }

            private boolean entitiesInFrameMatch(ServerPlayer player, CameraHolder cameraHolder, List<LivingEntity> entitiesInFrame) {
                return this.entitiesInFrame.isEmpty() || this.entitiesInFrame.get().stream().allMatch(predicate ->
                        entitiesInFrame.stream().anyMatch(entity -> {
                            LootContext context = createContextForHolder(player.serverLevel(), cameraHolder, entity);
                            return predicate.matches(context);
                        }));
            }

            public static LootContext createContextForHolder(ServerLevel level, CameraHolder holder, Entity entity) {
                LootParams lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, entity)
                        .withParameter(LootContextParams.ORIGIN, holder.asHolderEntity().position())
                        .create(LootContextParamSets.ADVANCEMENT_ENTITY);
                return new LootContext.Builder(lootParams).create(Optional.empty());
            }

        public Optional<ContextAwarePredicate> player() {
            return player;
        }

        public Optional<CameraPredicate> camera() {
            return camera;
        }

        public Optional<FramePredicate> frame() {
            return frame;
        }

        public Optional<LocationPredicate> locationInFrame() {
            return locationInFrame;
        }

        public Optional<List<ContextAwarePredicate>> entitiesInFrame() {
            return entitiesInFrame;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TriggerInstance) obj;
            return Objects.equals(this.player, that.player) &&
                    Objects.equals(this.camera, that.camera) &&
                    Objects.equals(this.frame, that.frame) &&
                    Objects.equals(this.locationInFrame, that.locationInFrame) &&
                    Objects.equals(this.entitiesInFrame, that.entitiesInFrame);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player, camera, frame, locationInFrame, entitiesInFrame);
        }

        @Override
        public String toString() {
            return "TriggerInstance[" +
                    "player=" + player + ", " +
                    "camera=" + camera + ", " +
                    "frame=" + frame + ", " +
                    "locationInFrame=" + locationInFrame + ", " +
                    "entitiesInFrame=" + entitiesInFrame + ']';
        }

        }
}