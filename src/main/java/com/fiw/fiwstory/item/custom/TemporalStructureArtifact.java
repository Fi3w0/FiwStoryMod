package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TemporalStructureArtifact extends BaseArtifactItem {

    private static final int BURST_DURATION   = 160;  // 8 segundos (Haste + Speed)
    private static final int SLOW_DURATION    = 60;   // 3 segundos (Slowness post-burst)
    private static final int ABILITY_COOLDOWN = 1200; // 60 segundos

    private static final Map<UUID, Long> slownessTrigger = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldownExpiry  = new ConcurrentHashMap<>();

    public TemporalStructureArtifact(Settings settings) {
        super(ArtifactType.SPECIAL, ArtifactRarity.LEGENDARY, 2, 0, settings.maxCount(1).fireproof());
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
    public String getArtifactDisplayName() { return "Estructura Atemporal"; }

    @Override
    public String getArtifactDescription() { return "Un artefacto que no pertenece a este mundo, paradoja temporal"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "El tiempo no le afecta",
            "Sientes que este objeto trasciende la realidad"
        );
    }

    @Override
    public String getArtifactQuote() { return "La realidad es solo una ilusión del tiempo"; }

    // ========== ACTIVA: Colapso Temporal (CD: 60s) ==========
    // Override use() directly to manage its own worldTime-based cooldown
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack);

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        Long cdExp = cooldownExpiry.get(uuid);
        if (cdExp != null && worldTime < cdExp) {
            long remaining = (cdExp - worldTime) / 20;
            player.sendMessage(Text.literal("§b⏱ Colapso Temporal — Cooldown: " + remaining + "s"), true);
            return TypedActionResult.fail(stack);
        }

        cooldownExpiry.put(uuid, worldTime + ABILITY_COOLDOWN);
        slownessTrigger.put(uuid, worldTime + BURST_DURATION);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, BURST_DURATION, 2, false, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, BURST_DURATION, 1, false, true, true));

        if (world instanceof ServerWorld sw) {
            for (int i = 0; i < 30; i++) {
                double a = (i / 30.0) * Math.PI * 2;
                double h = (i % 2) * 0.8;
                sw.spawnParticles(ParticleTypes.PORTAL,
                    player.getX() + Math.cos(a) * 1.2,
                    player.getY() + h,
                    player.getZ() + Math.sin(a) * 1.2,
                    1, 0.05, 0.1, 0.05, 0.05);
            }
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                15, 0.3, 0.5, 0.3, 0.08);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8f, 0.5f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5f, 1.4f);

        player.sendMessage(Text.literal("§b⏱ Colapso Temporal §7activado — 8s").formatted(Formatting.AQUA), true);
        player.getItemCooldownManager().set(this, ABILITY_COOLDOWN);
        return TypedActionResult.success(stack);
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // No-op: use() is overridden directly
    }

    // ========== PASIVA: Paradoja Temporal (slowness diferida) ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        UUID uuid = player.getUuid();
        Long slowTime = slownessTrigger.get(uuid);
        if (slowTime != null && world.getTime() >= slowTime) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, SLOW_DURATION, 1, false, true, true));
            player.sendMessage(Text.literal("§b⏱ Paradoja temporal §7— Slowness II"), true);
            slownessTrigger.remove(uuid);
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Temporal structure movement speed", 0.10, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }
}
