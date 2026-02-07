package com.fiw.fiwstory.lib;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Utilidades NBT simplificadas para Fiw Story Mod.
 * Proporciona métodos estáticos para trabajar con NBT de forma consistente.
 */
public class FiwNBT {
    
    // Namespace base para todos los tags NBT del mod
    public static final String NAMESPACE = "fiwstory";
    
    // Tags principales para sistema de bind
    public static final String BOUND_TO = NAMESPACE + ":bound_to";      // UUID del jugador vinculado
    public static final String BIND_DATE = NAMESPACE + ":bind_date";    // Fecha de vinculación (timestamp)
    public static final String BIND_BY = NAMESPACE + ":bind_by";        // Nombre del admin que vinculó
    public static final String BIND_LEVEL = NAMESPACE + ":bind_level";  // Nivel de vinculación (1-5)
    
    // Tags para datos de artefactos
    public static final String ARTIFACT_TYPE = NAMESPACE + ":artifact_type";  // Tipo de artefacto
    public static final String ARTIFACT_TIER = NAMESPACE + ":artifact_tier";  // Tier del artefacto
    public static final String ARTIFACT_USES = NAMESPACE + ":artifact_uses";  // Usos acumulados
    
    // Tags para sistema de corrupción
    public static final String CORRUPTION_LEVEL = NAMESPACE + ":corruption_level";  // Nivel de corrupción (0-100)
    public static final String CORRUPTION_SOURCE = NAMESPACE + ":corruption_source"; // Fuente de corrupción
    
    // Tags para cooldowns
    public static final String COOLDOWN_END = NAMESPACE + ":cooldown_end";  // Timestamp de fin de cooldown
    public static final String COOLDOWN_TYPE = NAMESPACE + ":cooldown_type"; // Tipo de cooldown
    
    // Tags para datos temporales
    public static final String LAST_USED = NAMESPACE + ":last_used";  // Último uso (timestamp)
    public static final String CHARGE_LEVEL = NAMESPACE + ":charge_level"; // Nivel de carga (0-100)
    
    // Tags para sistema de buffos sin pociones
    public static final String BUFF_CORRUPTION_DAMAGE = NAMESPACE + ":buff_corruption_damage"; // % daño por corrupción
    public static final String BUFF_CORRUPTION_SPEED = NAMESPACE + ":buff_corruption_speed";   // % velocidad por corrupción
    public static final String BUFF_KILL_DAMAGE = NAMESPACE + ":buff_kill_damage";             // % daño por kills
    public static final String BUFF_KILL_END = NAMESPACE + ":buff_kill_end";                   // Timestamp fin buffo kills
    public static final String BUFF_END_DAMAGE = NAMESPACE + ":buff_end_damage";               // % daño en End
    public static final String BUFF_ACTIVE = NAMESPACE + ":buff_active";                       // Buffos activos (bitmask)
    public static final String SOULBOUND = NAMESPACE + ":soulbound";                           // Item ligado al alma
    public static final String RECENT_KILLS = NAMESPACE + ":recent_kills";                     // Kills recientes
    public static final String LAST_KILL_TIME = NAMESPACE + ":last_kill_time";                 // Timestamp último kill
    
    /**
     * Verifica si un ItemStack tiene un tag NBT específico.
     */
    public static boolean hasTag(ItemStack stack, String tag) {
        if (stack.isEmpty() || !stack.hasNbt()) {
            return false;
        }
        return stack.getNbt().contains(tag);
    }
    
    /**
     * Obtiene un valor String de un tag NBT.
     */
    public static String getString(ItemStack stack, String tag, String defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        return stack.getNbt().getString(tag);
    }
    
    /**
     * Establece un valor String en un tag NBT.
     */
    public static void setString(ItemStack stack, String tag, String value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(tag, value);
    }
    
    /**
     * Obtiene un valor int de un tag NBT.
     */
    public static int getInt(ItemStack stack, String tag, int defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        return stack.getNbt().getInt(tag);
    }
    
    /**
     * Establece un valor int en un tag NBT.
     */
    public static void setInt(ItemStack stack, String tag, int value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(tag, value);
    }
    
    /**
     * Obtiene un valor long de un tag NBT.
     */
    public static long getLong(ItemStack stack, String tag, long defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        return stack.getNbt().getLong(tag);
    }
    
    /**
     * Establece un valor long en un tag NBT.
     */
    public static void setLong(ItemStack stack, String tag, long value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putLong(tag, value);
    }
    
    /**
     * Obtiene un valor float de un tag NBT.
     */
    public static float getFloat(ItemStack stack, String tag, float defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        return stack.getNbt().getFloat(tag);
    }
    
    /**
     * Establece un valor float en un tag NBT.
     */
    public static void setFloat(ItemStack stack, String tag, float value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putFloat(tag, value);
    }
    
    /**
     * Obtiene un valor boolean de un tag NBT.
     */
    public static boolean getBoolean(ItemStack stack, String tag, boolean defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        return stack.getNbt().getBoolean(tag);
    }
    
    /**
     * Establece un valor boolean en un tag NBT.
     */
    public static void setBoolean(ItemStack stack, String tag, boolean value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean(tag, value);
    }
    
    /**
     * Obtiene un UUID de un tag NBT.
     */
    public static UUID getUuid(ItemStack stack, String tag, UUID defaultValue) {
        if (!hasTag(stack, tag)) {
            return defaultValue;
        }
        NbtCompound nbt = stack.getNbt();
        if (nbt.containsUuid(tag)) {
            return nbt.getUuid(tag);
        }
        return defaultValue;
    }
    
    /**
     * Establece un UUID en un tag NBT.
     */
    public static void setUuid(ItemStack stack, String tag, UUID value) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putUuid(tag, value);
    }
    
    /**
     * Verifica si un artefacto está vinculado a un jugador.
     */
    public static boolean isBound(ItemStack stack) {
        return hasTag(stack, BOUND_TO) && getUuid(stack, BOUND_TO, null) != null;
    }
    
    /**
     * Obtiene el UUID del jugador al que está vinculado el artefacto.
     */
    public static UUID getBoundTo(ItemStack stack) {
        return getUuid(stack, BOUND_TO, null);
    }
    
    /**
     * Vincula un artefacto a un jugador.
     */
    public static void bindTo(ItemStack stack, UUID playerId, String adminName) {
        setUuid(stack, BOUND_TO, playerId);
        setLong(stack, BIND_DATE, System.currentTimeMillis());
        setString(stack, BIND_BY, adminName);
        setInt(stack, BIND_LEVEL, 1); // Nivel inicial
    }
    
    /**
     * Desvincula un artefacto.
     */
    public static void unbind(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            nbt.remove(BOUND_TO);
            nbt.remove(BIND_DATE);
            nbt.remove(BIND_BY);
            nbt.remove(BIND_LEVEL);
        }
    }
    
    /**
     * Incrementa el contador de usos de un artefacto.
     */
    public static void incrementUses(ItemStack stack) {
        int currentUses = getInt(stack, ARTIFACT_USES, 0);
        setInt(stack, ARTIFACT_USES, currentUses + 1);
    }
    
    /**
     * Obtiene el número de usos acumulados de un artefacto.
     */
    public static int getUses(ItemStack stack) {
        return getInt(stack, ARTIFACT_USES, 0);
    }
    
    /**
     * Verifica si un cooldown ha terminado.
     */
    public static boolean isCooldownOver(ItemStack stack, String cooldownType) {
        long cooldownEnd = getLong(stack, COOLDOWN_END + "_" + cooldownType, 0);
        return System.currentTimeMillis() > cooldownEnd;
    }
    
    /**
     * Establece un cooldown para un artefacto.
     */
    public static void setCooldown(ItemStack stack, String cooldownType, long durationMs) {
        setLong(stack, COOLDOWN_END + "_" + cooldownType, System.currentTimeMillis() + durationMs);
        setString(stack, COOLDOWN_TYPE, cooldownType);
    }
    
    /**
     * Obtiene el tiempo restante de cooldown en milisegundos.
     */
    public static long getCooldownRemaining(ItemStack stack, String cooldownType) {
        long cooldownEnd = getLong(stack, COOLDOWN_END + "_" + cooldownType, 0);
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Verifica si un cooldown está activo.
     */
    public static boolean isOnCooldown(ItemStack stack, String cooldownType) {
        return getCooldownRemaining(stack, cooldownType) > 0;
    }
    
    /**
     * Limpia todos los tags NBT específicos del mod de un ItemStack.
     */
    public static void clearAllModTags(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            nbt.remove(BOUND_TO);
            nbt.remove(BIND_DATE);
            nbt.remove(BIND_BY);
            nbt.remove(BIND_LEVEL);
            nbt.remove(ARTIFACT_TYPE);
            nbt.remove(ARTIFACT_TIER);
            nbt.remove(ARTIFACT_USES);
            nbt.remove(CORRUPTION_LEVEL);
            nbt.remove(CORRUPTION_SOURCE);
            nbt.remove(COOLDOWN_END);
            nbt.remove(COOLDOWN_TYPE);
            nbt.remove(LAST_USED);
            nbt.remove(CHARGE_LEVEL);
            nbt.remove(BUFF_CORRUPTION_DAMAGE);
            nbt.remove(BUFF_CORRUPTION_SPEED);
            nbt.remove(BUFF_KILL_DAMAGE);
            nbt.remove(BUFF_KILL_END);
            nbt.remove(BUFF_END_DAMAGE);
            nbt.remove(BUFF_ACTIVE);
            nbt.remove(SOULBOUND);
            nbt.remove(RECENT_KILLS);
            nbt.remove(LAST_KILL_TIME);
        }
    }
    
    /**
     * Copia los tags NBT específicos del mod de un ItemStack a otro.
     */
    public static void copyModTags(ItemStack source, ItemStack destination) {
        if (source.isEmpty() || destination.isEmpty() || !source.hasNbt()) {
            return;
        }
        
        NbtCompound sourceNbt = source.getNbt();
        NbtCompound destNbt = destination.getOrCreateNbt();
        
        String[] modTags = {
            BOUND_TO, BIND_DATE, BIND_BY, BIND_LEVEL,
            ARTIFACT_TYPE, ARTIFACT_TIER, ARTIFACT_USES,
            CORRUPTION_LEVEL, CORRUPTION_SOURCE,
            COOLDOWN_END, COOLDOWN_TYPE,
            LAST_USED, CHARGE_LEVEL,
            BUFF_CORRUPTION_DAMAGE, BUFF_CORRUPTION_SPEED,
            BUFF_KILL_DAMAGE, BUFF_KILL_END, BUFF_END_DAMAGE,
            BUFF_ACTIVE, SOULBOUND, RECENT_KILLS, LAST_KILL_TIME
        };
        
        for (String tag : modTags) {
            if (sourceNbt.contains(tag)) {
                destNbt.put(tag, sourceNbt.get(tag));
            }
        }
    }
    
    // ========== MÉTODOS PARA SISTEMA DE BUFFOS ==========
    
    /**
     * Verifica si un item está ligado al alma (no dropea al morir).
     */
    public static boolean isSoulbound(ItemStack stack) {
        return getBoolean(stack, SOULBOUND, false);
    }
    
    /**
     * Marca un item como ligado al alma.
     */
    public static void setSoulbound(ItemStack stack, boolean soulbound) {
        setBoolean(stack, SOULBOUND, soulbound);
    }
    
    /**
     * Obtiene kills recientes para buffos temporales.
     */
    public static int getRecentKills(ItemStack stack) {
        return getInt(stack, RECENT_KILLS, 0);
    }
    
    /**
     * Establece kills recientes.
     */
    public static void setRecentKills(ItemStack stack, int kills) {
        setInt(stack, RECENT_KILLS, Math.min(5, kills)); // Máximo 5 stacks
    }
    
    /**
     * Incrementa kills recientes.
     */
    public static void incrementRecentKills(ItemStack stack) {
        int current = getRecentKills(stack);
        setRecentKills(stack, current + 1);
        setLong(stack, LAST_KILL_TIME, System.currentTimeMillis());
    }
    
    /**
     * Obtiene el tiempo del último kill.
     */
    public static long getLastKillTime(ItemStack stack) {
        return getLong(stack, LAST_KILL_TIME, 0);
    }
    
    /**
     * Actualiza buffos de kills (decaimiento con tiempo).
     */
    public static void updateKillBuffs(ItemStack stack) {
        long lastKillTime = getLastKillTime(stack);
        if (lastKillTime == 0) return;
        
        long timeSinceKill = System.currentTimeMillis() - lastKillTime;
        // Decaimiento: -1 stack cada 6 segundos después de 30 segundos sin kills
        if (timeSinceKill > 30000) { // 30 segundos
            int decayStacks = (int) ((timeSinceKill - 30000) / 6000); // 6 segundos por stack
            int currentKills = getRecentKills(stack);
            int newKills = Math.max(0, currentKills - decayStacks);
            setRecentKills(stack, newKills);
            
            if (newKills == 0) {
                setLong(stack, LAST_KILL_TIME, 0); // Resetear si no hay kills
            }
        }
    }
    
    /**
     * Obtiene buffo de daño por corrupción (0.0 - 0.3 = 0-30%).
     */
    public static float getCorruptionDamageBuff(ItemStack stack) {
        return getFloat(stack, BUFF_CORRUPTION_DAMAGE, 0.0f);
    }
    
    /**
     * Establece buffo de daño por corrupción.
     */
    public static void setCorruptionDamageBuff(ItemStack stack, float buff) {
        setFloat(stack, BUFF_CORRUPTION_DAMAGE, Math.min(0.3f, buff)); // Máximo 30%
    }
    
    /**
     * Obtiene buffo de velocidad por corrupción.
     */
    public static float getCorruptionSpeedBuff(ItemStack stack) {
        return getFloat(stack, BUFF_CORRUPTION_SPEED, 0.0f);
    }
    
    /**
     * Establece buffo de velocidad por corrupción.
     */
    public static void setCorruptionSpeedBuff(ItemStack stack, float buff) {
        setFloat(stack, BUFF_CORRUPTION_SPEED, Math.min(0.15f, buff)); // Máximo 15%
    }
    
    /**
     * Obtiene buffo de daño por kills (0.0 - 0.25 = 0-25%).
     */
    public static float getKillDamageBuff(ItemStack stack) {
        return getFloat(stack, BUFF_KILL_DAMAGE, 0.0f);
    }
    
    /**
     * Establece buffo de daño por kills.
     */
    public static void setKillDamageBuff(ItemStack stack, float buff) {
        setFloat(stack, BUFF_KILL_DAMAGE, Math.min(0.25f, buff)); // Máximo 25%
    }
    
    /**
     * Obtiene buffo de daño en End.
     */
    public static float getEndDamageBuff(ItemStack stack) {
        return getFloat(stack, BUFF_END_DAMAGE, 0.0f);
    }
    
    /**
     * Establece buffo de daño en End.
     */
    public static void setEndDamageBuff(ItemStack stack, float buff) {
        setFloat(stack, BUFF_END_DAMAGE, Math.min(0.15f, buff)); // Máximo 15%
    }
    
    /**
     * Actualiza todos los buffos basados en condiciones actuales.
     */
    public static void updateAllBuffs(ItemStack stack, PlayerEntity player) {
        // Actualizar buffos de kills (decaimiento)
        updateKillBuffs(stack);
        
        // Calcular buffo por kills (5% por stack, máximo 25%)
        int recentKills = getRecentKills(stack);
        float killBuff = recentKills * 0.05f;
        setKillDamageBuff(stack, killBuff);
        
        // Buffo en End se maneja en tiempo real, no se guarda
    }
}