package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.world.item.InterplanarProjectorItem;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow
    @Nullable
    private static String validateName(String itemName) {
        return null;
    }

    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Redirect(method = "setItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;validateName(Ljava/lang/String;)Ljava/lang/String;"))
    private String onSetItemName(String itemName) {
        if (!Config.Server.INTERPLANAR_PROJECTOR_LARGER_RENAMING_LIMIT.get()
                || !(inputSlots.getItem(0).getItem() instanceof InterplanarProjectorItem)) {
            return validateName(itemName);
        }
        String filtered = SharedConstants.filterText(itemName);
        return filtered.length() <= 150 ? filtered : null;
    }
}
