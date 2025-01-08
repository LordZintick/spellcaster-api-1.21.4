package com.lordkittycat.mixin;

import com.lordkittycat.SpellCasterAPI;
import com.lordkittycat.loader.Spell;
import com.lordkittycat.loader.SpellLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class LoadWorldMixin {
	@Shadow @Nullable public abstract ServerWorld getWorld(RegistryKey<World> key);

	@Inject(at = @At("TAIL"), method = "loadWorld")
	private void loadWorld(CallbackInfo info) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		SpellLoader.loadData(server);
	}
}