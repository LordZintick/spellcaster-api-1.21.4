package com.lordkittycat;

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
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, b) -> SpellLoader.reloadData(server));
		UseItemCallback.EVENT.register((this::wandHandler));
		SpellLoader.SPELL_PROVIDERS.register(MOD_ID, DefaultSpells.class);
	}

	private ActionResult wandHandler(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		Item item = stack.getItem();

		if (item == Items.STICK) {
			if (Objects.equals(Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_NAME)).getLiteralString(), "Wand")) {
				if (Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)).floats().contains(750001f)) {
					CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
					Spell spell = SpellLoader.SPELLS.getByID(Identifier.of(customModelData.strings().getFirst()));
					assert spell != null;
					if (!player.getItemCooldownManager().isCoolingDown(stack)) {
						player.getItemCooldownManager().set(stack, (int) (spell.cooldown * 20));
						Spell.SpellParameterProvider parameterProvider = new Spell.SpellParameterProvider(world, player, stack);
						SpellLoader.SPELLS.castSpell(Identifier.of(customModelData.strings().getFirst()), parameterProvider);
					}
				}
			}
		}
		return ActionResult.PASS;
	}
}