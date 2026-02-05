package io.github.mortuusars.exposure.forge.loot;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/**
 * <p>Loot modifier that rolls one loot table (the "subtable" and adds the results to the loot being modified (the "target table").
 * Loot modifiers are not rolled for the subtable, as that could result in the subtables'
 * items being modified twice (by downstream loot modifiers modifying the target table).</p>
 *
 * <p> Json format:
 *
 * <pre>
 * {
 *   "type": "neoforge:add_table",
 *   "conditions": [], // conditions block to predicate target tables by
 *   "table": "namespace:loot_table_id" // subtable to roll loot for to add to the target table(s)
 * }
 * </pre>
 *
 * </p>
 */
public class AddTableLootModifier extends LootModifier {

    private final ResourceLocation table;

    public AddTableLootModifier(LootItemCondition[] conditionsIn, ResourceLocation table) {
        super(conditionsIn);
        this.table = table;
    }

    public ResourceLocation table() {
        return this.table;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable lootTable = context.getResolver().getLootTable(this.table);
        if (lootTable != null)
        {
            // Don't run loot modifiers for subtables;
            // the added loot will be modifiable by downstream loot modifiers modifying the target table,
            // so if we modify it here then it could get modified twice.
            lootTable.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return null;//ForgeMod.ADD_TABLE_LOOT_MODIFIER_TYPE.get();
    }
}
