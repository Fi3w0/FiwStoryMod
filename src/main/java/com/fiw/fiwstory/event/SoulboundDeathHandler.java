package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.lib.FiwNBT;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Sistema para items ligados al alma (soulbound).
 * - Lanza de Fi3w0 y Gafas de Fi3w0 no dropean al morir
 * - Compatible con mods de tumbas (se copian al respawnar)
 * - Efectos visuales/sonoros al mantener items
 */
public class SoulboundDeathHandler {
    
    public static void registerEvents() {
        // Copiar items soulbound al respawnar
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            handleSoulboundRespawn(oldPlayer, newPlayer);
        });
        
        // También manejar muerte (por si algún mod elimina items antes del respawn)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) { // Solo si murió (no teletransporte)
                handleSoulboundRespawn(oldPlayer, newPlayer);
            }
        });
    }
    
    /**
     * Maneja la copia de items soulbound al respawnar.
     */
    private static void handleSoulboundRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        boolean hasSoulboundItems = false;
        
        // Revisar inventario del jugador muerto
        for (int i = 0; i < oldPlayer.getInventory().size(); i++) {
            ItemStack stack = oldPlayer.getInventory().getStack(i);
            if (isSoulboundItem(stack)) {
                // Crear copia del item con sus datos NBT
                ItemStack copy = stack.copy();
                
                // Buscar slot vacío en el nuevo jugador
                int emptySlot = newPlayer.getInventory().getEmptySlot();
                if (emptySlot != -1) {
                    newPlayer.getInventory().setStack(emptySlot, copy);
                    hasSoulboundItems = true;
                }
            }
        }
        
        // Mensaje y efectos si tenía items soulbound
        if (hasSoulboundItems) {
            newPlayer.sendMessage(
                Text.literal("§5El alma de tus artefactos permanece contigo...§r")
                    .formatted(Formatting.DARK_PURPLE),
                false
            );
            
            // Efecto sonoro
            newPlayer.getWorld().playSound(
                null,
                newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                0.7f, 1.0f
            );
        }
    }
    
    /**
     * Maneja la prevención de drops para items soulbound.
     * Se llama desde otros sistemas cuando se detecta que un item soulbound va a ser dropeado.
     */
    public static boolean preventSoulboundDrop(PlayerEntity player, ItemStack stack) {
        if (isSoulboundItem(stack)) {
            // Efecto visual/sonoro de cancelación
            if (!player.getWorld().isClient()) {
                player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                    SoundCategory.PLAYERS,
                    0.5f, 1.5f
                );
            }
            
            return true; // Prevenir drop
        }
        
        return false; // Permitir drop
    }
    
    /**
     * Verifica si un item está ligado al alma.
     */
    public static boolean isSoulboundItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // Items específicos que son siempre soulbound
        if (stack.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0 ||
            stack.getItem() == ModItems.FI3W0_GLASSES) {
            return true;
        }
        
        // Items marcados como soulbound con NBT
        return FiwNBT.isSoulbound(stack);
    }
    
    /**
     * Marca un item como soulbound.
     */
    public static void setSoulbound(ItemStack stack, boolean soulbound) {
        FiwNBT.setSoulbound(stack, soulbound);
        
        // Efecto visual al marcar como soulbound
        if (soulbound && stack.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0) {
            // La lanza ya tiene glint por ser legendaria
            // Podríamos añadir partículas especiales aquí
        }
    }
    
    /**
     * Verifica si un jugador tiene items soulbound equipados.
     */
    public static boolean hasSoulboundEquipped(PlayerEntity player) {
        // Revisar equipo principal y armadura
        if (isSoulboundItem(player.getMainHandStack()) ||
            isSoulboundItem(player.getOffHandStack())) {
            return true;
        }
        
        // Revisar armadura
        for (ItemStack armorStack : player.getArmorItems()) {
            if (isSoulboundItem(armorStack)) {
                return true;
            }
        }
        
        return false;
    }
}