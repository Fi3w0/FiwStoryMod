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

public class PharaohScarabArtifact extends Item {
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4F5C-BE3C-9C1A8E5B5F5D");
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");

    public PharaohScarabArtifact(Settings settings) {
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
            // ATTACK_DAMAGE: +0.1
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Scarab attack damage", 0.1,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // ATTACK_SPEED: +0.1
            modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Scarab attack speed", 0.1,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // MAX_HEALTH: +2
            modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
                new EntityAttributeModifier(MAX_HEALTH_MODIFIER_ID, "Scarab max health", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // MOVEMENT_SPEED: +0.15
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "Scarab movement speed", 0.15,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Escarabajo del Faraón»").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Uno de los artefactos legendarios del Dios Faraón").formatted(Formatting.YELLOW, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6§oEste es un escarabajo§r").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("§7• Artefacto de poder ancestral§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Bendiciones del desierto eterno§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«Protección del desierto dorado»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
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