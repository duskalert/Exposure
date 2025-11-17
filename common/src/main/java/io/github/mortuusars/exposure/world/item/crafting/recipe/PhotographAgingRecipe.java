package io.github.mortuusars.exposure.world.item.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class PhotographAgingRecipe extends ComponentTransferringRecipe {
    public PhotographAgingRecipe(ResourceLocation id, Ingredient sourceIngredient,
                                 NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id,CraftingBookCategory.MISC, sourceIngredient, ingredients, result);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

        for (int i = 0; i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof BrushItem) {
                stack = stack.copy();
                int damage = stack.getDamageValue() + 1;
                stack.setDamageValue(damage);
                if (damage >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
                remainingItems.set(i, stack);
            }
        }

        return remainingItems;
    }

    public static class Serializer implements RecipeSerializer<PhotographAgingRecipe> {
        @Override
        public @NotNull PhotographAgingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            Ingredient photographIngredient = Ingredient.fromJson(GsonHelper.getNonNull(serializedRecipe, "photograph"));
            NonNullList<Ingredient> ingredients = getIngredients(GsonHelper.getAsJsonArray(serializedRecipe, "ingredients"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));

            if (photographIngredient.isEmpty())
                throw new JsonParseException("Recipe should have 'photograph' ingredient.");

            return new PhotographAgingRecipe(recipeId, photographIngredient, ingredients, result);
        }

        @Override
        public @NotNull PhotographAgingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient transferredIngredient = Ingredient.fromNetwork(buffer);
            int ingredientsCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();

            return new PhotographAgingRecipe(recipeId, transferredIngredient, ingredients, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PhotographAgingRecipe recipe) {
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
