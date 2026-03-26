package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwNBT;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import net.minecraft.util.Formatting;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verdugo del Trono — Espada LEGENDARIA
 * "Forjada para ejecutar la voluntad del rey. No conoce piedad, solo mandato."
 *
 * Activo — Sentencia Final (clic derecho, CD 20s):
 *   Condena al mob que miras a 10 bloques:
 *   → Slowness II durante 8s sobre el objetivo
 *   → Tu siguiente golpe contra él ignora armadura (+6 daño mágico)
 *
 * Pasiva — Ejecutor:
 *   Al matar un mob con esta espada → Speed I + Strength I durante 8s.
 */
public class VerdugoDeTronoArtifact extends BaseArtifactSwordItem {

    private static final long  SENTENCE_CD_MS     = 20_000L;
    private static final long  CONDEMN_TICKS      = 160L;    // 8 segundos
    private static final float IGNORE_ARMOR_BONUS = 6.0f;

    // playerUUID → condemned targetUUID
    private static final Map<UUID, UUID> condemnedTargets = new ConcurrentHashMap<>();
    // playerUUID → expiry world tick
    private static final Map<UUID, Long> condemnExpiry    = new ConcurrentHashMap<>();
    // anti-recursión para bonus de daño
    private static final Set<UUID>       processingBonus  = ConcurrentHashMap.newKeySet();

    private static final UUID SPEED_UUID = UUID.fromString("E1F2A3B4-C5D6-4789-EFAB-234567890123");
    private static final UUID ARMOR_UUID = UUID.fromString("F2A3B4C5-D6E7-4890-FABC-345678901234");

    private static final ToolMaterial TRONO_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()              { return 4000; }
        @Override public float getMiningSpeedMultiplier() { return 1.5f; }
        @Override public float getAttackDamage()          { return 0f;   }
        @Override public int getMiningLevel()             { return 3;    }
        @Override public int getEnchantability()          { return 10;   }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public VerdugoDeTronoArtifact(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(TRONO_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.LEGENDARY,
              0, 0,
              settings.maxDamage(4000));
    }

    @Override public boolean isDamageable()                 { return true; }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean hasGlint(ItemStack stack)      { return true; }

    @Override public String getArtifactDisplayName() { return "Verdugo del Trono"; }
    @Override public String getArtifactDescription()  { return "Forjada para ejecutar la voluntad del rey"; }
    @Override public String getArtifactQuote()        { return "No juzga. Solo ejecuta la sentencia del trono."; }

    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "§6Activo§r — §cSentencia Final§r: condena al mob que miras (10 bloques)",
            "§7Objetivo: Slowness II 8s + siguiente golpe ignora armadura§r",
            "§8CD: 20s§r",
            "§6Pasiva§r — §aEjecutor§r: al matar → Speed I + Strength I durante 8s"
        );
    }

    // ========== ACTIVO: Sentencia Final ==========

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(player.getStackInHand(hand));
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);
        if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.fail(stack);
        if (player.isSneaking()) return TypedActionResult.pass(stack);

        if (!FiwNBT.isCooldownOver(stack, "sentencia_cd")) {
            long remaining = FiwNBT.getCooldownRemaining(stack, "sentencia_cd");
            player.sendMessage(Text.literal("§cSentencia Final — " + (remaining / 1000) + "s restantes"), true);
            return TypedActionResult.fail(stack);
        }

        LivingEntity target = raycastTarget(world, player, 10.0);
        if (target == null) {
            player.sendMessage(Text.literal("§7Sin objetivo en rango"), true);
            return TypedActionResult.pass(stack);
        }

        // Condenar objetivo
        UUID playerUuid = player.getUuid();
        condemnedTargets.put(playerUuid, target.getUuid());
        condemnExpiry.put(playerUuid, world.getTime() + CONDEMN_TICKS);

        // Slowness II al objetivo
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1, false, true, true));

        // Corona de partículas doradas sobre el objetivo
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            serverWorld.spawnParticles(
                new DustParticleEffect(new Vector3f(0.9f, 0.7f, 0.1f), 1.0f),
                target.getX() + Math.cos(angle) * 0.6,
                target.getY() + target.getHeight() / 2.0,
                target.getZ() + Math.sin(angle) * 0.6,
                1, 0.02, 0.05, 0.02, 0.0);
        }
        serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
            target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
            10, 0.3, 0.3, 0.3, 0.05);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.PLAYERS, 0.7f, 1.2f);
        player.sendMessage(Text.literal("§6⚖ Sentencia impuesta"), true);

        FiwNBT.setCooldown(stack, "sentencia_cd", SENTENCE_CD_MS);
        return TypedActionResult.success(stack, false);
    }

    // ========== PASIVA: Ejecutor (detección de muerte) ==========

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player && !player.getWorld().isClient()) {
            if (target.getHealth() <= 0 || target.isDead()) {
                // Limpiar condena del jugador si coincide
                condemnedTargets.remove(player.getUuid());
                condemnExpiry.remove(player.getUuid());

                // Buff Ejecutor
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,    160, 0, false, true, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 160, 0, false, true, true));

                if (player.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        12, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    // ========== DAMAGE EVENT: ignora armadura contra condenados ==========

    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!(attacker.getMainHandStack().getItem() instanceof VerdugoDeTronoArtifact)) return true;

            UUID playerUuid    = attacker.getUuid();
            UUID condemnedUuid = condemnedTargets.get(playerUuid);
            if (condemnedUuid == null || !condemnedUuid.equals(entity.getUuid())) return true;

            Long expiry = condemnExpiry.get(playerUuid);
            if (expiry != null && entity.getWorld().getTime() > expiry) {
                condemnedTargets.remove(playerUuid);
                condemnExpiry.remove(playerUuid);
                return true;
            }

            UUID entityUuid = entity.getUuid();
            if (processingBonus.contains(entityUuid)) return true;
            processingBonus.add(entityUuid);
            try {
                entity.damage(entity.getDamageSources().magic(), IGNORE_ARMOR_BONUS);
            } finally {
                processingBonus.remove(entityUuid);
            }

            // Consumir la condena tras el golpe de ejecución
            condemnedTargets.remove(playerUuid);
            condemnExpiry.remove(playerUuid);

            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(new DustParticleEffect(new Vector3f(0.9f, 0.7f, 0.1f), 1.2f),
                    entity.getX(), entity.getY() + entity.getHeight() / 2.0, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
            }
            return true;
        });
    }

    // ========== INVENTARIO: limpiar condenas expiradas ==========

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity player)) return;
        UUID playerUuid = player.getUuid();
        Long expiry = condemnExpiry.get(playerUuid);
        if (expiry != null && world.getTime() > expiry) {
            condemnedTargets.remove(playerUuid);
            condemnExpiry.remove(playerUuid);
        }
    }

    // ========== ATRIBUTOS ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            Multimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_UUID, "Verdugo armor", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            map.put(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(SPEED_UUID, "Verdugo speed", 0.03,
                    EntityAttributeModifier.Operation.MULTIPLY_BASE));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== TOOLTIP ==========

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Verdugo del Trono»").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Forjada para ejecutar la voluntad del rey").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("No conoce piedad, solo mandato").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6■ Sentencia Final §8[CD: 20s]§r"));
        tooltip.add(Text.literal("§7  Condena al mob en 10 bloques: Slowness II 8s§r"));
        tooltip.add(Text.literal("§7  Siguiente golpe ignora su armadura§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6■ Ejecutor §7(pasiva)§r"));
        tooltip.add(Text.literal("§7  Al matar → Speed I + Strength I (8s)§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«No juzga. Solo ejecuta la sentencia del trono.»").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    // ========== UTILIDAD ==========

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {}

    private LivingEntity raycastTarget(World world, PlayerEntity player, double range) {
        Vec3d eyePos  = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d endPos  = eyePos.add(lookVec.multiply(range));
        Box   searchBox = player.getBoundingBox().expand(range);

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, searchBox,
                en -> en != player && en.isAlive())) {
            Box hitBox = e.getBoundingBox().expand(0.3);
            if (hitBox.raycast(eyePos, endPos).isPresent()) {
                double dist = eyePos.squaredDistanceTo(e.getPos());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = e;
                }
            }
        }
        return closest;
    }
}
