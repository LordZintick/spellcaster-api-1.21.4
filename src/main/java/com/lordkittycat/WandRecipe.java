package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import com.lordkittycat.loader.StringConvertable;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WandRecipe implements Recipe {
    public ItemStack ingredient = ItemStack.EMPTY;

    @Override
    public boolean matches(RecipeInput input, World world) {
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

    public record DefaultParameterFlags(boolean world, boolean player, boolean itemstack) implements StringConvertable {
        @Override
        public String asString() {
            return world + "," + player + "," + itemstack;
        }

        private static boolean getBoolFromString(String s) {
            return Objects.equals(s.toLowerCase(), "true");
        }

        public static DefaultParameterFlags fromString(String input) {
            String[] split = input.split(",");
            return new DefaultParameterFlags(getBoolFromString(split[0]), getBoolFromString(split[1]), getBoolFromString(split[2]));
        }
    }

    public static ItemStack createWand(Identifier spellID, DefaultParameterFlags params) {
        ItemStack result = new ItemStack(Items.STICK);
        List<Float> floats = List.of(750001f);
        List<Boolean> booleans = List.of();
        List<String> strings = List.of(spellID.toString(), params.asString());
        List<Integer> integers = List.of();

        // Set the custom name and model data.
        result.set(DataComponentTypes.CUSTOM_NAME, Text.of("Wand"));
        result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, booleans, strings, integers));
        return result;
    }

    private void updateIngredient(RecipeInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.getItem() != Items.AIR) {
                ingredient = stack;
            }
        }
    }

    @Override
    public ItemStack craft(RecipeInput input, RegistryWrapper.WrapperLookup registries) {
        // Create the custom item (wand).
        updateIngredient(input);
        Spell spell = Objects.requireNonNull(SpellLoader.SPELLS.getByIngredient(Registries.ITEM.getId(ingredient.getItem())));
        Identifier spellID = spell.id;
        DefaultParameterFlags parameterFlags;
        if (spell.parameterFlags != null) {
            parameterFlags = new DefaultParameterFlags(spell.parameterFlags.get("world").getAsBoolean(), spell.parameterFlags.get("player").getAsBoolean(), spell.parameterFlags.get("itemstack").getAsBoolean());
            return createWand(spellID, parameterFlags);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return Recipe.super.isIgnoredInRecipeBook();
    }

    @Override
    public boolean showNotification() {
        return Recipe.super.showNotification();
    }

    @Override
    public String getGroup() {
        return Recipe.super.getGroup();
    }

    @Override
    public RecipeSerializer<? extends Recipe> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<? extends Recipe> getType() {
        return null;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return null;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return Recipe.super.getDisplays();
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return null;
    }
}
