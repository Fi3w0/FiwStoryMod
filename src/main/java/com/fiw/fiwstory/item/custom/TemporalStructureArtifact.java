package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TemporalStructureArtifact extends Item {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");

    public TemporalStructureArtifact(Settings settings) {
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
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            // MOVEMENT_SPEED: +0.35 MULTIPLY_BASE
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "Temporal structure movement speed", 0.35,
                    EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }
        
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Estructura Atemporal»").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Un artefacto que no pertenece a este mundo, paradoja temporal").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§b§oHecho por la realidad misma§r").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("§7• El tiempo no le afecta§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Sientes que este objeto trasciende la realidad§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«La realidad es solo una ilusión del tiempo»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            // Verificar si está en mano principal o secundaria
            boolean inMainHand = player.getMainHandStack() == stack;
            boolean inOffHand = player.getOffHandStack() == stack;
            
            if (inMainHand || inOffHand) {
                // Los atributos se aplican automáticamente a través de getAttributeModifiers
                // No necesitamos hacer nada adicional aquí
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}