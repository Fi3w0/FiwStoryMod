package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.data.CorruptionData;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.server.MinecraftServer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Mix Puro - crafteo de Cristal Puro + Poción de Regeneración.
 * Consumible que reduce el nivel de corrupción.
 * 6-15 mixes para bajar una fase, hasta fase 1.
 */
public class PureMixItem extends Item {
    
    public PureMixItem(Settings settings) {
        super(settings.maxCount(16).food(
            new net.minecraft.item.FoodComponent.Builder()
                .hunger(0) // No da hambre
                .saturationModifier(0.0f)
                .alwaysEdible() // Se puede comer sin hambre
                .snack() // Comida rápida
                .build()
        ));
    }
    

    
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(user instanceof PlayerEntity player)) {
            return stack;
        }
        
        if (world.isClient()) {
            return super.finishUsing(stack, world, user);
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        // Estadísticas
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);

        // Verificar si el jugador tiene corrupción (doble verificación)
        int currentLevel = com.fiw.fiwstory.effect.CorruptionStatusEffect.getPlayerCorruptionLevel(player);

        if (currentLevel <= 0) {
            // Si no tiene corrupción, solo consumir sin efectos
            return super.finishUsing(stack, world, user);
        }

        // Obtener o inicializar contador de mixes (persistido en CorruptionData)
        java.util.UUID playerId = player.getUuid();
        com.fiw.fiwstory.data.CorruptionData corruptionData =
            com.fiw.fiwstory.data.CorruptionData.getServerState(serverPlayer.getServer());
        int mixCount = corruptionData.getMixCount(playerId) + 1;
        corruptionData.setMixCount(playerId, mixCount);

        // Calcular mixes requeridos (6-15 aleatorio por jugador)
        int requiredMixes = getRequiredMixesForPlayer(playerId);

        // Efectos inmediatos (sin mensajes)
        applyImmediateEffects(player, currentLevel);

        // Verificar si alcanzó mixes suficientes para bajar fase
        if (mixCount >= requiredMixes && currentLevel > 1) {
            // Reducir fase de corrupción (sin mensajes)
            reduceCorruptionLevel(player, currentLevel);

            // Resetear contador
            corruptionData.resetMixCount(playerId);
        }
        
        // Efectos visuales sutiles
        com.fiw.fiwstory.lib.FiwEffects.spawnExplosionParticles(world, player.getPos(),
            net.minecraft.particle.ParticleTypes.END_ROD, 10, 1.0);
        
        // Consumir item
        return super.finishUsing(stack, world, user);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        // Verificar si el jugador tiene corrupción
        int currentLevel = com.fiw.fiwstory.effect.CorruptionStatusEffect.getPlayerCorruptionLevel(player);
        
        if (currentLevel <= 0) {
            // No tiene corrupción - no puede comer el mix
            if (!world.isClient()) {
                // Efecto visual de rechazo
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    0.5f, 1.0f);
            }
            return TypedActionResult.fail(stack);
        }
        
        if (player.canConsume(false)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        
        return TypedActionResult.fail(stack);
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }
    
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32; // Tiempo para consumir (1.6 segundos)
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private int getRequiredMixesForPlayer(java.util.UUID playerId) {
        // Usar hash del UUID para valor semi-aleatorio pero consistente
        int hash = playerId.hashCode();
        return 6 + (Math.abs(hash) % 10); // 6-15 mixes
    }
    
    private void applyImmediateEffects(PlayerEntity player, int currentLevel) {
        // Efectos inmediatos al consumir mix (sin efectos negativos)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.REGENERATION,
            80, // 4 segundos
            0,
            false,
            false // No mostrar partículas
        ));
        
        // NO remover efectos negativos - los jugadores deben descubrir
        // NO mensajes
    }
    
    private void reduceCorruptionLevel(PlayerEntity player, int currentLevel) {
        int newLevel = currentLevel - 1;
        
        // Remover efecto actual
        com.fiw.fiwstory.effect.CorruptionStatusEffect.removeFromPlayer(player);
        
        if (newLevel >= 1) {
            // Aplicar nuevo efecto con nivel reducido
            com.fiw.fiwstory.effect.CorruptionStatusEffect.applyToPlayer(
                player, newLevel, Integer.MAX_VALUE);
        }
        
        // NO mensajes - los jugadores deben descubrir
        // Efectos visuales sutiles
        com.fiw.fiwstory.lib.FiwEffects.spawnExplosionParticles(player.getWorld(), player.getPos(),
            net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, 15, 1.5);
    }
    
    // ========== TOOLTIP PERSONALIZADO ==========
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Mix Puro»").formatted(Formatting.GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Breve tregua contra la oscuridad").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§a§oElixir de Purificación§r").formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.literal("§7• Energía purificante§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Sabor reconfortante§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«Un respiro en la tormenta eterna»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // El mix puro siempre brilla
        return true;
    }
    
    // ========== MÉTODOS ESTÁTICOS PARA COMANDOS ==========
    
    /**
     * Obtiene el progreso de purificación de un jugador.
     */
    public static String getPlayerPurificationProgress(MinecraftServer server, java.util.UUID playerId) {
        return CorruptionData.getPlayerPurificationProgress(server, playerId);
    }

    /**
     * Resetea el progreso de purificación de un jugador.
     */
    public static void resetPlayerPurificationProgress(MinecraftServer server, java.util.UUID playerId) {
        CorruptionData.getServerState(server).resetMixCount(playerId);
    }
}