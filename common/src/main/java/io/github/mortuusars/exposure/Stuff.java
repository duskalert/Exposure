package io.github.mortuusars.exposure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

public class Stuff {

    public static final Codec<MinMaxBounds.Ints> INTS_CODEC = RecordCodecBuilder.create(
            intsInstance -> intsInstance.group(Codec.INT.fieldOf("min")
                            .forGetter(MinMaxBounds::getMin),
                    Codec.INT.fieldOf("max")
                            .forGetter(MinMaxBounds::getMax)
            ).apply(intsInstance, MinMaxBounds.Ints::between));

    public static final Codec<EnchantmentPredicate> ENCHANTMENT_PREDICATE_CODEC = RecordCodecBuilder.create(enchantmentPredicateInstance ->
            enchantmentPredicateInstance.group(
                    BuiltInRegistries.ENCHANTMENT.byNameCodec().optionalFieldOf("enchantment").forGetter(object -> Optional.ofNullable(object.enchantment)),
                    INTS_CODEC.fieldOf("levels").forGetter(object -> object.level)
            ).apply(enchantmentPredicateInstance, Stuff::fromCodec));

    public static final Codec<NbtPredicate> NBT_PREDICATE_CODEC = RecordCodecBuilder.create(nbtPredicateInstance -> nbtPredicateInstance.group(
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(object -> Optional.ofNullable(object.tag))
    ).apply(nbtPredicateInstance, compoundTag -> new NbtPredicate(compoundTag.orElse(null))));


    public static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC = RecordCodecBuilder
            .create(itemPredicateInstance ->
                    itemPredicateInstance.group(
                                    TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag)),
                                    BuiltInRegistries.ITEM.byNameCodec().listOf().optionalFieldOf("items",new ArrayList<>()).forGetter(i -> List.copyOf(i.items)),
                                    INTS_CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(i -> i.count),
                                    INTS_CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(i -> i.durability),
                                    ENCHANTMENT_PREDICATE_CODEC.listOf().optionalFieldOf("enchantments",new ArrayList<>()).forGetter(i -> Arrays.asList(i.enchantments)),
                                    ENCHANTMENT_PREDICATE_CODEC.listOf().optionalFieldOf("stored_enchantments",new ArrayList<>()).forGetter(i -> Arrays.asList(i.storedEnchantments)),
                                    BuiltInRegistries.POTION.byNameCodec().optionalFieldOf("potion").forGetter(i -> Optional.ofNullable(i.potion)),
                                    NBT_PREDICATE_CODEC.optionalFieldOf("nbt",NbtPredicate.ANY).forGetter(i -> i.nbt)
                            ).apply(itemPredicateInstance, Stuff::fromCodec));

    private static ItemPredicate fromCodec(Optional<TagKey<Item>> itemTagKey, List<Item> items, MinMaxBounds.Ints ints,
                                           MinMaxBounds.Ints ints2, List<EnchantmentPredicate> enchantmentPredicates,
                                           List<EnchantmentPredicate> enchantmentPredicates2, Optional<Potion> potion, NbtPredicate nbtPredicate) {
        return new ItemPredicate(itemTagKey.orElse(null),new HashSet<>(items),ints,ints2,
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
}
