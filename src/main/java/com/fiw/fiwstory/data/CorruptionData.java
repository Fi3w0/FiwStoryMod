package com.fiw.fiwstory.data;

import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
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
 * Sistema de tracking de corrupción por items en inventario.
 * - Items corruptos en inventario: fase 1 después de 30-90 minutos
 * - Al quitar items: efecto se quita en 15 segundos
 * - Tracking por jugador y por item
 */
public class CorruptionData extends PersistentState {
    
    private static final String DATA_NAME = "fiwstory_corruption_data";
    
    // Datos por jugador
    private final Map<UUID, PlayerCorruptionData> playerData = new HashMap<>();
    
    // Tiempos en ticks (20 ticks = 1 segundo)
    private static final int REMOVAL_DELAY = 20 * 15; // 15 segundos para quitar si se remueven items
    private static final int MIN_ACTIVATION_TIME = 20 * 60 * 30; // 30 minutos mínimo
    private static final int MAX_ACTIVATION_TIME = 20 * 60 * 90; // 90 minutos máximo
    
    public static CorruptionData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        CorruptionData state = persistentStateManager.getOrCreate(CorruptionData::createFromNbt, CorruptionData::new, DATA_NAME);
        state.markDirty();
        return state;
    }
    
    public CorruptionData() {
        super();
    }
    
    public static CorruptionData createFromNbt(NbtCompound nbt) {
        CorruptionData data = new CorruptionData();
        
        NbtList playersList = nbt.getList("players", 10); // Tipo 10 = NbtCompound
        for (int i = 0; i < playersList.size(); i++) {
            NbtCompound playerCompound = playersList.getCompound(i);
            UUID playerId = playerCompound.getUuid("uuid");
            PlayerCorruptionData playerData = PlayerCorruptionData.fromNbt(playerCompound);
            data.playerData.put(playerId, playerData);
        }
        
        return data;
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList playersList = new NbtList();
        
        for (Map.Entry<UUID, PlayerCorruptionData> entry : playerData.entrySet()) {
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
     * Actualiza el tracking de corrupción para un jugador.
     * Debe llamarse regularmente (cada tick o cada pocos ticks).
     */
    public void updatePlayerCorruption(net.minecraft.server.network.ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerCorruptionData data = playerData.computeIfAbsent(playerId, uuid -> new PlayerCorruptionData());
        
        // Verificar items corruptos en inventario
        boolean hasCorruptItems = checkCorruptItems(player);
        long currentTime = player.getWorld().getTime();
        
        if (hasCorruptItems) {
            // Incrementar tiempo con items corruptos
            data.corruptItemTime++;
            
            // Calcular tiempo de activación aleatorio para este jugador
            if (data.activationTime == 0) {
                // Establecer tiempo de activación aleatorio entre 30-90 minutos
                data.activationTime = MIN_ACTIVATION_TIME + 
                    (player.getWorld().random.nextInt(MAX_ACTIVATION_TIME - MIN_ACTIVATION_TIME));
            }
            
            // Verificar si se activa la corrupción permanente
            if (data.corruptItemTime >= data.activationTime && !data.corruptionActive) {
                activateCorruption(player, data);
            }
            
            // Resetear timer de remoción si tenía items
            data.removalTimer = 0;
            data.hadCorruptItemsLastTick = true;
            
        } else if (data.hadCorruptItemsLastTick) {
            // El jugador tenía items corruptos en el tick anterior pero no ahora
            data.removalTimer++;
            data.hadCorruptItemsLastTick = false;
            
            // Verificar si se debe remover la corrupción (15 segundos)
            if (data.removalTimer >= REMOVAL_DELAY && data.corruptionActive) {
                deactivateCorruption(player, data);
            }
            
        } else {
            // Resetear todo si no hay items corruptos por un tiempo
            data.corruptItemTime = 0;
            data.removalTimer = 0;
            data.hadCorruptItemsLastTick = false;
        }
        
        // Marcar como dirty para guardar
        this.markDirty();
    }
    
    /**
     * Fuerza la activación de corrupción para un jugador (para comandos/testing).
     */
    public void forceActivateCorruption(net.minecraft.server.network.ServerPlayerEntity player, int level) {
        UUID playerId = player.getUuid();
        PlayerCorruptionData data = playerData.computeIfAbsent(playerId, uuid -> new PlayerCorruptionData());
        
        data.corruptionActive = true;
        data.corruptionLevel = level;
        data.corruptItemTime = data.activationTime; // Simular tiempo completado
        
        // Aplicar efecto
        CorruptionStatusEffect.applyToPlayer(player, level, Integer.MAX_VALUE);
        FiwUtils.sendWarningMessage(player, "La corrupción ha sido forzadamente activada!");
        
        this.markDirty();
    }
    
    /**
     * Fuerza la desactivación de corrupción para un jugador.
     */
    public void forceDeactivateCorruption(net.minecraft.server.network.ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerCorruptionData data = playerData.get(playerId);
        
        if (data != null && data.corruptionActive) {
            deactivateCorruption(player, data);
            FiwUtils.sendInfoMessage(player, "La corrupción ha sido forzadamente desactivada!");
        }
    }
    
    /**
     * Verifica si un jugador tiene corrupción activa.
     */
    public boolean hasActiveCorruption(net.minecraft.server.network.ServerPlayerEntity player) {
        PlayerCorruptionData data = playerData.get(player.getUuid());
        return data != null && data.corruptionActive;
    }
    
    /**
     * Previene que la muerte o leche quiten la corrupción.
     * Si el jugador tiene corrupción activa, la mantiene.
     */
    public void preventCorruptionRemoval(net.minecraft.server.network.ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerCorruptionData data = playerData.get(playerId);
        
        if (data != null && data.corruptionActive) {
            // Re-aplicar efecto de corrupción para prevenir que se quite
            CorruptionStatusEffect.applyToPlayer(player, data.corruptionLevel, Integer.MAX_VALUE);
        }
    }
    
    /**
     * Obtiene información de corrupción de un jugador.
     */
    public String getPlayerCorruptionInfo(net.minecraft.server.network.ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        PlayerCorruptionData data = playerData.get(playerId);
        
        if (data == null) {
            return "Sin datos de corrupción";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Estado: ").append(data.corruptionActive ? "§cActiva§r" : "§aInactiva§r").append("\n");
        
        if (data.corruptionActive) {
            info.append("Nivel: ").append(data.corruptionLevel).append("/5\n");
        }
        
        info.append("Tiempo con items: ").append(formatTicksToTime(data.corruptItemTime)).append("\n");
        info.append("Tiempo para activar: ").append(formatTicksToTime(data.activationTime)).append("\n");
        
        if (data.removalTimer > 0) {
            info.append("Remoción en: ").append(formatTicksToTime(REMOVAL_DELAY - data.removalTimer)).append("\n");
        }
        
        return info.toString();
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private boolean checkCorruptItems(net.minecraft.server.network.ServerPlayerEntity player) {
        // Lista de items que causan corrupción
        return player.getInventory().containsAny(itemStack -> 
            itemStack.isOf(ModItems.CURSED_SPEAR_OF_FI3W0) ||
            itemStack.isOf(ModItems.CORRUPTED_CRYSTAL) ||
            itemStack.isOf(ModItems.FI3W0_GLASSES)
        );
    }
    
    private void activateCorruption(net.minecraft.server.network.ServerPlayerEntity player, PlayerCorruptionData data) {
        data.corruptionActive = true;
        data.corruptionLevel = 1; // Siempre empieza en nivel 1
        
        // Aplicar efecto de corrupción permanente
        CorruptionStatusEffect.applyToPlayer(player, 1, Integer.MAX_VALUE);
        
        // NO mensajes - los jugadores deben descubrir
        // Solo efectos sutiles
        
        // Efectos visuales sutiles
        com.fiw.fiwstory.lib.FiwEffects.spawnExplosionParticles(
            player.getWorld(),
            player.getPos(),
            net.minecraft.particle.ParticleTypes.SMOKE,
            10,
            1.0
        );
    }
    
    private void deactivateCorruption(net.minecraft.server.network.ServerPlayerEntity player, PlayerCorruptionData data) {
        data.corruptionActive = false;
        data.corruptionLevel = 0;
        data.corruptItemTime = 0;
        data.activationTime = 0; // Resetear para próxima activación aleatoria
        data.removalTimer = 0;
        
        // Remover efecto de corrupción
        CorruptionStatusEffect.removeFromPlayer(player);
        
        // NO mensajes - los jugadores deben descubrir
    }
    
    private String formatTicksToTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    // ========== CLASE INTERNA PARA DATOS DE JUGADOR ==========
    
    private static class PlayerCorruptionData {
        long corruptItemTime = 0; // Ticks con items corruptos
        long activationTime = 0; // Ticks necesarios para activar (aleatorio 30-90 min)
        long removalTimer = 0; // Ticks desde que quitó items
        boolean corruptionActive = false;
        int corruptionLevel = 0;
        boolean hadCorruptItemsLastTick = false;
        
        void writeNbt(NbtCompound nbt) {
            nbt.putLong("corruptItemTime", corruptItemTime);
            nbt.putLong("activationTime", activationTime);
            nbt.putLong("removalTimer", removalTimer);
            nbt.putBoolean("corruptionActive", corruptionActive);
            nbt.putInt("corruptionLevel", corruptionLevel);
            nbt.putBoolean("hadCorruptItemsLastTick", hadCorruptItemsLastTick);
        }
        
        static PlayerCorruptionData fromNbt(NbtCompound nbt) {
            PlayerCorruptionData data = new PlayerCorruptionData();
            data.corruptItemTime = nbt.getLong("corruptItemTime");
            data.activationTime = nbt.getLong("activationTime");
            data.removalTimer = nbt.getLong("removalTimer");
            data.corruptionActive = nbt.getBoolean("corruptionActive");
            data.corruptionLevel = nbt.getInt("corruptionLevel");
            data.hadCorruptItemsLastTick = nbt.getBoolean("hadCorruptItemsLastTick");
            return data;
        }
    }
}