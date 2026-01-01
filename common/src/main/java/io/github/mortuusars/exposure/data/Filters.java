package io.github.mortuusars.exposure.data;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class Filters {

    public static final ResourceKey<Filter> BLACK_PANE = key("black_pane");
    public static final ResourceKey<Filter> BLUE_PANE = key("blue_pane");
    public static final ResourceKey<Filter> BROKEN_INTERPLANAR_PROJECTOR = key("broken_interplanar_projector");
    public static final ResourceKey<Filter> BROWN_PANE = key("brown_pane");
    public static final ResourceKey<Filter> CYAN_PANE = key("cyan_pane");
    public static final ResourceKey<Filter> GLASS_PANE = key("glass_pane");
    public static final ResourceKey<Filter> GRAY_PANE = key("gray_pane");
    public static final ResourceKey<Filter> GREEN_PANE = key("green_pane");
    public static final ResourceKey<Filter> INTERPLANAR_PROJECTOR = key("interplanar_projector");
    public static final ResourceKey<Filter> LIGHT_BLUE_PANE = key("light_blue_pane");
    public static final ResourceKey<Filter> LIGHT_GRAY_PANE = key("light_gray_pane");
    public static final ResourceKey<Filter> LIME_PANE = key("lime_pane");
    public static final ResourceKey<Filter> MAGENTA_PANE = key("magenta_pane");
    public static final ResourceKey<Filter> ORANGE_PANE = key("orange_pane");
    public static final ResourceKey<Filter> PINK_PANE = key("pink_pane");
    public static final ResourceKey<Filter> PURPLE_PANE = key("purple_pane");
    public static final ResourceKey<Filter> RED_PANE = key("red_pane");
    public static final ResourceKey<Filter> WHITE_PANE = key("white_pane");
    public static final ResourceKey<Filter> YELLOW_PANE = key("yellow_pane");

    public static Optional<Filter> of(RegistryAccess registryAccess, ItemStack stack) {
        return registryAccess.registryOrThrow(Exposure.Registries.FILTER)
                .stream()
                .filter(filter -> filter.predicate().matches(stack))
                .findFirst();
    }

    public static Optional<ResourceLocation> locationOf(RegistryAccess registryAccess, Filter filter) {
        return Optional.ofNullable(registryAccess.registryOrThrow(Exposure.Registries.FILTER).getKey(filter));
    }



    public static void bootstrap(BootstapContext<Filter> ctx) {
        ctx.register(BLACK_PANE,new Filter(ItemPredicate.Builder.item().of(Items.BLACK_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/black_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("1E1B1B")));
        ctx.register(BLUE_PANE,new Filter(ItemPredicate.Builder.item().of(Items.BLUE_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/blue_filter.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("253192")));
        ctx.register(BROKEN_INTERPLANAR_PROJECTOR,new Filter(ItemPredicate.Builder.item().of(Exposure.Items.BROKEN_INTERPLANAR_PROJECTOR.get()).build(),
                Exposure.resource("shaders/post/bsod.json"),
                Exposure.resource("textures/gui/filter/broken_interplanar_projector.png"), Color.WHITE));
        ctx.register(BROWN_PANE,new Filter(ItemPredicate.Builder.item().of(Items.BROWN_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/brown_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("51301A")));
        ctx.register(CYAN_PANE,new Filter(ItemPredicate.Builder.item().of(Items.CYAN_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/cyan_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("287697")));
        ctx.register(GLASS_PANE,new Filter(ItemPredicate.Builder.item().of(Items.GLASS_PANE).build(),
                Exposure.resource("shaders/post/crisp.json"),
                Exposure.resource("textures/gui/filter/glass.png"), Color.WHITE));
        ctx.register(GRAY_PANE,new Filter(ItemPredicate.Builder.item().of(Items.GRAY_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/gray_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("434343")));
        ctx.register(GREEN_PANE,new Filter(ItemPredicate.Builder.item().of(Items.GREEN_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/green_filter.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("3B511A")));
        ctx.register(INTERPLANAR_PROJECTOR,new Filter(ItemPredicate.Builder.item().of(Exposure.Items.INTERPLANAR_PROJECTOR.get()).build(),
                Exposure.resource("shaders/post/invert.json"),
                Exposure.resource("textures/gui/filter/interplanar_projector.png"), Color.WHITE));
        ctx.register(LIGHT_BLUE_PANE,new Filter(ItemPredicate.Builder.item().of(Items.LIGHT_BLUE_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/light_blue_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("6689D3")));
        ctx.register(LIGHT_GRAY_PANE,new Filter(ItemPredicate.Builder.item().of(Items.LIGHT_GRAY_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/light_gray_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("ABABAB")));
        ctx.register(LIME_PANE,new Filter(ItemPredicate.Builder.item().of(Items.LIME_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/lime_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("41CD34")));
        ctx.register(MAGENTA_PANE,new Filter(ItemPredicate.Builder.item().of(Items.MAGENTA_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/magenta_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("C354CD")));
        ctx.register(ORANGE_PANE,new Filter(ItemPredicate.Builder.item().of(Items.ORANGE_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/orange_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("EB8844")));
        ctx.register(PINK_PANE,new Filter(ItemPredicate.Builder.item().of(Items.PINK_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/pink_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("D88198")));
        ctx.register(PURPLE_PANE,new Filter(ItemPredicate.Builder.item().of(Items.PURPLE_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/purple_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("7B2FBE")));
        ctx.register(RED_PANE,new Filter(ItemPredicate.Builder.item().of(Items.RED_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/red_filter.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("B3312C")));
        ctx.register(WHITE_PANE,new Filter(ItemPredicate.Builder.item().of(Items.WHITE_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/white_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.WHITE));
        ctx.register(YELLOW_PANE,new Filter(ItemPredicate.Builder.item().of(Items.YELLOW_STAINED_GLASS_PANE).build(),
                Exposure.resource("shaders/post/yellow_tint.json"), Filter.DEFAULT_GLASS_TEXTURE, Color.fromHex("DECF2A")));
    }

    private static ResourceKey<Filter> key(String name) {
        return ResourceKey.create(Exposure.Registries.FILTER, Exposure.resource(name));
    }
}