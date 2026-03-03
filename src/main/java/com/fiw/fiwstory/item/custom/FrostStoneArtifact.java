package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Piedra de Escarcha - Recuerdo del Pasado
 *
 * Artefacto del Rey de Hielo. Su alma sigue en nuestro universo
 * después de su muerte, lleno de energía de muerte y frío.
 *
 * Habilidad pasiva de hielo:
 * - Aplica Slowness a mobs hostiles cercanos (radio 6)
 * - Aura de partículas de nieve
 * - Atributos: +2 Armor, +1 Armor Toughness, +10% Knockback Resistance
 */
public class FrostStoneArtifact extends BaseArtifactItem {

    private static final int FROST_AURA_RADIUS = 6;
    private static final int FROST_TICK_INTERVAL = 40; // 2 seconds
    private static final String TICK_COUNTER_TAG = "fiwstory:frost_stone_ticks";

    public FrostStoneArtifact(Settings settings) {
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
        return "Piedra de Escarcha";
    }

    @Override
    public String getArtifactDescription() {
        return "Recuerdo del Pasado - Rey de Hielo";
    }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "El alma del Rey de Hielo persiste en este artefacto",
            "Lleno de energía de muerte y frío eterno",
            "Aura gélida que ralentiza enemigos cercanos",
            "+2 Armadura | +1 Dureza | +10% Resist. Retroceso"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Su alma sigue en nuestro universo hasta después de su muerte";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Passive only
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        boolean isActive = player.getMainHandStack() == stack
            || player.getOffHandStack() == stack
            || TrinketHelper.hasTrinketEquipped(player, stack.getItem());
        if (!isActive) {
            return;
        }

        int tickCounter = stack.getOrCreateNbt().getInt(TICK_COUNTER_TAG);
        tickCounter++;
        stack.getOrCreateNbt().putInt(TICK_COUNTER_TAG, tickCounter);

        // Frost aura particles every 10 ticks
        if (tickCounter % 10 == 0 && world instanceof ServerWorld serverWorld) {
            double angle = (tickCounter / 10.0) * 0.5;
            for (int i = 0; i < 3; i++) {
                double a = angle + (i * Math.PI * 2 / 3);
                double x = player.getX() + Math.cos(a) * 1.2;
                double z = player.getZ() + Math.sin(a) * 1.2;
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
                    x, player.getY() + 0.5, z,
                    1, 0.1, 0.2, 0.1, 0.0);
            }
        }

        // Apply slowness to nearby hostiles every 2 seconds
        if (tickCounter % FROST_TICK_INTERVAL != 0) {
            return;
        }

        Box searchBox = player.getBoundingBox().expand(FROST_AURA_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class, searchBox,
            e -> e.isAlive() && e.squaredDistanceTo(player) <= FROST_AURA_RADIUS * FROST_AURA_RADIUS
        );

        for (HostileEntity hostile : hostiles) {
            hostile.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 60, 0, false, false, true
            ));

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
                    hostile.getX(), hostile.getY() + hostile.getHeight() / 2, hostile.getZ(),
                    5, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Frost Stone armor", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(uuid, "Frost Stone toughness", 1.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Frost Stone knockback resist", 0.10, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
