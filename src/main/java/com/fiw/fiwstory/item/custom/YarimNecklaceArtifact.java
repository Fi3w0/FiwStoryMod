package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.TrinketHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sello del Cazador — Artefacto LEGENDARIO
 * «Artefacto de Yarim — God Hunter»
 *
 * Resuena cerca de seres poderosos.
 * Yarim lo usaba para detectar esencias del mundo anterior.
 *
 * Pasiva — Resonancia de Poder:
 *   Detecta entidades poderosas (jugadores o mobs con >40 HP) en 16 bloques.
 *   Mientras haya una cerca: Speed I pasivo en el jugador.
 *   Al entrar una en rango: sonido sutil + partículas doradas pulsantes sobre el objetivo.
 *   Bonus: +2 daño extra contra esas entidades (via damage event).
 *
 * Stats: +2 daño | +5% velocidad | +2 armor toughness
 */
public class YarimNecklaceArtifact extends BaseArtifactItem {

    private static final int   SCAN_INTERVAL  = 40;   // 2 segundos
    private static final int   PARTICLE_INTERVAL = 20; // 1 segundo
    private static final float RESONANCE_RANGE  = 16.0f;
    private static final float POWERFUL_HP      = 40.0f;
    private static final float BONUS_DAMAGE     = 2.0f;

    private static final String TICK_TAG = "fiwstory:yarim_neck_ticks";

    // playerUUID → set de entity UUIDs en rango en el último scan
    private static final Map<UUID, Set<UUID>> trackedEntities = new ConcurrentHashMap<>();

    // Anti-recursión para el bonus de daño
    private static final Set<UUID> processingBonus = ConcurrentHashMap.newKeySet();

    private static final UUID ATK_UUID      = UUID.fromString("AA112233-4455-4667-8899-AABBCCDDEEFF");
    private static final UUID SPEED_UUID    = UUID.fromString("BB223344-5566-4778-99AA-BBCCDDEEFF00");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("CC334455-6677-4889-AABB-CCDDEEFF0011");

    public YarimNecklaceArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.LEGENDARY, 1, 0, settings);
    }

    @Override public boolean isDamageable()                 { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "Sello del Cazador"; }
    @Override public String getArtifactDescription()  { return "Artefacto de Yarim — God Hunter"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Resuena cerca de seres poderosos",
            "Yarim lo usaba para detectar esencias del mundo anterior",
            "§6Pasivo§r — Resonancia de Poder: detecta entidades §c>40 HP§r en 16 bloques",
            "§7Mientras haya una cerca: §6Speed I§r pasivo + partículas doradas",
            "§7Sonido sutil al entrar en rango",
            "§7+2 daño de ataque extra contra esas entidades",
            "+2 Daño  |  +5% Velocidad  |  +2 Dureza de Armadura"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Antes de ver al enemigo, el sello ya lo sabe";
    }

    // ========== TICK: resonancia pasiva ==========

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;
        if (!TrinketHelper.hasTrinketEquipped(player, stack.getItem()) &&
            player.getOffHandStack().getItem() != this) return;

        int ticks = stack.getOrCreateNbt().getInt(TICK_TAG) + 1;
        stack.getOrCreateNbt().putInt(TICK_TAG, ticks);

        if (!(world instanceof ServerWorld serverWorld)) return;

        UUID playerUuid = player.getUuid();
        Set<UUID> prevInRange = trackedEntities.computeIfAbsent(playerUuid, k -> new HashSet<>());

        // ── Scan cada 2s ──
        if (ticks % SCAN_INTERVAL == 0) {
            Set<UUID> nowInRange = new HashSet<>();
            Box box = player.getBoundingBox().expand(RESONANCE_RANGE);

            world.getEntitiesByClass(LivingEntity.class, box, e ->
                e != player && e.isAlive() && isPowerful(e)
            ).forEach(e -> nowInRange.add(e.getUuid()));

            // Nuevos en rango → sonido
            for (UUID uid : nowInRange) {
                if (!prevInRange.contains(uid)) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.5f, 1.8f);
                    break; // un sonido por scan es suficiente
                }
            }

            // Speed I mientras haya poderosos cerca
            if (!nowInRange.isEmpty()) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, SCAN_INTERVAL + 5, 0, false, false, true));
            }

            trackedEntities.put(playerUuid, nowInRange);
            prevInRange = nowInRange;
        }

        // ── Partículas doradas pulsantes sobre entidades en rango (cada 1s) ──
        if (ticks % PARTICLE_INTERVAL == 0 && !prevInRange.isEmpty()) {
            Box box = player.getBoundingBox().expand(RESONANCE_RANGE);
            world.getEntitiesByClass(LivingEntity.class, box, e ->
                e != player && e.isAlive() && isPowerful(e)
            ).forEach(target -> {
                // Corona de partículas doradas sobre el objetivo
                for (int i = 0; i < 6; i++) {
                    double angle = (i / 6.0) * Math.PI * 2;
                    serverWorld.spawnParticles(
                        new DustParticleEffect(new Vector3f(1.0f, 0.8f, 0.0f), 0.8f),
                        target.getX() + Math.cos(angle) * 0.4,
                        target.getY() + target.getHeight() + 0.25,
                        target.getZ() + Math.sin(angle) * 0.4,
                        1, 0.02, 0.05, 0.02, 0.0);
                }
            });
        }
    }

    // ========== DAMAGE EVENT: +2 contra entidades poderosas ==========

    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!isPowerful(entity)) return true;
            if (!TrinketHelper.hasArtifactOfType(attacker, YarimNecklaceArtifact.class)) return true;

            UUID uuid = entity.getUuid();
            if (processingBonus.contains(uuid)) return true;
            processingBonus.add(uuid);
            try {
                entity.damage(entity.getDamageSources().magic(), BONUS_DAMAGE);
            } finally {
                processingBonus.remove(uuid);
            }

            // Pequeña partícula dorada en el objetivo al proc
            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.WAX_ON,
                    entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                    4, 0.3, 0.3, 0.3, 0.05);
            }
            return true;
        });
    }

    // ========== UTILIDAD ==========

    private static boolean isPowerful(LivingEntity e) {
        return e instanceof PlayerEntity || e.getMaxHealth() > POWERFUL_HP;
    }

    // ========== TRINKETS API ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        map.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(ATK_UUID, "Yarim necklace atk", 2.0,
                EntityAttributeModifier.Operation.ADDITION));
        map.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(SPEED_UUID, "Yarim necklace speed", 0.05,
                EntityAttributeModifier.Operation.MULTIPLY_BASE));
        map.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(TOUGHNESS_UUID, "Yarim necklace toughness", 2.0,
                EntityAttributeModifier.Operation.ADDITION));
        return map;
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Solo pasivo
    }
}
