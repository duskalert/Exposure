package io.github.mortuusars.exposure.world.item.crafting.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographAgingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographCopyingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ComponentTransferringRecipeSerializer<T extends ComponentTransferringRecipe> implements RecipeSerializer<T> {

    protected final String target;
    private final Factory<T> factory;

    public ComponentTransferringRecipeSerializer(String target, Factory<T> factory) {
        this.target = target;
        this.factory = factory;
    }

    public String target() {
        return target;
    }

    public static final Factory<ComponentTransferringRecipe> COMPONENT_TRANSFERRING = (id, targetIngredient, ingredients, result) ->
      new ComponentTransferringRecipe(id,CraftingBookCategory.MISC,targetIngredient,ingredients,result);

    public static final Factory<ComponentTransferringRecipe> PHOTOGRAPH_AGING = PhotographAgingRecipe::new;
    public static final Factory<ComponentTransferringRecipe> PHOTOGRAPH_COPYING = PhotographCopyingRecipe::new;
    public static final Factory<ComponentTransferringRecipe> FILM_DEVELOPING = FilmDevelopingRecipe::new;

    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
        Ingredient targetIngredient = Ingredient.fromJson(GsonHelper.getNonNull(serializedRecipe, target));
        NonNullList<Ingredient> ingredients = getIngredients(GsonHelper.getAsJsonArray(serializedRecipe, "ingredients"));
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));
        if (targetIngredient.isEmpty())
            throw new JsonParseException("Recipe should have '"+target+"' ingredient.");
        return factory.create(recipeId,targetIngredient,ingredients,result);
    }

    private static NonNullList<Ingredient> getIngredients(JsonArray jsonArray) {
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

    @Override
    public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient transferredIngredient = Ingredient.fromNetwork(buffer);
        int ingredientsCount = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsCount, Ingredient.EMPTY);
        ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
        ItemStack result = buffer.readItem();

        return factory.create(recipeId,transferredIngredient,ingredients,result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        recipe.getSourceIngredient().toNetwork(buffer);
        buffer.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buffer);
        }
        buffer.writeItem(recipe.getResult());
    }

    public interface Factory<T>{
        T create(ResourceLocation id,Ingredient targetIngredient,NonNullList<Ingredient> ingredients,ItemStack result);
    }
}
