package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.lib.TrinketHelper;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhilosopherStoneArtifact extends Item implements Trinket {
    public PhilosopherStoneArtifact(Settings settings) {
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
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (TrinketHelper.handleCreativeDuplication(player, stack, slot)) return;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Piedra Filosófica»").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Artefacto de Dios Faraón").formatted(Formatting.GOLD, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oDicen que puede convertir materia misma§r").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("§7• No comprendes como funciona este artefacto§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Parece inerte, pero sientes su poder latente§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Versión básica - potencial oculto§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«La verdadera alquimia no es convertir plomo en oro, sino comprender la esencia de la materia»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}