package com.fiw.fiwstory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DivineBloodItem extends Item {
    
    public DivineBloodItem(Settings settings) {
        super(settings.maxCount(42).food(
            new net.minecraft.item.FoodComponent.Builder()
                .hunger(20) // Máxima comida (10 corazones)
                .saturationModifier(20.0f) // Saturación máxima
                .alwaysEdible() // Siempre se puede comer
                .build()
        ));
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK; // Acción de beber
    }
    
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32; // Tiempo para consumir (1.6 segundos)
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.canConsume(true)) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        } else {
            return TypedActionResult.fail(itemStack);
        }
    }
    
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            // Aplicar efectos potentes por 15 minutos (900 segundos = 18000 ticks)
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 18000, 4)); // Regeneración nivel 5 (nivel 4 = V)
            
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 18000, 1)); // Velocidad nivel 2 (nivel 1 = II)
            
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 18000, 1)); // Resistencia nivel 2 (nivel 1 = II)
            
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 18000, 0)); // Protección contra fuego
            
            // Restaurar comida y saturación al máximo
            player.getHungerManager().add(20, 20.0f);
            
            // Mensaje personalizado solo para el jugador
            player.sendMessage(Text.literal("§4§l¡Sientes como la sangre divina fluye en tu cuerpo!").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
            player.sendMessage(Text.literal("§cTu alma recuerda algo que tu mente no...").formatted(Formatting.RED), false);
            
            // Consumir el item (un solo uso)
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Sangre Divina»").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Porque esta enbotellada?").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§4§oAl menos no esta llena de corrupcion§r").formatted(Formatting.DARK_RED));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8§oItem de un solo uso - Apilable hasta 42§r").formatted(Formatting.DARK_GRAY));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Brilla como item especial
    }
}