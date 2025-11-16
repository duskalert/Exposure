package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
