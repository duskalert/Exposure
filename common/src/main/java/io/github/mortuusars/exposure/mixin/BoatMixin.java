package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.CameraStandItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class BoatMixin extends VehicleEntity {
    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.getItem() instanceof CameraStandItem cameraStandItem) {
            InteractionResult result = cameraStandItem.interactWithBoat(player, hand, ((Boat)(Object) this));
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
                return;
            }
        }
    }
}
