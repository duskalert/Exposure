package io.github.mortuusars.exposure.world.item.crafting.recipe.serializer;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class ComponentTransferringRecipeSerializer<T extends ComponentTransferringRecipe> implements RecipeSerializer<T> {

    public ComponentTransferringRecipeSerializer(String recipeName, String sourceName, RecipeConstructor<T> constructor) {
    }

    public ComponentTransferringRecipeSerializer(String serializedSourceIngredientName, RecipeConstructor<T> constructor) {
        this("component_transferring", serializedSourceIngredientName, constructor);
    }

    public ComponentTransferringRecipeSerializer(RecipeConstructor<T> constructor) {
        this("component_transferring", "source_ingredient", constructor);
    }

    @FunctionalInterface
    public interface RecipeConstructor<T extends ComponentTransferringRecipe> {
        T create(CraftingBookCategory arg, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result);
    }
}
