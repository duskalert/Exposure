package io.github.mortuusars.exposure.forge.datagen;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.loot.packs.VanillaChestLoot;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExposureDatagen {

    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        generator.addProvider(true,new Blockstates(packOutput,existingFileHelper));
        generator.addProvider(true,new Recipes(packOutput));
        generator.addProvider(true,LootTables.create(packOutput));

        BlockTagsProvider blockTagsProvider = new ModBlockTags(packOutput,lookupProvider,existingFileHelper);
        generator.addProvider(true,blockTagsProvider);
        generator.addProvider(true,new ModItemTags(packOutput,lookupProvider,blockTagsProvider.contentsGetter(),existingFileHelper));
        generator.addProvider(true,new ModEntityTypeTags(packOutput,lookupProvider,existingFileHelper));
    }

    static class Blockstates extends BlockStateProvider {

        public Blockstates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, Exposure.ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
        }
    }

    static class ModItemTags extends ItemTagsProvider {
        public ModItemTags(PackOutput arg, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> completableFuture2, @Nullable ExistingFileHelper existingFileHelper) {
            super(arg, completableFuture, completableFuture2, Exposure.ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(Exposure.Tags.Items.PHOTO_AGERS).add(Items.BROWN_DYE).addOptional(new ResourceLocation("supplementaries:antique_ink"));
            tag(ItemTags.BOOKSHELF_BOOKS).add(Exposure.Items.ALBUM.get(),Exposure.Items.SIGNED_ALBUM.get());
            tag(ItemTags.LECTERN_BOOKS).add(Exposure.Items.ALBUM.get(),Exposure.Items.SIGNED_ALBUM.get());
        }
    }

    static class ModEntityTypeTags extends EntityTypeTagsProvider {
        public ModEntityTypeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider,Exposure.ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(Exposure.Tags.Entities.IGNORES_CAMERA).add(
                    EntityType.WARDEN,
                    EntityType.WITHER,
                    EntityType.ENDER_DRAGON,
                    EntityType.ELDER_GUARDIAN,
                    EntityType.PHANTOM);
        }
    }

    static class ModBlockTags extends BlockTagsProvider {
        public ModBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider,Exposure.ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_AXE).add(Exposure.Blocks.LIGHTROOM.get());
        }
    }

    static class Recipes extends RecipeProvider {

        public Recipes(PackOutput output) {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> writer) {

            Ingredient ironIngot = Ingredient.of(Items.IRON_INGOT);
            Ingredient goldIngot = Ingredient.of(Items.GOLD_INGOT);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,Exposure.Items.ALBUM.get())
                    .requires(Items.WRITABLE_BOOK)
                    .requires(Items.PHANTOM_MEMBRANE)
                    .unlockedBy(getHasName(Items.PHANTOM_MEMBRANE),has(Items.PHANTOM_MEMBRANE))
                    .save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.BLACK_AND_WHITE_FILM.get())
                    .define('B',Items.BONE_MEAL)
                    .define('G',Items.GUNPOWDER)
                    .define('I',ironIngot)
                    .define('K',Items.DRIED_KELP)
                    .define('N',Items.IRON_NUGGET)
                    .pattern("NBB")
                    .pattern("IGG")
                    .pattern("IKK")
                    .unlockedBy(getHasName(Items.DRIED_KELP),has(Items.DRIED_KELP)).save(writer);

            ExposureRecipeBuilder.exposure(Exposure.RecipeSerializers.COMPONENT_TRANSFERRING.get(),
                            Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get(),
                            Exposure.Items.INTERPLANAR_PROJECTOR.get())
                    .with(Items.ENDER_EYE)
                    //.unlockedBy(getHasName(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get()),has(Exposure.Items.INTERPLANAR_PROJECTOR.get()))
                    .save(writer,Exposure.resource("broken_interplanar_projector_fixing"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.CAMERA.get())
                    .define('B', ItemTags.BUTTONS)
                    .define('G',Items.GLASS_PANE)//glass_panes colorless
                    .define('I',ironIngot)
                    .define('L',Items.LEVER)
                    .pattern("LIB")
                    .pattern("IGI")
                    .pattern("III")
                    .unlockedBy(getHasName(Items.LEVER),has(Items.LEVER)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.CAMERA_STAND.get())
                    .define('S',Items.STICK)
                    .define('B', Items.SMOOTH_STONE_SLAB)
                    .define('I',ironIngot)
                    .pattern(" I ")
                    .pattern("SSS")
                    .pattern("BSB")
                    .unlockedBy(getHasName(Items.SMOOTH_STONE_SLAB),has(Items.SMOOTH_STONE_SLAB)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.COLOR_FILM.get())
                    .define('L',Items.LAPIS_LAZULI)
                    .define('G',Items.GUNPOWDER)
                    .define('I',goldIngot)
                    .define('K',Items.DRIED_KELP)
                    .define('N',Items.GOLD_NUGGET)
                    .pattern("NLL")
                    .pattern("IGG")
                    .pattern("IKK")
                    .unlockedBy(getHasName(Items.DRIED_KELP),has(Items.DRIED_KELP)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.GLASS_PHOTOGRAPH_FRAME.get())
                    .define('P',Items.GLASS_PANE)
                    .define('F',Exposure.Items.PHOTOGRAPH_FRAME.get())
                    .pattern(" P ")
                    .pattern("PFP")
                    .pattern(" P ")
                    .unlockedBy(getHasName(Exposure.Items.PHOTOGRAPH_FRAME.get()),has(Exposure.Items.PHOTOGRAPH_FRAME.get())).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.HIGH_SENSITIVITY_BLACK_AND_WHITE_FILM.get())
                    .define('B',Items.BONE_MEAL)
                    .define('P',Items.PRISMARINE_CRYSTALS)
                    .define('I',ironIngot)
                    .define('K',Items.DRIED_KELP)
                    .define('N',Items.IRON_NUGGET)
                    .pattern("NBB")
                    .pattern("IPP")
                    .pattern("IKK")
                    .unlockedBy(getHasName(Items.DRIED_KELP),has(Items.DRIED_KELP)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.HIGH_SENSITIVITY_COLOR_FILM.get())
                    .define('L',Items.LAPIS_LAZULI)
                    .define('P',Items.PRISMARINE_CRYSTALS)
                    .define('I',goldIngot)
                    .define('K',Items.DRIED_KELP)
                    .define('N',Items.GOLD_NUGGET)
                    .pattern("NLL")
                    .pattern("IPP")
                    .pattern("IKK")
                    .unlockedBy(getHasName(Items.DRIED_KELP),has(Items.DRIED_KELP)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.INTERPLANAR_PROJECTOR.get())
                    .define('P',Items.TINTED_GLASS)
                    .define('R', Items.REDSTONE)
                    .define('E',Items.ENDER_EYE)
                    .pattern("PRP")
                    .pattern("RER")
                    .pattern("PRP")
                    .unlockedBy(getHasName(Items.ENDER_EYE),has(Items.ENDER_EYE)).save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.LIGHTROOM.get())
                    .define('I',Items.IRON_TRAPDOOR)
                    .define('P', ItemTags.PLANKS)
                    .define('T',Items.REDSTONE_TORCH)
                    .pattern("IT")
                    .pattern("PP")
                    .pattern("PP")
                    .unlockedBy(getHasName(Items.REDSTONE_TORCH),has(Items.REDSTONE_TORCH)).save(writer);

            ExposureRecipeBuilder.exposure(Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get(),
                            Exposure.Items.PHOTOGRAPH.get(),
                            Exposure.Items.AGED_PHOTOGRAPH.get())
                    .with(Exposure.Tags.Items.PHOTO_AGERS)
                    .with(Items.BRUSH)
                    .save(writer);

            ExposureRecipeBuilder.exposure(Exposure.RecipeSerializers.PHOTOGRAPH_COPYING.get(),
                            Exposure.Items.PHOTOGRAPH.get(),
                            Exposure.Items.PHOTOGRAPH.get())
                    .with(Exposure.Tags.Items.PHOTO_PAPERS)
                    .with(Exposure.Tags.Items.BLACK_PRINTING_DYES)
                    .with(Exposure.Tags.Items.YELLOW_PRINTING_DYES)
                    .with(Exposure.Tags.Items.CYAN_PRINTING_DYES)
                    .with(Exposure.Tags.Items.MAGENTA_PRINTING_DYES)
                    .save(writer);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Exposure.Items.PHOTOGRAPH_FRAME.get())
                    .define('P', ItemTags.PLANKS)
                    .define('F',Items.ITEM_FRAME)
                    .define('S',Items.STICK)
                    .pattern("SPS")
                    .pattern("PFP")
                    .pattern("SPS")
                    .unlockedBy(getHasName(Items.ITEM_FRAME),has(Items.ITEM_FRAME)).save(writer);
        }
    }

    static class LootTables extends LootTableProvider {

        public LootTables(PackOutput output, Set<ResourceLocation> requiredTables, List<SubProviderEntry> subProviders) {
            super(output, requiredTables, subProviders);
        }

        public static LootTableProvider create(PackOutput pOutput) {
            return new LootTables(pOutput, BuiltInLootTables.all(),
                    List.of(//new SubProviderEntry(ChestLoot::new, LootContextParamSets.CHEST),
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
