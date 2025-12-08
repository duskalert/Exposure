package io.github.mortuusars.exposure.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.world.item.crafting.recipe.serializer.ComponentTransferringRecipeSerializer;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Consumer;

public class ExposureRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {


    final ComponentTransferringRecipeSerializer<?>  serializer;
    Ingredient targetIngredient;
    NonNullList<Ingredient> ingredients = NonNullList.create();
    Item result;

    public ExposureRecipeBuilder(ComponentTransferringRecipeSerializer<?>  serializer,Ingredient targetIngredient, Item result) {
        this.serializer = serializer;
        this.targetIngredient = targetIngredient;
        this.result = result;
    }

    @Override
    public RecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
        return null;
    }

    public RecipeBuilder with(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public RecipeBuilder with(ItemLike ingredient) {
        return with(Ingredient.of(ingredient));
    }

    public static ExposureRecipeBuilder exposure(ComponentTransferringRecipeSerializer<?>  serializer,
                                                 Ingredient targetIngredient,Item result) {
        return new ExposureRecipeBuilder(serializer,targetIngredient,result);
    }

    public static ExposureRecipeBuilder exposure(ComponentTransferringRecipeSerializer<?>  serializer,
                                                 ItemLike targetItem, Item result) {
        return exposure(serializer,Ingredient.of(targetItem),result);
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
        finishedRecipeConsumer.accept(new Result(serializer,recipeId,targetIngredient,ingredients,this.result, determineBookCategory(RecipeCategory.MISC)));
    }

    public static class Result extends CraftingResult {
        private final ComponentTransferringRecipeSerializer<?>  serializer;
        private final ResourceLocation id;
        private final Ingredient targetIngredient;
        private final NonNullList<Ingredient> ingredients;
        private final Item result;

        protected Result(ComponentTransferringRecipeSerializer<?> serializer, ResourceLocation id, Ingredient targetIngredient, NonNullList<Ingredient> ingredients, Item result,
                         CraftingBookCategory category) {
            super(category);
            this.serializer = serializer;
            this.id = id;
            this.targetIngredient = targetIngredient;
            this.ingredients = ingredients;
            this.result = result;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            super.serializeRecipeData(json);
            json.add(serializer.target(),targetIngredient.toJson());
            JsonArray jsonArray = new JsonArray();

            for (Ingredient ingredient : this.ingredients) {
                jsonArray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonArray);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());

            json.add("result", jsonObject);
        }
    }
}
