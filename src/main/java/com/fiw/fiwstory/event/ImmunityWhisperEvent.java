package com.fiw.fiwstory.event;

import com.fiw.fiwstory.data.ImmunityData;
import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

/**
 * Evento para manejar susurros cada 10 minutos para jugadores inmunes con corrupción.
 */
public class ImmunityWhisperEvent {
    
    private static int tickCounter = 0;
    private static final Random RANDOM = new Random();
    
    // Susurros para jugadores inmunes con corrupción
    private static final String[] IMMUNE_WHISPERS = {
        "La inmunidad es solo una ilusión...",
        "La corrupción acecha incluso tras el velo...",
        "Tu protección es frágil como cristal...",
        "Los susurros encuentran camino incluso en la inmunidad...",
        "La oscuridad conoce todos los rincones...",
        "Tu escudo tiene grietas que solo yo veo...",
        "La pureza que buscas está manchada...",
        "Inmune pero no ignorante de mi presencia...",
        "Tu barrera no silencia los ecos...",
        "La corrupción respeta tu inmunidad, pero no la olvida..."
    };
    
    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(ImmunityWhisperEvent::onServerTick);
    }
    
    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        // Solo procesar cada 5 ticks (4Hz) para optimización
        if (tickCounter % 5 != 0) {
            return;
        }
        
        ImmunityData immunityData = ImmunityData.getServerState(server);
        
        // Procesar cada jugador
        server.getPlayerManager().getPlayerList().forEach(player -> {
            // Verificar si el jugador es inmune y tiene corrupción
            if (immunityData.isPlayerImmune(player.getUuid())) {
                int corruptionLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(player);
                
                if (corruptionLevel > 0) {
                    // Actualizar timer y verificar si es hora de susurro
                    boolean shouldWhisper = immunityData.updatePlayerImmunity(player);
                    
                    if (shouldWhisper) {
                        sendImmuneWhisper(player);
                    }
                }
            }
        });
    }
    
    private static void sendImmuneWhisper(net.minecraft.server.network.ServerPlayerEntity player) {
        String whisper = IMMUNE_WHISPERS[RANDOM.nextInt(IMMUNE_WHISPERS.length)];
        
        Text message = Text.literal("§8§o«" + whisper + "»§r")
            .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        
        player.sendMessage(message, false);
        
        // Sonido de susurro sutil
        player.getWorld().playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_STARE,
            net.minecraft.sound.SoundCategory.AMBIENT,
            0.2f,
            1.5f + (RANDOM.nextFloat() * 0.5f)
        );
    }
}