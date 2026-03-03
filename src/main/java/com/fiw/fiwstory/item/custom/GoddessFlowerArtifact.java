package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.FiwUtils;
import com.fiw.fiwstory.lib.TrinketHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Amuleto de la Diosa de la Naturaleza - Recuerdo del Pasado
 *
 * Atributos pasivos (trinket slot):
 * - +2 Corazones máximos (+4 HP)
 * - +10% Regeneración natural (via Regeneration effect)
 * - +15% Resistencia al veneno (armor toughness como proxy)
 * - +10% Velocidad de Movimiento sobre bloques naturales
 *
 * Habilidades:
 * - Juicio de la Naturaleza: Raíces contra mobs cada 5s
 * - Bendición Vital: Curación al matar mobs afectados
 * - Protección de Emergencia: Explosión natural al bajar del 30% HP
 */
public class GoddessFlowerArtifact extends BaseArtifactItem {

    // Configuración de Juicio de la Naturaleza
    private static final int JUDGMENT_INTERVAL_TICKS = 100; // 5 segundos
    private static final int HOSTILE_CHECK_RADIUS = 14;
    private static final int MIN_HOSTILES_REQUIRED = 3;
    private static final int MAX_ROOT_TARGETS = 3;
    private static final float ROOT_DAMAGE = 5.0f;
    private static final float ROOT_CHANCE = 0.20f; // 20% enraizamiento extra (0.5s)
    private static final int SLOWNESS_DURATION_TICKS = 20; // 1 segundo

    // Configuración de Protección de Emergencia
    private static final float EMERGENCY_HEALTH_THRESHOLD = 0.30f; // 30% HP
    private static final int EMERGENCY_COOLDOWN_TICKS = 1200; // 60 segundos
    private static final int EMERGENCY_PUSH_RADIUS = 6;
    private static final float EMERGENCY_PUSH_FORCE = 1.5f;
    private static final int EMERGENCY_SLOWNESS_DURATION = 40; // 2 segundos

    // Bendición Vital
    private static final float VITAL_BLESSING_HEAL = 2.0f; // 1 corazón

    // NBT Tags para contadores internos
    private static final String TICK_COUNTER_TAG = "fiwstory:goddess_flower_ticks";
    private static final String EMERGENCY_COOLDOWN_TAG = "fiwstory:goddess_flower_emergency_cd";
    private static final String REGEN_COUNTER_TAG = "fiwstory:goddess_flower_regen";

    public GoddessFlowerArtifact(Settings settings) {
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
        return "Amuleto de la Diosa de la Naturaleza";
    }

    @Override
    public String getArtifactDescription() {
        return "Recuerdo del Pasado";
    }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Poder de una diosa de la naturaleza desconocida",
            "Donde su energía florece, la vida responde",
            "Juicio de la Naturaleza contra invasores",
            "+2 Corazones | +10% Regeneración | +15% Res. Veneno",
            "Protección de emergencia al borde de la muerte"
        );
    }

    @Override
    public String getArtifactQuote() {
        return "Donde su energía florece, la vida responde... y los intrusos son castigados";
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            FiwUtils.sendInfoMessage(player, "Sientes la calidez de la naturaleza...");
        }
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) {
            return;
        }

        // Funciona en mainhand, offhand o trinket slot
        boolean isActive = player.getMainHandStack() == stack
            || player.getOffHandStack() == stack
            || TrinketHelper.hasTrinketEquipped(player, stack.getItem());
        if (!isActive) {
            return;
        }

        int tickCounter = stack.getOrCreateNbt().getInt(TICK_COUNTER_TAG);
        tickCounter++;
        stack.getOrCreateNbt().putInt(TICK_COUNTER_TAG, tickCounter);

        // Regeneración natural cada 3 segundos
        int regenCounter = stack.getOrCreateNbt().getInt(REGEN_COUNTER_TAG);
        regenCounter++;
        stack.getOrCreateNbt().putInt(REGEN_COUNTER_TAG, regenCounter);
        if (regenCounter % 60 == 0) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 40, 0, false, false, true
            ));
        }

        // Velocidad extra sobre bloques naturales
        if (tickCounter % 20 == 0) {
            applyNatureSpeedBonus(player, world);
        }

        // Protección de emergencia (verificar cada 10 ticks)
        if (tickCounter % 10 == 0) {
            checkEmergencyProtection(player, world, stack);
        }

        // Juicio de la Naturaleza cada 5 segundos
        if (tickCounter % JUDGMENT_INTERVAL_TICKS != 0) {
            return;
        }

        Box searchBox = player.getBoundingBox().expand(HOSTILE_CHECK_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class, searchBox,
            e -> e.isAlive() && e.squaredDistanceTo(player) <= HOSTILE_CHECK_RADIUS * HOSTILE_CHECK_RADIUS
        );

        if (hostiles.size() < MIN_HOSTILES_REQUIRED) {
            return;
        }

        executeNatureJudgment(player, world, hostiles);
    }

    private void executeNatureJudgment(PlayerEntity player, World world, List<HostileEntity> hostiles) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        hostiles.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)));

        List<HostileEntity> targets = hostiles.stream()
            .limit(MAX_ROOT_TARGETS)
            .collect(Collectors.toList());

        for (HostileEntity target : targets) {
            Vec3d targetPos = target.getPos();

            spawnRootParticles(serverWorld, targetPos);

            boolean wasAlive = target.isAlive();
            target.damage(player.getDamageSources().magic(), ROOT_DAMAGE);

            target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, 0, false, false, false
            ));

            if (FiwUtils.randomChance(ROOT_CHANCE)) {
                target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, 10, 3, false, false, false
                ));
                serverWorld.spawnParticles(
                    ParticleTypes.COMPOSTER,
                    targetPos.x, targetPos.y + 0.3, targetPos.z,
                    10, 0.3, 0.1, 0.3, 0.02
                );
            }

            if (!target.isAlive() && wasAlive) {
                player.heal(VITAL_BLESSING_HEAL);
                serverWorld.spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    8, 0.4, 0.4, 0.4, 0.02
                );
                serverWorld.spawnParticles(
                    new DustParticleEffect(new Vector3f(0.2f, 0.8f, 0.2f), 0.7f),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    5, 0.3, 0.3, 0.3, 0.0
                );
            }
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_AZALEA_LEAVES_BREAK, SoundCategory.PLAYERS,
            0.8f, 0.6f + world.getRandom().nextFloat() * 0.4f);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN, SoundCategory.PLAYERS,
            0.5f, 0.8f);
    }

    private void spawnRootParticles(ServerWorld world, Vec3d targetPos) {
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.8;
            double height = random.nextDouble() * 1.5;

            world.spawnParticles(
                new DustParticleEffect(new Vector3f(0.15f, 0.5f, 0.1f), 0.9f),
                targetPos.x + offsetX, targetPos.y + height * 0.3, targetPos.z + offsetZ,
                1, 0, 0.1, 0, 0.0
            );
        }

        world.spawnParticles(
            ParticleTypes.COMPOSTER,
            targetPos.x, targetPos.y + 0.5, targetPos.z,
            6, 0.4, 0.2, 0.4, 0.01
        );

        world.spawnParticles(
            new DustParticleEffect(new Vector3f(0.9f, 0.4f, 0.6f), 0.5f),
            targetPos.x, targetPos.y + 0.8, targetPos.z,
            3, 0.3, 0.2, 0.3, 0.0
        );
    }

    private void applyNatureSpeedBonus(PlayerEntity player, World world) {
        BlockPos below = player.getBlockPos().down();
        BlockState blockBelow = world.getBlockState(below);

        boolean isNaturalBlock = blockBelow.isIn(BlockTags.DIRT)
            || blockBelow.isIn(BlockTags.SAND)
            || blockBelow.isIn(BlockTags.LEAVES)
            || blockBelow.isIn(BlockTags.FLOWERS)
            || blockBelow.isIn(BlockTags.MOSS_REPLACEABLE);

        if (isNaturalBlock) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SPEED, 30, 0, false, false, false
            ));
        }
    }

    private void checkEmergencyProtection(PlayerEntity player, World world, ItemStack stack) {
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent >= EMERGENCY_HEALTH_THRESHOLD) {
            return;
        }

        long lastEmergency = stack.getOrCreateNbt().getLong(EMERGENCY_COOLDOWN_TAG);
        long currentTime = world.getTime();
        if (currentTime - lastEmergency < EMERGENCY_COOLDOWN_TICKS) {
            return;
        }

        stack.getOrCreateNbt().putLong(EMERGENCY_COOLDOWN_TAG, currentTime);

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Box pushBox = player.getBoundingBox().expand(EMERGENCY_PUSH_RADIUS);
        List<HostileEntity> nearbyHostiles = world.getEntitiesByClass(
            HostileEntity.class, pushBox, LivingEntity::isAlive
        );

        Vec3d playerPos = player.getPos();

        for (HostileEntity hostile : nearbyHostiles) {
            Vec3d pushDir = hostile.getPos().subtract(playerPos).normalize();
            hostile.setVelocity(
                pushDir.x * EMERGENCY_PUSH_FORCE,
                0.4,
                pushDir.z * EMERGENCY_PUSH_FORCE
            );
            hostile.velocityModified = true;

            hostile.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, EMERGENCY_SLOWNESS_DURATION, 1, false, false, false
            ));
        }

        serverWorld.spawnParticles(
            ParticleTypes.HAPPY_VILLAGER,
            playerPos.x, playerPos.y + 1.0, playerPos.z,
            40, 3.0, 1.0, 3.0, 0.1
        );

        serverWorld.spawnParticles(
            new DustParticleEffect(new Vector3f(0.1f, 0.9f, 0.2f), 1.2f),
            playerPos.x, playerPos.y + 1.0, playerPos.z,
            30, 2.5, 0.8, 2.5, 0.05
        );

        serverWorld.spawnParticles(
            ParticleTypes.COMPOSTER,
            playerPos.x, playerPos.y + 0.5, playerPos.z,
            20, 2.0, 0.5, 2.0, 0.03
        );

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.PLAYERS,
            1.5f, 0.5f);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK, SoundCategory.PLAYERS,
            0.8f, 1.2f);

        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.REGENERATION, 60, 1, false, false, true
        ));

        FiwUtils.sendWarningMessage(player, "La naturaleza te protege en tu momento de necesidad...");
    }

    /**
     * Maneja la Bendición Vital cuando un mob muere.
     * Usa TrinketHelper para detectar en cualquier slot.
     */
    public static void onMobKilledByPlayer(PlayerEntity player, LivingEntity killed) {
        if (player.getWorld().isClient()) {
            return;
        }

        // Verificar si tiene el amuleto (manos o trinket)
        boolean hasFlower = TrinketHelper.hasArtifactOfType(player, GoddessFlowerArtifact.class);
        if (!hasFlower) {
            return;
        }

        player.heal(VITAL_BLESSING_HEAL);

        if (player.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1.0, player.getZ(),
                6, 0.3, 0.3, 0.3, 0.02
            );
        }
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH,
            new EntityAttributeModifier(uuid, "Goddess Flower max health", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(uuid, "Goddess Flower poison resist", 3.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
            new EntityAttributeModifier(uuid, "Goddess Flower nature speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }
}
