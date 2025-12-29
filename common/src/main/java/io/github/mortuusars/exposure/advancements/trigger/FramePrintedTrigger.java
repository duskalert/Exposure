package io.github.mortuusars.exposure.advancements.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class FramePrintedTrigger extends SimpleCriterionTrigger<FramePrintedTrigger.TriggerInstance> {
    public static final ResourceLocation ID = Exposure.resource("frame_printed");


    public void trigger(ServerPlayer player,
                        BlockPos pos,
                        Frame frame,
                        ItemStack result) {
        this.trigger(player, triggerInstance ->
                triggerInstance.matches(player, pos, frame, result));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        LocationPredicate location = LocationPredicate.fromJson(json.get("location"));
        FramePredicate framePredicate = FramePredicate.fromJson(json.get("frame"));
        ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
        return new TriggerInstance(predicate, location, framePredicate, item);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static final class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final LocationPredicate location;
        private final FramePredicate frame;
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate player,
                               LocationPredicate location,
                               FramePredicate frame,
                               ItemPredicate item) {
            super(ID, player);
            this.location = location;
            this.frame = frame;
            this.item = item;
        }

        public boolean matches(ServerPlayer player,
                               BlockPos pos,
                               Frame frame,
                               ItemStack result) {
            return location.matches(player.serverLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    && this.frame.matches(frame)
                    && item.matches(result);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject jsonObject = super.serializeToJson(context);
            if (location != LocationPredicate.ANY) {
                jsonObject.add("location", location.serializeToJson());
            }
            if (frame != FramePredicate.ANY) {
                jsonObject.add("frame", frame.serializeToJson());
            }
            if (item != ItemPredicate.ANY) {
                jsonObject.add("item", item.serializeToJson());
            }
            return jsonObject;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TriggerInstance) obj;
            return Objects.equals(this.location, that.location) &&
                    Objects.equals(this.frame, that.frame) &&
                    Objects.equals(this.item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, frame, item);
        }

        @Override
        public String toString() {
            return "TriggerInstance[" +
                    "location=" + location + ", " +
                    "frame=" + frame + ", " +
                    "item=" + item + ']';
        }
    }
}