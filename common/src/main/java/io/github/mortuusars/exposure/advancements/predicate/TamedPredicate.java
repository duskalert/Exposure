package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record TamedPredicate(boolean isTamed) implements EntitySubPredicate {
    public static final Codec<TamedPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.BOOL.fieldOf("is_tamed").forGetter(TamedPredicate::isTamed))
                    .apply(instance, TamedPredicate::new)
    );

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        return entity instanceof TamableAnimal animal && animal.isTame() == isTamed;
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("is_tamed",isTamed);
        return jsonObject;
    }

    @Override
    public Type type() {
        return Exposure.EntitySubPredicates.TAMED;
    }
}
