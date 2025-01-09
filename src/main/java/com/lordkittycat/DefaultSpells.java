package com.lordkittycat;

import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellAction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public final class DefaultSpells {
    @SpellAction(id="fireball")
    public static void castFireball(Spell.SpellParameterProvider paramProvider, Integer testint) {
        SpellCasterAPI.LOGGER.info("Cast fireball with test int: {}", testint);
        World world = paramProvider.world();
        PlayerEntity player = paramProvider.player();
        if (!world.isClient()) {
            world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1f);
            SmallFireballEntity fireball = new SmallFireballEntity(world, player.getX(), player.getY() + 1, player.getZ(), player.getRotationVector());
            fireball.setOwner(player);
            world.spawnEntity(fireball);
        }
    }
}
