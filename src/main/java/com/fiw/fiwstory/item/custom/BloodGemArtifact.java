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
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BloodGemArtifact extends Item implements Trinket {

    private static final UUID OFFHAND_UUID = UUID.fromString("B2C3D4E5-F6A7-4890-BC12-DE34FA567890");

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
        if (!(entity instanceof PlayerEntity player)) return;
        TrinketHelper.handleCreativeDuplication(player, stack, slot);
    }

    // ========== EVENTO: Robo de Vida (lifesteal 15%) ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!TrinketHelper.hasArtifactOfType(attacker, BloodGemArtifact.class)) return true;

            float heal = amount * 0.15f;
            attacker.heal(heal);

            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HEART,
                    attacker.getX(), attacker.getY() + 1.2, attacker.getZ(),
                    2, 0.3, 0.2, 0.3, 0.01);
            }
            return true;
        });
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return buildModifiers(uuid);
    }

    private Multimap<EntityAttribute, EntityAttributeModifier> buildModifiers(UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
            new EntityAttributeModifier(uuid, "Blood gem max health", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Blood gem armor", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Blood gem max absorption", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Blood gem knockback resistance", 2.0, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
