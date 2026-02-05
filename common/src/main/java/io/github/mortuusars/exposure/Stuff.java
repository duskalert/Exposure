package io.github.mortuusars.exposure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Stuff {

    public static final Codec<MinMaxBounds.Ints> INTS_CODEC = RecordCodecBuilder.create(
            intsInstance -> intsInstance.group(Codec.INT.optionalFieldOf("min")
                            .forGetter(object -> Optional.ofNullable(object.getMin())),
                    Codec.INT.optionalFieldOf("max")
                            .forGetter(ints -> Optional.ofNullable(ints.getMax()))
            ).apply(intsInstance, (integer, integer2) -> new MinMaxBounds.Ints(integer.orElse(null),integer2.orElse(null))));

    public static final Codec<MinMaxBounds.Doubles> DOUBLES_CODEC = RecordCodecBuilder.create(
            intsInstance -> intsInstance.group(Codec.DOUBLE.fieldOf("min")
                            .forGetter(MinMaxBounds::getMin),
                    Codec.DOUBLE.fieldOf("max")
                            .forGetter(MinMaxBounds::getMax)
            ).apply(intsInstance, MinMaxBounds.Doubles::between));

    public static final Codec<EnchantmentPredicate> ENCHANTMENT_PREDICATE_CODEC = RecordCodecBuilder.create(enchantmentPredicateInstance ->
            enchantmentPredicateInstance.group(
                    BuiltInRegistries.ENCHANTMENT.byNameCodec().optionalFieldOf("enchantment").forGetter(object -> Optional.ofNullable(object.enchantment)),
                    INTS_CODEC.fieldOf("levels").forGetter(object -> object.level)
            ).apply(enchantmentPredicateInstance, Stuff::fromCodec));

    public static final Codec<NbtPredicate> NBT_PREDICATE_CODEC = RecordCodecBuilder.create(nbtPredicateInstance -> nbtPredicateInstance.group(
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(object -> Optional.ofNullable(object.tag))
    ).apply(nbtPredicateInstance, compoundTag -> new NbtPredicate(compoundTag.orElse(null))));

    public static final Codec<LightPredicate> LIGHT_PREDICATE_CODEC = RecordCodecBuilder.create(
    p_337376_ -> p_337376_.group(INTS_CODEC.optionalFieldOf("light", MinMaxBounds.Ints.ANY).forGetter(lightPredicate -> lightPredicate.composite))
            .apply(p_337376_, LightPredicate::new)
    );


    public static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC = RecordCodecBuilder
            .create(itemPredicateInstance ->
                    itemPredicateInstance.group(
                                    TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag)),
                                    BuiltInRegistries.ITEM.byNameCodec().listOf().xmap(Set::copyOf, List::copyOf)
                                            .optionalFieldOf("items").forGetter(i -> Optional.ofNullable(i.items)),
                                    INTS_CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(i -> i.count),
                                    INTS_CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(i -> i.durability),
                                    ENCHANTMENT_PREDICATE_CODEC.listOf().optionalFieldOf("enchantments",new ArrayList<>()).forGetter(i -> Arrays.asList(i.enchantments)),
                                    ENCHANTMENT_PREDICATE_CODEC.listOf().optionalFieldOf("stored_enchantments",new ArrayList<>()).forGetter(i -> Arrays.asList(i.storedEnchantments)),
                                    BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potion").forGetter(i -> Optional.ofNullable(i.potion)),
                                    NBT_PREDICATE_CODEC.optionalFieldOf("nbt",NbtPredicate.ANY).forGetter(i -> i.nbt)
                            ).apply(itemPredicateInstance, Stuff::fromItemCodec));

    public static final Codec<StatePropertiesPredicate.ExactPropertyMatcher> EXACT_VALUE_CODEC = RecordCodecBuilder.create(
            exactPropertyMatcherInstance -> exactPropertyMatcherInstance.group(
                    Codec.STRING.fieldOf("name").forGetter(StatePropertiesPredicate.PropertyMatcher::getName),
                    Codec.STRING.fieldOf("value").forGetter(p -> p.value)
            ).apply(exactPropertyMatcherInstance, StatePropertiesPredicate.ExactPropertyMatcher::new)
    );

    public static final Codec<StatePropertiesPredicate.RangedPropertyMatcher> RANGED_VALUE_CODEC = RecordCodecBuilder.create(
            exactPropertyMatcherInstance -> exactPropertyMatcherInstance.group(
                    Codec.STRING.fieldOf("name").forGetter(StatePropertiesPredicate.PropertyMatcher::getName),
                    Codec.STRING.fieldOf("value").forGetter(p -> p.minValue),
                    Codec.STRING.fieldOf("value").forGetter(p -> p.maxValue)
            ).apply(exactPropertyMatcherInstance, StatePropertiesPredicate.RangedPropertyMatcher::new)
    );

    public static <U> U unwrap(final Either<? extends U, ? extends U> either) {
        return either.map(Function.identity(), Function.identity());
    }

    static final Codec<StatePropertiesPredicate.PropertyMatcher> VALUE_CODEC = Codec.either(
                    EXACT_VALUE_CODEC, RANGED_VALUE_CODEC
            )
            .xmap(Stuff::unwrap, p_299089_ -> {
                if (p_299089_ instanceof StatePropertiesPredicate.ExactPropertyMatcher statepropertiespredicate$exactmatcher) {
                    return Either.left(statepropertiespredicate$exactmatcher);
                } else if (p_299089_ instanceof StatePropertiesPredicate.RangedPropertyMatcher statepropertiespredicate$rangedmatcher) {
                    return Either.right(statepropertiespredicate$rangedmatcher);
                } else {
                    throw new UnsupportedOperationException();
                }
            });

    private static final Codec<List<StatePropertiesPredicate.PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
                    Codec.STRING, VALUE_CODEC
            )
            .xmap(
                    p_297916_ -> p_297916_.entrySet()
                            .stream()
                            .map(entry -> {
                                StatePropertiesPredicate.PropertyMatcher value = entry.getValue();

                                if (value instanceof StatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher) {
                                    return exactPropertyMatcher;
                                } else if (value instanceof StatePropertiesPredicate.RangedPropertyMatcher rangedPropertyMatcher) {
                                    return rangedPropertyMatcher;
                                } else throw new RuntimeException();
                            })
                            .toList(),
                    p_297915_ -> p_297915_.stream()
                            .collect(Collectors.toMap(StatePropertiesPredicate.PropertyMatcher::getName, propertyMatcher -> propertyMatcher))
            );



    public static final Codec<StatePropertiesPredicate> STATE_PROPERTIES_PREDICATE_CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, s -> s.properties);

    public static final Codec<BlockPredicate> BLOCK_PREDICATE_CODEC = RecordCodecBuilder.create(
            p_337342_ -> p_337342_.group(
                            TagKey.codec(Registries.BLOCK).optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag)),
                            BuiltInRegistries.BLOCK.byNameCodec().listOf()
                                    .xmap(Set::copyOf, List::copyOf).optionalFieldOf("blocks").forGetter(b -> Optional.ofNullable(b.blocks)),
                            STATE_PROPERTIES_PREDICATE_CODEC.fieldOf("state").forGetter(b -> b.properties),
                            NBT_PREDICATE_CODEC.fieldOf("nbt").forGetter(b -> b.nbt)
                    )
                    .apply(p_337342_, (Optional<TagKey<Block>> tag, Optional<Set<Block>> blocks, StatePropertiesPredicate properties, NbtPredicate nbt) -> new BlockPredicate(
                            tag.orElse(null), blocks.orElse(null), properties,nbt)));

    public static final Codec<FluidPredicate> FLUID_PREDICATE_CODEC = RecordCodecBuilder.create(
            p_337342_ -> p_337342_.group(
                            TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag)),
                            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(b -> Optional.ofNullable(b.fluid)),
                            STATE_PROPERTIES_PREDICATE_CODEC.fieldOf("state").forGetter(b -> b.properties)
                    )
                    .apply(p_337342_, (Optional<TagKey<Fluid>> tag, Optional<Fluid> blocks, StatePropertiesPredicate properties) -> new FluidPredicate(
                            tag.orElse(null), blocks.orElse(null), properties)));

    public static final Codec<LocationPredicate> LOCATION_PREDICATE_CODEC = RecordCodecBuilder.create(locationPredicateInstance -> {
        return locationPredicateInstance.group(
                PositionPredicate.CODEC.optionalFieldOf("location",PositionPredicate.ANY)
                        .forGetter(locationPredicate -> new PositionPredicate(locationPredicate.x,locationPredicate.y,locationPredicate.z)),
                ResourceKey.codec(Registries.BIOME).optionalFieldOf("biomes").forGetter(l -> Optional.ofNullable(l.biome)),
                ResourceKey.codec(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(l -> Optional.ofNullable(l.structure)),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(l -> Optional.ofNullable(l.dimension)),
                Codec.BOOL.optionalFieldOf("smokey").forGetter(l -> Optional.ofNullable(l.smokey)),
                LIGHT_PREDICATE_CODEC.optionalFieldOf("light",LightPredicate.ANY).forGetter(l -> l.light),
                BLOCK_PREDICATE_CODEC.optionalFieldOf("block",BlockPredicate.ANY).forGetter(l -> l.block),
                FLUID_PREDICATE_CODEC.optionalFieldOf("fluid",FluidPredicate.ANY).forGetter(l -> l.fluid)
        ).apply(locationPredicateInstance, Stuff::fromLocationCodec);
    });

    private static LocationPredicate fromLocationCodec(PositionPredicate t1, Optional<ResourceKey<Biome>> t2, Optional<ResourceKey<Structure>> t3,
                                                       Optional<ResourceKey<Level>> t4, Optional<Boolean> t5, LightPredicate t6, BlockPredicate t7, FluidPredicate t8) {
        return new LocationPredicate(t1.x,t1.y,t1.z,t2.orElse(null),t3.orElse(null),t4.orElse(null),t5.orElse(null),t6,t7,t8);
    }

    private static ItemPredicate fromItemCodec(Optional<TagKey<Item>> itemTagKey, Optional<Set<Item>> items, MinMaxBounds.Ints ints,
                                           MinMaxBounds.Ints ints2, List<EnchantmentPredicate> enchantmentPredicates,
                                           List<EnchantmentPredicate> enchantmentPredicates2, Optional<Potion> potion, NbtPredicate nbtPredicate) {
        return new ItemPredicate(itemTagKey.orElse(null),items.orElse(null),ints,ints2,
                enchantmentPredicates.toArray(EnchantmentPredicate[]::new),enchantmentPredicates.toArray(EnchantmentPredicate[]::new),
                potion.orElse(null),nbtPredicate);
    }

    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(Component.Serializer::fromJson, Component.Serializer::toJson);

    //    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create((instance) -> {
    //        return instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStack::getItem), Codec.INT.fieldOf("Count").forGetter(ItemStack::getCount), CompoundTag.CODEC.optionalFieldOf("tag").forGetter((itemStack) -> {
    //            return Optional.ofNullable(itemStack.getTag());
    //        })).apply(instance, ItemStack::new);
    //    });

    public static final int MILLIS_PER_TICK = 50;

    static EnchantmentPredicate fromCodec(Optional<Enchantment> enchantment, MinMaxBounds.Ints levels) {
        return new EnchantmentPredicate(enchantment.orElse(null), levels);
    }

    public record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {

        public static final PositionPredicate ANY = new PositionPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);

        public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create(
                p_337379_ -> p_337379_.group(
                                DOUBLES_CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::x),
                                DOUBLES_CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::y),
                                DOUBLES_CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::z)
                        )
                        .apply(p_337379_, PositionPredicate::new)
        );

        static Optional<PositionPredicate> of(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
            return x.isAny() && y.isAny() && z.isAny()
                    ? Optional.empty()
                    : Optional.of(new PositionPredicate(x, y, z));
        }

        public boolean matches(double x, double y, double z) {
            return this.x.matches(x) && this.y.matches(y) && this.z.matches(z);
        }
    }
}
