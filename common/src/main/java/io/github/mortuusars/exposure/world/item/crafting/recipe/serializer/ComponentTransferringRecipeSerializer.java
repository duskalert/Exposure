package io.github.mortuusars.exposure.world.item.crafting.recipe.serializer;

import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ComponentTransferringRecipeSerializer<T extends ComponentTransferringRecipe> implements RecipeSerializer<T> {

    public ComponentTransferringRecipeSerializer(String recipeName, String sourceName, RecipeConstructor<T> constructor) {
    }

    public ComponentTransferringRecipeSerializer(String serializedSourceIngredientName, RecipeConstructor<T> constructor) {
        this("component_transferring", serializedSourceIngredientName, constructor);
    }

    public ComponentTransferringRecipeSerializer(RecipeConstructor<T> constructor) {
        this("component_transferring", "source_ingredient", constructor);
    }

    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
        return null;
    }

    @Override
    public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {

    }

    @FunctionalInterface
    public interface RecipeConstructor<T extends ComponentTransferringRecipe> {
        T create(CraftingBookCategory arg, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result);
    }
}
