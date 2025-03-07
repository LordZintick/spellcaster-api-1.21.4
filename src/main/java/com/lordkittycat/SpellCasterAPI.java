package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import com.lordkittycat.loader.SpellPack;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.mixin.item.CraftingRecipeMixin;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SpellCasterAPI implements ModInitializer {
	public static final String MOD_ID = "spellcaster-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final RecipeSerializer<WandRecipe> WAND_RECIPE = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "wand_craft"), new SpecialCraftingRecipe.SpecialRecipeSerializer<>(WandRecipe::new));
	public static final GameRules.Key<GameRules.BooleanRule> ENABLE_COOLDOWNS = GameRuleRegistry.register("enableCooldowns", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.BooleanRule> ENABLE_MANA_COSTS = GameRuleRegistry.register("enableManaCosts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.IntRule> MANA_REGEN_RATE = GameRuleRegistry.register("manaRegenRate", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(10));
	private static Optional<MinecraftServer> SERVER = Optional.empty();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing " + MOD_ID + "...");

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			SERVER = Optional.of(server);
			SpellLoader.refreshCurrentSpellPacks(server.getSavePath(WorldSavePath.DATAPACKS));
        });
		ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getCommandManager().executeWithPrefix(server.getCommandSource().withOutput(server), "/reload"));
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of(MOD_ID, "spell_loader");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (SERVER.isPresent()) {
					SERVER.get().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, SERVER.get());
					SpellLoader.refreshCurrentSpellPacks(SERVER.get().getSavePath(WorldSavePath.DATAPACKS));
					SpellLoader.reloadData(manager);
				}
			}
		});
		UseItemCallback.EVENT.register((this::wandHandler));
		SpellLoader.SPELL_PROVIDERS.register(MOD_ID, DefaultSpells.class);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("spellpack")
						.requires(source -> source.hasPermissionLevel(4))
				.then(CommandManager.literal("enable")
						.then(CommandManager.argument("packname", StringArgumentType.greedyString())
								.suggests(new SpellPackTypeSuggestionProvider(false))
								.executes(context -> {
									String packname = context.getArgument("packname", String.class);
									if (SpellPack.PACKS.getByID(packname) != null) {
										SpellPack pack = SpellPack.PACKS.getByID(packname);
                                        assert pack != null;
                                        if (!pack.enabled) {
											pack.enabled = true;
                                            SERVER.ifPresent(server -> SpellLoader.loadSpellPack(server.getSavePath(WorldSavePath.DATAPACKS), Objects.requireNonNull(SpellPack.Type.getByID(packname))));
											SERVER.ifPresent(server -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload"));
											return 1;
										}
									}
									return 0;
								})
						))
				.then(CommandManager.literal("enableAll")
						.executes(context -> {
							if (SERVER.isPresent()) {
								SpellLoader.loadAllSpellPacks(SERVER.get().getSavePath(WorldSavePath.DATAPACKS));
								SERVER.ifPresent(server -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload"));
								return 1;
							}
							return 0;
						})
				)
				.then(CommandManager.literal("disable")
						.then(CommandManager.argument("packname", StringArgumentType.greedyString())
								.suggests(new SpellPackTypeSuggestionProvider(true))
								.executes(context -> {
									String packname = context.getArgument("packname", String.class);
									if (SpellPack.PACKS.getByID(packname) != null) {
										SpellPack pack = SpellPack.PACKS.getByID(packname);
										assert pack != null;
										if (pack.enabled) {
											pack.enabled = false;
											if (SERVER.isPresent()) {
												SpellLoader.deleteSpellPack(SERVER.get().getSavePath(WorldSavePath.DATAPACKS), packname);
												SERVER.ifPresent(server -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload"));
												return 1;
											}
										}
									}
									return 0;
								})
						))
				.then(CommandManager.literal("disableAll")
						.executes(context -> {
							if (SERVER.isPresent()) {
								for (SpellPack pack : SpellPack.PACKS) {
									pack.enabled = false;
									SpellLoader.deleteSpellPack(SERVER.get().getSavePath(WorldSavePath.DATAPACKS), pack.type.id);
								}
								SERVER.ifPresent(server -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload"));
								return 1;
							}
							return 0;
						})
				)
		));
		CommandRegistrationCallback.EVENT.register(
				(commandDispatcher, commandRegistryAccess, registrationEnvironment) ->
						commandDispatcher.register(CommandManager.literal("wand")
								.requires(source -> source.hasPermissionLevel(2))
								.then(CommandManager.argument("player", EntityArgumentType.player())
								.then(CommandManager.argument("spell", StringArgumentType.greedyString())
										.suggests(new SpellSuggestionProvider())
								.executes(context -> {
									String spellIdString = context.getArgument("spell", String.class);
									Identifier spellId = Identifier.of(spellIdString);
									ServerPlayerEntity player = context.getArgument("player", EntitySelector.class).getPlayer(context.getSource());
									ServerWorld world = context.getSource().getWorld();

                				    if (player != null) {
                                        ItemStack wand;
                                        try {
                                            wand = WandRecipe.tryCreateWand(spellId);
											world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
											world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1f, 1f);
                                        } catch (IllegalArgumentException e) {
                                            throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Invalid spell ID")), new LiteralMessage("Could not find spell with ID \"" + spellId + "\""));
                                        }
                                        player.getInventory().insertStack(wand);
                                        context.getSource().sendFeedback(() -> Text.literal("Successfully granted " + player.getStyledDisplayName() + " a [" + wand.getCustomName() + "]"), false);
                                        return 1;
                                    }

									return 0;
               					})))
						)
		);
	}

	public static final class SpellSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
		@Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			for (Spell spell : SpellLoader.SPELLS) {
				if ((spell.id.toString().contains(builder.getRemainingLowerCase()) && !Objects.equals(builder.getRemainingLowerCase(), "")) || Objects.equals(builder.getRemainingLowerCase(), "")) {
					builder.suggest(spell.id.toString());
				}
			}
            return builder.buildFuture();
		}
	}

	public static final class SpellPackTypeSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
		private final boolean removing;

        public SpellPackTypeSuggestionProvider(boolean removing) {
            this.removing = removing;
        }

        @Override
		public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
			for (SpellPack pack : SpellPack.PACKS) {
				if ((pack.type.id.contains(builder.getRemainingLowerCase()) && !Objects.equals(builder.getRemainingLowerCase(), "")) || Objects.equals(builder.getRemainingLowerCase(), "")) {
					if ((removing && pack.enabled) || (!removing && !pack.enabled)) {
						builder.suggest(pack.type.id);
					}
				}
			}
			return builder.buildFuture();
		}
	}

	public static boolean checkIfWand(ItemStack stack) {
		if (Objects.requireNonNull(stack.getOrDefault(DataComponentTypes.CUSTOM_NAME, Text.of("")).getLiteralString()).contains("Wand")) {
            return stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(), List.of())).floats().contains(750001f);
		}
		return false;
	}

	private ActionResult wandHandler(PlayerEntity player, World regWorld, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		Item item = stack.getItem();

		if (regWorld instanceof ServerWorld world) {
			if (item == Items.STICK) {
				if (checkIfWand(stack)) {
					CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
					boolean validSpell = SpellLoader.SPELLS.getByID(Identifier.of(customModelData.strings().getFirst())) != null;
					if (validSpell) {
						Spell spell = SpellLoader.SPELLS.getByID(Identifier.of(customModelData.strings().getFirst()));
						assert spell != null;
						stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Wand (" + spell.displayName + ") (" + spell.manaCost + " mana)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
						stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
								Text.of("A magical wand that can cast spells!"),
								Text.of("This wand casts: "),
								Text.of(spell.displayName + " (costs " + spell.manaCost + " mana)"),
								Text.of(spell.description)
						)));
						if (world.getGameRules().get(ENABLE_MANA_COSTS).get() && !player.isInCreativeMode() && !player.isSpectator()) {
							EntityAttributeInstance mana = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.TEMPT_RANGE));
							if (mana.getValue() >= spell.manaCost) {
								mana.setBaseValue(mana.getBaseValue() - spell.manaCost);
								return castSpell(player, world, stack, spell, customModelData);
							} else {
								EntityAttributeInstance NEMInstance = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS));
								NEMInstance.setBaseValue(1);
							}
						} else {
							return castSpell(player, world, stack, spell, customModelData);
						}
					} else {
						stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(Text.of("Wand (Invalid spell)").copy().formatted(Formatting.RESET, Formatting.LIGHT_PURPLE)));
						stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
								Text.literal("Invalid spell")
						)));
					}
				}
			} else if (item == Items.FIRE_CHARGE) {
				if (!player.getItemCooldownManager().isCoolingDown(stack)) {
					player.getItemCooldownManager().set(stack, 20);
					stack.setCount(stack.getCount() - 1);
					world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
					SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getEyeY(), player.getZ(), new Vec3d(player.getRotationVector().x, player.getRotationVector().y, player.getRotationVector().z));
					fireball.setOwner(player);
					world.spawnEntity(fireball);
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.PASS;
	}

	private ActionResult castSpell(PlayerEntity player, ServerWorld world, ItemStack stack, Spell spell, CustomModelDataComponent customModelData) {
		assert spell != null;
		if (world.getGameRules().get(ENABLE_COOLDOWNS).get() && !player.isInCreativeMode() && !player.isSpectator()) {
			if (!player.getItemCooldownManager().isCoolingDown(stack)) {
				player.getItemCooldownManager().set(stack, (int) (spell.cooldown * 20));
				Spell.SpellParameterProvider parameterProvider = new Spell.SpellParameterProvider(player, stack);
				SpellLoader.SPELLS.castSpell(Identifier.of(customModelData.strings().getFirst()), parameterProvider);
				return ActionResult.SUCCESS;
			}
		} else {
			Spell.SpellParameterProvider parameterProvider = new Spell.SpellParameterProvider(player, stack);
			SpellLoader.SPELLS.castSpell(Identifier.of(customModelData.strings().getFirst()), parameterProvider);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
}