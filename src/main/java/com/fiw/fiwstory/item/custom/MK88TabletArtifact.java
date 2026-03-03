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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MK88 Tablet — Dispositivo táctico de octava generación.
 * Habilidad pasiva automática: Emergency Recovery Module.
 * Cuando el jugador recibe daño, activa recuperación si no está en cooldown.
 */
public class MK88TabletArtifact extends BaseArtifactItem {

    private static final int RECOVERY_COOLDOWN  = 600;  // 30 segundos
    private static final int ABSORPTION_DURATION = 100; // 5 segundos

    private static final Map<UUID, Long> mk88Cooldowns = new ConcurrentHashMap<>();

    public MK88TabletArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 0, 0, settings);
    }

    @Override public boolean isDamageable() { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "MK88 Tableta"; }
    @Override public String getArtifactDescription()  { return "Dispositivo Táctico Gen-8"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Dispositivo táctico de octava generación",
            "Cada error, una variable descartada",
            "§eEmergency Recovery Module§7: auto-activa al recibir daño",
            "Absorción +3 corazones, limpia veneno/wither/fuego §8(30s CD)"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "El protocolo de emergencia no falla. Nunca.";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Pasivo — sin uso activo
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // Solo pasivo mediante eventos de daño
    }

    // ========== ATRIBUTOS ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> m = HashMultimap.create();
        m.put(EntityAttributes.GENERIC_ATTACK_SPEED,
            new EntityAttributeModifier(uuid, "MK88 attack speed", 0.08, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        m.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "MK88 movement speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return m;
    }

    // ========== REGISTRO DE EVENTO DE DAÑO ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(entity instanceof PlayerEntity player)) return true;

            UUID uuid = player.getUuid();
            long worldTime = entity.getWorld().getTime();

            // Verificar si tiene MK88 equipado (cualquier posición)
            if (!TrinketHelper.hasArtifactOfType(player, MK88TabletArtifact.class)) return true;

            // Verificar cooldown
            Long cdExp = mk88Cooldowns.get(uuid);
            if (cdExp != null && worldTime < cdExp) return true;

            // Activar Emergency Recovery Module
            mk88Cooldowns.put(uuid, worldTime + RECOVERY_COOLDOWN);

            // 3 corazones de absorción (Absorption III = amplifier 2)
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.ABSORPTION, ABSORPTION_DURATION, 2, false, true, true));

            // Limpiar estados negativos
            player.removeStatusEffect(StatusEffects.POISON);
            player.removeStatusEffect(StatusEffects.WITHER);
            player.extinguish();

            // Partículas de recuperación
            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + player.getHeight(), player.getZ(),
                    8, 0.4, 0.3, 0.4, 0.05);
                sw.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    5, 0.3, 0.5, 0.3, 0.1);
            }

            entity.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.9f, 1.4f);

            player.sendMessage(Text.literal("§e⚙ Emergency Recovery activado").formatted(Formatting.YELLOW), true);

            return true; // Permite el daño original de todos modos
        });
    }
}
