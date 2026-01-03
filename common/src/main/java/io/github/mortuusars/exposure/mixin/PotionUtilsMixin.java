package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PotionUtils.class)
@Debug(export = true)
public abstract class PotionUtilsMixin {

    @Redirect(method = "getColor(Lnet/minecraft/world/item/ItemStack;)I",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getColor(Ljava/util/Collection;)I"))
    private static int customPotionColor(Collection<MobEffectInstance> effects,ItemStack stack) {
        if (Config.Common.DIFFERENT_DEVELOPING_POTION_COLORS.get()) {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion == Potions.MUNDANE) {
                return 0xFF424D8F;
            } else if (potion == Potions.AWKWARD) {
                return 0xFF653594;
            } else if (potion == Potions.THICK) {
                return 0xFF3E7782;
            }
        }
        return PotionUtils.getColor(effects);
    }

    @Inject(method = "getColor(Lnet/minecraft/world/item/alchemy/Potion;)I", at = @At("HEAD"), cancellable = true)
    private static void customColors(Potion potion, CallbackInfoReturnable<Integer> cir) {
        if (Config.Common.DIFFERENT_DEVELOPING_POTION_COLORS.get()) {
            if (potion == Potions.MUNDANE) {
                cir.setReturnValue(0xFF424D8F);
            } else if (potion == Potions.AWKWARD) {
                cir.setReturnValue(0xFF653594);
            } else if (potion == Potions.THICK) {
                cir.setReturnValue(0xFF3E7782);
            }
        }
    }
}
