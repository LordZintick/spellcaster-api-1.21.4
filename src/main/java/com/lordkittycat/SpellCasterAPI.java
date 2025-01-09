package com.lordkittycat;

import com.google.gson.JsonPrimitive;
import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.ArmorDyeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SpellCasterAPI implements ModInitializer {
	public static final String MOD_ID = "spellcaster-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RecipeSerializer<WandRecipe> WAND_RECIPE = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "wand_craft"), new SpecialCraftingRecipe.SpecialRecipeSerializer<>(WandRecipe::new));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + MOD_ID + "...");

		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> SpellLoader.reloadData(server));
		UseItemCallback.EVENT.register(((player, world, hand) -> {
			LOGGER.info("Use item callback triggered!");
			ItemStack stack = player.getStackInHand(hand);
			Item item = stack.getItem();

			if (item == Items.STICK) {
				LOGGER.info("Item is a stick");
				if (Objects.equals(Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_NAME)).getLiteralString(), "Wand")) {
					LOGGER.info("Stick is name \"Wand\"");
					if (Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)).floats().contains(750001f)) {
						LOGGER.info("Stick's custom model data contains \"750001\"");
						CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
						Spell spell = SpellLoader.SPELLS.getByID(Identifier.of(customModelData.strings().getFirst()));
						ArrayList<String> paramKeys = new ArrayList<>(spell.parameters.keySet());
						Map<String, Object> paramValues = new HashMap<>(Map.of());
						for (int i = 0; i < paramKeys.size(); i++) {
							String key = paramKeys.get(i);
							JsonPrimitive param = spell.parameters.getAsJsonPrimitive(key);
							if (param.isString()) {
								paramValues.put(key, spell.parameters.get(key).getAsString());
							}
							if (param.isNumber()) {
								paramValues.put(key, spell.parameters.get(key).getAsNumber().intValue());
							}
							if (param.isBoolean()) {
								paramValues.put(key, spell.parameters.get(key).getAsBoolean());
							}
						}
						Spell.SpellParameterProvider parameterProvider = new Spell.SpellParameterProvider(paramValues, world, player, stack);
						SpellLoader.SPELLS.castSpell(Identifier.of(customModelData.strings().getFirst()), parameterProvider);
					}
				}
			}
			return ActionResult.PASS;
		}));
	}
}