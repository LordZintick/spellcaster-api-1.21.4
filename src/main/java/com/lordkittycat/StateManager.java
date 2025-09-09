package com.lordkittycat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class StateManager extends PersistentState {

    public Boolean hasDefaultSpellPack = true;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("hasDefaultSpellPack", hasDefaultSpellPack);
        return nbt;
    }

    public static StateManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        StateManager sm = new StateManager();
        sm.hasDefaultSpellPack = nbt.getBoolean("hasDefaultSpellPack");
        return sm;
    }

    public static void setHasDefaults(MinecraftServer server, boolean hasDefaultSpellPack) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;

        StateManager sm = StateManager.getServerState(server);
        sm.hasDefaultSpellPack = hasDefaultSpellPack;
    }

    public static boolean doesServerHaveDefaults(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;

        return StateManager.getServerState(server).hasDefaultSpellPack;
    }

    public static StateManager create() {
        StateManager sm = new StateManager();
        sm.hasDefaultSpellPack = true;
        return sm;
    }

    private static final Type<StateManager> type = new Type<>(
            StateManager::create,
            StateManager::fromNbt,
            null
    );

    public static StateManager getServerState(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;

        StateManager sm = world.getPersistentStateManager().getOrCreate(type, SpellCasterAPI.MOD_ID);
        sm.markDirty();
        return sm;
    }
}
