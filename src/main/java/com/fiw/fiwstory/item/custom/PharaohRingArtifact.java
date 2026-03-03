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

public class PharaohRingArtifact extends Item implements Trinket {

    private static final UUID OFFHAND_UUID = UUID.fromString("C3D4E5F6-A7B8-4901-CD23-EF45AB678901");

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
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Pharaoh ring armor", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(uuid, "Pharaoh ring armor toughness", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_LUCK,
            new EntityAttributeModifier(uuid, "Pharaoh ring luck", 10.0, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
