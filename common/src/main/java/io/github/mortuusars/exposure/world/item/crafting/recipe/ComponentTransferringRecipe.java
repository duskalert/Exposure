package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ComponentTransferringRecipe extends CustomRecipe {
    private final Ingredient sourceIngredient;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public ComponentTransferringRecipe(ResourceLocation id,CraftingBookCategory category, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id,category);
        this.sourceIngredient = sourceIngredient;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.COMPONENT_TRANSFERRING.get();
    }

    public @NotNull Ingredient getSourceIngredient() {
        return sourceIngredient;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registries) {
        return getResult();
    }

    public @NotNull ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        if (getSourceIngredient().isEmpty() || ingredients.isEmpty())
            return false;

        List<Ingredient> unmatchedIngredients = new ArrayList<>(ingredients);
        unmatchedIngredients.add(0, getSourceIngredient());

        int itemsInCraftingGrid = 0;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty())
                itemsInCraftingGrid++;

            if (itemsInCraftingGrid > ingredients.size() + 1)
                return false;

            if (!unmatchedIngredients.isEmpty()) {
                for (int j = 0; j < unmatchedIngredients.size(); j++) {
                    if (unmatchedIngredients.get(j).test(stack)) {
                        unmatchedIngredients.remove(j);
                        break;
                    }
                }
            }
        }

        return unmatchedIngredients.isEmpty() && itemsInCraftingGrid == ingredients.size() + 1;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        for (int index = 0; index < input.getContainerSize(); index++) {
            ItemStack itemStack = input.getItem(index);

            if (getSourceIngredient().test(itemStack)) {
                return transferNbt(itemStack, getResultItem(registries).copy());
            }
        }

        return getResultItem(registries);
    }

    public @NotNull ItemStack transferNbt(ItemStack transferIngredientStack, ItemStack recipeResultStack) {
        @Nullable CompoundTag transferTag = transferIngredientStack.getTag();
        if (transferTag != null) {
            if (recipeResultStack.getTag() != null)
                recipeResultStack.getTag().merge(transferTag);
            else
                recipeResultStack.setTag(transferTag.copy());
        }
        return recipeResultStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return ingredients.size() <= width * height;
    }
}
