package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellAction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DefaultSpells {
    @SpellAction(id="fireball")
    public static void castFireball(Spell.SpellParameterProvider paramProvider, Float speed, Boolean big) {
        World world = paramProvider.world();
        PlayerEntity player = paramProvider.player();
        if (!world.isClient()) {
            world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
            if (!big) {
                SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getY() + 1, player.getZ(), new Vec3d(player.getRotationVector().x * speed, player.getRotationVector().y * speed, player.getRotationVector().z * speed));
                fireball.setOwner(player);
                world.spawnEntity(fireball);
            } else {
                FireballEntity fireball = new FireballEntity(world, player, new Vec3d(player.getRotationVector().x * speed, player.getRotationVector().y * speed, player.getRotationVector().z * speed), 3);
                fireball.setPos(player.getX(), player.getY() + 1, player.getZ());
                world.spawnEntity(fireball);
            }
        }
    }
}
