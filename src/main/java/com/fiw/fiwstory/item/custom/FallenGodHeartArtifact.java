package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.data.HeartData;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.TrinketHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FallenGodHeartArtifact extends BaseArtifactItem {

    private static final UUID MAX_HEALTH_MODIFIER_ID = UUID.fromString("A3B2C1D0-E4F5-4678-9A0B-1C2D3E4F5A6B");

    private static final int DRAIN_INTERVAL    = 20 * 20; // 400 ticks = 20 segundos
    private static final int RECOVERY_INTERVAL = 60 * 20; // 1200 ticks = 60 segundos
    private static final float MIN_HEALTH = 6.0f; // 3 corazones

    // Per-player timers — fix global sync bug
    private static final Map<UUID, Long> nextDrainTime    = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> nextRecoveryTime = new ConcurrentHashMap<>();

    // Per-player last applied modifier — skip expensive remove+add when unchanged
    private static final Map<UUID, Float> lastAppliedHealthDiff = new ConcurrentHashMap<>();

    public FallenGodHeartArtifact(Settings settings) {
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
    public String getArtifactDisplayName() { return "Corazón de Dios Caído"; }

    @Override
    public String getArtifactDescription() { return "Uno de los artefactos legendarios de Dios Faraón"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Parece que sigue vivo, que asco",
            "Sientes cada latido del corazón",
            "§6Resistencia I + Fuerza I",
            "§cCada 20s: -1 corazón de vida máxima",
            "§cMínimo: 3 corazones",
            "§aRecuperación: 1 corazón/min sin usar"
        );
    }

    @Override
    public String getArtifactQuote() { return "El precio del poder divino es un pedazo de tu propia esencia"; }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            player.sendMessage(Text.literal("§c❤ Sientes el latido del corazón divino...").formatted(Formatting.DARK_RED), false);
        }
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // Logic is handled by ModEvents.registerServerEvents() which calls handleHeartEffects
        // for all players every 5 ticks regardless of equip slot.
    }

    // ========== LÓGICA DEL CORAZÓN ==========
    // Llamado desde ModEvents.registerServerEvents() cada 5 ticks para todos los jugadores.
    // Mantener public static para compatibilidad con ModEvents.
    public static void handleHeartEffects(PlayerEntity player, World world) {
        if (world.isClient()) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        boolean hasHeart = TrinketHelper.hasInOffhandOrTrinket(player, FallenGodHeartArtifact.class);
        HeartData.PlayerHeartData heartData = HeartData.get(player);

        if (hasHeart) {
            if (heartData.getCurrentMaxHealth() > MIN_HEALTH) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 100, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH, 100, 0, false, false, true));
            }

            // Per-player drain timer
            Long nextDrain = nextDrainTime.get(uuid);
            if (nextDrain == null) {
                nextDrainTime.put(uuid, worldTime + DRAIN_INTERVAL);
            } else if (worldTime >= nextDrain) {
                nextDrainTime.put(uuid, worldTime + DRAIN_INTERVAL);
                drainMaxHealth(player, heartData);
            }

            heartData.setUsingHeart(true);
        } else {
            heartData.setUsingHeart(false);

            // Per-player recovery timer
            if (heartData.getCurrentMaxHealth() < heartData.getOriginalMaxHealth()) {
                Long nextRecovery = nextRecoveryTime.get(uuid);
                if (nextRecovery == null) {
                    nextRecoveryTime.put(uuid, worldTime + RECOVERY_INTERVAL);
                } else if (worldTime >= nextRecovery) {
                    nextRecoveryTime.put(uuid, worldTime + RECOVERY_INTERVAL);
                    recoverMaxHealth(player, heartData);
                }
            }
        }

        applyMaxHealth(player, heartData);
        heartData.markDirty();
    }

    private static void drainMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float current = heartData.getCurrentMaxHealth();
        if (current > MIN_HEALTH) {
            heartData.setCurrentMaxHealth(current - 2.0f);
            player.sendMessage(Text.literal("§4❤ El corazón divino consume parte de tu esencia vital...").formatted(Formatting.DARK_RED), false);
        } else if (current == MIN_HEALTH) {
            player.sendMessage(Text.literal("§4⚠ ¡Has alcanzado el límite mínimo de vida! El corazón ya no otorga buffs.").formatted(Formatting.DARK_RED), false);
        }
    }

    private static void recoverMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float current = heartData.getCurrentMaxHealth();
        float original = heartData.getOriginalMaxHealth();
        if (current < original) {
            float newHealth = Math.min(current + 2.0f, original);
            heartData.setCurrentMaxHealth(newHealth);
            if (newHealth < original) {
                player.sendMessage(Text.literal("§a❤ Tu esencia vital se recupera lentamente...").formatted(Formatting.GREEN), false);
            } else {
                player.sendMessage(Text.literal("§a✨ ¡Has recuperado toda tu esencia vital!").formatted(Formatting.GREEN), false);
            }
        }
    }

    private static void applyMaxHealth(PlayerEntity player, HeartData.PlayerHeartData heartData) {
        float current = heartData.getCurrentMaxHealth();
        float original = heartData.getOriginalMaxHealth();
        float healthDiff = current - original;

        // Skip if value hasn't changed — avoids removeModifier+addPersistentModifier every tick
        Float lastDiff = lastAppliedHealthDiff.get(player.getUuid());
        if (lastDiff != null && Math.abs(lastDiff - healthDiff) < 0.01f) return;

        var attributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attributeInstance == null) return;

        var oldModifier = attributeInstance.getModifier(MAX_HEALTH_MODIFIER_ID);
        if (oldModifier != null) {
            attributeInstance.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }

        if (current != original) {
            attributeInstance.addPersistentModifier(new EntityAttributeModifier(
                MAX_HEALTH_MODIFIER_ID, "Fallen God Heart max health", healthDiff,
                EntityAttributeModifier.Operation.ADDITION));
            if (player.getHealth() > current) {
                player.setHealth(current);
            }
        }

        lastAppliedHealthDiff.put(player.getUuid(), healthDiff);
    }

    public static void resetMaxHealth(PlayerEntity player) {
        HeartData.PlayerHeartData heartData = HeartData.get(player);
        heartData.resetToOriginal();
        lastAppliedHealthDiff.remove(player.getUuid());
        applyMaxHealth(player, heartData);
        heartData.markDirty();
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return HashMultimap.create(); // No passive stat bonuses — only the dynamic HP drain system
    }
}
