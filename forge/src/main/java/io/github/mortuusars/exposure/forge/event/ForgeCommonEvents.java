package io.github.mortuusars.exposure.forge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import io.github.mortuusars.exposure.network.forge.PacketsImpl;
import io.github.mortuusars.exposure.network.packet.C2SPackets;
import io.github.mortuusars.exposure.network.packet.CommonPackets;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.S2CPackets;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ForgeCommonEvents {
    @Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // Makes stats show up in stat screen
                Exposure.Stats.STATS.forEach((location, statFormatter) -> {
                    Stats.CUSTOM.get(location);
                });
            });
            PacketsImpl.register();
        }

        @SubscribeEvent
        public static void addDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(Exposure.Registries.COLOR_PALETTE, ColorPalette.CODEC, ColorPalette.CODEC);
            event.dataPackRegistry(Exposure.Registries.LENS, Lens.CODEC, Lens.CODEC);
            event.dataPackRegistry(Exposure.Registries.FILTER, Filter.CODEC, Filter.CODEC);
        }
    }

    public static <MSG extends Packet> BiConsumer<MSG, Supplier<NetworkEvent.Context>> wrapS2C() {
        return ((msg, contextSupplier) -> {
            contextSupplier.get().enqueueWork(() -> msg.handle(PacketFlow.CLIENTBOUND, Minecrft.player()));
            contextSupplier.get().setPacketHandled(true);
        });
    }

    public static <MSG extends Packet> BiConsumer<MSG, Supplier<NetworkEvent.Context>> wrapC2S() {
        return ((msg, contextSupplier) -> {
            ServerPlayer player = contextSupplier.get().getSender();
            contextSupplier.get().enqueueWork(() -> msg.handle(PacketFlow.SERVERBOUND, player));
            contextSupplier.get().setPacketHandled(true);
        });
    }

    @Mod.EventBusSubscriber(modid = Exposure.ID)
    public static class GameBus {
        @SubscribeEvent
        public static void serverStarted(ServerStartedEvent event) {
            ServerEvents.serverStarted(event.getServer());
        }

        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            ServerEvents.syncDatapack(event.getPlayers());
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            CommonEvents.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
            BlockEntity object = event.getObject();
            if (object instanceof LightroomBlockEntity lightroomBlockEntity) {
                event.addCapability(Exposure.resource("lightroom"), new ICapabilityProvider() {

                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                        if (capability == ForgeCapabilities.ITEM_HANDLER) {
                            return LazyOptional.of(() -> side == null ? new InvWrapper(lightroomBlockEntity) : new SidedInvWrapper(lightroomBlockEntity, side)).cast();
                        }
                        return LazyOptional.empty();
                    }
                });
            }
        }
    }
}
