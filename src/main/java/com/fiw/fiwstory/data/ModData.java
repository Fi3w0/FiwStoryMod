package com.fiw.fiwstory.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModData extends PersistentState {
    private static final String DATA_NAME = "fiwstory_data";
    private final Set<UUID> immunePlayers = new HashSet<>();
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList immuneList = new NbtList();
        for (UUID playerId : immunePlayers) {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putUuid("UUID", playerId);
            immuneList.add(playerNbt);
        }
        nbt.put("immunePlayers", immuneList);
        return nbt;
    }
    
    public static ModData createFromNbt(NbtCompound nbt) {
        ModData data = new ModData();
        NbtList immuneList = nbt.getList("immunePlayers", 10); // 10 = COMPOUND
        for (int i = 0; i < immuneList.size(); i++) {
            NbtCompound playerNbt = immuneList.getCompound(i);
            data.immunePlayers.add(playerNbt.getUuid("UUID"));
        }
        return data;
    }
    
    public static ModData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        ModData state = persistentStateManager.getOrCreate(ModData::createFromNbt, ModData::new, DATA_NAME);
        state.markDirty();
        return state;
    }
    
    public boolean addImmunePlayer(UUID playerId) {
        if (immunePlayers.add(playerId)) {
            markDirty();
            return true;
        }
        return false;
    }
    
    public boolean removeImmunePlayer(UUID playerId) {
        if (immunePlayers.remove(playerId)) {
            markDirty();
            return true;
        }
        return false;
    }
    
    public boolean isPlayerImmune(UUID playerId) {
        return immunePlayers.contains(playerId);
    }
    
    public Set<UUID> getImmunePlayers() {
        return new HashSet<>(immunePlayers);
    }
}