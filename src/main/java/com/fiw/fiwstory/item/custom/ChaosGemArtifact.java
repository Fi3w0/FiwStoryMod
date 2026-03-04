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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChaosGemArtifact extends BaseArtifactItem {

    private static final Random CHAOS_RNG = new Random();
    private static final int CHAOS_INTERVAL = 600; // 30 segundos
    private static final Map<UUID, Long> nextChaosTime = new ConcurrentHashMap<>();

    public ChaosGemArtifact(Settings settings) {
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
    public String getArtifactDisplayName() { return "Gema del Lord de Caos"; }

    @Override
    public String getArtifactDescription() { return "Artefacto del mundo pasado - Paradoja temporal"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Sensación de no pertenecer a este mundo",
            "Energía caótica contenida"
        );
    }

    @Override
    public String getArtifactQuote() { return "El caos siempre encuentra su camino"; }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo
    }

    // ========== PASIVA: Caos Latente ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        UUID uuid = player.getUuid();
        long worldTime = world.getTime();

        // Per-player timer (fix global sync bug)
        Long nextTime = nextChaosTime.get(uuid);
        if (nextTime == null) {
            nextChaosTime.put(uuid, worldTime + CHAOS_INTERVAL);
            return;
        }
        if (worldTime < nextTime) return;

        nextChaosTime.put(uuid, worldTime + CHAOS_INTERVAL);

        int roll = CHAOS_RNG.nextInt(4);
        StatusEffectInstance effect = switch (roll) {
            case 0 -> new StatusEffectInstance(StatusEffects.HASTE,       100, 1, false, true, true); // Haste II
            case 1 -> new StatusEffectInstance(StatusEffects.SPEED,       100, 1, false, true, true); // Speed II
            case 2 -> new StatusEffectInstance(StatusEffects.STRENGTH,    100, 0, false, true, true); // Strength I
            default -> new StatusEffectInstance(StatusEffects.NIGHT_VISION, 200, 0, false, true, true); // Night Vision
        };
        player.addStatusEffect(effect);

        String[] names = {"§aHaste II", "§bSpeed II", "§cStrength I", "§7Night Vision"};
        player.sendMessage(Text.literal("§5⚡ Caos Latente§7: " + names[roll]), true);

        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.4, 0.6, 0.4, 0.12);
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Chaos gem movement speed", 0.04, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Chaos gem knockback resistance", 0.10, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Chaos gem attack damage", 1.5, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
