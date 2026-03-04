package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhilosopherStoneArtifact extends BaseArtifactItem {

    private static final int XP_INTERVAL = 600; // 30 segundos
    private static final Map<UUID, Long> nextXpTime = new ConcurrentHashMap<>();

    public PhilosopherStoneArtifact(Settings settings) {
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
    public String getArtifactDisplayName() { return "Piedra Filosófica"; }

    @Override
    public String getArtifactDescription() { return "Artefacto de Dios Faraón"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "No comprendes como funciona este artefacto",
            "Parece inerte, pero sientes su poder latente",
            "Versión básica - potencial oculto"
        );
    }

    @Override
    public String getArtifactQuote() { return "La verdadera alquimia no es convertir plomo en oro, sino comprender la esencia de la materia"; }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo
    }

    // ========== PASIVA: Transmutación ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        // Transmutación: Luck I permanente visible
        if (!player.hasStatusEffect(StatusEffects.LUCK)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 100, 0, false, false, true));
        }

        // Transmutación: generar 1-2 XP cada 30 segundos (per-player, fix global sync)
        UUID uuid = player.getUuid();
        long worldTime = world.getTime();
        Long nextTime = nextXpTime.get(uuid);
        if (nextTime == null) {
            nextXpTime.put(uuid, worldTime + XP_INTERVAL);
            return;
        }
        if (worldTime < nextTime) return;

        nextXpTime.put(uuid, worldTime + XP_INTERVAL);

        if (world instanceof ServerWorld sw) {
            int xp = 1 + sw.getRandom().nextInt(2); // 1-2 XP
            ExperienceOrbEntity.spawn(sw, new Vec3d(player.getX(), player.getY() + 0.5, player.getZ()), xp);
            sw.spawnParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 0.5, player.getZ(),
                8, 0.3, 0.4, 0.3, 0.05);
        }
    }

    // No stat attributes — Luck and XP via onArtifactTick only
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return HashMultimap.create();
    }
}
