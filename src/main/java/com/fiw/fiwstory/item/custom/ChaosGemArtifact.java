package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.lib.TrinketHelper;
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

public class ChaosGemArtifact extends Item implements Trinket {

    private static final UUID OFFHAND_UUID = UUID.fromString("A1B2C3D4-E5F6-4789-AB01-CD23EF456789");

    public ChaosGemArtifact(Settings settings) {
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
        tooltip.add(Text.literal("«Gema del Lord de Caos»").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Artefacto del mundo pasado - Paradoja temporal").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oUn recuerdo del pasado§r").formatted(Formatting.DARK_PURPLE));
        tooltip.add(Text.literal("§7• Sensación de no pertenecer a este mundo§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Energía caótica contenida§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«El caos siempre encuentra su camino»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
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
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            if (TrinketHelper.handleCreativeDuplication(player, stack, slot)) return;
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return buildModifiers(uuid);
    }

    private Multimap<EntityAttribute, EntityAttributeModifier> buildModifiers(UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Chaos gem movement speed", 0.1, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Chaos gem knockback resistance", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Chaos gem attack damage", 0.25, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
