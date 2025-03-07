package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.CraftingInventory;
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
import java.util.Objects;
import java.util.Optional;

public class WandRecipe extends SpecialCraftingRecipe {
    public ArrayList<ItemStack> ingredients = new ArrayList<>();
    private boolean disenchant = false;

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

        assert spell != null;
        result.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Wand (" + spell.displayName + ") (" + spell.manaCost + " mana)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
        result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.of("A magical wand that can cast spells!"),
                Text.of("This wand casts: "),
                Text.of(spell.displayName + " (costs " + spell.manaCost + " mana)"),
                Text.of(spell.description)
        )));
        return result;
    }

    public static ItemStack tryCreateWand(Identifier spellID) throws IllegalArgumentException {
        ItemStack result = new ItemStack(Items.STICK);
        Spell spell = SpellLoader.SPELLS.getByID(spellID);
        List<Float> floats = List.of(750001f);
        List<Boolean> booleans = List.of();
        List<String> strings = List.of(spellID.toString());
        List<Integer> integers = List.of();

        if (spell != null) {
            result.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Wand (" + spell.displayName + ") (" + spell.manaCost + " mana)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
            result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
            result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.of("A magical wand that can cast spells!"),
                    Text.of("This wand casts: "),
                    Text.of(spell.displayName + " (costs " + spell.manaCost + " mana)"),
                    Text.of(spell.description)
            )));
            return result;
        } else {
            throw new IllegalArgumentException();
        }
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
        ItemStack stick = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() == Items.STICK) {
                hasStick = true;
                stick = stack;
            } else if (stack.getItem() != Items.AIR) {
                hasIngredient = true;
            }
        }
        if (!hasIngredient) {
            if (SpellCasterAPI.checkIfWand(stick)) {
                disenchant = true;
            } else {
                disenchant = false;
            }
        } else {
            disenchant = false;
        }
        return hasStick && (hasIngredient || disenchant);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        if (!disenchant) {
            updateIngredients(input);
            ArrayList<Identifier> ingredientIDs = new ArrayList<>();
            for (ItemStack ingredient : ingredients) {
                ingredientIDs.add(Registries.ITEM.getId(ingredient.getItem()));
            }
            Spell spell = SpellLoader.SPELLS.getByIngredients(ingredientIDs);
            assert spell != null;
            Identifier spellID = spell.id;
            return createWand(spellID);
        } else {
            ItemStack stick = Items.STICK.getDefaultStack();
            stick.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of("disenchanted"), List.of()));
            return stick;
        }
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return SpellCasterAPI.WAND_RECIPE;
    }
}
