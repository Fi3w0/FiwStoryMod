package com.fiw.fiwstory.event;

import com.fiw.fiwstory.data.CorruptionData;
import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.effect.ModStatusEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class CorruptionPreventionEvent {
    
    public static void registerEvents() {
        // Prevenir que la muerte quite la corrupción (solo si ya la tenía)
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!newPlayer.getWorld().isClient()) {
                CorruptionData corruptionData = CorruptionData.getServerState(newPlayer.getServer());
                
                // Verificar si el jugador viejo (antes de morir) tenía corrupción
                // Solo re-aplicar si realmente la tenía
                if (corruptionData.hasActiveCorruption((ServerPlayerEntity) oldPlayer)) {
                    corruptionData.preventCorruptionRemoval((ServerPlayerEntity) newPlayer);
                }
            }
        });
        
        // Prevenir que la leche quite la corrupción - usando evento de tick
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player && !world.isClient()) {
                // Programar una verificación única después de cargar
                world.getServer().execute(() -> {
                    CorruptionData corruptionData = CorruptionData.getServerState(player.getServer());
                    
                    // Solo verificar si el jugador debería tener corrupción
                    if (corruptionData.hasActiveCorruption(player)) {
                        StatusEffectInstance corruptionEffect = player.getStatusEffect(ModStatusEffects.CORRUPTION);
                        
                        // Si debería tener corrupción pero no la tiene, re-aplicarla
                        if (corruptionEffect == null) {
                            corruptionData.preventCorruptionRemoval(player);
                        }
                    }
                });
            }
        });
        
        // También prevenir que la leche quite la corrupción al momento de usar
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            
            if (stack.getItem() == Items.MILK_BUCKET && !world.isClient()) {
                CorruptionData corruptionData = CorruptionData.getServerState(player.getServer());
                
                // Guardar el nivel de corrupción antes de que la leche actúe
                if (corruptionData.hasActiveCorruption((ServerPlayerEntity) player)) {
                    // Programar para re-aplicar la corrupción después de que la leche actúe
                    world.getServer().execute(() -> {
                        // Esperar un tick para que la leche actúe, luego re-aplicar
                        corruptionData.preventCorruptionRemoval((ServerPlayerEntity) player);
                    });
                }
            }
            
            return net.minecraft.util.TypedActionResult.pass(stack);
        });
    }
}