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

public class CorruptedCrystal extends Item {
    
    public CorruptedCrystal(Settings settings) {
        super(settings.maxCount(16)); // Stackable hasta 16
    }
    
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player && !world.isClient()) {
            // Aplicar efecto de corrupción nivel 1 permanente
            player.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.CORRUPTION,
                Integer.MAX_VALUE, // Duración infinita
                0, // Nivel 1 (amplifier 0)
                false,
                true,
                true
            ));
            
            // Sonido de corrupción
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WITHER_AMBIENT,
                net.minecraft.sound.SoundCategory.PLAYERS,
                0.5f, 1.0f);
            
            // Consumir el item
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        
        return stack;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // Comenzar a comer el cristal
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }
    
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32; // Tiempo para comer (1.6 segundos)
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Cristal Corrupto»").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Energía mágica distorsionada").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oConsumible Corrupto§r").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("§7• Contiene corrupción concentrada§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Efecto al consumir§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Stackable hasta 16§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«Corrupción que consume al portador»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Siempre tiene brillo para indicar su naturaleza mágica
    }
}