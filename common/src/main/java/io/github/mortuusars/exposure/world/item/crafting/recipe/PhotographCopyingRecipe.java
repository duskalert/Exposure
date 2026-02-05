package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class PhotographCopyingRecipe extends ComponentTransferringRecipe {
    public PhotographCopyingRecipe(ResourceLocation id, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id,CraftingBookCategory.MISC, sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_COPYING.get();
    }

    @Override
    public @NotNull ItemStack transferNbt(ItemStack photographStack, ItemStack recipeResultStack) {
        if (photographStack.getItem() instanceof PhotographItem
                && photographStack.hasTag() && Exposure.DataComponents.getPhotographGeneration(photographStack, 0) < 2) {
            ItemStack result = super.transferNbt(photographStack, recipeResultStack);
            Exposure.DataComponents.setPhotographGeneration(result,
                  Math.min(Exposure.DataComponents.getPhotographGeneration(result, 0) + 1, 2));
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);;

        for(int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof PhotographItem) {
                ItemStack remainingPhotographStack = stack.copy();
                remainingPhotographStack.setCount(1);
                remainingItems.set(i, remainingPhotographStack);
            }
        }

        return remainingItems;
    }

}
