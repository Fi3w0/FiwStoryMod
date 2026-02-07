package com.fiw.fiwstory.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema de datos para inmunidad con susurros cada 10 minutos.
 */
public class ImmunityData extends PersistentState {
    
    private static final String DATA_NAME = "fiwstory_immunity_data";
    
    // Datos por jugador: tiempo desde último susurro (en ticks)
    private final Map<UUID, PlayerImmunityData> playerData = new HashMap<>();
    
    // Tiempo entre susurros: 10 minutos = 20 * 60 * 10 = 12000 ticks
    private static final int WHISPER_INTERVAL = 20 * 60 * 10; // 10 minutos
    
    public static ImmunityData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        ImmunityData state = persistentStateManager.getOrCreate(ImmunityData::createFromNbt, ImmunityData::new, DATA_NAME);
        state.markDirty();
        return state;
    }
    
    public ImmunityData() {
        super();
    }
    
    public static ImmunityData createFromNbt(NbtCompound nbt) {
        ImmunityData data = new ImmunityData();
        
        NbtList playersList = nbt.getList("players", 10); // Tipo 10 = NbtCompound
        for (int i = 0; i < playersList.size(); i++) {
            NbtCompound playerCompound = playersList.getCompound(i);
            UUID playerId = playerCompound.getUuid("uuid");
            PlayerImmunityData playerData = PlayerImmunityData.fromNbt(playerCompound);
            data.playerData.put(playerId, playerData);
        }
        
        return data;
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList playersList = new NbtList();
        
        for (Map.Entry<UUID, PlayerImmunityData> entry : playerData.entrySet()) {
            NbtCompound playerCompound = new NbtCompound();
            playerCompound.putUuid("uuid", entry.getKey());
            entry.getValue().writeNbt(playerCompound);
            playersList.add(playerCompound);
        }
        
        nbt.put("players", playersList);
        return nbt;
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    /**
     * Actualiza el timer de susurros para un jugador inmune.
     * Retorna true si es hora de enviar un susurro.
     */
    public boolean updatePlayerImmunity(net.minecraft.server.network.ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerImmunityData data = playerData.get(playerId);
        
        if (data == null || !data.isImmune) {
            return false;
        }
        
        // Incrementar timer
        data.whisperTimer++;
        
        // Verificar si es hora de enviar susurro
        if (data.whisperTimer >= WHISPER_INTERVAL) {
            data.whisperTimer = 0;
            this.markDirty();
            return true;
        }
        
        this.markDirty();
        return false;
    }
    
    /**
     * Establece inmunidad para un jugador.
     */
    public void setPlayerImmune(UUID playerId, boolean immune) {
        PlayerImmunityData data = playerData.computeIfAbsent(playerId, uuid -> new PlayerImmunityData());
        data.isImmune = immune;
        data.whisperTimer = 0; // Resetear timer
        
        this.markDirty();
    }
    
    /**
     * Verifica si un jugador tiene inmunidad.
     */
    public boolean isPlayerImmune(UUID playerId) {
        PlayerImmunityData data = playerData.get(playerId);
        return data != null && data.isImmune;
    }
    
    /**
     * Obtiene información de inmunidad de un jugador.
     */
    public String getPlayerImmunityInfo(UUID playerId) {
        PlayerImmunityData data = playerData.get(playerId);
        
        if (data == null || !data.isImmune) {
            return "Sin inmunidad";
        }
        
        long ticksUntilWhisper = WHISPER_INTERVAL - data.whisperTimer;
        long seconds = ticksUntilWhisper / 20;
        long minutes = seconds / 60;
        
        return String.format("Inmune - Próximo susurro en: %dm %ds", minutes, seconds % 60);
    }
    
    // ========== CLASE INTERNA PARA DATOS DE JUGADOR ==========
    
    private static class PlayerImmunityData {
        boolean isImmune = false;
        long whisperTimer = 0; // Ticks desde último susurro
        
        void writeNbt(NbtCompound nbt) {
            nbt.putBoolean("isImmune", isImmune);
            nbt.putLong("whisperTimer", whisperTimer);
        }
        
        static PlayerImmunityData fromNbt(NbtCompound nbt) {
            PlayerImmunityData data = new PlayerImmunityData();
            data.isImmune = nbt.getBoolean("isImmune");
            data.whisperTimer = nbt.getLong("whisperTimer");
            return data;
        }
    }
}