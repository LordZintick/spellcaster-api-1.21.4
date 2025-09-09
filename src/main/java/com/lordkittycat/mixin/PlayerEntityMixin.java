package com.lordkittycat.mixin;

import com.lordkittycat.SpellCasterAPI;
import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Random;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Inject(at= @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci) {
        PlayerEntity clientplayer = (PlayerEntity) (Object) this;
        if (clientplayer instanceof ServerPlayerEntity player) {
            EntityAttributeInstance mana = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.TEMPT_RANGE));
            EntityAttributeInstance maxMana = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.FOLLOW_RANGE));
            EntityAttributeInstance NEMInstance = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS));
            if (NEMInstance.getValue() < 0) {
                NEMInstance.setBaseValue(0);
            }
            boolean showingNEM = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS)).getValue() > 0;
            if (maxMana.getValue() < 100) {
                maxMana.setBaseValue(100d);
            }
            if (mana.getValue() < maxMana.getValue()) {
                ServerWorld world = (ServerWorld) player.getWorld();
                mana.setBaseValue(mana.getBaseValue() + (double) world.getGameRules().get(SpellCasterAPI.MANA_REGEN_RATE).get() / 20);
            }

            ItemStack stack = getEquippedStack(EquipmentSlot.MAINHAND);
            Item item = stack.getItem();
            if (item == Items.STICK) {
                if (stack.get(DataComponentTypes.CUSTOM_NAME) != null && stack.get(DataComponentTypes.CUSTOM_MODEL_DATA) != null) {
                    if (Objects.requireNonNull(Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_NAME)).getLiteralString()).contains("Wand")) {
                        if (Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA)).floats().contains(750001f)) {
                            CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
                            Identifier spellID = Identifier.of(customModelData.strings().getFirst());
                            Spell spell = SpellLoader.SPELLS.getByID(spellID);
                            if (spell != null) {
                                if (spell.particleEffect != null) {
                                    if (Registries.PARTICLE_TYPE.containsId(Identifier.of(spell.particleEffect))) {
                                        ParticleType<?> particle = Registries.PARTICLE_TYPE.get(Identifier.of(spell.particleEffect));
                                        ((ServerPlayerEntity) clientplayer).getServerWorld().addParticle(new ParticleEffect() {
                                            @Override
                                            public ParticleType<?> getType() {
                                                return particle;
                                            }
                                        },
                                                clientplayer.getX() + clientplayer.getRandom().nextBetween(-5, 5) / 10,
                                                clientplayer.getY() + clientplayer.getRandom().nextBetween(0,20) / 10,
                                                clientplayer.getZ() + clientplayer.getRandom().nextBetween(-5, 5) / 10,
                                                clientplayer.getRandom().nextBetween(-10,10) / 100,
                                                clientplayer.getRandom().nextBetween(0,20) / 100,
                                                clientplayer.getRandom().nextBetween(-10,10) / 100
                                        );
                                    }
                                }
                            }
                            if (showingNEM) {
                                sendMessage(Text.of("Not enough mana!").copy().formatted(Formatting.RED), true);
                                NEMInstance.setBaseValue(NEMInstance.getValue() - 0.1);
                            } else {
                                sendMessage(Text.of("Mana: " + (int) player.getAttributeValue(EntityAttributes.TEMPT_RANGE) + "/" + (int) player.getAttributeValue(EntityAttributes.FOLLOW_RANGE)).copy().formatted(Formatting.BLUE), true);
                            }
                        }
                    }
                }
            } else if (mana.getValue() < maxMana.getValue()) {
                if (showingNEM) {
                    sendMessage(Text.of("Not enough mana!").copy().formatted(Formatting.RED), true);
                    NEMInstance.setBaseValue(NEMInstance.getValue() - 0.1);
                } else {
                    sendMessage(Text.of("Mana: " + (int) player.getAttributeValue(EntityAttributes.TEMPT_RANGE) + "/" + (int) player.getAttributeValue(EntityAttributes.FOLLOW_RANGE)).copy().formatted(Formatting.BLUE), true);
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "createPlayerAttributes", cancellable = true)
    private static void createAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(LivingEntity.createLivingAttributes()
                .add(EntityAttributes.ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(EntityAttributes.ATTACK_SPEED)
                .add(EntityAttributes.LUCK)
                .add(EntityAttributes.BLOCK_INTERACTION_RANGE, 4.5)
                .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3.0)
                .add(EntityAttributes.BLOCK_BREAK_SPEED)
                .add(EntityAttributes.SUBMERGED_MINING_SPEED)
                .add(EntityAttributes.SNEAKING_SPEED)
                .add(EntityAttributes.MINING_EFFICIENCY)
                .add(EntityAttributes.SWEEPING_DAMAGE_RATIO)
                .add(EntityAttributes.FOLLOW_RANGE, 100d)
                .add(EntityAttributes.TEMPT_RANGE, 100d)
                .add(EntityAttributes.SPAWN_REINFORCEMENTS, 0d));
    }
}
