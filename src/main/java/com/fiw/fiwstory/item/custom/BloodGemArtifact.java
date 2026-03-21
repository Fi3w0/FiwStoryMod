package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwUtils;
import com.fiw.fiwstory.lib.TrinketHelper;
import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BloodGemArtifact extends BaseArtifactItem {

    private static final int LIFESTEAL_COOLDOWN = 40; // 2 segundos
    private static final Map<UUID, Long> lifeStealCooldowns = new ConcurrentHashMap<>();

    public BloodGemArtifact(Settings settings) {
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
    public String getArtifactDisplayName() { return "Gema de Sangre Divina"; }

    @Override
    public String getArtifactDescription() { return "Uno de los artefactos legendarios del Dios Faraón"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Hecha de sangre divina",
            "Energía vital concentrada"
        );
    }

    @Override
    public String getArtifactQuote() { return "La sangre del faraón fluye eternamente"; }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo
    }

    private static final int CONFLICT_CHECK_INTERVAL = 40; // cada 2s

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            if (world.getTime() % CONFLICT_CHECK_INTERVAL == 0) {
                if (FiwUtils.hasItemAnywhere(player, EspadaMgshtraklar.class)) {
                    player.damage(player.getDamageSources().magic(), 2.0f);
                    FiwEffects.spawnParticlesAroundEntity(player, ParticleTypes.SOUL_FIRE_FLAME, 10, 1.0);
                    player.sendMessage(
                        Text.literal("§4§lLa sangre divina rechaza la duplicidad§r"), false);
                }
            }
        }
    }

    // ========== EVENTO: Robo de Vida (lifesteal 15%, cooldown 2s) ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!TrinketHelper.hasArtifactOfType(attacker, BloodGemArtifact.class)) return true;

            UUID uuid = attacker.getUuid();
            long worldTime = entity.getWorld().getTime();
            Long cdExp = lifeStealCooldowns.get(uuid);
            if (cdExp != null && worldTime < cdExp) return true;

            lifeStealCooldowns.put(uuid, worldTime + LIFESTEAL_COOLDOWN);
            float heal = amount * 0.15f;
            attacker.heal(heal);

            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HEART,
                    attacker.getX(), attacker.getY() + 1.2, attacker.getZ(),
                    2, 0.3, 0.2, 0.3, 0.01);
            }
            return true;
        });
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
            new EntityAttributeModifier(uuid, "Blood gem max health", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Blood gem armor", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(uuid, "Blood gem attack damage", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Blood gem knockback resistance", 0.10, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
