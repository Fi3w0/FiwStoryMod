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

public class BloodGemArtifact extends Item {
    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID MAX_ABSORPTION_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4F5C-BE3C-9C1A8E5B5F5D");

    public BloodGemArtifact(Settings settings) {
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
            // MAX_HEALTH: +4
            modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
                new EntityAttributeModifier(MAX_HEALTH_MODIFIER_ID, "Blood gem max health", 4.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // ARMOR: +2
            modifiers.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_MODIFIER_ID, "Blood gem armor", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // MAX_ABSORPTION: +2 (usando ATTACK_DAMAGE como placeholder, ya que MAX_ABSORPTION no existe en 1.20.1)
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(MAX_ABSORPTION_MODIFIER_ID, "Blood gem max absorption", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // KNOCKBACK_RESISTANCE: +2 (MULTIPLY_BASE)
            modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                new EntityAttributeModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID, "Blood gem knockback resistance", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Gema de Sangre Divina»").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Uno de los artefactos legendarios del Dios Faraón").formatted(Formatting.RED, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§4§oHecha de sangre divina§r").formatted(Formatting.DARK_RED));
        tooltip.add(Text.literal("§7• Sientes el poder divino que no te pertenece§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Energía vital concentrada§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«La sangre del faraón fluye eternamente»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
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