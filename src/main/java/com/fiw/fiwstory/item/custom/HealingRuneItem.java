package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.effect.ModStatusEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HealingRuneItem extends Item {
    
    public HealingRuneItem(Settings settings) {
        super(settings.maxCount(1)); // Solo 1 por stack
    }
    
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player && !world.isClient()) {
            // Verificar nivel de corrupción
            StatusEffectInstance corruptionEffect = player.getStatusEffect(ModStatusEffects.CORRUPTION);
            
            if (corruptionEffect != null) {
                int corruptionLevel = corruptionEffect.getAmplifier() + 1;
                
                // Solo funciona en nivel 1
                if (corruptionLevel == 1) {
                    // Curar completamente la corrupción
                    player.removeStatusEffect(ModStatusEffects.CORRUPTION);
                    
                    // Sonido de curación
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        1.0f, 1.0f);
                    
                    // Partículas de curación
                    if (!world.isClient()) {
                        for (int i = 0; i < 20; i++) {
                            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
                            double offsetY = world.random.nextDouble() * 2.0;
                            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
                            
                            ((net.minecraft.server.world.ServerWorld) world).spawnParticles(
                                net.minecraft.particle.ParticleTypes.HEART,
                                player.getX() + offsetX,
                                player.getY() + offsetY,
                                player.getZ() + offsetZ,
                                1, 0, 0, 0, 0.1
                            );
                        }
                    }
                    
                    // Consumir el item
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                } else {
                    // Nivel de corrupción demasiado alto - no hace nada
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        net.minecraft.sound.SoundCategory.PLAYERS,
                        0.5f, 1.0f);
                }
            } else {
                // No tiene corrupción - no hace nada
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    0.5f, 1.0f);
            }
        }
        
        return stack;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Comenzar a usar la runa
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW; // Animación similar a usar un arco
    }
    
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 40; // Tiempo para activar (2 segundos)
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Runa de Curación»").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Símbolos antiguos de purificación").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6§oRuna Sagrada§r").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("§7• Purificación completa§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Un solo uso§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Solo en etapas tempranas§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La luz que disipa la oscuridad»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Siempre tiene brillo mágico
    }
}