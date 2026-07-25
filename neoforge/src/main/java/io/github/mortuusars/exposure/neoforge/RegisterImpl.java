package io.github.mortuusars.exposure.neoforge;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Register;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegisterImpl {
    public static void bind() {
        Register.bind(new ServiceImpl());
    }

    private static class ServiceImpl implements Register.Service {
        @Override public <T extends Block> Supplier<T> block(String id, Supplier<T> s) { return BLOCKS.register(id, s); }
        @Override public <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> s) { return BLOCK_ENTITY_TYPES.register(id, s); }
        @Override public <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> sup, Block... blocks) { return new BlockEntityType<>(sup::create, java.util.Set.of(blocks)); }
        @Override public <T extends Item> Supplier<T> item(String id, Supplier<T> s) { return ITEMS.register(id, s); }
        @Override public <T extends CreativeModeTab> Supplier<T> creativeTab(String id, Supplier<T> s) { return CREATIVE_MODE_TAB.register(id, s); }
        @Override public <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> f, MobCategory c, float w, float h, int r, boolean v, int u) { return RegisterImpl.entityType(id, f, c, w, h, r, v, u); }
        @Override public <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> f, MobCategory c, boolean v, Consumer<EntityType.Builder<T>> tb) { return RegisterImpl.entityType(id, f, c, v, tb); }
        @Override public <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> s) { return SOUND_EVENTS.register(id, s); }
        @Override public <E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> s) { return RegisterImpl.menuType(id, s); }
        @Override public <T extends net.minecraft.world.item.crafting.Recipe<I>, I extends net.minecraft.world.item.crafting.RecipeInput> Supplier<RecipeType<T>> recipeType(String id, Supplier<RecipeType<T>> s) { return RECIPE_TYPES.register(id, s); }
        @Override public <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeSerializer<T>> recipeSerializer(String id, Supplier<RecipeSerializer<T>> s) { return RECIPE_SERIALIZERS.register(id, s); }
        @Override public <T extends CriterionTrigger<?>> Supplier<T> criterionTrigger(String id, Supplier<T> s) { return CRITERION_TRIGGERS.register(id, s); }
        @Override public <T extends net.minecraft.core.component.predicates.DataComponentPredicate> Supplier<net.minecraft.core.component.predicates.DataComponentPredicate.Type<T>> itemSubPredicate(String id, Supplier<net.minecraft.core.component.predicates.DataComponentPredicate.Type<T>> s) { return null; /* not used */ }
        @Override public <T extends EntitySubPredicate> Supplier<MapCodec<T>> entitySubPredicate(String id, Supplier<MapCodec<T>> s) { return ENTITY_SUB_PREDICATES.register(id, s); }
        @Override public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> Supplier<ArgumentTypeInfo<A, T>> commandArgumentType(String id, Class<A> cls, I info) { return RegisterImpl.commandArgumentType(id, cls, info); }
        @Override public <T extends FeatureConfiguration> Supplier<Feature<?>> worldGenFeature(String id, Supplier<Feature<T>> s) { return WORLD_GEN_FEATURES.register(id, s); }
        @Override public <T> DataComponentType<T> dataComponentType(String id, Consumer<DataComponentType.Builder<T>> b) { return RegisterImpl.dataComponentType(id, b); }
        @SuppressWarnings({"rawtypes", "unchecked"})
@Override public Supplier<ParticleType<?>> particleType(String id, Supplier supplier) { return (Supplier) RegisterImpl.particleType(id, supplier); }
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Exposure.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Exposure.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Exposure.ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Exposure.ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Exposure.ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Exposure.ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Exposure.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Exposure.ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Exposure.ID);
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Exposure.ID);
    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATES = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, Exposure.ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Exposure.ID);
    public static final DeferredRegister<Feature<?>> WORLD_GEN_FEATURES = DeferredRegister.create(Registries.FEATURE, Exposure.ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Exposure.ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Exposure.ID);
    public static final DeferredRegister<Identifier> CUSTOM_STATS = DeferredRegister.create(Registries.CUSTOM_STAT, Exposure.ID);

    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) { return BLOCKS.register(id, supplier); }
    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) { return BLOCK_ENTITY_TYPES.register(id, sup); }
    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(Register.BlockEntitySupplier<T> b, Block... blocks) { return new BlockEntityType<>(b::create, java.util.Set.of(blocks)); }
    public static <T extends Item> Supplier<T> item(String id, Supplier<T> s) { return ITEMS.register(id, s); }
    public static <T extends CreativeModeTab> Supplier<T> creativeTab(String id, Supplier<T> s) { return CREATIVE_MODE_TAB.register(id, s); }
    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> f, MobCategory c, float w, float h, int r, boolean v, int u) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(f, c).sized(w, h).clientTrackingRange(r).setShouldReceiveVelocityUpdates(v).updateInterval(u).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.tryParse(Exposure.ID + ":" + id))));
    }
    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> f, MobCategory c, boolean v, Consumer<EntityType.Builder<T>> tb) {
        return ENTITY_TYPES.register(id, () -> { EntityType.Builder<T> b = EntityType.Builder.of(f, c); b.setShouldReceiveVelocityUpdates(v); tb.accept(b); return b.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.tryParse(Exposure.ID + ":" + id))); });
    }
    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> s) { return SOUND_EVENTS.register(id, s); }
    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<MenuType<E>> menuType(String id, Register.MenuTypeSupplier<E> s) { return MENU_TYPES.register(id, () -> IMenuTypeExtension.create(s::create)); }
    public static Supplier<RecipeType<?>> recipeType(String id, Supplier<RecipeType<?>> s) { return RECIPE_TYPES.register(id, s); }
    public static Supplier<RecipeSerializer<?>> recipeSerializer(String id, Supplier<RecipeSerializer<?>> s) { return RECIPE_SERIALIZERS.register(id, s); }
    public static <T extends CriterionTrigger<?>> Supplier<T> criterionTrigger(String id, Supplier<T> s) { return CRITERION_TRIGGERS.register(id, s); }
    public static <T extends MapCodec<EntitySubPredicate>> Supplier<T> entitySubPredicate(String id, Supplier<T> s) { return ENTITY_SUB_PREDICATES.register(id, s); }
    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> Supplier<ArgumentTypeInfo<A, T>> commandArgumentType(String id, Class<A> cls, I info) { return COMMAND_ARGUMENT_TYPES.register(id, () -> ArgumentTypeInfos.registerByClass(cls, info)); }
    public static <T extends FeatureConfiguration> Supplier<Feature<?>> worldGenFeature(String id, Supplier<Feature<T>> s) { return WORLD_GEN_FEATURES.register(id, s); }
    public static <T> DataComponentType<T> dataComponentType(String id, Consumer<DataComponentType.Builder<T>> b) { var x = DataComponentType.<T>builder(); b.accept(x); var y = x.build(); DATA_COMPONENT_TYPES.register(id, () -> y); return y; }
    public static <T extends ParticleType<? extends ParticleOptions>> Supplier<ParticleType<?>> particleType(String id, Supplier<T> s) { return PARTICLE_TYPES.register(id, s); }
}
