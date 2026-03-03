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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skyxern Legacy Fragment — fragmento del primer firmamento.
 *
 * Habilidad activa Ascenso del Legado (6s, 90s CD):
 *   - +Speed I (≈+15% velocidad)
 *   - +Strength I (≈+15% daño)
 *   - Reducción de knockback (0.4 knockback resistance temporal)
 *   - Partículas celestes intensas durante la activación
 *
 * Pasiva: curación de 1 corazón cada 10 segundos
 *
 * Atributos: +5% velocidad, +3 armadura, -30% daño de caída (evento)
 */
public class SkyxernLegacyArtifact extends BaseArtifactItem {

    private static final int ABILITY_DURATION = 120;   // 6 segundos
    private static final int ABILITY_COOLDOWN  = 1800; // 90 segundos
    private static final int HEAL_INTERVAL     = 200;  // 10 segundos

    // UUID fijo para el modificador de knockback temporal durante la habilidad
    private static final UUID ASCENSO_KB_UUID =
        UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890");

    // Habilidad activa
    private static final Map<UUID, Long> abilityExpiry   = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> abilityCooldown = new ConcurrentHashMap<>();

    // Curación pasiva
    private static final Map<UUID, Long> nextHealTime = new ConcurrentHashMap<>();

    // Guardia anti-recursión para daño de caída
    private static final Set<UUID> processingFall = ConcurrentHashMap.newKeySet();

    public SkyxernLegacyArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.LEGENDARY, 1, 0, settings);
    }

    @Override public boolean isDamageable() { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "Skyxern Legacy Fragment"; }
    @Override public String getArtifactDescription()  { return "Fragmento del primer firmamento"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Fragmento del primer firmamento que vio nacer este mundo",
            "Cuando el cielo se quebró, su voluntad cayó a la tierra",
            "§dAscenso del Legado§7: vel+15% | dmg+15% | 6s §8(90s CD)",
            "§7Pasiva§7: 1 corazón cada 10s | §7-30% daño de caída",
            "+5% velocidad | +3 armadura"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Su voluntad cayó a la tierra, y aún sigue aquí";
    }

    // ========== HABILIDAD: ASCENSO DEL LEGADO ==========
    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (world.isClient()) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        Long cdExp = abilityCooldown.get(uuid);
        if (cdExp != null && worldTime < cdExp) {
            long remaining = (cdExp - worldTime) / 20;
            player.sendMessage(
                Text.literal("§dAscenso del Legado — §7Cooldown: " + remaining + "s"), true);
            return;
        }

        // Activar habilidad
        abilityExpiry.put(uuid, worldTime + ABILITY_DURATION);
        abilityCooldown.put(uuid, worldTime + ABILITY_COOLDOWN);

        // Efectos: Speed I + Strength I
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED, ABILITY_DURATION, 0, false, true, true));
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.STRENGTH, ABILITY_DURATION, 0, false, true, true));

        // Knockback resistance temporal (leve)
        var kbAttr = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (kbAttr != null) {
            kbAttr.removeModifier(ASCENSO_KB_UUID); // limpiar si hubiera uno anterior
            kbAttr.addTemporaryModifier(new EntityAttributeModifier(
                ASCENSO_KB_UUID, "Skyxern Ascenso KB", 0.4, EntityAttributeModifier.Operation.ADDITION));
        }

        // Partículas celestes de activación
        if (world instanceof ServerWorld sw) {
            for (int i = 0; i < 36; i++) {
                double a = (i / 36.0) * Math.PI * 2;
                double h = (i % 3) * 0.75;
                sw.spawnParticles(ParticleTypes.END_ROD,
                    player.getX() + Math.cos(a) * 1.4,
                    player.getY() + h,
                    player.getZ() + Math.sin(a) * 1.4,
                    1, 0.05, 0.1, 0.05, 0.05);
            }
            sw.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.0, player.getZ(),
                25, 0.5, 0.8, 0.5, 0.18);
            sw.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                player.getX(), player.getY() + 0.5, player.getZ(),
                15, 0.4, 0.6, 0.4, 0.1);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 2.0f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.5f, 1.8f);

        player.sendMessage(
            Text.literal("§d✦ Ascenso del Legado §7activado — 6s").formatted(Formatting.LIGHT_PURPLE), true);
        player.getItemCooldownManager().set(this, ABILITY_COOLDOWN);
    }

    // ========== TICK: partículas, expiración, curación pasiva ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        boolean isActive = player.getMainHandStack() == stack
            || player.getOffHandStack() == stack
            || TrinketHelper.hasTrinketEquipped(player, stack.getItem());
        if (!isActive) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        // Expiración de Ascenso del Legado
        Long expiry = abilityExpiry.get(uuid);
        if (expiry != null && worldTime >= expiry) {
            var kbAttr = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
            if (kbAttr != null) {
                kbAttr.removeModifier(ASCENSO_KB_UUID);
            }
            abilityExpiry.remove(uuid);
        }

        // Partículas celestes durante la habilidad activa
        if (expiry != null && worldTime < expiry && world instanceof ServerWorld sw) {
            if (worldTime % 4 == 0) {
                double a = (worldTime / 4.0) * 0.9;
                sw.spawnParticles(ParticleTypes.END_ROD,
                    player.getX() + Math.cos(a) * 1.0,
                    player.getY() + 1.0,
                    player.getZ() + Math.sin(a) * 1.0,
                    2, 0.1, 0.2, 0.1, 0.03);
                sw.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                    player.getX(), player.getY() + 0.8, player.getZ(),
                    1, 0.3, 0.4, 0.3, 0.02);
            }
        }

        // Curación pasiva: 1 corazón cada 10 segundos
        Long nextHeal = nextHealTime.get(uuid);
        if (nextHeal == null) {
            nextHealTime.put(uuid, worldTime + HEAL_INTERVAL);
        } else if (worldTime >= nextHeal) {
            nextHealTime.put(uuid, worldTime + HEAL_INTERVAL);
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(2.0f); // 1 corazón = 2 HP
                if (world instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.6, player.getZ(),
                        3, 0.3, 0.2, 0.3, 0.02);
                }
            }
        }
    }

    // ========== EVENTO: reducción de daño de caída (30%) ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;
            if (!source.getType().msgId().equals("fall")) return true;
            if (!TrinketHelper.hasArtifactOfType(player, SkyxernLegacyArtifact.class)) return true;

            UUID uuid = player.getUuid();
            if (processingFall.contains(uuid)) return true;

            processingFall.add(uuid);
            try {
                entity.damage(source, amount * 0.70f); // 70% → reducción 30%
            } finally {
                processingFall.remove(uuid);
            }
            return false;
        });
    }

    // ========== ATRIBUTOS ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> m = HashMultimap.create();
        m.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Skyxern Legacy speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        m.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Skyxern Legacy armor", 3.0, EntityAttributeModifier.Operation.ADDITION));
        return m;
    }
}
