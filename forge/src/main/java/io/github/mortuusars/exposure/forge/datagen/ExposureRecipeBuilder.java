package io.github.mortuusars.exposure.forge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.world.item.crafting.recipe.serializer.ComponentTransferringRecipeSerializer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ExposureRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final ComponentTransferringRecipeSerializer<?> serializer;
    private final RecipeCategory category;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    private final Ingredient targetIngredient;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Item result;

    public ExposureRecipeBuilder(ComponentTransferringRecipeSerializer<?> serializer, RecipeCategory category, Ingredient targetIngredient, Item result) {
        this.serializer = serializer;
        this.category = category;
        this.targetIngredient = targetIngredient;
        this.result = result;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull CriterionTriggerInstance criterionTrigger) {
        this.advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    public ExposureRecipeBuilder with(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public ExposureRecipeBuilder with(ItemLike ingredient) {
        return with(Ingredient.of(ingredient));
    }

    public ExposureRecipeBuilder with(TagKey<Item> ingredient) {
        return with(Ingredient.of(ingredient));
    }

    public static ExposureRecipeBuilder exposure(ComponentTransferringRecipeSerializer<?> serializer,
                                                 RecipeCategory category,
                                                 Ingredient targetIngredient,
                                                 Item result) {
        return new ExposureRecipeBuilder(serializer, category, targetIngredient, result);
    }

    public static ExposureRecipeBuilder exposure(ComponentTransferringRecipeSerializer<?> serializer,
                                                 RecipeCategory category,
                                                 ItemLike targetItem,
                                                 Item result) {
        return exposure(serializer, category, Ingredient.of(targetItem), result);
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, @NotNull ResourceLocation recipeId) {
        this.advancement
              .parent(ROOT_RECIPE_ADVANCEMENT)
              .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
              .rewards(AdvancementRewards.Builder.recipe(recipeId))
              .requirements(RequirementsStrategy.OR);

        finishedRecipeConsumer.accept(new Result(serializer,
              recipeId,
              targetIngredient,
              ingredients,
              advancement,
              recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"),
              this.result,
              determineBookCategory(RecipeCategory.MISC)));
    }

    public static class Result extends CraftingResult {
        private final ComponentTransferringRecipeSerializer<?> serializer;
        private final ResourceLocation id;
        private final Ingredient targetIngredient;
        private final NonNullList<Ingredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final Item result;

        protected Result(ComponentTransferringRecipeSerializer<?> serializer,
                         ResourceLocation id,
                         Ingredient targetIngredient,
                         NonNullList<Ingredient> ingredients,
                         Advancement.Builder advancement,
                         ResourceLocation advancementId,
                         Item result,
                         CraftingBookCategory category) {
            super(category);
            this.serializer = serializer;
            this.id = id;
            this.targetIngredient = targetIngredient;
            this.ingredients = ingredients;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.result = result;
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return serializer;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            super.serializeRecipeData(json);
            json.add(serializer.target(), targetIngredient.toJson());
            JsonArray jsonArray = new JsonArray();

            for (Ingredient ingredient : this.ingredients) {
                jsonArray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonArray);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());

            json.add("result", jsonObject);
        }

        @javax.annotation.Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @javax.annotation.Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
