package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
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

public class ScrollRecipe extends SpecialCraftingRecipe {
    public ArrayList<ItemStack> ingredients = new ArrayList<>();
    private boolean disenchant = false;

    public ScrollRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    public static ItemStack createScroll(Identifier spellID) {
        ItemStack result = new ItemStack(Items.PAPER);
        Spell spell = SpellLoader.SPELLS.getByID(spellID);
        List<Float> floats = List.of(750001f);
        List<Boolean> booleans = List.of();
        List<String> strings = List.of(spellID.toString());
        List<Integer> integers = List.of();

        assert spell != null;
        result.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Scroll (" + spell.displayName + ") (" + spell.manaCost + " mana)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
        result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.of("A magical scroll that can cast a certain spell!"),
                Text.of("One-Time use"),
                Text.of("Spell: "),
                Text.of(spell.displayName + " (costs " + spell.manaCost + " mana)"),
                Text.of(spell.description)
        )));
        return result;
    }

    public static ItemStack tryCreateScroll(Identifier spellID) throws IllegalArgumentException {
        ItemStack result = new ItemStack(Items.PAPER);
        Spell spell = SpellLoader.SPELLS.getByID(spellID);
        List<Float> floats = List.of(750001f);
        List<Boolean> booleans = List.of();
        List<String> strings = List.of(spellID.toString());
        List<Integer> integers = List.of();

        if (spell != null) {
            result.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Scroll (" + spell.displayName + ") (" + spell.manaCost + " mana)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
            result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
            result.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.of("A magical scroll that can cast a certain spell!"),
                    Text.of("One-Time use"),
                    Text.of("Spell: "),
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
            if (stack.getItem() != Items.AIR && stack.getItem() != Items.PAPER) {
                ingredients.add(stack);
            }
        }
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean hasPaper = false;
        boolean hasIngredient = false;
        updateIngredients(input);
        ItemStack stick = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() == Items.PAPER) {
                hasPaper = true;
                stick = stack;
            } else if (stack.getItem() != Items.AIR) {
                hasIngredient = true;
            }
        }
        if (!hasIngredient) {
            disenchant = SpellCasterAPI.checkIfMagicalItem(stick);
        } else {
            disenchant = false;
        }
        return hasPaper && (hasIngredient || disenchant);
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
            if (spell == null) {
                SpellCasterAPI.LOGGER.warn("Invalid ingredients, ignoring");
                return ItemStack.EMPTY;
            }
            Identifier spellID = spell.id;
            return createScroll(spellID);
        } else {
            ItemStack paper = Items.PAPER.getDefaultStack();
            paper.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of("disenchanted"), List.of()));
            return paper;
        }
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return SpellCasterAPI.SCROLL_RECIPE;
    }
}
