package io.github.mortuusars.exposure.forge.datagen;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.loot.packs.VanillaChestLoot;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ExposureDatagen {

    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(true,new Blockstates(packOutput,existingFileHelper));
        generator.addProvider(true,new Recipes(packOutput));
        generator.addProvider(true,LootTables.create(packOutput));
    }

    static class Blockstates extends BlockStateProvider {

        public Blockstates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, Exposure.ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
        }
    }

    static class Recipes extends RecipeProvider {

        public Recipes(PackOutput output) {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> writer) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,Exposure.Items.ALBUM.get())
                    .requires(Items.WRITABLE_BOOK)
                    .requires(Items.PHANTOM_MEMBRANE)
                    .unlockedBy(getHasName(Items.PHANTOM_MEMBRANE),has(Items.PHANTOM_MEMBRANE))
                    .save(writer);
        }
    }

    static class LootTables extends LootTableProvider {

        public LootTables(PackOutput output, Set<ResourceLocation> requiredTables, List<SubProviderEntry> subProviders) {
            super(output, requiredTables, subProviders);
        }

        public static LootTableProvider create(PackOutput pOutput) {
            return new LootTables(pOutput, BuiltInLootTables.all(),
                    List.of(new SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST),
                            new SubProviderEntry(BlockLoot::new, LootContextParamSets.ENTITY)
                    ));
        }

        static class BlockLoot extends VanillaBlockLoot {

            @Override
            public void generate() {
                dropSelf(Exposure.Blocks.LIGHTROOM.get());
            }


            @Override
            protected Iterable<Block> getKnownBlocks() {
                return BuiltInRegistries.BLOCK.stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Exposure.ID))
                        .toList();
            }
        }

        static class ChestLoot extends VanillaChestLoot {

            @Override
            public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {

            }
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        }

    }

}
