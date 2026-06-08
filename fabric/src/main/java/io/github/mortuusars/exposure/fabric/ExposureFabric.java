package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.fabric.PacketsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExposureFabric implements ModInitializer {
    // Server field to access when no other objects are available to get it from.
    public static @Nullable MinecraftServer server = null;

    @Override
    public void onInitialize() {
        Exposure.init();

        DynamicRegistries.registerSynced(Exposure.Registries.COLOR_PALETTE, ColorPalette.CODEC, ColorPalette.CODEC);
        DynamicRegistries.registerSynced(Exposure.Registries.LENS, Lens.CODEC, Lens.CODEC);
        DynamicRegistries.registerSynced(Exposure.Registries.FILTER, Filter.CODEC, Filter.CODEC);

        ForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        ForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ForgeConfigRegistry.INSTANCE.register(Exposure.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        CommandRegistrationCallback.EVENT.register(CommonEvents::registerCommands);

        Exposure.Stats.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerEvents.serverStarted(server);
            ExposureFabric.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ServerEvents.serverStopped(server);
            ExposureFabric.server = null;
        });

        LootTableEvents.MODIFY.register(ExposureFabric::modifyLoot);

        PacketsImpl.registerC2SPackets();
    }

    private static void modifyLoot(ResourceManager resourceManager, LootDataManager lootDataManager, ResourceLocation location, LootTable.Builder builder, LootTableSource lootTableSource) {
        if (!Config.Common.GENERATE_LOOT.get() || !lootTableSource.isBuiltin())
            return;

        if (BuiltInLootTables.SIMPLE_DUNGEON.equals(location)) {
            builder.pool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(Exposure.LootTables.SIMPLE_DUNGEON_INJECT))
                    .build());
        }
        if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(location)) {
            builder.pool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(Exposure.LootTables.ABANDONED_MINESHAFT_INJECT))
                    .build());
        }
        if (BuiltInLootTables.STRONGHOLD_CROSSING.equals(location)) {
            builder.pool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(Exposure.LootTables.STRONGHOLD_CROSSING_INJECT))
                    .build());
        }
        if (BuiltInLootTables.VILLAGE_PLAINS_HOUSE.equals(location)) {
            builder.pool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(Exposure.LootTables.VILLAGE_PLAINS_HOUSE_INJECT))
                    .build());
        }
        if (BuiltInLootTables.SHIPWRECK_MAP.equals(location)) {
            builder.pool(LootPool.lootPool()
                    .add(LootTableReference.lootTableReference(Exposure.LootTables.SHIPWRECK_MAP_INJECT))
                    .build());
        }
    }
}
