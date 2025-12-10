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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
                    ItemStack stack = Exposure.Items.AGED_PHOTOGRAPH.get().getDefaultInstance();

                    Exposure.DataComponents.setPhotographFrame(stack, new Frame(ExposureIdentifier.texture(
                            Exposure.resource("textures/exposure/dungeon/skull_on_fire.png")),
                            ExposureType.COLOR,
                            Photographer.EMPTY,
                            Collections.emptyList(),
                            ExtraData.EMPTY));

                    biConsumer.accept(Exposure.LootTables.SHIPWRECK_MAP_INJECT,
                            LootTable.lootTable().withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Exposure.Items.PHOTOGRAPH.get())
                                            .apply(SetNbtFunction.setTag(stack.getTag()))
                                    )
                                    .when(LootItemRandomChanceCondition.randomChance(.1f)
                                    )
                            )
                    );
                    //{
                    //  "type": "minecraft:chest",
                    //  "pools": [
                    //    {
                    //      "rolls": 1,
                    //      "entries": [
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:aged_photograph",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:photograph_frame": {
                    //                  "identifier": {
                    //                    "texture": "exposure:textures/exposure/shipwreck/ship_dock_1.png"
                    //                  }
                    //                }
                    //              }
                    //            }
                    //          ]
                    //        },
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:aged_photograph",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:photograph_frame": {
                    //                  "identifier": {
                    //                    "texture": "exposure:textures/exposure/shipwreck/ship_dock_2.png"
                    //                  }
                    //                }
                    //              }
                    //            }
                    //          ]
                    //        }
                    //      ],
                    //      "conditions": [
                    //        {
                    //          "condition": "minecraft:random_chance",
                    //          "chance": 0.1
                    //        }
                    //      ]
                    //    },
                    //    {
                    //      "rolls": 1,
                    //      "entries": [
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:aged_photograph",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:photograph_frame": {
                    //                  "identifier": {
                    //                    "texture": "exposure:textures/exposure/shipwreck/ship_dock_3.png"
                    //                  }
                    //                }
                    //              }
                    //            }
                    //          ]
                    //        },
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:aged_photograph",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:photograph_frame": {
                    //                  "identifier": {
                    //                    "texture": "exposure:textures/exposure/shipwreck/ship_dock_4.png"
                    //                  }
                    //                }
                    //              }
                    //            }
                    //          ]
                    //        }
                    //      ],
                    //      "conditions": [
                    //        {
                    //          "condition": "minecraft:random_chance",
                    //          "chance": 0.1
                    //        }
                    //      ]
                    //    },
                    //    {
                    //      "rolls": 1,
                    //      "entries": [
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:black_and_white_film",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:film_frame_size": 56,
                    //                "exposure:film_frames": [
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_crates_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_crates_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_crates_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_1_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_1_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_1_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_2_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_2_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_2_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  }
                    //                ]
                    //              }
                    //            }
                    //          ]
                    //        },
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "exposure:black_and_white_film",
                    //          "functions": [
                    //            {
                    //              "function": "minecraft:set_components",
                    //              "components": {
                    //                "exposure:film_frame_size": 56,
                    //                "exposure:film_frames": [
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_1_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_1_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_1_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_2_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_2_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/cargo_gold_2_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_3_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_3_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_3_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_4_r.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "red"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_4_g.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "green"
                    //                    }
                    //                  },
                    //                  {
                    //                    "identifier": {
                    //                      "texture": "exposure:textures/exposure/shipwreck/chromatic/deck_sunset_4_b.png"
                    //                    },
                    //                    "type": "black_and_white",
                    //                    "extra_data": {
                    //                      "color_channel": "blue"
                    //                    }
                    //                  }
                    //                ]
                    //              }
                    //            }
                    //          ]
                    //        }
                    //      ],
                    //      "conditions": [
                    //        {
                    //          "condition": "minecraft:random_chance",
                    //          "chance": 0.15
                    //        }
                    //      ]
                    //    },
                    //    {
                    //      "rolls": 2,
                    //      "entries": [
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "minecraft:red_stained_glass_pane"
                    //        },
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "minecraft:green_stained_glass_pane"
                    //        },
                    //        {
                    //          "type": "minecraft:item",
                    //          "name": "minecraft:blue_stained_glass_pane"
                    //        }
                    //      ],
                    //      "conditions": [
                    //        {
                    //          "condition": "minecraft:random_chance",
                    //          "chance": 0.3
                    //        }
                    //      ]
                    //    }
                    //  ]
                    //}
                }
            }
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        }

    }

}
