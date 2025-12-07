package io.github.mortuusars.exposure.forge;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.forge.datagen.ExposureDatagen;
import io.github.mortuusars.exposure.forge.loot.ConfigurableAddTableLootModifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Exposure.ID)
public class ExposureForge {
    public ExposureForge() {
        Exposure.init();

        Exposure.Stats.STATS.forEach((location, formatter) -> {
            RegisterImpl.CUSTOM_STATS.register(location.getPath(), () -> location);
        });

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

         IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Preconditions.checkNotNull(modEventBus);

        modEventBus.addListener(ExposureDatagen::gather);
        RegisterImpl.BLOCKS.register(modEventBus);
        RegisterImpl.BLOCK_ENTITY_TYPES.register(modEventBus);
        RegisterImpl.ENTITY_TYPES.register(modEventBus);
        RegisterImpl.ITEMS.register(modEventBus);
        RegisterImpl.CREATIVE_MODE_TAB.register(modEventBus);
        RegisterImpl.MENU_TYPES.register(modEventBus);
        RegisterImpl.RECIPE_TYPES.register(modEventBus);
        RegisterImpl.RECIPE_SERIALIZERS.register(modEventBus);
       // RegisterImpl.ITEM_SUB_PREDICATES.register(modEventBus);
       // RegisterImpl.ENTITY_SUB_PREDICATES.register(modEventBus);
        RegisterImpl.SOUND_EVENTS.register(modEventBus);
        RegisterImpl.COMMAND_ARGUMENT_TYPES.register(modEventBus);
        RegisterImpl.WORLD_GEN_FEATURES.register(modEventBus);
        RegisterImpl.PARTICLE_TYPES.register(modEventBus);
        RegisterImpl.CUSTOM_STATS.register(modEventBus);
        LootModifiers.LOOT_MODIFIERS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ExposureForgeClient.init(modEventBus);
        }
    }

    public static class LootModifiers {
        private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
                DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Exposure.ID);

        public static final RegistryObject<Codec<ConfigurableAddTableLootModifier>> ADD_TABLE =
                LOOT_MODIFIERS.register("add_table", () -> ConfigurableAddTableLootModifier.CODEC);
    }
}
