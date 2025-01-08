package com.lordkittycat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class DefaultSpells {
    public static void castFireball(ServerWorld world, PlayerEntity player) {
        world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
        SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getY() + 1, player.getZ(), player.getRotationVector());
        fireball.setOwner(player);
        world.spawnEntity(fireball);
    }
}
