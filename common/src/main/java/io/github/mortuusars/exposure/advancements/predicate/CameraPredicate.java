package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Stuff;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record CameraPredicate(ItemPredicate camera,
                              ItemPredicate film,
                              ItemPredicate flash,
                              ItemPredicate lens,
                              ItemPredicate filter,
                              LocationPredicate location) {


    public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(cameraPredicateInstance ->
            cameraPredicateInstance.group(
                    Stuff.ITEM_PREDICATE_CODEC.optionalFieldOf("camera",ItemPredicate.ANY).forGetter(CameraPredicate::camera),
                    Stuff.ITEM_PREDICATE_CODEC.optionalFieldOf("film",ItemPredicate.ANY).forGetter(CameraPredicate::film),
                    Stuff.ITEM_PREDICATE_CODEC.optionalFieldOf("flash",ItemPredicate.ANY).forGetter(CameraPredicate::flash),
                    Stuff.ITEM_PREDICATE_CODEC.optionalFieldOf("lens",ItemPredicate.ANY).forGetter(CameraPredicate::lens),
                    Stuff.ITEM_PREDICATE_CODEC.optionalFieldOf("filter",ItemPredicate.ANY).forGetter(CameraPredicate::filter),
                    Stuff.LOCATION_PREDICATE_CODEC.optionalFieldOf("location",LocationPredicate.ANY).forGetter(CameraPredicate::location)
            ).apply(cameraPredicateInstance,CameraPredicate::new));

    public static final CameraPredicate ANY = new CameraPredicate(ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY,
            ItemPredicate.ANY,LocationPredicate.ANY);


    public static CameraPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonObj = GsonHelper.convertToJsonObject(json, "camera");

        return new CameraPredicate(
                ItemPredicate.fromJson(jsonObj.getAsJsonObject("camera")),
                ItemPredicate.fromJson(jsonObj.getAsJsonObject("film")),
                ItemPredicate.fromJson(jsonObj.getAsJsonObject("flash")),
                ItemPredicate.fromJson(jsonObj.getAsJsonObject("lens")),
                ItemPredicate.fromJson(jsonObj.getAsJsonObject("filter")),
                LocationPredicate.fromJson(jsonObj.getAsJsonObject("location"))
        );
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            return CODEC.encodeStart(JsonOps.INSTANCE,this).resultOrPartial(Exposure.LOGGER::error).orElseThrow();
        }
    }

    public boolean matches(ServerLevel level, ItemStack cameraStack, Vec3 cameraLocation) {
        if (!(cameraStack.getItem() instanceof CameraItem)) return false;

        return (camera.matches(cameraStack))
                && (film.matches(Attachment.FILM.get(cameraStack).getForReading()))
                && (flash.matches(Attachment.FLASH.get(cameraStack).getForReading()))
                && (lens.matches(Attachment.LENS.get(cameraStack).getForReading()))
                && (filter.matches(Attachment.FILTER.get(cameraStack).getForReading()))
                && (location.matches(level, cameraLocation.x, cameraLocation.y, cameraLocation.z));
    }
}
