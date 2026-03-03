package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.FiwUtils;
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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Amuleto de Cobre Despertado - Recuerdo del Pasado
 *
 * Versión mejorada del Amuleto de Cobre Corroído.
 * Se craftea con Corroído + Lingote de Netherita.
 *
 * Atributos pasivos (trinket slot):
 * - +10% Velocidad de Movimiento
 * - +2 Daño de Ataque
 * - +10% Velocidad de Ataque
 * - Inmunidad total a rayos naturales
 * - +15% Resistencia al retroceso
 *
 * Habilidad pasiva mejorada: Descarga eléctrica cada 5s (3s en tormenta)
 * Bonus en tormenta: +20% daño, descarga adicional por rayos cercanos
 */
public class PlainCopperRingArtifact extends BaseArtifactItem {

    // Configuración de la habilidad
    private static final int DISCHARGE_INTERVAL_NORMAL = 100; // 5 segundos
    private static final int DISCHARGE_INTERVAL_STORM = 60;   // 3 segundos
    private static final int HOSTILE_CHECK_RADIUS = 16;
    private static final int MIN_HOSTILES_REQUIRED = 3;
    private static final int MAX_CHAIN_TARGETS = 3;
    private static final float BASE_CHAIN_DAMAGE = 7.0f;
    private static final float STORM_DAMAGE_BONUS = 0.20f; // +20% daño en tormenta
    private static final float CHAIN_DAMAGE_REDUCTION = 0.10f; // 10% reducción por salto
    private static final float STUN_CHANCE = 0.25f; // 25% chance de aturdimiento
    private static final int LIGHTNING_DETECT_RADIUS = 12;
    private static final float LIGHTNING_HEAL_AMOUNT = 2.0f; // 1 corazón

    // Contadores internos (NBT tags)
    private static final String TICK_COUNTER_TAG = "fiwstory:plain_ring_ticks";
    private static final String AURA_COUNTER_TAG = "fiwstory:plain_ring_aura";

    public PlainCopperRingArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 1, 0, settings);
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
    public String getArtifactDisplayName() {
        return "Amuleto de Cobre Despertado";
    }

    @Override
    public String getArtifactDescription() {
        return "Recuerdo del Pasado";
    }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "La energía de un dios del trueno restaurada",
            "Su poder responde al caos de la batalla",
            "Descarga eléctrica potente en combate",
            "+10% Velocidad | +2 Daño | +10% Vel. Ataque",
            "Inmunidad total a rayos naturales",
            "Potenciado durante tormentas eléctricas"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Su poder responde al caos de la batalla";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            FiwUtils.sendInfoMessage(player, "El amuleto pulsa con energía restaurada...");
        }
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        // Funciona en mainhand, offhand o trinket slot
        boolean isActive = player.getMainHandStack() == stack
            || player.getOffHandStack() == stack
            || TrinketHelper.hasTrinketEquipped(player, stack.getItem());
        if (!isActive) {
            return;
        }

        // Contador interno
        int tickCounter = stack.getOrCreateNbt().getInt(TICK_COUNTER_TAG);
        tickCounter++;
        stack.getOrCreateNbt().putInt(TICK_COUNTER_TAG, tickCounter);

        // Aura visual sutil cada 40 ticks (2 segundos)
        int auraCounter = stack.getOrCreateNbt().getInt(AURA_COUNTER_TAG);
        auraCounter++;
        stack.getOrCreateNbt().putInt(AURA_COUNTER_TAG, auraCounter);
        if (auraCounter % 40 == 0 && world instanceof ServerWorld serverWorld) {
            spawnAuraParticles(serverWorld, player);
        }

        // Intervalo de descarga: 5s normal, 3s en tormenta
        boolean isStorm = world.isThundering();
        int interval = isStorm ? DISCHARGE_INTERVAL_STORM : DISCHARGE_INTERVAL_NORMAL;

        if (tickCounter % interval != 0) {
            return;
        }

        // Buscar mobs hostiles en radio
        Box searchBox = player.getBoundingBox().expand(HOSTILE_CHECK_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class, searchBox,
            e -> e.isAlive() && e.squaredDistanceTo(player) <= HOSTILE_CHECK_RADIUS * HOSTILE_CHECK_RADIUS
        );

        if (hostiles.size() < MIN_HOSTILES_REQUIRED) {
            return;
        }

        executeChainDischarge(player, world, hostiles, isStorm);
    }

    private void executeChainDischarge(PlayerEntity player, World world, List<HostileEntity> hostiles, boolean isStorm) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        hostiles.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

        List<HostileEntity> targets = hostiles.stream()
            .limit(MAX_CHAIN_TARGETS)
            .collect(Collectors.toList());

        Vec3d previousPos = player.getPos().add(0, 1.0, 0);
        float baseDamage = BASE_CHAIN_DAMAGE;
        if (isStorm) {
            baseDamage *= (1.0f + STORM_DAMAGE_BONUS);
        }
        float currentDamage = baseDamage;

        for (int i = 0; i < targets.size(); i++) {
            HostileEntity target = targets.get(i);
            Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);

            spawnChainLightningParticles(serverWorld, previousPos, targetPos);

            target.damage(player.getDamageSources().magic(), currentDamage);

            if (FiwUtils.randomChance(STUN_CHANCE)) {
                target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 10, 4, false, false, false
                ));
            }

            serverWorld.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                targetPos.x, targetPos.y, targetPos.z,
                12, 0.4, 0.4, 0.4, 0.08
            );

            currentDamage *= (1.0f - CHAIN_DAMAGE_REDUCTION);
            previousPos = targetPos;
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
            0.4f, 1.5f + world.getRandom().nextFloat() * 0.5f);

        serverWorld.spawnParticles(
            new DustParticleEffect(new Vector3f(0.6f, 0.8f, 1.0f), 1.0f),
            player.getX(), player.getY() + 1.0, player.getZ(),
            15, 0.6, 0.6, 0.6, 0.0
        );
    }

    private void spawnChainLightningParticles(ServerWorld world, Vec3d start, Vec3d end) {
        int particleCount = 12;
        Vec3d direction = end.subtract(start);
        Random random = new Random();

        for (int i = 0; i <= particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3d point = start.add(direction.multiply(progress));

            double offsetX = (random.nextDouble() - 0.5) * 0.25;
            double offsetY = (random.nextDouble() - 0.5) * 0.25;
            double offsetZ = (random.nextDouble() - 0.5) * 0.25;

            world.spawnParticles(
                new DustParticleEffect(new Vector3f(0.7f, 0.85f, 1.0f), 0.8f),
                point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                1, 0, 0, 0, 0.0
            );

            if (i % 2 == 0) {
                world.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                    3, 0.1, 0.1, 0.1, 0.03
                );
            }

            if (i % 3 == 0) {
                world.spawnParticles(
                    ParticleTypes.END_ROD,
                    point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                    1, 0.05, 0.05, 0.05, 0.0
                );
            }
        }
    }

    private void spawnAuraParticles(ServerWorld world, PlayerEntity player) {
        Vec3d pos = player.getPos();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.6 + random.nextDouble() * 0.3;
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + 0.5 + random.nextDouble() * 1.5;
            double z = pos.z + Math.sin(angle) * radius;

            world.spawnParticles(
                new DustParticleEffect(new Vector3f(0.5f, 0.7f, 1.0f), 0.4f),
                x, y, z,
                1, 0, 0.02, 0, 0.0
            );
        }
    }

    /**
     * Maneja la descarga adicional cuando un rayo cae cerca.
     * Llamado desde el evento de rayo en ModEvents.
     * Usa TrinketHelper para detectar en cualquier slot.
     */
    public static void onNearbyLightningStrike(PlayerEntity player, World world, Vec3d lightningPos) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Verificar si tiene el amuleto equipado (manos o trinket)
        boolean hasRing = TrinketHelper.hasArtifactOfType(player, PlainCopperRingArtifact.class);
        if (!hasRing) {
            return;
        }

        double distance = player.getPos().distanceTo(lightningPos);
        if (distance > LIGHTNING_DETECT_RADIUS) {
            return;
        }

        // Descarga adicional
        Box searchBox = player.getBoundingBox().expand(HOSTILE_CHECK_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class, searchBox,
            e -> e.isAlive()
        );

        if (!hostiles.isEmpty()) {
            hostiles.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));
            int targets = Math.min(MAX_CHAIN_TARGETS, hostiles.size());

            for (int i = 0; i < targets; i++) {
                HostileEntity target = hostiles.get(i);
                target.damage(player.getDamageSources().magic(), BASE_CHAIN_DAMAGE * 0.5f);

                serverWorld.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05
                );
            }
        }

        player.heal(LIGHTNING_HEAL_AMOUNT);

        serverWorld.spawnParticles(
            ParticleTypes.END_ROD,
            player.getX(), player.getY() + 1.0, player.getZ(),
            20, 0.5, 1.0, 0.5, 0.1
        );

        FiwUtils.sendInfoMessage(player, "El amuleto absorbe la energía del rayo...");
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Awakened Ring speed", 0.10, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Awakened Ring attack", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED,
            new EntityAttributeModifier(uuid, "Awakened Ring attack speed", 0.10, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Awakened Ring knockback resist", 0.15, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
