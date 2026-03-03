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
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EscarabajoArtifact extends BaseArtifactItem {

    private static final int SHIELD_COOLDOWN = 400; // 20 segundos
    private static final Map<UUID, Long> shieldCooldowns = new ConcurrentHashMap<>();

    public EscarabajoArtifact(Settings settings) {
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
    public String getArtifactDisplayName() {
        return "Escarabajo de Plata del Faraón";
    }

    @Override
    public String getArtifactDescription() {
        return "Uno de los artefactos legendarios del Dios Faraón";
    }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Protección divina del desierto",
            "Bendición de defensa eterna"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "La plata del desierto protege a su portador";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo, no tiene uso activo
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // Atributos pasivos aplicados via Trinkets getModifiers
    }

    // ========== EVENTO: Escudo de Arena ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(entity instanceof PlayerEntity player)) return true;
            if (amount < 4.0f) return true; // solo golpes de más de 2 corazones
            if (!TrinketHelper.hasArtifactOfType(player, EscarabajoArtifact.class)) return true;

            UUID uuid = player.getUuid();
            long worldTime = entity.getWorld().getTime();
            Long cdExp = shieldCooldowns.get(uuid);
            if (cdExp != null && worldTime < cdExp) return true;

            shieldCooldowns.put(uuid, worldTime + SHIELD_COOLDOWN);
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.ABSORPTION, 100, 1, false, true, true)); // Absorption II, 5s

            if (entity.getWorld() instanceof ServerWorld sw) {
                for (int i = 0; i < 16; i++) {
                    double a = (i / 16.0) * Math.PI * 2;
                    sw.spawnParticles(ParticleTypes.GLOW,
                        player.getX() + Math.cos(a) * 1.0,
                        player.getY() + 0.5,
                        player.getZ() + Math.sin(a) * 1.0,
                        1, 0.05, 0.1, 0.05, 0.0);
                }
            }
            entity.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.PLAYERS, 0.6f, 0.7f);
            return true;
        });
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Escarabajo armor", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(uuid, "Escarabajo armor toughness", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Escarabajo knockback resistance", 0.2, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
