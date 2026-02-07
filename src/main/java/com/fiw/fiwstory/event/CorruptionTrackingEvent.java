package com.fiw.fiwstory.event;

import com.fiw.fiwstory.data.CorruptionData;
import com.fiw.fiwstory.item.custom.PureCrystalItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;

/**
 * Eventos para tracking de corrupción y drops especiales.
 */
public class CorruptionTrackingEvent {
    
    private static int tickCounter = 0;
    
    public static void registerEvents() {
        // Tick del servidor para tracking de corrupción
        ServerTickEvents.END_SERVER_TICK.register(CorruptionTrackingEvent::onServerTick);
        
        // Evento para drop de Cristal Puro al minar diamante
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient()) {
                handleDiamondMining(player, state);
            }
        });
    }
    
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        // Solo procesar cada 5 ticks (4Hz) para optimización
        if (tickCounter % 5 != 0) {
            return;
        }
        
        // Actualizar tracking de corrupción para todos los jugadores
        server.getPlayerManager().getPlayerList().forEach(player -> {
            CorruptionData data = CorruptionData.getServerState(server);
            data.updatePlayerCorruption(player);
        });
    }
    
    private static void handleDiamondMining(net.minecraft.entity.player.PlayerEntity player, BlockState state) {
        // Verificar si es diamante
        if (state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            PureCrystalItem.handleDiamondMining(player, state);
        }
    }
}