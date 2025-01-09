package com.lordkittycat.mixin;

import com.lordkittycat.loader.SpellLoader;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class LoadWorldMixin {
	@Inject(at = @At("TAIL"), method = "loadWorld")
	private void loadWorld(CallbackInfo info) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		SpellLoader.loadData(server);
	}
}