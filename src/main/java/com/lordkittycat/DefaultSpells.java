package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DefaultSpells {
    public static void castFireball(Spell.SpellParameterProvider paramProvider, int testint) {
        SpellCasterAPI.LOGGER.info("Cast fireball with test int: " + testint);
        World world = paramProvider.world();
        PlayerEntity player = paramProvider.player();
        world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
        SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getY() + 1, player.getZ(), player.getRotationVector());
        fireball.setOwner(player);
        world.spawnEntity(fireball);
    }
}
