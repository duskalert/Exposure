package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WrittenBookItem.class)
public class WrittenBookItemMixin {
    @Inject(method = "getPageCount", at = @At("HEAD"), cancellable = true)
    private static void onGetPageCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof AlbumItem albumItem) {
            cir.setReturnValue(albumItem.getContent(stack).pages().size());
        }
        else if (stack.getItem() instanceof SignedAlbumItem signedAlbumItem) {
            cir.setReturnValue(signedAlbumItem.getContent(stack).pages().size());
        }
    }
}
