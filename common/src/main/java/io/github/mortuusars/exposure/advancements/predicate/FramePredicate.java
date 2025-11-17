package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Stuff;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record FramePredicate(Optional<ExposureIdentifier> identifier,
                             Optional<String> type,
                             Optional<String> photographer,
                             Optional<ShutterSpeedPredicate> shutterSpeed,
                             Optional<MinMaxBounds.Ints> focalLength,
                             Optional<MinMaxBounds.Ints> lightLevel,
                             Optional<MinMaxBounds.Ints> dayTime,
                             Optional<MinMaxBounds.Ints> entitiesInFrameCount,
                             Optional<List<EntityInFramePredicate>> entitiesInFrame,
                             Optional<ExtraDataPredicate> extraData) {
    public static final Codec<FramePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExposureIdentifier.CODEC.optionalFieldOf("identifier").forGetter(FramePredicate::identifier),
            Codec.STRING.optionalFieldOf("type").forGetter(FramePredicate::type),
            Codec.STRING.optionalFieldOf("photographer").forGetter(FramePredicate::photographer),
            ShutterSpeedPredicate.CODEC.optionalFieldOf("shutter_speed").forGetter(FramePredicate::shutterSpeed),
            Stuff.INTS_CODEC.optionalFieldOf("focal_length").forGetter(FramePredicate::focalLength),
            Stuff.INTS_CODEC.optionalFieldOf("light_level").forGetter(FramePredicate::lightLevel),
            Stuff.INTS_CODEC.optionalFieldOf("day_time").forGetter(FramePredicate::dayTime),
            Stuff.INTS_CODEC.optionalFieldOf("entities_in_frame_count").forGetter(FramePredicate::entitiesInFrameCount),
            EntityInFramePredicate.CODEC.listOf().optionalFieldOf("entities_in_frame").forGetter(FramePredicate::entitiesInFrame),
            ExtraDataPredicate.CODEC.optionalFieldOf("extra_data").forGetter(FramePredicate::extraData)
    ).apply(instance, FramePredicate::new));

    public static final FramePredicate ANY = new FramePredicate(Optional.empty(),Optional.empty(),
            Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
            Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());

    public static FramePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonObj = GsonHelper.convertToJsonObject(json, "frame");

        return CODEC.decode(JsonOps.INSTANCE,jsonObj).resultOrPartial(Exposure.LOGGER::error).get().getFirst();
    }


        public boolean matches(ItemStack stack, Frame frame) {
        return matches(frame);
    }

    public boolean matches(Frame frame) {
        return (identifier.isEmpty() || identifier.get().equals(frame.identifier()))
                && (type.isEmpty() || type.get().equals(frame.type().getSerializedName()))
                && (photographer.isEmpty() || photographer.get().equals(frame.photographer().name()))
                && (shutterSpeed.isEmpty() || shutterSpeed.get().matches(frame.extraData().get(Frame.SHUTTER_SPEED)))
                && (focalLength.isEmpty() || frame.extraData().get(Frame.FOCAL_LENGTH).map(focalLength.get()::matches).orElse(false))
                && (lightLevel.isEmpty() || frame.extraData().get(Frame.LIGHT_LEVEL).map(lightLevel.get()::matches).orElse(false))
                && (dayTime.isEmpty() || frame.extraData().get(Frame.DAY_TIME).map(dayTime.get()::matches).orElse(false))
                && (entitiesInFrameCount.isEmpty() || entitiesInFrameCount.get().matches(frame.entitiesInFrame().size()))
                && (entitiesInFrame.isEmpty() || entitiesInFrame.get().stream().allMatch(predicate -> predicate.matches(frame.entitiesInFrame())))
                && (extraData.isEmpty() || extraData.get().matches(frame.extraData()));
    }
}
