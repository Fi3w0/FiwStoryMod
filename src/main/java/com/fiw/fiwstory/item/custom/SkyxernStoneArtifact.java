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
 * Núcleo Astral — fragmento del cielo.
 *
 * Pasiva Zero Step:
 *   - Reduce daño de caída un 20%
 *   - Doble salto automático en el ápice del salto
 *
 * Auto-Bendición del Horizonte cada 42 segundos: +velocidad 10s
 *
 * Atributos: +7% velocidad de movimiento, -1 armadura
 */
public class SkyxernStoneArtifact extends BaseArtifactItem {

    private static final int BLESSING_INTERVAL = 840;  // 42 segundos
    private static final int BLESSING_DURATION  = 200; // 10 segundos

    // Doble salto: estado por jugador
    private static final Map<UUID, Boolean> doubleJumpReady = new ConcurrentHashMap<>();
    private static final Map<UUID, Double>  prevYVel        = new ConcurrentHashMap<>();

    // Bendición: cuándo debe activarse
    private static final Map<UUID, Long> nextBlessingTime = new ConcurrentHashMap<>();

    // Guardia de anti-recursión para reducción de caída
    private static final Set<UUID> processingFall = ConcurrentHashMap.newKeySet();

    public SkyxernStoneArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 1, 0, settings);
    }

    @Override public boolean isDamageable() { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "Núcleo Astral"; }
    @Override public String getArtifactDescription()  { return "Un fragmento del cielo en reposo"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "En su interior duerme una fuerza que no empuja… atrae",
            "Sientes paz interior al sostener esta piedra",
            "§bZero Step§7: doble salto + -20% daño de caída",
            "§bBendición del Horizonte§7: +vel 10s §8(auto c/42s)",
            "+7% velocidad de movimiento | -1 armadura"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Un fragmento del cielo que aún recuerda cómo volar";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Solo pasiva, sin habilidad activa
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        boolean isActive = player.getMainHandStack() == stack
            || player.getOffHandStack() == stack
            || TrinketHelper.hasTrinketEquipped(player, stack.getItem());
        if (!isActive) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        // --- DOBLE SALTO (detección de ápice) ---
        double currentY = player.getVelocity().y;
        double prevY    = prevYVel.getOrDefault(uuid, currentY);

        if (player.isOnGround()) {
            doubleJumpReady.put(uuid, true);
        } else if (Boolean.TRUE.equals(doubleJumpReady.get(uuid)) && prevY >= 0 && currentY < 0) {
            // Ápice del salto: impulso hacia arriba
            player.setVelocity(player.getVelocity().x, 0.45, player.getVelocity().z);
            player.velocityModified = true;
            doubleJumpReady.put(uuid, false);

            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.3, player.getZ(),
                    10, 0.3, 0.15, 0.3, 0.04);
                sw.spawnParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    5, 0.2, 0.1, 0.2, 0.01);
            }
        }
        prevYVel.put(uuid, currentY);

        // --- BENDICIÓN DEL HORIZONTE (auto c/42s) ---
        Long nextBlessing = nextBlessingTime.get(uuid);
        if (nextBlessing == null) {
            nextBlessingTime.put(uuid, worldTime + BLESSING_INTERVAL);
        } else if (worldTime >= nextBlessing) {
            nextBlessingTime.put(uuid, worldTime + BLESSING_INTERVAL);

            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, BLESSING_DURATION, 0, false, true, true));

            if (world instanceof ServerWorld sw) {
                for (int i = 0; i < 24; i++) {
                    double a = (i / 24.0) * Math.PI * 2;
                    sw.spawnParticles(ParticleTypes.CLOUD,
                        player.getX() + Math.cos(a) * 1.1,
                        player.getY() + 0.6,
                        player.getZ() + Math.sin(a) * 1.1,
                        1, 0.05, 0.1, 0.05, 0.0);
                }
                sw.spawnParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 0.4, 0.5, 0.4, 0.04);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.9f);
            player.sendMessage(
                Text.literal("§b✦ Bendición del Horizonte §7activada").formatted(Formatting.AQUA), true);
        }
    }

    // ========== EVENTO: reducción de daño de caída (20%) ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;
            if (!source.getType().msgId().equals("fall")) return true;
            if (!TrinketHelper.hasArtifactOfType(player, SkyxernStoneArtifact.class)) return true;

            UUID uuid = player.getUuid();
            if (processingFall.contains(uuid)) return true; // evitar recursión

            processingFall.add(uuid);
            try {
                entity.damage(source, amount * 0.80f); // 80% → reducción 20%
            } finally {
                processingFall.remove(uuid);
            }
            return false; // cancelar el daño original
        });
    }

    // ========== ATRIBUTOS ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> m = HashMultimap.create();
        m.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Skyxern Stone speed", 0.07, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        m.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Skyxern Stone armor", -1.0, EntityAttributeModifier.Operation.ADDITION));
        return m;
    }
}
