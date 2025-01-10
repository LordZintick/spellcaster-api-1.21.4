package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WandRecipe extends SpecialCraftingRecipe {
    public ArrayList<ItemStack> ingredients = new ArrayList<>();

    public WandRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    public static ItemStack createWand(Identifier spellID) {
        ItemStack result = new ItemStack(Items.STICK);
        Spell spell = SpellLoader.SPELLS.getByID(spellID);
        List<Float> floats = List.of(750001f);
        List<Boolean> booleans = List.of();
        List<String> strings = List.of(spellID.toString());
        List<Integer> integers = List.of();

        result.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Wand").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
        result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.of("A magical wand that can cast spells!"),
                Text.of("This wand casts: "),
                Text.of(spell.displayName),
                Text.of(spell.description)
        )));
        return result;
    }

    private void updateIngredients(RecipeInput input) {
        ingredients.clear();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() != Items.AIR && stack.getItem() != Items.STICK) {
                ingredients.add(stack);
            }
        }
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean hasStick = false;
        boolean hasIngredient = false;
        updateIngredients(input);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() == Items.STICK) {
                hasStick = true;
            } else if (stack.getItem() != Items.AIR) {
                hasIngredient = true;
            }
        }

        return hasStick && hasIngredient;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        updateIngredients(input);
        ArrayList<Identifier> ingredientIDs = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            ingredientIDs.add(Registries.ITEM.getId(ingredient.getItem()));
        }
        Spell spell = SpellLoader.SPELLS.getByIngredients(ingredientIDs);
        Identifier spellID = spell.id;
        return createWand(spellID);
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return SpellCasterAPI.WAND_RECIPE;
    }
}
