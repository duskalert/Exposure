package io.github.mortuusars.exposure.forge.datagen;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.camera.frame.Photographer;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

        generator.addProvider(true,new ExposureAdvancements(packOutput,lookupProvider,existingFileHelper,List.of(new ExposureAdvancementGenerator())));
        generator.addProvider(true,new ExposureDatapackRegistryProvider(packOutput,lookupProvider,Set.of(Exposure.ID)));
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
                    .save(writer,Exposure.resource("photograph_copying"));

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
                {
                    ItemStack stack = Exposure.Items.PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/dungeon/skull_on_fire.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    biConsumer.accept(Exposure.LootTables.SIMPLE_DUNGEON_INJECT,
                            LootTable.lootTable().withPool(LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .add(LootItem.lootTableItem(Exposure.Items.PHOTOGRAPH.get())
                                                    .apply(SetNbtFunction.setTag(stack.getTag()))
                                    )
                                    .when(LootItemRandomChanceCondition.randomChance(.1f)
                                    )
                            )
                    );
                }
                {
                    ItemStack stack1 = Exposure.Items.PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack1, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/mineshaft/tunnel.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack stack2 = Exposure.Items.PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack2, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/mineshaft/skeleton.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack stack3 = Exposure.Items.PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack3, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/mineshaft/skeleton_smirk.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    biConsumer.accept(Exposure.LootTables.ABANDONED_MINESHAFT_INJECT,
                            LootTable.lootTable().withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Exposure.Items.PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack1.getTag())).setWeight(3)
                                    )
                                    .add(LootItem.lootTableItem(Exposure.Items.PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack2.getTag())).setWeight(3)
                                    )
                                    .add(LootItem.lootTableItem(Exposure.Items.PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack3.getTag())).setWeight(1)
                                    )
                                    .when(LootItemRandomChanceCondition.randomChance(.2f)
                                    )
                            )
                    );
                }
                {
                    ItemStack stack1 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack1, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/shipwreck/ship_dock_1.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack stack2 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack2, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/shipwreck/ship_dock_2.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack stack3 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack3, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/shipwreck/ship_dock_3.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack stack4 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack4, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/shipwreck/ship_dock_4.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    ItemStack film1Stack = film1();
                    ItemStack film2Stack = film2();

                    biConsumer.accept(Exposure.LootTables.SHIPWRECK_MAP_INJECT,
                            LootTable.lootTable()
                                    .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Exposure.Items.AGED_PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack1.getTag()))
                                    )
                                    .add(LootItem.lootTableItem(Exposure.Items.AGED_PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack2.getTag()))
                                    )
                                    .when(LootItemRandomChanceCondition.randomChance(.1f)
                                    )
                            )
                                    .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Exposure.Items.AGED_PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack3.getTag()))
                                    )
                                    .add(LootItem.lootTableItem(Exposure.Items.AGED_PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack4.getTag()))
                                    )
                                    .when(LootItemRandomChanceCondition.randomChance(.1f)
                                    )
                            )
                                    .withPool(LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .add(LootItem.lootTableItem(Exposure.Items.BLACK_AND_WHITE_FILM.get())
                                                    .apply(SetNbtFunction.setTag(film1Stack.getTag()))
                                            )
                                            .add(LootItem.lootTableItem(Exposure.Items.BLACK_AND_WHITE_FILM.get())
                                                    .apply(SetNbtFunction.setTag(film2Stack.getTag()))
                                            )
                                            .when(LootItemRandomChanceCondition.randomChance(.15f)
                                            )
                                    )
                                    .withPool(LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(2))
                                            .add(LootItem.lootTableItem(Items.RED_STAINED_GLASS_PANE)
                                            )
                                            .add(LootItem.lootTableItem(Items.GREEN_STAINED_GLASS_PANE)
                                            )
                                            .add(LootItem.lootTableItem(Items.BLUE_STAINED_GLASS_PANE)
                                            )
                                            .when(LootItemRandomChanceCondition.randomChance(.3f)
                                            )
                                    )
                    );
                }

                ItemStack photoCorridor1 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoCorridor1,new Frame(ExposureIdentifier.texture(
                    Exposure.resource("textures/exposure/stronghold/corridor_1.png")),
                    ExposureType.COLOR,
                    Photographer.EMPTY,
                    Collections.emptyList(),
                    ExtraData.EMPTY));

                ItemStack photoCorridor2 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoCorridor2,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/stronghold/corridor_2.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                ItemStack photoCorridor3 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoCorridor3,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/stronghold/corridor_3.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                ItemStack photoCorridor4 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoCorridor4,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/stronghold/corridor_4.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                biConsumer.accept(Exposure.LootTables.STRONGHOLD_CROSSING_INJECT,
                        LootTable.lootTable().withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        LootItem.lootTableItem(photoCorridor1.getItem())
                                        .apply(SetNbtFunction.setTag(photoCorridor1.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoCorridor2.getItem())
                                                .apply(SetNbtFunction.setTag(photoCorridor2.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoCorridor3.getItem())
                                                .apply(SetNbtFunction.setTag(photoCorridor3.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoCorridor4.getItem())
                                                .apply(SetNbtFunction.setTag(photoCorridor4.getTag()))
                                )
                                .when(LootItemRandomChanceCondition.randomChance(.3f)
                                )
                        )
                );


                ItemStack photoVillageAttack1 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillageAttack1,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/attack_1.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                ItemStack photoVillageAttack2 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillageAttack2,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/attack_2.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                //////////

                ItemStack photoVillage1 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillage1,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/village_1.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                ItemStack photoVillage2 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillage2,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/village_2.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));


                ItemStack photoVillage3 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillage3,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/village_3.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                ItemStack photoVillage4 = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                Exposure.DataComponents.setPhotographFrame(photoVillage4,new Frame(ExposureIdentifier.texture(
                        Exposure.resource("textures/exposure/village/village_4.png")),
                        ExposureType.COLOR,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        ExtraData.EMPTY));

                biConsumer.accept(Exposure.LootTables.VILLAGE_PLAINS_HOUSE_INJECT,
                        LootTable.lootTable().withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        LootItem.lootTableItem(photoVillage1.getItem())
                                                .apply(SetNbtFunction.setTag(photoVillage1.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoVillage2.getItem())
                                                .apply(SetNbtFunction.setTag(photoVillage2.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoVillage3.getItem())
                                                .apply(SetNbtFunction.setTag(photoVillage3.getTag()))
                                )
                                .add(
                                        LootItem.lootTableItem(photoVillage4.getItem())
                                                .apply(SetNbtFunction.setTag(photoVillage4.getTag()))
                                )
                                .when(LootItemRandomChanceCondition.randomChance(.3f)
                                )
                        )
                                .withPool(
                                        LootPool.lootPool()
                                                .setRolls(ConstantValue.exactly(1.0F))
                                                .add(
                                                        LootItem.lootTableItem(photoVillageAttack1.getItem())
                                                                .apply(SetNbtFunction.setTag(photoVillageAttack1.getTag()))
                                                )
                                                .add(
                                                        LootItem.lootTableItem(photoVillageAttack2.getItem())
                                                                .apply(SetNbtFunction.setTag(photoVillageAttack2.getTag()))
                                                )
                                                .when(LootItemRandomChanceCondition.randomChance(.15f)
                                                )
                                )
                );

            }
            protected ItemStack film1() {
                ItemStack filmStack = Exposure.Items.BLACK_AND_WHITE_FILM.get().getDefaultInstance();
                Exposure.DataComponents.setFilmFrameSize(filmStack,56);

                List<Frame> frames = new ArrayList<>();

                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/cargo_crates"));
                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/deck_sunset_1"));
                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/deck_sunset_2"));

                Exposure.DataComponents.setFilmFrames(filmStack, frames);
                return filmStack;
            }

            protected ItemStack film2() {
                ItemStack filmStack = Exposure.Items.BLACK_AND_WHITE_FILM.get().getDefaultInstance();
                Exposure.DataComponents.setFilmFrameSize(filmStack,56);

                List<Frame> frames = new ArrayList<>();

                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/cargo_gold_1"));
                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/cargo_gold_2"));
                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/deck_sunset_3"));
                frames.addAll(colorSet("textures/exposure/shipwreck/chromatic/deck_sunset_4"));

                Exposure.DataComponents.setFilmFrames(filmStack, frames);
                return filmStack;
            }

            List<Frame> colorSet(String s) {
                List<Frame> frames = new ArrayList<>();
                CompoundTag tagR = new CompoundTag();
                tagR.putString("color_channel","red");

                CompoundTag tagG = new CompoundTag();
                tagG.putString("color_channel","green");

                CompoundTag tagB = new CompoundTag();
                tagB.putString("color_channel","blue");
                frames.add(new Frame(
                        ExposureIdentifier.texture(Exposure.resource(s+"_r.png")),
                        ExposureType.BLACK_AND_WHITE,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        new ExtraData(tagR)));

                frames.add(new Frame(
                        ExposureIdentifier.texture(Exposure.resource(s+"_g.png")),
                        ExposureType.BLACK_AND_WHITE,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        new ExtraData(tagG)));

                frames.add(new Frame(
                        ExposureIdentifier.texture(Exposure.resource(s+"_b.png")),
                        ExposureType.BLACK_AND_WHITE,
                        Photographer.EMPTY,
                        Collections.emptyList(),
                        new ExtraData(tagB)));
                return frames;
            }

        }



        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        }

    }

}
