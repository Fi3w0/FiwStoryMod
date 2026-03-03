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
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Autómata de Bronce Axiom-7.
 * Pasivo Núcleo de Combustión: en offhand/trinket, cualquier golpe del jugador
 * prende fuego al objetivo. Mantiene Fire Resistance I mientras está equipado.
 */
public class BronzeAxiomArtifact extends BaseArtifactItem {

    public BronzeAxiomArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 0, 0, settings);
    }

    @Override public boolean isDamageable() { return false; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }

    @Override public String getArtifactDisplayName() { return "Autómata de Bronce Axiom-7"; }
    @Override public String getArtifactDescription()  { return "Recuerdo del Pasado"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Núcleo de combustión interna activo",
            "Cada golpe transmite fuego al enemigo",
            "§6Núcleo de Combustión§7: golpes prenden fuego (offhand/trinket)",
            "+10% Resistencia a fuego | +2 Armadura | -5% Velocidad"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "El bronce no se oxida. Su llama tampoco se apaga.";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Pasivo — sin uso activo
    }

    // ========== TICK: dar Fire Resistance si en offhand o trinket ==========
    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;
        if (world.getTime() % 30 != 0) return; // Refrescar cada 1.5s

        // Solo activo en offhand o trinket slot
        if (!TrinketHelper.hasInOffhandOrTrinket(player, BronzeAxiomArtifact.class)) return;

        if (!player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE, 60, 0, false, false, true));
        }
    }

    // ========== ATRIBUTOS ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> m = HashMultimap.create();
        m.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Axiom armor", 2.0, EntityAttributeModifier.Operation.ADDITION));
        m.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Axiom speed penalty", -0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return m;
    }

    // ========== REGISTRO DE EVENTO: fuego en golpe ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;

            // Solo interesa si el atacante es un jugador con Axiom-7 en offhand/trinket
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!TrinketHelper.hasInOffhandOrTrinket(attacker, BronzeAxiomArtifact.class)) return true;

            // Prender fuego al objetivo por 3 segundos
            entity.setOnFireFor(3);

            // Partículas de fuego
            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                    6, 0.2, 0.3, 0.2, 0.05);
                sw.spawnParticles(ParticleTypes.SMALL_FLAME,
                    entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                    4, 0.15, 0.2, 0.15, 0.02);
            }

            return true;
        });
    }
}
