package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import com.lordkittycat.loader.StringConvertable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
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
import java.util.Objects;

public class WandRecipe extends SpecialCraftingRecipe {
    public ItemStack ingredient = ItemStack.EMPTY;

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

        // Set the custom name and model data.
        result.set(DataComponentTypes.CUSTOM_NAME, Text.of("Wand").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
        result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.of("A magical wand that can cast spells!"),
                Text.of("This wand casts: "),
                Text.of(spell.displayName),
                Text.of(spell.description)
        )));
        return result;
    }

    private void updateIngredient(RecipeInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() != Items.AIR && stack.getItem() != Items.STICK) {
                ingredient = stack;
            }
        }
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean hasStick = false;
        boolean hasIngredient = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() == Items.STICK) {
                hasStick = true;
            }
            if (stack.getItem() != Items.AIR) {
                hasIngredient = true;
                ingredient = stack;
            }
        }

        return hasStick && hasIngredient;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        // Create the custom item (wand).
        updateIngredient(input);
        Spell spell = SpellLoader.SPELLS.getByIngredient(Registries.ITEM.getId(ingredient.getItem()));
        Identifier spellID = spell.id;
        return createWand(spellID);
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return SpellCasterAPI.WAND_RECIPE;
    }
}
