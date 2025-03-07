package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellAction;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.ServerTickManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public final class DefaultSpells {
    @SpellAction(id="fireball")
    public static void castFireball(Spell.SpellParameterProvider paramProvider, Float speed, Boolean big) {
        PlayerEntity player = paramProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            if (!big) {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
                SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getEyeY(), player.getZ(), new Vec3d(player.getRotationVector().x * speed, player.getRotationVector().y * speed, player.getRotationVector().z * speed));
                fireball.setOwner(player);
                world.spawnEntity(fireball);
            } else {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 0.7f);
                FireballEntity fireball = new FireballEntity(world, player, new Vec3d(player.getRotationVector().x * speed, player.getRotationVector().y * speed, player.getRotationVector().z * speed), 2);
                fireball.setPosition(player.getEyePos());
                world.spawnEntity(fireball);
            }
        }
    }

    @SpellAction(id="dragonFireball")
    public static void castDragonFireball(Spell.SpellParameterProvider paramProvider, Float speed) {
        PlayerEntity player = paramProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
            DragonFireballEntity fireball = new DragonFireballEntity(world, player, new Vec3d(player.getRotationVector().x * speed, player.getRotationVector().y * speed, player.getRotationVector().z * speed));
            fireball.setPosition(player.getEyePos());
            world.spawnEntity(fireball);
        }
    }

    @SpellAction(id="shootEntities")
    public static void shootEntities(Spell.SpellParameterProvider paramProvider, String entityID, Integer amount, Float speed, Float delay, Float inconsistency) {
        PlayerEntity player = paramProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            if (amount > 10) {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
            }
            new Thread(() -> {
                for (int i = 0; i < amount; i++) {
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
                    Entity entity = Registries.ENTITY_TYPE.get(Identifier.of(entityID.replace(";",":"))).create(world, SpawnReason.MOB_SUMMONED);
                    assert entity != null;
                    entity.setPosition(player.getEyePos());
                    Random random = new Random();
                    entity.setVelocity(new Vec3d(
                            player.getRotationVector().x + random.nextInt(-10,10) * 0.1 * inconsistency,
                            player.getRotationVector().y + random.nextInt(-10,10) * 0.1 * inconsistency,
                            player.getRotationVector().z + random.nextInt(-10,10) * 0.1 * inconsistency
                            ).multiply(speed)
                    );
                    entity.setBodyYaw(player.getBodyYaw());
                    entity.setPitch(player.getPitch());
                    entity.setHeadYaw(player.getHeadYaw());
                    if (entity instanceof TameableEntity tameable) {
                        tameable.setOwner(player);
                    }
                    if (entity instanceof ThrownItemEntity thrown) {
                        thrown.setOwner(player);
                    }
                    if (entity instanceof ProjectileEntity proj) {
                        proj.setOwner(player);
                    }
                    world.spawnEntity(entity);
                    try {
                        Thread.sleep((long) (delay * 1000));
                    } catch (InterruptedException e) {
                        SpellCasterAPI.LOGGER.info("An error occurred when shooting entities: {}", e.getMessage());
                    }
                }
            }).start();
        }
    }

    @SpellAction(id="shootItems")
    public static void shootItems(Spell.SpellParameterProvider paramProvider, String itemID, Integer amount, Float speed, Float delay, Float inconsistency) {
        PlayerEntity player = paramProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
            Random random = new Random();
            new Thread(() -> {
                for (int i = 0; i < amount; i++) {
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
                    ItemEntity entity = new ItemEntity(world, player.getEyePos().x, player.getEyeY(), player.getEyePos().z, Registries.ITEM.get(Identifier.of(itemID.replace(";",":"))).getDefaultStack());
                    entity.setPosition(player.getEyePos());
                    entity.setVelocity(new Vec3d(
                                    player.getRotationVector().x + random.nextInt(-10,10) * 0.1 * inconsistency,
                                    player.getRotationVector().y + random.nextInt(-10,10) * 0.1 * inconsistency,
                                    player.getRotationVector().z + random.nextInt(-10,10) * 0.1 * inconsistency
                            ).multiply(speed)
                    );
                    entity.setBodyYaw(player.getBodyYaw());
                    entity.setPitch(player.getPitch());
                    entity.setHeadYaw(player.getHeadYaw());
                    world.spawnEntity(entity);
                    try {
                        Thread.sleep((long) (delay * 1000));
                    } catch (InterruptedException e) {
                        SpellCasterAPI.LOGGER.info("An error occurred when shooting items: {}", e.getMessage());
                    }
                }
            }).start();
        }
    }

    @SpellAction(id="fangs")
    public static void evokerFangs(Spell.SpellParameterProvider paramProvider, Integer size, Boolean circle) {
        PlayerEntity player = paramProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK, SoundCategory.PLAYERS, 1f, 1f);
            if (circle) {
                for (int dist = 0; dist < size; dist++) {
                    for (int angle = 0; angle < 360; angle += 360 / (10 + dist * 5)) {
                        Vec3d pos = player.getPos().add(new Vec3d(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle))).multiply(dist));
                        EvokerFangsEntity fangs = new EvokerFangsEntity(world, pos.getX(), pos.getY(), pos.getZ(), player.headYaw, 2, player);
                        world.spawnEntity(fangs);
                    }
                }
            } else {
                for (int dist = 0; dist < size; dist++) {
                    Vec3d projPos = player.getPos().add(player.getRotationVector().normalize().multiply(dist));
                    EvokerFangsEntity fangs = new EvokerFangsEntity(world, projPos.getX(), projPos.getY(), projPos.getZ(), player.headYaw, 2, player);
                    world.spawnEntity(fangs);
                }
            }
        }
    }

    @SpellAction(id="teleport")
    public static void teleport(Spell.SpellParameterProvider parameterProvider, Float speed) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 1f, 1f);
            EnderPearlEntity enderPearl = new EnderPearlEntity(world, player, Items.ENDER_PEARL.getDefaultStack());
            enderPearl.setPosition(player.getEyePos());
            enderPearl.setVelocity(player.getRotationVector().normalize().multiply(speed));
            world.spawnEntity(enderPearl);
        }
    }

    @SpellAction(id="execute")
    public static void execute(Spell.SpellParameterProvider parameterProvider, String command) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource(), command.replace("~ ~ ~", player.getX() + " " + player.getY() + " " + player.getZ()));
        }
    }

    @SpellAction(id="playSound")
    public static void playSound(Spell.SpellParameterProvider parameterProvider, String soundID, Float volume, Float pitch) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), Registries.SOUND_EVENT.get(Identifier.of(soundID.replace(";",":"))), SoundCategory.PLAYERS, volume, pitch);
        }
    }

    @SpellAction(id="tickRate")
    public static void tickRate(Spell.SpellParameterProvider parameterProvider, Integer tickrate, Float duration) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            ServerTickManager tickManager = world.getServer().getTickManager();
            if (tickrate > 0) {
                tickManager.setTickRate(tickrate);
            } else {
                tickManager.setFrozen(true);
            }
            Timer expiretimer = new Timer();
            expiretimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (tickrate > 0) {
                        tickManager.setTickRate(20);
                    } else {
                        tickManager.setFrozen(false);
                    }
                }
            }, (long) (duration * 1000));
        }
    }

    @SpellAction(id="setScale")
    public static void setScale(Spell.SpellParameterProvider parameterProvider, Float scale, Float duration) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        EntityAttributeInstance scaleAttr = player.getAttributeInstance(EntityAttributes.SCALE);
        if (!world.isClient() && scaleAttr != null && scaleAttr.getBaseValue() == 1) {
            scaleAttr.setBaseValue(scale);
            Timer expiretimer = new Timer();
            expiretimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    scaleAttr.setBaseValue(1);
                }
            }, (long) (duration * 1000));
        }
    }

    @SpellAction(id="addEffect")
    public static void addEffect(Spell.SpellParameterProvider parameterProvider, String effectID, Integer duration, Integer amplifier) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1f, 1f);
            Optional<RegistryEntry.Reference<StatusEffect>> effect = Registries.STATUS_EFFECT.getEntry(Identifier.of(effectID.replace(";",":")));
            effect.ifPresent(statusEffectReference -> player.addStatusEffect(new StatusEffectInstance(statusEffectReference, duration * 20, amplifier)));
        }
    }

    @SpellAction(id="spawnEntity")
    public static void spawnEntity(Spell.SpellParameterProvider parameterProvider, String entityID, Float x, Float y, Float z, Float vx, Float vy, Float vz) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            Entity entity = Registries.ENTITY_TYPE.get(Identifier.of(entityID.replace(";",":"))).create(world, SpawnReason.MOB_SUMMONED);
            assert entity != null;
            entity.setPos(x, y, z);
            entity.setVelocity(vx, vy, vz);
            world.spawnEntity(entity);
        }
    }

    @SpellAction(id="setBlock")
    public static void setBlock(Spell.SpellParameterProvider parameterProvider, String blockID, Integer x, Integer y, Integer z) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            Block block = Registries.BLOCK.get(Identifier.of(blockID.replace(";",":")));
            world.setBlockState(new BlockPos(x,y,z), block.getDefaultState());
        }
    }

    @SpellAction(id="fillBlocks")
    public static void fillBlocks(Spell.SpellParameterProvider parameterProvider, String blockID, Integer x1, Integer y1, Integer z1, Integer x2, Integer y2, Integer z2) {
        PlayerEntity player = parameterProvider.player();
        World world = player.getWorld();
        if (!world.isClient()) {
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    for (int z = z1; z < z2; z++) {
                        Block block = Registries.BLOCK.get(Identifier.of(blockID.replace(";",":")));
                        world.setBlockState(new BlockPos(x,y,z), block.getDefaultState());
                    }
                }
            }
        }
    }
}
