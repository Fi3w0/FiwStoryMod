package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EspadaMgshtraklar extends BaseArtifactSwordItem {

    private static final int CRIMSON_SLASH_COOLDOWN_MS  = 10000; // 10 segundos
    private static final int LIFESTEAL_COOLDOWN_TICKS   = 40;    // 2 segundos
    private static final int CONFLICT_CHECK_INTERVAL    = 40;    // cada 2s

    private static final Map<UUID, Long> lifeStealCooldowns = new ConcurrentHashMap<>();

    // UUIDs fijos para los modificadores de atributo (mainhand)
    private static final UUID HEALTH_UUID      = UUID.fromString("A1B2C3D4-E5F6-7890-AB12-CD34EF567890");
    private static final UUID EXTRA_ATTACK_UUID = UUID.fromString("B2C3D4E5-F6A7-8901-BC23-DE45F678901A");

    private static final ToolMaterial MGSHTRAKLAR_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()                { return 3000; }
        @Override public float getMiningSpeedMultiplier()   { return 1.5f; }
        @Override public float getAttackDamage()            { return 0f;   }
        @Override public int getMiningLevel()               { return 3;    }
        @Override public int getEnchantability()            { return 10;   }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
        }
    };

    public EspadaMgshtraklar(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(MGSHTRAKLAR_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.LEGENDARY,
              3,
              0,
              settings.maxDamage(3000));
    }

    @Override public String getArtifactDisplayName() { return "Espada Mgshtraklar"; }
    @Override public String getArtifactDescription()  { return "Espada de un dios muerto"; }
    @Override public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Poder que no te pertenece",
            "Magia de sangre ancestral",
            "Sangre divina que nunca se seca"
        );
    }
    @Override public String getArtifactQuote() { return "La sangre divina nunca se seca"; }

    // ========== ATTRIBUTE MODIFIERS: +4 HP, +2 ATK en mainhand ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            HashMultimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(EntityAttributes.GENERIC_MAX_HEALTH,
                new EntityAttributeModifier(HEALTH_UUID, "Mgshtraklar max health",
                    4.0, EntityAttributeModifier.Operation.ADDITION));
            map.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(EXTRA_ATTACK_UUID, "Mgshtraklar attack bonus",
                    2.0, EntityAttributeModifier.Operation.ADDITION));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== BLOOD STEAL: lifesteal pasivo 10%, cooldown 2s ==========

    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(source.getAttacker() instanceof PlayerEntity attacker)) return true;
            if (!(attacker.getMainHandStack().getItem() instanceof EspadaMgshtraklar)) return true;

            UUID uuid = attacker.getUuid();
            long worldTime = entity.getWorld().getTime();
            Long cdExp = lifeStealCooldowns.get(uuid);
            if (cdExp != null && worldTime < cdExp) return true;

            lifeStealCooldowns.put(uuid, worldTime + LIFESTEAL_COOLDOWN_TICKS);
            attacker.heal(amount * 0.10f);

            if (entity.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HEART,
                    attacker.getX(), attacker.getY() + 1.2, attacker.getZ(),
                    2, 0.3, 0.2, 0.3, 0.01);
            }
            return true;
        });
    }

    // ========== CRIMSON SLASH: activo, click derecho, 10s cooldown ==========

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);
        if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.fail(stack);

        if (!FiwNBT.isCooldownOver(stack, "crimson_slash")) {
            long remaining = FiwNBT.getCooldownRemaining(stack, "crimson_slash");
            FiwUtils.sendErrorMessage(player, "Crimson Slash en cooldown: " +
                FiwUtils.formatTimeSeconds(remaining / 1000.0));
            return TypedActionResult.fail(stack);
        }

        FiwEffects.executeCrimsonSlash(serverWorld, player);
        player.sendMessage(Text.literal("§4§l⚔ CRIMSON SLASH ⚔§r"), true);
        FiwNBT.setCooldown(stack, "crimson_slash", CRIMSON_SLASH_COOLDOWN_MS);
        FiwNBT.incrementUses(stack);
        FiwNBT.setLong(stack, FiwNBT.LAST_USED, System.currentTimeMillis());
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Handled by use()
    }

    // ========== INVENTORY TICK: anti-dual check cada 2s ==========

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient() && entity instanceof PlayerEntity player) {
            if (world.getTime() % CONFLICT_CHECK_INTERVAL == 0) {
                if (FiwUtils.hasItemAnywhere(player, BloodGemArtifact.class)) {
                    player.damage(player.getDamageSources().magic(), 2.0f);
                    FiwEffects.spawnParticlesAroundEntity(player, ParticleTypes.SOUL_FIRE_FLAME, 10, 1.0);
                    player.sendMessage(
                        Text.literal("§4§lLa sangre divina rechaza la duplicidad§r"), false);
                }
            }
        }
    }

    // ========== TOOLTIP ==========

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§5§l«Espada Mgshtraklar»§r"));
        tooltip.add(Text.literal("§7Espada de un dios muerto cuyos§r"));
        tooltip.add(Text.literal("§7conocimientos en magia de sangre§r"));
        tooltip.add(Text.literal("§7eran colosales, su sangre sigue en§r"));
        tooltip.add(Text.literal("§7esta espada y sientes que su poder§r"));
        tooltip.add(Text.literal("§7no te pertenece.§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c[Click D] §7Crimson Slash §8(10s CD)"));

        long crimsonCd = FiwNBT.getCooldownRemaining(stack, "crimson_slash");
        if (crimsonCd > 0) {
            tooltip.add(Text.literal("§7Crimson Slash: " +
                FiwUtils.formatTimeSeconds(crimsonCd / 1000.0) + "§r"));
            tooltip.add(Text.literal(""));
        }

        tooltip.add(Text.literal("§8«La sangre divina nunca se seca»§r"));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantability() { return 10; }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL;
    }
}
