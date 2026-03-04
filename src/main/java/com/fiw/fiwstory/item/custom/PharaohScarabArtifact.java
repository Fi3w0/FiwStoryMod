package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PharaohScarabArtifact extends BaseArtifactItem {

    private static final int HEAL_INTERVAL = 60; // 3 segundos
    private static final Map<UUID, Long> nextHealTime = new ConcurrentHashMap<>();

    public PharaohScarabArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.LEGENDARY, 2, 0, settings.maxCount(1).fireproof());
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
    public String getArtifactDisplayName() { return "Escarabajo del Faraón"; }

    @Override
    public String getArtifactDescription() { return "Uno de los artefactos legendarios del Dios Faraón"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Artefacto de poder ancestral",
            "Bendiciones del desierto eterno"
        );
    }

    @Override
    public String getArtifactQuote() { return "Protección del desierto dorado"; }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo
    }

    // ========== PASIVA: Bendición Solar ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        // Bendición Solar: regenerar 0.5 corazón cada 3s durante el día (per-player timer)
        if (!world.isDay()) return;
        UUID uuid = player.getUuid();
        long worldTime = world.getTime();
        Long nextTime = nextHealTime.get(uuid);
        if (nextTime == null) {
            nextHealTime.put(uuid, worldTime + HEAL_INTERVAL);
            return;
        }
        if (worldTime < nextTime) return;

        nextHealTime.put(uuid, worldTime + HEAL_INTERVAL);
        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0f);
            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.GLOW,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    4, 0.3, 0.4, 0.3, 0.02);
            }
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Scarab attack damage", 1.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED,
            new EntityAttributeModifier(uuid, "Scarab attack speed", 0.5, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
            new EntityAttributeModifier(uuid, "Scarab max health", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Scarab movement speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }
}
