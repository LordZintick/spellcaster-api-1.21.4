package com.lordkittycat;

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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class SpellCasterAPI implements ModInitializer {
	public static final String MOD_ID = "spellcaster-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + MOD_ID + "...");

		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> SpellLoader.reloadData(server));
		UseItemCallback.EVENT.register(((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			Item item = stack.getItem();

			if (item == Items.STICK) {
				if (Objects.equals(Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_NAME)).toString(), "Wand")) {
					if (Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)).floats().getFirst() == 750001f) {
						CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
						ArrayList<Object> params = extractParams(customModelData, WandRecipe.DefaultParameterFlags.fromString(Objects.requireNonNull(customModelData.getString(1))), world, player, stack);
						SpellLoader.SPELLS.castSpell(Identifier.of(customModelData.strings().getFirst()), params.toArray());
					}
				}
			}
			return ActionResult.PASS;
		}));
	}

	private static @NotNull ArrayList<Object> extractParams(CustomModelDataComponent customModelData, WandRecipe.DefaultParameterFlags parameterFlags, World world, PlayerEntity player, ItemStack stack) {
		ArrayList<String> stringParams = new ArrayList<>();
		if (customModelData.strings().size() > 2) {
			for (int i = 2; i < customModelData.strings().size() - 1; i++) {
				stringParams.add(customModelData.strings().get(i));
			}
		}
		ArrayList<Integer> intParams = new ArrayList<>();
		if (customModelData.colors().size() > 2) {
			for (int i = 2; i < customModelData.colors().size() - 1; i++) {
				intParams.add(customModelData.colors().get(i));
			}
		}
		ArrayList<Boolean> boolParams = new ArrayList<>();
		if (customModelData.flags().size() > 2) {
			for (int i = 2; i < customModelData.flags().size() - 1; i++) {
				boolParams.add(customModelData.flags().get(i));
			}
		}
		ArrayList<Object> params = new ArrayList<>();
		if (parameterFlags.world()) {
			params.add(world);
		}
		if (parameterFlags.player()) {
			params.add(player);
		}
		if (parameterFlags.itemstack()) {
			params.add(stack);
		}
		params.addAll(boolParams);
		params.addAll(stringParams);
		params.addAll(intParams);
		return params;
	}
}