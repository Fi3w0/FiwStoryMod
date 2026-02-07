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

public class PharaohRingArtifact extends Item {
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    public PharaohRingArtifact(Settings settings) {
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
            // ARMOR: +4
            modifiers.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_MODIFIER_ID, "Pharaoh ring armor", 4.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // ARMOR_TOUGHNESS: +2
            modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                new EntityAttributeModifier(ARMOR_TOUGHNESS_MODIFIER_ID, "Pharaoh ring armor toughness", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // LUCK: +10
            modifiers.put(EntityAttributes.GENERIC_LUCK,
                new EntityAttributeModifier(LUCK_MODIFIER_ID, "Pharaoh ring luck", 10.0,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Anillo del Faraón»").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Uno de los artefactos legendarios del Dios Faraón").formatted(Formatting.YELLOW, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6§oAnillo de poder divino§r").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("§7• Sientes la protección del desierto dorado§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• La suerte del faraón te acompaña§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«El poder del faraón reside en sus anillos»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
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