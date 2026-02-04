package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FramePredicate(Optional<ExposureIdentifier> identifier,
                             Optional<String> type,
                             Optional<String> photographer,
                             Optional<ShutterSpeedPredicate> shutterSpeed,
                             MinMaxBounds.Ints focalLength,
                             MinMaxBounds.Ints lightLevel,
                             MinMaxBounds.Ints dayTime,
                             MinMaxBounds.Ints entitiesInFrameCount,
                             List<EntityInFramePredicate> entitiesInFrame,
                             ExtraDataPredicate extraData) {
    public static final Codec<FramePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExposureIdentifier.CODEC.optionalFieldOf("identifier").forGetter(FramePredicate::identifier),
            Codec.STRING.optionalFieldOf("type").forGetter(FramePredicate::type),
            Codec.STRING.optionalFieldOf("photographer").forGetter(FramePredicate::photographer),
            ShutterSpeedPredicate.CODEC.optionalFieldOf("shutter_speed").forGetter(FramePredicate::shutterSpeed),
            Stuff.INTS_CODEC.optionalFieldOf("focal_length", MinMaxBounds.Ints.ANY).forGetter(FramePredicate::focalLength),
            Stuff.INTS_CODEC.optionalFieldOf("light_level", MinMaxBounds.Ints.ANY).forGetter(FramePredicate::lightLevel),
            Stuff.INTS_CODEC.optionalFieldOf("day_time", MinMaxBounds.Ints.ANY).forGetter(FramePredicate::dayTime),
            Stuff.INTS_CODEC.optionalFieldOf("entities_in_frame_count", MinMaxBounds.Ints.ANY).forGetter(FramePredicate::entitiesInFrameCount),
            EntityInFramePredicate.CODEC.listOf().optionalFieldOf("entities_in_frame",List.of()).forGetter(FramePredicate::entitiesInFrame),
            ExtraDataPredicate.CODEC.optionalFieldOf("extra_data",ExtraDataPredicate.ANY).forGetter(FramePredicate::extraData)
    ).apply(instance, FramePredicate::new));

    public static final FramePredicate ANY = new FramePredicate(Optional.empty(),Optional.empty(),
            Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
            MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,new ArrayList<>(),ExtraDataPredicate.ANY);

    public static FramePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonObj = GsonHelper.convertToJsonObject(json, "frame");

        return CODEC.decode(JsonOps.INSTANCE,jsonObj).resultOrPartial(Exposure.LOGGER::error).get().getFirst();
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            return CODEC.encodeStart(JsonOps.INSTANCE,this).resultOrPartial(Exposure.LOGGER::error).get();
        }
    }


        public boolean matches(ItemStack stack, Frame frame) {
        return matches(frame);
    }

    public boolean matches(Frame frame) {
        return (identifier.isEmpty() || identifier.get().equals(frame.identifier()))
                && (type.isEmpty() || type.get().equals(frame.type().getSerializedName()))
                && (photographer.isEmpty() || photographer.get().equals(frame.photographer().name()))
                && (shutterSpeed.isEmpty() || shutterSpeed.get().matches(frame.getExtraDataForReading().get(Frame.SHUTTER_SPEED)))
                && (frame.getExtraDataForReading().get(Frame.FOCAL_LENGTH).map(focalLength::matches).orElse(false))
                && (frame.getExtraDataForReading().get(Frame.LIGHT_LEVEL).map(lightLevel::matches).orElse(false))
                && (frame.getExtraDataForReading().get(Frame.DAY_TIME).map(dayTime::matches).orElse(false))
                && (entitiesInFrameCount.matches(frame.entitiesInFrame().size()))
                && (entitiesInFrame.isEmpty() || entitiesInFrame.stream().allMatch(predicate -> predicate.matches(frame.entitiesInFrame())))
                && (extraData.matches(frame.extraData()));
    }
}
