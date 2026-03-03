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
 * Amuleto de Cobre Corroído - Recuerdo del Pasado
 *
 * Artefacto de un dios del trueno de otro universo.
 * La corrosión no ha apagado su poder... solo lo ha vuelto inestable.
 *
 * Atributos pasivos (trinket slot):
 * - +5% Velocidad de Movimiento
 * - +1 Daño de Ataque
 * - +10% Resistencia a rayos (knockback resistance como proxy)
 * - -5% Resistencia general (armor negativa)
 *
 * Habilidad pasiva: Descarga eléctrica en cadena cada 5s
 * si hay 3+ mobs hostiles en radio 12.
 */
public class CorrodedCopperRingArtifact extends BaseArtifactItem {

    // Configuración de la habilidad
    private static final int DISCHARGE_INTERVAL_TICKS = 100; // 5 segundos
    private static final int HOSTILE_CHECK_RADIUS = 12;
    private static final int MIN_HOSTILES_REQUIRED = 3;
    private static final int MAX_CHAIN_TARGETS = 3;
    private static final float BASE_CHAIN_DAMAGE = 4.0f;
    private static final float CHAIN_DAMAGE_REDUCTION = 0.20f; // 20% reducción por salto
    private static final float STUN_CHANCE = 0.10f; // 10% chance de aturdimiento
    private static final float FAIL_CHANCE = 0.15f; // 15% chance de fallo

    // Contador interno por jugador (NBT tag)
    private static final String TICK_COUNTER_TAG = "fiwstory:corroded_ring_ticks";

    public CorrodedCopperRingArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.RARE, 1, 0, settings);
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
        return "Amuleto de Cobre Corroído";
    }

    @Override
    public String getArtifactDescription() {
        return "Recuerdo del Pasado";
    }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Energía de un dios del trueno desconocido",
            "La corrosión ha vuelto su poder inestable",
            "Descarga eléctrica automática en combate",
            "+5% Velocidad | +1 Daño | -5% Defensa"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "La corrosión no ha apagado su poder... solo lo ha vuelto inestable";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            FiwUtils.sendInfoMessage(player, "El amuleto vibra con energía inestable...");
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

        // Contador interno optimizado
        int tickCounter = stack.getOrCreateNbt().getInt(TICK_COUNTER_TAG);
        tickCounter++;
        stack.getOrCreateNbt().putInt(TICK_COUNTER_TAG, tickCounter);

        if (tickCounter % DISCHARGE_INTERVAL_TICKS != 0) {
            return;
        }

        // 15% probabilidad de fallo
        if (FiwUtils.randomChance(FAIL_CHANCE)) {
            if (world instanceof ServerWorld serverWorld) {
                Vec3d pos = player.getPos();
                serverWorld.spawnParticles(
                    new DustParticleEffect(new Vector3f(0.4f, 0.7f, 0.3f), 0.5f),
                    pos.x, pos.y + 1.0, pos.z,
                    3, 0.3, 0.3, 0.3, 0.0
                );
            }
            return;
        }

        // Buscar mobs hostiles en radio
        Box searchBox = player.getBoundingBox().expand(HOSTILE_CHECK_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class, searchBox, e -> e.isAlive() && e.squaredDistanceTo(player) <= HOSTILE_CHECK_RADIUS * HOSTILE_CHECK_RADIUS
        );

        if (hostiles.size() < MIN_HOSTILES_REQUIRED) {
            return;
        }

        executeChainDischarge(player, world, hostiles);
    }

    private void executeChainDischarge(PlayerEntity player, World world, List<HostileEntity> hostiles) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        hostiles.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

        List<HostileEntity> targets = hostiles.stream()
            .limit(MAX_CHAIN_TARGETS)
            .collect(Collectors.toList());

        Vec3d previousPos = player.getPos().add(0, 1.0, 0);
        float currentDamage = BASE_CHAIN_DAMAGE;

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
                8, 0.3, 0.3, 0.3, 0.05
            );

            currentDamage *= (1.0f - CHAIN_DAMAGE_REDUCTION);
            previousPos = targetPos;
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS,
            0.6f, 0.5f + world.getRandom().nextFloat() * 0.3f);

        serverWorld.spawnParticles(
            new DustParticleEffect(new Vector3f(0.3f, 0.8f, 0.4f), 0.8f),
            player.getX(), player.getY() + 1.0, player.getZ(),
            10, 0.5, 0.5, 0.5, 0.0
        );
    }

    private void spawnChainLightningParticles(ServerWorld world, Vec3d start, Vec3d end) {
        int particleCount = 8;
        Vec3d direction = end.subtract(start);
        Random random = new Random();

        for (int i = 0; i <= particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3d point = start.add(direction.multiply(progress));

            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;

            world.spawnParticles(
                new DustParticleEffect(new Vector3f(0.4f, 0.85f, 0.35f), 0.6f),
                point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                1, 0, 0, 0, 0.0
            );

            if (i % 2 == 0) {
                world.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    point.x + offsetX, point.y + offsetY, point.z + offsetZ,
                    2, 0.1, 0.1, 0.1, 0.02
                );
            }
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Corroded Ring speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Corroded Ring attack", 1.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Corroded Ring lightning resist", 0.10, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Corroded Ring instability", -1.0, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
