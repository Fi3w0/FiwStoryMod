package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MagicTomeItem extends Item {

    // UUIDs for attribute modifiers
    private static final UUID OFFHAND_DAMAGE_UUID = UUID.fromString("F6A7B8C9-D0E1-4234-FA56-BC78DE901234");
    private static final UUID OFFHAND_SPEED_UUID = UUID.fromString("F7B8C9D0-E1F2-4345-AB67-CD89EF012345");
    private static final UUID ARMOR_PEN_UUID = UUID.fromString("11223344-5566-4778-8899-AABBCCDDEEFF");
    private static final UUID MARCA_ARMOR_UUID = UUID.fromString("22334455-6677-4889-99AA-BBCCDDEEFF00");
    private static final UUID MARCA_TOUGHNESS_UUID = UUID.fromString("33445566-7788-4990-AABB-CCDDEEFF0011");

    // Cooldowns in ticks
    private static final int DESMANTELAR_CD = 60;   // 3 seconds
    private static final int MARCA_CD = 240;         // 12 seconds
    private static final int DOMINIO_CD = 360;       // 18 seconds

    // Durations in ticks
    private static final int MARCA_DURATION = 120;   // 6 seconds
    private static final int DOMINIO_DURATION = 60;  // 3 seconds

    // Damage values
    private static final float DESMANTELAR_DAMAGE = 10.0f;
    private static final float DESMANTELAR_RANGE = 10.0f;
    private static final float ARMOR_PENETRATION = 5.0f;

    // Tracking active effects (entity UUID -> expiry world time)
    private static final Map<UUID, Long> markedEntities = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> dominioPlayers = new ConcurrentHashMap<>();

    // Anti-recursion flag for damage events
    private static final ThreadLocal<Boolean> PROCESSING_BONUS = ThreadLocal.withInitial(() -> false);

    public MagicTomeItem(Settings settings) {
        super(settings.maxCount(1).fireproof());
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
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    // ========== PASSIVE ATTRIBUTES (OFFHAND) ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.OFFHAND) {
            Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
            // +2 Daño de ataque
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(OFFHAND_DAMAGE_UUID, "Tomo attack damage", 2.0, EntityAttributeModifier.Operation.ADDITION));
            // +5% velocidad de movimiento
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(OFFHAND_SPEED_UUID, "Tomo movement speed", 0.05, EntityAttributeModifier.Operation.MULTIPLY_BASE));
            return modifiers;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== ABILITIES ==========
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient()) return TypedActionResult.pass(stack);

        long worldTime = world.getTime();

        if (hand == Hand.MAIN_HAND) {
            if (player.isSneaking()) {
                return useMarcaCorrupta(world, player, stack, worldTime);
            } else {
                return useDesmantelar(world, player, stack, worldTime);
            }
        } else {
            // Offhand: Dominio Fracturado
            return useDominioFracturado(world, player, stack, worldTime);
        }
    }

    // ========== HABILIDAD 1: DESMANTELAR (Sukuna-style slash) ==========
    private TypedActionResult<ItemStack> useDesmantelar(World world, PlayerEntity player, ItemStack stack, long worldTime) {
        if (isOnCooldown(stack, "desmantelar", worldTime)) {
            return TypedActionResult.fail(stack);
        }

        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        ServerWorld serverWorld = (ServerWorld) world;

        // Calculate perpendicular vectors for the X-slash pattern
        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d right = lookDir.crossProduct(up).normalize();
        Vec3d slashUp = lookDir.crossProduct(right).normalize();

        // Spawn Sukuna-style X slash particles (two crossing diagonal lines)
        float slashWidth = 2.5f; // Width of the slash at max range
        for (int i = 0; i < 50; i++) {
            double t = i / 50.0;
            double dist = t * DESMANTELAR_RANGE;
            Vec3d centerPoint = eyePos.add(lookDir.multiply(dist));

            // Diagonal slash 1: top-left to bottom-right
            double offset1 = (t - 0.5) * slashWidth;
            Vec3d slash1Pos = centerPoint.add(right.multiply(offset1)).add(slashUp.multiply(-offset1));
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.6f, 0.0f, 0.9f), 1.5f),
                slash1Pos.x, slash1Pos.y, slash1Pos.z,
                1, 0.02, 0.02, 0.02, 0.0
            );

            // Diagonal slash 2: bottom-left to top-right
            Vec3d slash2Pos = centerPoint.add(right.multiply(-offset1)).add(slashUp.multiply(-offset1));
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.4f, 0.0f, 0.7f), 1.5f),
                slash2Pos.x, slash2Pos.y, slash2Pos.z,
                1, 0.02, 0.02, 0.02, 0.0
            );
        }

        // Central energy line (brighter, thicker)
        for (int i = 0; i < 40; i++) {
            double dist = (i / 40.0) * DESMANTELAR_RANGE;
            Vec3d particlePos = eyePos.add(lookDir.multiply(dist));
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.8f, 0.2f, 1.0f), 1.8f),
                particlePos.x, particlePos.y, particlePos.z,
                1, 0.05, 0.05, 0.05, 0.0
            );
        }

        // Burst particles at origin (the "cast point")
        serverWorld.spawnParticles(
            new DustParticleEffect(new Vector3f(0.7f, 0.1f, 0.95f), 2.0f),
            eyePos.x + lookDir.x, eyePos.y + lookDir.y, eyePos.z + lookDir.z,
            20, 0.3, 0.3, 0.3, 0.05
        );

        // End-point slash burst
        Vec3d endPoint = eyePos.add(lookDir.multiply(DESMANTELAR_RANGE));
        serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,
            endPoint.x, endPoint.y, endPoint.z,
            15, 0.5, 0.5, 0.5, 0.1
        );

        // Sounds: sharp slash + ender dragon growl for impact
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5f, 0.5f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 0.3f, 2.0f);

        // Damage entities in the slash corridor (wider hitbox than before)
        Box searchBox = new Box(eyePos, eyePos.add(lookDir.multiply(DESMANTELAR_RANGE))).expand(2.5);

        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive())) {
            Vec3d toEntity = target.getPos().add(0, target.getHeight() / 2, 0).subtract(eyePos);
            double proj = toEntity.dotProduct(lookDir);
            if (proj < 0 || proj > DESMANTELAR_RANGE) continue;

            Vec3d closest = eyePos.add(lookDir.multiply(proj));
            double perpDist = closest.distanceTo(target.getPos().add(0, target.getHeight() / 2, 0));
            if (perpDist > 2.0) continue;

            // Apply armor penetration
            var armorAttr = target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (armorAttr != null) {
                armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                    ARMOR_PEN_UUID, "Desmantelar penetration", -ARMOR_PENETRATION, EntityAttributeModifier.Operation.ADDITION));
            }

            target.damage(player.getDamageSources().playerAttack(player), DESMANTELAR_DAMAGE);

            if (armorAttr != null) {
                armorAttr.removeModifier(ARMOR_PEN_UUID);
            }

            // Sukuna-style hit particles: cross-shaped burst on target
            double tx = target.getX();
            double ty = target.getY() + target.getHeight() / 2;
            double tz = target.getZ();
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.7f, 0.0f, 1.0f), 1.4f),
                tx, ty, tz, 20, 0.4, 0.4, 0.4, 0.08
            );
            serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                tx, ty, tz, 10, 0.3, 0.3, 0.3, 0.15);
        }

        setCooldown(stack, "desmantelar", worldTime, DESMANTELAR_CD);
        player.getItemCooldownManager().set(this, DESMANTELAR_CD);
        return TypedActionResult.success(stack);
    }

    // ========== HABILIDAD 2: MARCA CORRUPTA ==========
    private TypedActionResult<ItemStack> useMarcaCorrupta(World world, PlayerEntity player, ItemStack stack, long worldTime) {
        if (isOnCooldown(stack, "marca", worldTime)) {
            return TypedActionResult.fail(stack);
        }

        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        ServerWorld serverWorld = (ServerWorld) world;

        // Find target entity via raycast (16 block range)
        LivingEntity target = null;
        double closestDist = 16.0;
        Box searchBox = player.getBoundingBox().expand(16.0);

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive())) {
            Vec3d toEntity = entity.getPos().add(0, entity.getHeight() / 2, 0).subtract(eyePos);
            double proj = toEntity.dotProduct(lookDir);
            if (proj < 0 || proj > 16.0) continue;

            Vec3d closest = eyePos.add(lookDir.multiply(proj));
            double perpDist = closest.distanceTo(entity.getPos().add(0, entity.getHeight() / 2, 0));
            if (perpDist > 2.0) continue;

            if (proj < closestDist) {
                closestDist = proj;
                target = entity;
            }
        }

        if (target == null) {
            return TypedActionResult.fail(stack);
        }

        // Mark the target
        markedEntities.put(target.getUuid(), worldTime + MARCA_DURATION);

        // Apply armor debuff (-2 armor, -1 armor toughness)
        var armorAttr = target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(MARCA_ARMOR_UUID);
            armorAttr.addTemporaryModifier(new EntityAttributeModifier(
                MARCA_ARMOR_UUID, "Marca corrupta armor", -2.0, EntityAttributeModifier.Operation.ADDITION));
        }
        var toughnessAttr = target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        if (toughnessAttr != null) {
            toughnessAttr.removeModifier(MARCA_TOUGHNESS_UUID);
            toughnessAttr.addTemporaryModifier(new EntityAttributeModifier(
                MARCA_TOUGHNESS_UUID, "Marca corrupta toughness", -1.0, EntityAttributeModifier.Operation.ADDITION));
        }

        // Apply Glowing effect (visible through walls)
        target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.GLOWING, MARCA_DURATION, 0, false, false, true));

        // Visual and sound feedback
        serverWorld.spawnParticles(
            new DustParticleEffect(new Vector3f(0.6f, 0.0f, 0.9f), 1.5f),
            target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
            30, 0.5, 0.5, 0.5, 0.05);

        world.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 0.8f, 1.2f);

        player.sendMessage(Text.literal("§5✦ Marca Corrupta aplicada").formatted(Formatting.DARK_PURPLE), true);

        setCooldown(stack, "marca", worldTime, MARCA_CD);
        return TypedActionResult.success(stack);
    }

    // ========== HABILIDAD 3: DOMINIO FRACTURADO ==========
    private TypedActionResult<ItemStack> useDominioFracturado(World world, PlayerEntity player, ItemStack stack, long worldTime) {
        if (isOnCooldown(stack, "dominio", worldTime)) {
            return TypedActionResult.fail(stack);
        }

        // Activate damage reflection for 3 seconds
        dominioPlayers.put(player.getUuid(), worldTime + DOMINIO_DURATION);

        // Apply Resistance I for visual feedback
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.RESISTANCE, DOMINIO_DURATION, 0, false, true, true));

        // Visual: purple shield particles
        ServerWorld serverWorld = (ServerWorld) world;
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double x = player.getX() + Math.cos(angle) * 1.5;
            double z = player.getZ() + Math.sin(angle) * 1.5;
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.4f, 0.0f, 0.7f), 1.0f),
                x, player.getY() + 1.0, z,
                3, 0.05, 0.5, 0.05, 0.01);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 1.0f, 1.5f);

        player.sendMessage(Text.literal("§5✦ Dominio Fracturado activado").formatted(Formatting.DARK_PURPLE), true);

        setCooldown(stack, "dominio", worldTime, DOMINIO_CD);
        return TypedActionResult.success(stack);
    }

    // ========== INVENTORY TICK (for cleaning up marca effects) ==========
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity)) return;

        long worldTime = world.getTime();

        // Clean up expired Marca Corrupta armor debuffs every second
        if (worldTime % 20 == 0) {
            cleanupExpiredMarks(world, worldTime);
        }
    }

    private static void cleanupExpiredMarks(World world, long worldTime) {
        markedEntities.entrySet().removeIf(entry -> {
            if (worldTime > entry.getValue()) {
                // Try to remove armor debuffs from the entity
                if (world instanceof ServerWorld serverWorld) {
                    Entity entity = serverWorld.getEntity(entry.getKey());
                    if (entity instanceof LivingEntity living) {
                        var armorAttr = living.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
                        if (armorAttr != null) armorAttr.removeModifier(MARCA_ARMOR_UUID);
                        var toughnessAttr = living.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
                        if (toughnessAttr != null) toughnessAttr.removeModifier(MARCA_TOUGHNESS_UUID);
                    }
                }
                return true;
            }
            return false;
        });
        dominioPlayers.entrySet().removeIf(entry -> worldTime > entry.getValue());
    }

    // ========== COOLDOWN HELPERS ==========
    private boolean isOnCooldown(ItemStack stack, String ability, long worldTime) {
        var nbt = stack.getOrCreateNbt();
        long lastUse = nbt.getLong("cd_" + ability);
        int cdTicks = switch (ability) {
            case "desmantelar" -> DESMANTELAR_CD;
            case "marca" -> MARCA_CD;
            case "dominio" -> DOMINIO_CD;
            default -> 0;
        };
        return (worldTime - lastUse) < cdTicks;
    }

    private void setCooldown(ItemStack stack, String ability, long worldTime, int cdTicks) {
        stack.getOrCreateNbt().putLong("cd_" + ability, worldTime);
    }

    // ========== TOOLTIP ==========
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Tomo del Eco Corrupto»").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Un vestigio del universo anterior").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oContiene una fracción del poder corrupto de Fi3w0§r"));
        tooltip.add(Text.literal("§5§oUn eco de su antigua divinidad§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Lo que sostienes no es un arma.§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7Solo sientes cómo la realidad cede ante ti.§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§d§lHabilidades:§r"));
        tooltip.add(Text.literal("§7• §5Click derecho§7 — Desmantelar §8(3s CD)§r"));
        tooltip.add(Text.literal("§7  Slash cortante 10 bloques, 10 daño (5 ♥), penetra armadura§r"));
        tooltip.add(Text.literal("§7• §5Agachado + Click§7 — Marca Corrupta §8(12s CD)§r"));
        tooltip.add(Text.literal("§7  Marca objetivo 6s, +daño recibido§r"));
        tooltip.add(Text.literal("§7• §5Offhand Click§7 — Dominio Fracturado §8(18s CD)§r"));
        tooltip.add(Text.literal("§7  3s refleja 30% daño como daño mágico§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6Pasivo (Offhand):§r"));
        tooltip.add(Text.literal("§7• +2 Daño de ataque§r"));
        tooltip.add(Text.literal("§7• +5% Velocidad de movimiento§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La realidad cede ante el eco del dios caído»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    // ========== DAMAGE EVENT REGISTRATION ==========
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (PROCESSING_BONUS.get()) return true;

            long worldTime = entity.getWorld().getTime();

            // Marca Corrupta: bonus damage to marked entities
            Long markExpiry = markedEntities.get(entity.getUuid());
            if (markExpiry != null && worldTime <= markExpiry) {
                float bonus = amount * 0.10f;
                if (bonus > 0.5f) {
                    PROCESSING_BONUS.set(true);
                    try {
                        entity.damage(entity.getDamageSources().magic(), bonus);
                    } finally {
                        PROCESSING_BONUS.set(false);
                    }

                    // Purple particles on hit
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        sw.spawnParticles(
                            new DustParticleEffect(new Vector3f(0.6f, 0.0f, 0.9f), 0.8f),
                            entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(),
                            8, 0.3, 0.3, 0.3, 0.02);
                    }
                }
            }

            // Dominio Fracturado: reflect 30% damage
            if (entity instanceof PlayerEntity player) {
                Long dominioExpiry = dominioPlayers.get(player.getUuid());
                if (dominioExpiry != null && worldTime <= dominioExpiry) {
                    if (source.getAttacker() instanceof LivingEntity attacker && attacker != player) {
                        float reflected = amount * 0.30f;
                        if (reflected > 0.5f) {
                            PROCESSING_BONUS.set(true);
                            try {
                                attacker.damage(player.getDamageSources().magic(), reflected);
                            } finally {
                                PROCESSING_BONUS.set(false);
                            }

                            // Reflection particles
                            if (entity.getWorld() instanceof ServerWorld sw) {
                                sw.spawnParticles(
                                    new DustParticleEffect(new Vector3f(0.4f, 0.0f, 0.7f), 1.0f),
                                    attacker.getX(), attacker.getY() + attacker.getHeight() / 2, attacker.getZ(),
                                    10, 0.3, 0.3, 0.3, 0.05);
                            }
                        }
                    }
                }
            }

            return true;
        });
    }

    // ========== STATIC CLEANUP ==========
    public static void tickEffects(long worldTime) {
        markedEntities.entrySet().removeIf(e -> worldTime > e.getValue());
        dominioPlayers.entrySet().removeIf(e -> worldTime > e.getValue());
    }
}
