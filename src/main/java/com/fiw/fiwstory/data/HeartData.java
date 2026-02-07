package com.fiw.fiwstory.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeartData extends PersistentState {
    private static final String DATA_NAME = "fiwstory_heart_data";
    
    private final Map<UUID, PlayerHeartData> playerData = new HashMap<>();
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, PlayerHeartData> entry : playerData.entrySet()) {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putFloat("originalMaxHealth", entry.getValue().originalMaxHealth);
            playerNbt.putFloat("currentMaxHealth", entry.getValue().currentMaxHealth);
            playerNbt.putBoolean("usingHeart", entry.getValue().usingHeart);
            playerNbt.putLong("lastUpdateTime", entry.getValue().lastUpdateTime);
            playersNbt.put(entry.getKey().toString(), playerNbt);
        }
        nbt.put("playerData", playersNbt);
        return nbt;
    }
    
    public static HeartData createFromNbt(NbtCompound nbt) {
        HeartData data = new HeartData();
        NbtCompound playersNbt = nbt.getCompound("playerData");
        
        for (String playerIdStr : playersNbt.getKeys()) {
            try {
                UUID playerId = UUID.fromString(playerIdStr);
                NbtCompound playerNbt = playersNbt.getCompound(playerIdStr);
                
                PlayerHeartData playerHeartData = new PlayerHeartData();
                playerHeartData.originalMaxHealth = playerNbt.getFloat("originalMaxHealth");
                playerHeartData.currentMaxHealth = playerNbt.getFloat("currentMaxHealth");
                playerHeartData.usingHeart = playerNbt.getBoolean("usingHeart");
                playerHeartData.lastUpdateTime = playerNbt.getLong("lastUpdateTime");
                
                data.playerData.put(playerId, playerHeartData);
            } catch (IllegalArgumentException e) {
                // Ignorar UUIDs inválidos
            }
        }
        
        return data;
    }
    
    public static HeartData getServerState(net.minecraft.server.MinecraftServer server) {
        var persistentStateManager = server.getWorld(net.minecraft.world.World.OVERWORLD).getPersistentStateManager();
        HeartData state = persistentStateManager.getOrCreate(HeartData::createFromNbt, HeartData::new, DATA_NAME);
        state.markDirty();
        return state;
    }
    
    public PlayerHeartData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerHeartData());
    }
    
    public void updatePlayerData(UUID playerId, PlayerHeartData data) {
        playerData.put(playerId, data);
        markDirty();
    }
    
    public static PlayerHeartData get(PlayerEntity player) {
        if (player.getServer() == null) {
            return new PlayerHeartData(); // Datos temporales para cliente
        }
        
        HeartData serverState = getServerState(player.getServer());
        PlayerHeartData data = serverState.getPlayerData(player.getUuid());
        
        // Inicializar datos si es la primera vez
        if (data.originalMaxHealth <= 0) {
            data.originalMaxHealth = player.getMaxHealth();
            data.currentMaxHealth = player.getMaxHealth();
            serverState.markDirty();
        }
        
        return data;
    }
    
    public static class PlayerHeartData {
        public float originalMaxHealth = 20.0f; // Vida máxima original (10 corazones)
        public float currentMaxHealth = 20.0f;  // Vida máxima actual
        public boolean usingHeart = false;      // Si está usando el corazón
        public long lastUpdateTime = 0;         // Última actualización
        
        public float getOriginalMaxHealth() {
            return originalMaxHealth;
        }
        
        public void setOriginalMaxHealth(float health) {
            this.originalMaxHealth = health;
        }
        
        public float getCurrentMaxHealth() {
            return currentMaxHealth;
        }
        
        public void setCurrentMaxHealth(float health) {
            this.currentMaxHealth = Math.max(health, 0);
        }
        
        public boolean isUsingHeart() {
            return usingHeart;
        }
        
        public void setUsingHeart(boolean using) {
            this.usingHeart = using;
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
        
        public void setLastUpdateTime(long time) {
            this.lastUpdateTime = time;
        }
        
        public void resetToOriginal() {
            this.currentMaxHealth = this.originalMaxHealth;
            this.usingHeart = false;
        }
        
        public void markDirty() {
            // Este método se llama cuando los datos cambian
        }
    }
}