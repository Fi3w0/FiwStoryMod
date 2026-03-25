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
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Globo del Abismo — Artefacto ÉPICO
 *
 * Un pez globo de las profundidades que acumuló siglos de veneno abismal.
 * Sus espinas reaccionan ante cualquier amenaza, envenenando al agresor.
 *
 * Pasivo — Represalia Tóxica:
 *   Al recibir daño de una entidad, el atacante recibe Veneno II (4s) + Lentitud I (3s). CD: 3s.
 *
 * Activo — Marea Venenosa (clic derecho, CD: 45s):
 *   Ola tóxica de 8 bloques (10 en agua). Veneno I + Lentitud II + Náusea a todas las entidades cercanas.
 *   El jugador recibe Respiración Acuática (30s) + Velocidad I (5s).
 *
 * Stats: +3 Armadura | +2 Daño | +10% Resist. Retroceso
 */
public class PufferfishArtifact extends BaseArtifactItem {

    private static final int ACTIVE_RADIUS        = 6;
    private static final int ACTIVE_RADIUS_WATER  = 8;
    private static final long ACTIVE_CD_MS        = 45_000L; // 45 segundos
    private static final long PASSIVE_CD_TICKS    = 100L;    // 5 segundos

    private static final UUID ARMOR_UUID     = UUID.fromString("A1B2C3D4-E5F6-4789-ABCD-EF0123456789");
    private static final UUID DAMAGE_UUID    = UUID.fromString("B2C3D4E5-F6A7-4890-BCDE-F01234567890");
    private static final UUID KNOCKBACK_UUID = UUID.fromString("C3D4E5F6-A7B8-4901-CDEF-012345678901");

    // UUID del jugador → world time del último proc de Represalia
    private static final Map<UUID, Long> retaliationCooldowns = new ConcurrentHashMap<>();

    public PufferfishArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.EPIC, 1, 0, settings);
    }

    @Override public boolean isDamageable()                  { return false; }
    @Override public boolean isEnchantable(ItemStack stack)  { return false; }

    @Override
    public String getArtifactDisplayName() { return "Globo del Abismo"; }

    @Override
    public String getArtifactDescription() { return "Recuerdo del Pasado — Profundidades Olvidadas"; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Criatura del abismo que acumuló siglos de veneno",
            "Sus espinas reaccionan ante cualquier amenaza",
            "§9Pasivo§r — Represalia Tóxica: el que te golpea recibe §5Veneno I§r + §9Lentitud I§r §8(5s CD)§r",
            "§9Activo§r — Marea Venenosa §8(45s CD)§r: ola tóxica 6 bloques, §5Veneno I§r + §9Lentitud I§r",
            "§7En agua el radio aumenta a 8 bloques§r",
            "+3 Armadura  |  +2 Daño de ataque  |  +10% Resist. Retroceso"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "En las profundidades no hay depredadores. Solo veneno esperando su turno.";
    }

    // ========== TICK: sin efectos de corrupción en mano ==========

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // No llamar a super para evitar efectos de corrupción al sostenerlo
    }

    // ========== ACTIVO: MAREA VENENOSA ==========

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (world.isClient()) return;

        setCooldown(stack, "use", ACTIVE_CD_MS);

        ServerWorld serverWorld = (ServerWorld) world;
        boolean inWater = player.isTouchingWater();
        int radius = inWater ? ACTIVE_RADIUS_WATER : ACTIVE_RADIUS;

        // Buff al jugador
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 400, 0, false, false, true)); // 20s

        // AoE tóxico — mobs y jugadores enemigos
        Box searchBox = player.getBoundingBox().expand(radius);
        world.getEntitiesByClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive())
            .forEach(target -> {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,  80, 0, false, true, true)); // Veneno I   4s
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, true, true)); // Lentitud I 3s

                serverWorld.spawnParticles(ParticleTypes.BUBBLE,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    12, 0.5, 0.4, 0.5, 0.08);
            });

        // Partículas en ola circular alrededor del jugador
        for (int i = 0; i < 36; i++) {
            double angle = (i / 36.0) * Math.PI * 2;
            double r = 1.8 + (i % 3) * 0.7;
            double x = player.getX() + Math.cos(angle) * r;
            double z = player.getZ() + Math.sin(angle) * r;
            serverWorld.spawnParticles(ParticleTypes.BUBBLE, x, player.getY() + 0.8, z,
                2, 0.05, 0.3, 0.05, 0.04);
        }
        serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP,
            player.getX(), player.getY() + 1.0, player.getZ(),
            40, 1.8, 1.0, 1.8, 0.1);
        serverWorld.spawnParticles(ParticleTypes.SPLASH,
            player.getX(), player.getY() + 0.5, player.getZ(),
            60, 2.0, 0.5, 2.0, 0.2);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_ELDER_GUARDIAN_HURT, SoundCategory.PLAYERS, 0.7f, 1.6f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.PLAYERS, 1.0f, 1.0f);

        player.sendMessage(
            Text.literal("§9〜 Marea Venenosa — radio " + radius + " bloques").formatted(Formatting.AQUA),
            true
        );
    }

    // ========== PASIVO: REPRESALIA TÓXICA (damage event) ==========

    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof LivingEntity attacker)) return true;
            if (attacker == player) return true;
            if (!TrinketHelper.hasArtifactOfType(player, PufferfishArtifact.class)) return true;

            long worldTime = entity.getWorld().getTime();
            Long last = retaliationCooldowns.get(player.getUuid());
            if (last != null && worldTime < last + PASSIVE_CD_TICKS) return true;

            retaliationCooldowns.put(player.getUuid(), worldTime);

            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,   60, 0, false, true, true)); // Veneno I 3s
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, true, true)); // Lentitud I 2s

            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.BUBBLE,
                    attacker.getX(), attacker.getY() + attacker.getHeight() / 2, attacker.getZ(),
                    10, 0.4, 0.4, 0.4, 0.05);
                serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP,
                    attacker.getX(), attacker.getY() + attacker.getHeight() / 2, attacker.getZ(),
                    4, 0.3, 0.3, 0.3, 0.02);
            }

            return true;
        });
    }

    // ========== TRINKETS API ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(ARMOR_UUID,     "Pufferfish armor",     3.0,  EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(DAMAGE_UUID,    "Pufferfish damage",    2.0,  EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(KNOCKBACK_UUID, "Pufferfish knockback", 0.10, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
