package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.data.HeartData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FallenGodHeartArtifact extends Item {
    // UUIDs para modificadores de atributos
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("A3B2C1D0-E4F5-4678-9A0B-1C2D3E4F5A6B");
    
    // Tiempos en ticks (20 ticks = 1 segundo)
    private static final int DRAIN_INTERVAL = 20 * 20; // 20 segundos
    private static final int RECOVERY_INTERVAL = 60 * 20; // 60 segundos
    private static final float MIN_HEALTH = 6.0f; // 3 corazones (6 puntos de vida)
    
    public FallenGodHeartArtifact(Settings settings) {
        super(settings.maxCount(1).fireproof());
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Corazón de Dios Caído»").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Uno de los artefactos legendarios de Dios Faraón").formatted(Formatting.GOLD, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oParece que sigue vivo, que asco§r").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("§7• Sientes cada latido del corazón§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Habilidad pasiva en offhand§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Resistencia I + Fuerza I§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Cada 20s: -1 corazón de vida máxima§r").formatted(Formatting.RED));
        tooltip.add(Text.literal("§7• Mínimo: 3 corazones (sin buffs)§r").formatted(Formatting.RED));
        tooltip.add(Text.literal("§7• Recuperación: 1 corazón/min sin usar§r").formatted(Formatting.GREEN));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«El precio del poder divino es un pedazo de tu propia esencia»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (!world.isClient()) {
            // Efecto visual al usar (partículas de corazón)
            player.sendMessage(Text.literal("§c❤ Sientes el latido del corazón divino...").formatted(Formatting.DARK_RED), false);
        }
        
        return TypedActionResult.success(stack);
    }

    // Método para manejar los efectos del corazón
    public static void handleHeartEffects(PlayerEntity player, World world) {
        if (world.isClient()) return;
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        long worldTime = world.getTime();
        
        // Verificar si tiene el corazón en offhand
        ItemStack offhandStack = player.getOffHandStack();
        boolean hasHeartInOffhand = offhandStack.getItem() instanceof FallenGodHeartArtifact;
        
        // Obtener datos persistentes del jugador
        HeartData.PlayerHeartData heartData = HeartData.get(player);
        
        if (hasHeartInOffhand) {
            // Aplicar buffs si no está en mínimo de vida
            if (heartData.getCurrentMaxHealth() > MIN_HEALTH) {
                // Resistencia I (en lugar de Regeneración)
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 
                    100, // 5 segundos
                    0, // Nivel 1
                    false, // No partículas ambientes
                    false, // No mostrar icono
                    true // Mostrar partículas
                ));
                
                // Fuerza I
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH, 
                    100, // 5 segundos
                    0, // Nivel 1
                    false, // No partículas ambientes
                    false, // No mostrar icono
                    true // Mostrar partículas
                ));
            }
            
            // Drenar vida máxima cada 30 segundos
            if (worldTime % DRAIN_INTERVAL == 0) {
                drainMaxHealth(player, heartData);
            }
            
            // Marcar que el jugador está usando el corazón
            heartData.setUsingHeart(true);
        } else {
            // El jugador no está usando el corazón
            heartData.setUsingHeart(false);
            
            // Recuperar vida máxima cada 60 segundos si no está en máximo
            if (worldTime % RECOVERY_INTERVAL == 0 && heartData.getCurrentMaxHealth() < heartData.getOriginalMaxHealth()) {
                recoverMaxHealth(player, heartData);
            }
        }
        
        // Aplicar la vida máxima actual al jugador
        applyMaxHealth(player, heartData);
        
        // Guardar datos
        heartData.markDirty();
    }
    
    private static void drainMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float currentMaxHealth = heartData.getCurrentMaxHealth();
        
        // Solo drenar si no hemos llegado al mínimo
        if (currentMaxHealth > MIN_HEALTH) {
            heartData.setCurrentMaxHealth(currentMaxHealth - 2.0f); // -1 corazón (2 puntos)
            
            // Efecto visual y sonido
            player.sendMessage(Text.literal("§4❤ El corazón divino consume parte de tu esencia vital...").formatted(Formatting.DARK_RED), false);
            
            // Partículas de daño
            if (player instanceof ServerPlayerEntity serverPlayer) {
                // Aquí podríamos agregar partículas si fuera necesario
            }
        } else if (currentMaxHealth == MIN_HEALTH) {
            // Mensaje cuando se llega al mínimo
            player.sendMessage(Text.literal("§4⚠ ¡Has alcanzado el límite mínimo de vida! El corazón ya no otorga buffs.").formatted(Formatting.DARK_RED), false);
        }
    }
    
    private static void recoverMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float currentMaxHealth = heartData.getCurrentMaxHealth();
        float originalMaxHealth = heartData.getOriginalMaxHealth();
        
        // Solo recuperar si no hemos llegado al máximo original
        if (currentMaxHealth < originalMaxHealth) {
            float newHealth = Math.min(currentMaxHealth + 2.0f, originalMaxHealth); // +1 corazón (2 puntos)
            heartData.setCurrentMaxHealth(newHealth);
            
            // Mensaje de recuperación
            if (newHealth < originalMaxHealth) {
                player.sendMessage(Text.literal("§a❤ Tu esencia vital se recupera lentamente...").formatted(Formatting.GREEN), false);
            } else {
                player.sendMessage(Text.literal("§a✨ ¡Has recuperado toda tu esencia vital!").formatted(Formatting.GREEN), false);
            }
        }
    }
    
    private static void applyMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float currentMaxHealth = heartData.getCurrentMaxHealth();
        float originalMaxHealth = heartData.getOriginalMaxHealth();
        
        // Aplicar modificador de vida máxima
        var attributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attributeInstance != null) {
            // Remover modificador anterior si existe
            var oldModifier = attributeInstance.getModifier(MAX_HEALTH_MODIFIER_ID);
            if (oldModifier != null) {
                attributeInstance.removeModifier(MAX_HEALTH_MODIFIER_ID);
            }
            
            // Aplicar nuevo modificador si la vida máxima es diferente
            if (currentMaxHealth != originalMaxHealth) {
                float healthDifference = currentMaxHealth - originalMaxHealth;
                attributeInstance.addPersistentModifier(new EntityAttributeModifier(
                    MAX_HEALTH_MODIFIER_ID,
                    "Fallen God Heart max health",
                    healthDifference,
                    EntityAttributeModifier.Operation.ADDITION
                ));
                
                // Ajustar vida actual si es mayor que la nueva vida máxima
                if (player.getHealth() > currentMaxHealth) {
                    player.setHealth(currentMaxHealth);
                }
            }
        }
    }
    
    // Método para resetear la vida máxima (al morir, salir del servidor, etc.)
    public static void resetMaxHealth(PlayerEntity player) {
        HeartData.PlayerHeartData heartData = HeartData.get(player);
        heartData.resetToOriginal();
        applyMaxHealth(player, heartData);
        heartData.markDirty();
    }
}