package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TemporalStructureArtifact extends Item implements Trinket {

    private static final UUID OFFHAND_UUID = UUID.fromString("D4E5F6A7-B8C9-4012-DE34-FA56BC789012");

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

    // ========== VANILLA OFFHAND ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND) {
            return buildModifiers(OFFHAND_UUID);
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return buildModifiers(uuid);
    }

    private Multimap<EntityAttribute, EntityAttributeModifier> buildModifiers(UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Temporal structure movement speed", 0.35, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        return modifiers;
    }
}
