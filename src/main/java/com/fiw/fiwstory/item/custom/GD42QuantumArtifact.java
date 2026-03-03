package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GD42 Quantum - Tecnología de era prohibida.
 * Habilidad: Quantum Anchor — ancla posición 5s con inmortalidad, luego retorna.
 */
public class GD42QuantumArtifact extends BaseArtifactItem {

    private static final int ANCHOR_DURATION = 100;   // 5 segundos
    private static final int ANCHOR_COOLDOWN  = 1200; // 60 segundos

    // Player UUID → data
    private static final Map<UUID, Vec3d> anchorPositions = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>  anchorExpiry    = new ConcurrentHashMap<>();
    private static final Map<UUID, Long>  cooldownExpiry  = new ConcurrentHashMap<>();

    public GD42QuantumArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 1, 0, settings);
    }

    @Override public boolean isDamageable() { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "GD42 Quantum"; }
    @Override public String getArtifactDescription()  { return "Tecnología de Era Prohibida"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Tecnología avanzada conectada con magia prohibida",
            "Prototipo de manipulación cuántica",
            "Parece tecnología de Fi3w0... aunque no del todo",
            "§3Quantum Anchor§7: inmortalidad 5s + retorno §8(60s CD)"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Donde la ciencia dejó de respetar los límites";
    }

    // ========== HABILIDAD: QUANTUM ANCHOR ==========
    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (world.isClient()) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        // No activar si anchor ya está activo
        if (anchorExpiry.containsKey(uuid)) {
            player.sendMessage(Text.literal("§3⚛ Quantum Anchor ya activo").formatted(Formatting.DARK_AQUA), true);
            return;
        }

        // Verificar cooldown propio
        Long cdExp = cooldownExpiry.get(uuid);
        if (cdExp != null && worldTime < cdExp) {
            long remaining = (cdExp - worldTime) / 20;
            player.sendMessage(Text.literal("§3⚛ Cooldown: " + remaining + "s").formatted(Formatting.DARK_AQUA), true);
            return;
        }

        // Guardar posición actual
        anchorPositions.put(uuid, player.getPos());
        anchorExpiry.put(uuid, worldTime + ANCHOR_DURATION);
        cooldownExpiry.put(uuid, worldTime + ANCHOR_COOLDOWN);

        // Resistance V (amplifier 4) = inmunidad completa al daño
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.RESISTANCE, ANCHOR_DURATION, 4, false, true, true));

        // Partículas de activación
        if (world instanceof ServerWorld sw) {
            for (int i = 0; i < 24; i++) {
                double a = (i / 24.0) * Math.PI * 2;
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX() + Math.cos(a) * 1.2,
                    player.getY() + 1.0,
                    player.getZ() + Math.sin(a) * 1.2,
                    1, 0.05, 0.05, 0.05, 0.02);
            }
            sw.spawnParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(),
                15, 0.3, 0.6, 0.3, 0.05);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.8f, 1.6f);

        player.sendMessage(Text.literal("§3⚛ Quantum Anchor activado — 5s").formatted(Formatting.DARK_AQUA), true);
        player.getItemCooldownManager().set(this, ANCHOR_COOLDOWN);
    }

    // ========== TICK: comprobar expiración del anchor ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;
        checkAnchorExpiry(player, world);
    }

    private static void checkAnchorExpiry(PlayerEntity player, World world) {
        UUID uuid = player.getUuid();
        Long expiry = anchorExpiry.get(uuid);
        if (expiry == null) return;
        if (world.getTime() < expiry) return;

        // Expiró: teleportar de vuelta
        Vec3d returnPos = anchorPositions.get(uuid);
        if (returnPos != null && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.teleport(returnPos.x, returnPos.y, returnPos.z);

            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.PORTAL,
                    returnPos.x, returnPos.y + 1.0, returnPos.z,
                    25, 0.5, 0.8, 0.5, 0.1);
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    returnPos.x, returnPos.y + 0.5, returnPos.z,
                    10, 0.3, 0.3, 0.3, 0.05);
            }
            world.playSound(null, returnPos.x, returnPos.y, returnPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.3f);
        }

        anchorPositions.remove(uuid);
        anchorExpiry.remove(uuid);
        player.sendMessage(Text.literal("§3⚛ Quantum Anchor expirado").formatted(Formatting.DARK_AQUA), true);
    }

    // ========== ATRIBUTOS ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> m = HashMultimap.create();
        m.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "GD42 attack damage", 1.5, EntityAttributeModifier.Operation.ADDITION));
        m.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "GD42 movement speed", 0.07, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        m.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "GD42 knockback resist", 0.10, EntityAttributeModifier.Operation.ADDITION));
        return m;
    }
}
