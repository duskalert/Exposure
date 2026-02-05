package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentTransferringShapelessExtension implements ICraftingCategoryExtension {

    private final ComponentTransferringRecipe recipe;

    public ComponentTransferringShapelessExtension(ComponentTransferringRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder,
                          ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ComponentTransferringRecipe recipe = this.recipe;

        List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> List.of(ingredient.getItems()))
                .collect(Collectors.toList());

        inputs.add(0,List.of(recipe.getSourceIngredient().getItems()));

        ItemStack resultItem = recipe.getResult();

        int width = 3;//grecipe);
        int height = 3;//getHeight(recipeHolder);todo
        craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
        craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
    }

}