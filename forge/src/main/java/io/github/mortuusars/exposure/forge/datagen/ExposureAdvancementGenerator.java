package io.github.mortuusars.exposure.forge.datagen;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.advancements.predicate.CameraPredicate;
import io.github.mortuusars.exposure.advancements.predicate.EntityInFramePredicate;
import io.github.mortuusars.exposure.advancements.predicate.ExtraDataPredicate;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.advancements.trigger.FrameExposedTrigger;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.camera.CameraSetting;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
                        Optional.empty(),Optional.of(MinMaxBounds.Ints.exactly(1)),Optional.of(
                                List.of(new EntityInFramePredicate(Optional.of(new ResourceLocation("player")),Optional.empty(), MinMaxBounds.Ints.ANY))),
                        Optional.of(new ExtraDataPredicate(selfie))), LocationPredicate.ANY,new ArrayList<>()))
                .save(consumer,Exposure.resource("adventure/spotlight"),existingFileHelper);
    }
}
