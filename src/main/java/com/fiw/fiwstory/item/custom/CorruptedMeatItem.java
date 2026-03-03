package com.fiw.fiwstory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CorruptedMeatItem extends Item {

    private static final FoodComponent CORRUPTED_MEAT_FOOD = new FoodComponent.Builder()
        .hunger(4)
        .saturationModifier(0.3f)
        .meat()
        .alwaysEdible()
        .build();

    public CorruptedMeatItem(Settings settings) {
        super(settings.food(CORRUPTED_MEAT_FOOD));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity) {
            // Resistencia II por 30 segundos (600 ticks)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 1, false, true, true));
            // Hambre por 30 segundos (600 ticks)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0, false, true, true));
            // Nausea por 10 segundos (200 ticks)
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0, false, true, true));
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Carne Corrupta»").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Carne podrida con magia corrupta").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§2§oQue asco es esto§r").formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.literal("§7• Resistencia II (30s)§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Hambre (30s)§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Nausea (10s)§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La corrupción fluye a través de la carne»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
