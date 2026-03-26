package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hoja de la Purga — Espada MÍTICA
 * «Espada de Yarim — God Hunter»
 *
 * Esta hoja posee capacidad de herir a los inmortales.
 * Eco del universo anterior.
 *
 * Habilidad — Marca del Cazador (clic derecho):
 *   Fase 1: Marca al mob/jugador que miras a 20 bloques con partículas doradas.
 *   Fase 2: Clic derecho de nuevo (en 6s) → teletransporte detrás del objetivo + 8 daño automático.
 *   Si el objetivo muere mientras está marcado → Speed II + Strength I (10s) al jugador.
 *   CD: 15s (comienza tras la cazada o si la marca expira sin uso).
 */
public class HojaDeLaPurgaArtifact extends BaseArtifactSwordItem {

    // Cooldowns
    private static final long  ABILITY_CD_MS   = 15_000L; // 15 segundos
    private static final long  MARK_DURATION   = 120L;    // 6 segundos en ticks
    private static final float MARK_RANGE       = 20.0f;
    private static final float HUNT_DAMAGE      = 8.0f;

    // Estados de la marca: playerUUID → targetUUID / expiryTick
    private static final Map<UUID, UUID> markedTargets = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> markExpiryTick = new ConcurrentHashMap<>();

    private static final UUID SPEED_UUID  = UUID.fromString("D1E2F3A4-B5C6-4789-DEFA-123456789012");
    private static final UUID ARMOR_UUID  = UUID.fromString("F3A4B5C6-D7E8-4901-FABC-345678901234");

    private static final ToolMaterial PURGA_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()               { return 4000; }
        @Override public float getMiningSpeedMultiplier()  { return 1.5f; }
        @Override public float getAttackDamage()           { return 0f;   }
        @Override public int getMiningLevel()              { return 3;    }
        @Override public int getEnchantability()           { return 0;    }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
        }
    };

    public HojaDeLaPurgaArtifact(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(PURGA_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.MYTHIC,
              2, 0,
              settings.maxDamage(4000));
    }

    @Override public String getArtifactDisplayName() { return "Hoja de la Purga"; }
    @Override public String getArtifactDescription()  { return "Espada de Yarim — God Hunter"; }
    @Override public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Esta hoja posee capacidad de herir a los inmortales",
            "Eco del universo anterior",
            "Ningún ser vivo puede huir de quien empuña la Purga"
        );
    }
    @Override public String getArtifactQuote() { return "La inmortalidad es solo una promesa que esta hoja no respeta"; }

    // ========== ATRIBUTOS MAINHAND ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            HashMultimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(SPEED_UUID, "Purga speed", 0.05,
                    EntityAttributeModifier.Operation.MULTIPLY_BASE));
            map.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_UUID, "Purga armor", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== MARCA DEL CAZADOR ==========

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);
        if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.fail(stack);
        if (player.isSneaking()) return TypedActionResult.pass(stack); // shift reservado para futura habilidad

        UUID playerUuid = player.getUuid();
        long worldTime  = world.getTime();

        // ── Fase 2: ya hay marca activa → cazada ──
        UUID targetUuid = markedTargets.get(playerUuid);
        Long expiry     = markExpiryTick.get(playerUuid);

        if (targetUuid != null && expiry != null && worldTime <= expiry) {
            LivingEntity target = findMarkedEntity(world, targetUuid);
            if (target != null && target.isAlive()) {
                executeHunt(serverWorld, player, target, stack);
                return TypedActionResult.success(stack, false);
            }
            // Target desapareció → limpiar marca
            clearMark(playerUuid);
        }

        // ── Fase 1: sin marca → buscar objetivo ──
        if (!FiwNBT.isCooldownOver(stack, "marca")) {
            long rem = FiwNBT.getCooldownRemaining(stack, "marca");
            FiwUtils.sendErrorMessage(player, "Marca del Cazador: " +
                FiwUtils.formatTimeSeconds(rem / 1000.0));
            return TypedActionResult.fail(stack);
        }

        LivingEntity target = raycastTarget(player, serverWorld, MARK_RANGE);
        if (target == null) {
            FiwUtils.sendErrorMessage(player, "Ningún objetivo en rango.");
            return TypedActionResult.fail(stack);
        }

        // Marcar objetivo
        markedTargets.put(playerUuid, target.getUuid());
        markExpiryTick.put(playerUuid, worldTime + MARK_DURATION);

        // Partículas doradas sobre el objetivo
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 1.2f),
                target.getX() + Math.cos(angle) * 0.5,
                target.getY() + target.getHeight() + 0.3,
                target.getZ() + Math.sin(angle) * 0.5,
                1, 0.05, 0.05, 0.05, 0.0);
        }
        serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
            target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
            15, 0.4, 0.4, 0.4, 0.1);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.8f, 1.8f);

        player.sendMessage(Text.literal("§6✦ Objetivo marcado — clic de nuevo para cazar"), true);
        FiwNBT.incrementUses(stack);
        return TypedActionResult.success(stack, false);
    }

    /** Teletransporta al jugador detrás del objetivo y golpea automáticamente. */
    private void executeHunt(ServerWorld world, PlayerEntity player, LivingEntity target, ItemStack stack) {
        // Posición detrás del objetivo (1.5 bloques)
        Vec3d facing = Vec3d.fromPolar(0, target.getYaw());
        Vec3d behind = target.getPos().subtract(facing.multiply(1.5)).add(0, 0.1, 0);

        player.requestTeleport(behind.x, behind.y, behind.z);

        // Daño automático
        target.damage(player.getDamageSources().playerAttack(player), HUNT_DAMAGE);

        // Partículas de cazada
        world.spawnParticles(new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 1.5f),
            target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
            25, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticles(ParticleTypes.CRIT,
            target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
            20, 0.4, 0.4, 0.4, 0.2);

        world.playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.2f, 0.8f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.6f, 1.5f);

        player.sendMessage(Text.literal("§6⚔ Cazada ejecutada"), true);

        clearMark(player.getUuid());
        FiwNBT.setCooldown(stack, "marca", ABILITY_CD_MS);
        FiwNBT.incrementUses(stack);
        FiwNBT.setLong(stack, FiwNBT.LAST_USED, System.currentTimeMillis());
    }

    // ========== INVENTORYTICK: limpiar marcas expiradas + partículas persistentes ==========

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;

        UUID playerUuid = player.getUuid();
        Long expiry = markExpiryTick.get(playerUuid);
        if (expiry == null) return;

        long worldTime = world.getTime();

        // Marca expirada sin cazar → poner cooldown y limpiar
        if (worldTime > expiry) {
            if (FiwNBT.isCooldownOver(stack, "marca")) {
                FiwNBT.setCooldown(stack, "marca", ABILITY_CD_MS);
            }
            clearMark(playerUuid);
            player.sendMessage(Text.literal("§7✦ Marca expirada"), true);
            return;
        }

        // Partículas doradas pulsantes sobre el objetivo mientras está marcado (cada 10 ticks)
        if (worldTime % 10 == 0 && world instanceof ServerWorld serverWorld) {
            UUID targetUuid = markedTargets.get(playerUuid);
            if (targetUuid != null) {
                LivingEntity target = findMarkedEntity(world, targetUuid);
                if (target != null && target.isAlive()) {
                    serverWorld.spawnParticles(
                        new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 1.0f),
                        target.getX(), target.getY() + target.getHeight() + 0.2, target.getZ(),
                        3, 0.3, 0.1, 0.3, 0.02);
                }
            }
        }
    }

    // ========== DAMAGE EVENT: recompensa si el objetivo marcado muere ==========

    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (amount < entity.getHealth()) return true; // no va a morir

            UUID targetUuid = entity.getUuid();

            // Buscar qué jugador tiene marcado este objetivo
            for (Map.Entry<UUID, UUID> entry : markedTargets.entrySet()) {
                if (!entry.getValue().equals(targetUuid)) continue;

                UUID playerUuid = entry.getKey();
                Long expiry = markExpiryTick.get(playerUuid);
                if (expiry == null) continue;
                if (entity.getWorld().getTime() > expiry) continue;

                // Encontrado — dar recompensa al jugador
                entity.getWorld().getPlayers().stream()
                    .filter(p -> p.getUuid().equals(playerUuid))
                    .findFirst()
                    .ifPresent(player -> {
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SPEED, 200, 1, false, true, true));    // Speed II 10s
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.STRENGTH, 200, 0, false, true, true)); // Strength I 10s

                        if (entity.getWorld() instanceof ServerWorld sw) {
                            sw.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                                player.getX(), player.getY() + 1, player.getZ(),
                                30, 0.6, 0.8, 0.6, 0.15);
                        }
                        player.sendMessage(
                            Text.literal("§6§l⚔ PRESA CAZADA — Speed II + Strength I (10s)§r"), true);
                        clearMark(playerUuid);
                    });
                break;
            }
            return true;
        });
    }

    // ========== UTILIDADES ==========

    private static void clearMark(UUID playerUuid) {
        markedTargets.remove(playerUuid);
        markExpiryTick.remove(playerUuid);
    }

    /** Raycast contra entidades vivas en el rango dado. */
    private static LivingEntity raycastTarget(PlayerEntity player, ServerWorld world, float range) {
        Vec3d eyePos  = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        Vec3d endPos  = eyePos.add(lookDir.multiply(range));

        Box searchBox = player.getBoundingBox().expand(range);
        for (LivingEntity candidate : world.getEntitiesByClass(
                LivingEntity.class, searchBox, e -> e != player && e.isAlive())) {
            Box hitBox = candidate.getBoundingBox().expand(0.3);
            if (hitBox.raycast(eyePos, endPos).isPresent()) return candidate;
        }
        return null;
    }

    /** Busca una entidad viva en el mundo por UUID. */
    private static LivingEntity findMarkedEntity(World world, UUID uuid) {
        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
                new Box(-30_000_000, -64, -30_000_000, 30_000_000, 320, 30_000_000), e -> true)) {
            if (e.getUuid().equals(uuid)) return e;
        }
        return null;
    }

    // ========== TOOLTIP ==========

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§d§l«Hoja de la Purga»§r"));
        tooltip.add(Text.literal("§7§oEspada de Yarim — God Hunter§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Esta hoja posee capacidad de herir§r"));
        tooltip.add(Text.literal("§7a los inmortales. Eco del universo anterior.§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6[Click D] §7Marca del Cazador §8(15s CD)"));
        tooltip.add(Text.literal("§7  ▸ Marca objetivo a 20 bloques con partículas doradas"));
        tooltip.add(Text.literal("§7  ▸ Clic de nuevo → teletransporte + 8 daño automático"));
        tooltip.add(Text.literal("§7  ▸ Si el objetivo muere marcado: §6Speed II + Strength I§7 (10s)"));

        long cd = FiwNBT.getCooldownRemaining(stack, "marca");
        if (cd > 0) {
            tooltip.add(Text.literal("§8Marca: " + FiwUtils.formatTimeSeconds(cd / 1000.0) + "§r"));
        }

        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La inmortalidad es solo una promesa que esta hoja no respeta»§r"));
    }

    @Override public boolean hasGlint(ItemStack stack)          { return false; }
    @Override public boolean isEnchantable(ItemStack stack)     { return false; }
    @Override public boolean isDamageable()                     { return true; }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL;
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Gestionado por use()
    }
}
