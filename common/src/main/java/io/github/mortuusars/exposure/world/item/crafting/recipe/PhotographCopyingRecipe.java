package io.github.mortuusars.exposure.world.item.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
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
                && photographStack.hasTag() && WrittenBookItem.getGeneration(photographStack) < 2) {
            ItemStack result = super.transferNbt(photographStack, recipeResultStack);
            Exposure.DataComponents.setPhotographGeneration(result, Math.min(WrittenBookItem.getGeneration(result) + 1, 2));
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

    public static class Serializer implements RecipeSerializer<PhotographCopyingRecipe> {
        @Override
        public @NotNull PhotographCopyingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            Ingredient photographIngredient = Ingredient.fromJson(GsonHelper.getNonNull(serializedRecipe, "photograph"));
            NonNullList<Ingredient> ingredients = getIngredients(GsonHelper.getAsJsonArray(serializedRecipe, "ingredients"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));

            if (photographIngredient.isEmpty())
                throw new JsonParseException("Recipe should have 'photograph' ingredient.");

            return new PhotographCopyingRecipe(recipeId, photographIngredient, ingredients, result);
        }

        @Override
        public @NotNull PhotographCopyingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient transferredIngredient = Ingredient.fromNetwork(buffer);
            int ingredientsCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();

            return new PhotographCopyingRecipe(recipeId, transferredIngredient, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PhotographCopyingRecipe recipe) {
            recipe.getSourceIngredient().toNetwork(buffer);
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.getResult());
        }

        private NonNullList<Ingredient> getIngredients(JsonArray jsonArray) {
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
                if (!ingredient.isEmpty())
                    ingredients.add(ingredient);
            }

            if (ingredients.isEmpty())
                throw new JsonParseException("No ingredients for a recipe.");
            else if (ingredients.size() > 3 * 3)
                throw new JsonParseException("Too many ingredients for a recipe. The maximum is 9.");
            return ingredients;
        }
    }
}
