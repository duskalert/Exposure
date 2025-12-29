package io.github.mortuusars.exposure.forge.datagen;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancements.predicate.*;
import io.github.mortuusars.exposure.advancements.trigger.FrameExposedTrigger;
import io.github.mortuusars.exposure.advancements.trigger.FramePrintedTrigger;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.camera.CameraSetting;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.client.CameraType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ExposureAdvancementGenerator implements ForgeAdvancementProvider.AdvancementGenerator {
    @Override
    public void generate(HolderLookup.Provider arg, Consumer<Advancement> consumer, ExistingFileHelper existingFileHelper) {
        ItemStack camera = Exposure.Items.CAMERA.get().getDefaultInstance();
        Exposure.DataComponents.setCameraActive(camera,true);
        Advancement root = Advancement.Builder.advancement()
                .parent(new ResourceLocation("adventure/root"))
                .display(camera, Component.translatable("advancement.exposure.exposure.title"),
                        Component.translatable("advancement.exposure.exposure.description"),
                        null, FrameType.TASK, true, true, false)
                .addCriterion("expose_frame", new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                        CameraPredicate.ANY, FramePredicate.ANY, LocationPredicate.ANY,new ArrayList<>()))
                .save(consumer, Exposure.resource("adventure/exposure"),existingFileHelper);

        ExtraData selfie = new ExtraData();
        selfie.put(Frame.SELFIE,true);

        Advancement spotlight = Advancement.Builder.advancement()
                .parent(root)
                .display(Items.PLAYER_HEAD,Component.translatable("advancement.exposure.spotlight.title"),
                        Component.translatable("advancement.exposure.spotlight.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("take_selfie",new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                        CameraPredicate.ANY, new FramePredicate(Optional.empty(),Optional.empty(),
                        Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                        MinMaxBounds.Ints.ANY,MinMaxBounds.Ints.exactly(1),
                                List.of(new EntityInFramePredicate(Optional.of(new ResourceLocation("player")),Optional.empty(), MinMaxBounds.Ints.ANY)),
                        new ExtraDataPredicate(selfie)), LocationPredicate.ANY,new ArrayList<>()))
                .save(consumer,Exposure.resource("adventure/spotlight"),existingFileHelper);

        Advancement splittingThePhoton = Advancement.Builder.advancement()
                .parent(root)
                .display(Items.RED_STAINED_GLASS_PANE,Component.translatable("advancement.exposure.splitting_the_photon.title"),
                        Component.translatable("advancement.exposure.splitting_the_photon.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("expose_frame_with_rgb_filter",new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                        new CameraPredicate(ItemPredicate.ANY,ItemPredicate.Builder.item().of(Exposure.Items.BLACK_AND_WHITE_FILM.get()).build(),
                                ItemPredicate.ANY,ItemPredicate.ANY,ItemPredicate.Builder.item().of(Items.RED_STAINED_GLASS_PANE
                        ,Items.GREEN_STAINED_GLASS_PANE,Items.BLUE_STAINED_GLASS_PANE).build(),LocationPredicate.ANY), FramePredicate.ANY, LocationPredicate.ANY,new ArrayList<>()))
                .save(consumer,Exposure.resource("adventure/splitting_the_photon"),existingFileHelper);

        Advancement momentInTime = Advancement.Builder.advancement()
                .parent(root)
                .display(Exposure.Items.PHOTOGRAPH.get(),Component.translatable("advancement.exposure.moment_in_time.title"),
                        Component.translatable("advancement.exposure.moment_in_time.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("print_photograph",new FramePrintedTrigger.TriggerInstance(ContextAwarePredicate.ANY,LocationPredicate.ANY,FramePredicate.ANY,
                        ItemPredicate.Builder.item().of(Exposure.Items.PHOTOGRAPH.get()).build()))
                .save(consumer,Exposure.resource("adventure/moment_in_time"),existingFileHelper);

        ExtraData onStand = new ExtraData();
        onStand.put(Frame.ON_STAND,true);

        Advancement familyPortrait = Advancement.Builder.advancement()
                .parent(root)
                .display(Exposure.Items.PHOTOGRAPH.get(),Component.translatable("advancement.exposure.family_portrait.title"),
                        Component.translatable("advancement.exposure.family_portrait.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("captured_entities",
                        new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,CameraPredicate.ANY,
                                new FramePredicate(Optional.empty(),Optional.empty(),
                                        Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        List.of(),ExtraDataPredicate.ANY),LocationPredicate.ANY,
                        List.of(
                                ContextAwarePredicate.create(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.CAT))
                                                        .subPredicate(new TamedPredicate(true)).build()).build()
                                ),
                                ContextAwarePredicate.create(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.WOLF))
                                                        .subPredicate(new TamedPredicate(true)).build()).build()
                                ),
                                ContextAwarePredicate.create(
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER)).build()).build()
                                )
                        )))
                .save(consumer,Exposure.resource("adventure/family_portrait"),existingFileHelper);

        ExtraData flash = new ExtraData();
        flash.put(Frame.FLASH,true);

        Advancement lightsUp = Advancement.Builder.advancement()
                .parent(spotlight)
                .display(Items.REDSTONE_LAMP,Component.translatable("advancement.exposure.lights_up.title"),
                        Component.translatable("advancement.exposure.lights_up.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("flash_in_darkness",
                        new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,CameraPredicate.ANY,
                                new FramePredicate(Optional.empty(),Optional.empty(),
                                        Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.atMost(3),
                                        MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        List.of(),new ExtraDataPredicate(flash)),LocationPredicate.ANY,
                                List.of()))
                .save(consumer,Exposure.resource("adventure/lights_up"),existingFileHelper);

        Advancement exposedPaparazzi = Advancement.Builder.advancement()
                .parent(lightsUp)
                .display(Items.JACK_O_LANTERN,Component.translatable("advancement.exposure.exposed_paparazzi.title"),
                        Component.translatable("advancement.exposure.exposed_paparazzi.description"),
                        null,FrameType.TASK,true,true,true)
                .addCriterion("shoot_with_flash",
                        new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,CameraPredicate.ANY,
                                new FramePredicate(Optional.empty(),Optional.empty(),
                                        Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        MinMaxBounds.Ints.between(13000,23000), MinMaxBounds.Ints.ANY,
                                        List.of(
                                                new EntityInFramePredicate(Optional.of(new ResourceLocation("villager")),
                                                        Optional.empty(),MinMaxBounds.Ints.atLeast(6))
                                        ),new ExtraDataPredicate(flash)),LocationPredicate.ANY,
                                List.of()))
                .save(consumer,Exposure.resource("adventure/exposed_paparazzi"),existingFileHelper);

        ExtraData chromatic = new ExtraData();
        chromatic.put(Frame.CHROMATIC,true);

        Advancement complexCompositeCompound = Advancement.Builder.advancement()
                .parent(splittingThePhoton)
                .display(Exposure.Items.CHROMATIC_SHEET.get(),Component.translatable("advancement.exposure.complex_composite_compound.title"),
                        Component.translatable("advancement.exposure.complex_composite_compound.description"),
                        null,FrameType.TASK,true,true,false)
                .addCriterion("print_chromatic_photograph",new FramePrintedTrigger.TriggerInstance(
                        ContextAwarePredicate.ANY,LocationPredicate.ANY,FramePredicate.ANY,
                        ItemPredicate.Builder.item().of(Exposure.Items.PHOTOGRAPH.get(),Exposure.Items.AGED_PHOTOGRAPH.get())
                                .hasNbt(chromatic)
                                .build()))
                .save(consumer,Exposure.resource("adventure/complex_composite_shirt"),existingFileHelper);

        Advancement unforeseenConsequences = Advancement.Builder.advancement()
                .parent(exposedPaparazzi)
                .display(Items.ENDER_EYE,Component.translatable("advancement.exposure.unforeseen_consequences.title"),
                        Component.translatable("advancement.exposure.unforeseen_consequences.description"),
                        null,FrameType.TASK,true,true,true)
                .addCriterion("photograph_enderman_eyes",
                        new PlayerTrigger.TriggerInstance(Exposure.ExposureCriteriaTriggers.PHOTOGRAPH_ENDERMAN_EYES.getId(),ContextAwarePredicate.ANY))
                .save(consumer,Exposure.resource("adventure/unforeseen_consequences"),existingFileHelper);

        Advancement photosOfSpiderMan = Advancement.Builder.advancement()
                .parent(unforeseenConsequences)
                .display(Items.COBWEB,Component.translatable("advancement.exposure.photos_of_spider_man.title"),
                        Component.translatable("advancement.exposure.photos_of_spider_man.description"),
                        null,FrameType.TASK,true,true,true)
                .addCriterion("photograph_spider_man",
                        new FrameExposedTrigger.TriggerInstance(ContextAwarePredicate.ANY,CameraPredicate.ANY,
                                new FramePredicate(Optional.empty(),Optional.empty(),
                                        Optional.empty(),Optional.empty(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                                        List.of(),ExtraDataPredicate.ANY),LocationPredicate.ANY,
                                List.of(
                                        ContextAwarePredicate.create(
                                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.SKELETON))
                                                                .vehicle(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.SPIDER))
                                                                .build()).build()
                                        ).build()
                                                ))))
                .save(consumer,Exposure.resource("adventure/photos_of_spider_man"),existingFileHelper);
    }

}
