package com.fiw.fiwstory.event;

import com.fiw.fiwstory.data.CorruptionData;
import com.fiw.fiwstory.data.ImmunityData;
import com.fiw.fiwstory.effect.CorruptionConstants;
import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.item.custom.PureCrystalItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

/**
 * Eventos para tracking de corrupción, susurros de inmunidad y drops especiales.
 */
public class CorruptionTrackingEvent {

    private static int tickCounter = 0;
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

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
        // Tick del servidor para tracking de corrupción + susurros de inmunidad
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
        if (tickCounter % CorruptionConstants.TICK_PROCESSING_INTERVAL != 0) {
            return;
        }

        // Obtener datos una sola vez por tick (no por jugador)
        CorruptionData corruptionData = CorruptionData.getServerState(server);
        ImmunityData immunityData = ImmunityData.getServerState(server);

        // Actualizar tracking de corrupción y susurros de inmunidad para todos los jugadores
        server.getPlayerManager().getPlayerList().forEach(player -> {
            corruptionData.updatePlayerCorruption(player);

            // Susurros de inmunidad
            if (immunityData.isPlayerImmune(player.getUuid())) {
                int corruptionLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(player);
                if (corruptionLevel > 0) {
                    boolean shouldWhisper = immunityData.updatePlayerImmunity(player);
                    if (shouldWhisper) {
                        sendImmuneWhisper(player);
                    }
                }
            }
        });
    }

    private static void sendImmuneWhisper(ServerPlayerEntity player) {
        Random random = RANDOM.get();
        String whisper = IMMUNE_WHISPERS[random.nextInt(IMMUNE_WHISPERS.length)];

        Text message = Text.literal("§8§o«" + whisper + "»§r")
            .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);

        player.sendMessage(message, false);

        // Sonido de susurro sutil
        player.getWorld().playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_STARE,
            net.minecraft.sound.SoundCategory.AMBIENT,
            CorruptionConstants.IMMUNE_WHISPER_VOLUME,
            CorruptionConstants.IMMUNE_WHISPER_PITCH_BASE + (random.nextFloat() * CorruptionConstants.IMMUNE_WHISPER_PITCH_RANGE)
        );
    }

    private static void handleDiamondMining(net.minecraft.entity.player.PlayerEntity player, BlockState state) {
        // Verificar si es diamante
        if (state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            PureCrystalItem.handleDiamondMining(player, state);
        }
    }
}
