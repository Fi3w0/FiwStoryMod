package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwNBT;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HachaRelampagoArtifact extends BaseArtifactSwordItem {

    // playerUUID -> expiry time in millis for lightning immunity
    private static final Map<UUID, Long> lightningImmune = new ConcurrentHashMap<>();

    private static final UUID SPEED_UUID = UUID.fromString("E5F6A7B8-C9D0-1234-EF56-7890123456AB");
    private static final UUID ARMOR_UUID = UUID.fromString("F6A7B8C9-D0E1-2345-F067-890123456ABC");
    private static final UUID ATK_UUID   = UUID.fromString("A7B8C9D0-E1F2-3456-0178-90123456ABCD");

    private static final ToolMaterial HACHA_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()              { return 2500; }
        @Override public float getMiningSpeedMultiplier() { return 1.5f; }
        @Override public float getAttackDamage()          { return 0f;   }
        @Override public int getMiningLevel()             { return 3;    }
        @Override public int getEnchantability()          { return 10;   }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
        }
    };

    public HachaRelampagoArtifact(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(HACHA_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.EPIC,
              2, 0,
              settings.maxDamage(2500));
    }

    @Override public String getArtifactDisplayName() { return "Hacha de Relámpago"; }
    @Override public String getArtifactDescription()  { return "Memoria del Pasado - Dios del Trueno"; }
    @Override public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Eco del trueno de otro universo",
            "Perdió su poder, pero sigue siendo peligrosa",
            "Los ecos del trueno nunca callan"
        );
    }
    @Override public String getArtifactQuote() { return "Los ecos del trueno nunca callan"; }

    // ========== ATTRIBUTE MODIFIERS: +7% Speed, +1 Armor, +1 ATK en mainhand ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            HashMultimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(SPEED_UUID, "Hacha speed", 0.07,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            map.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_UUID, "Hacha armor", 1.0,
                    EntityAttributeModifier.Operation.ADDITION));
            map.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATK_UUID, "Hacha attack bonus", 1.0,
                    EntityAttributeModifier.Operation.ADDITION));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== LIGHTNING STRIKE: passive every 3 hits ==========

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient() && attacker instanceof PlayerEntity player) {
            int hits = FiwNBT.getInt(stack, "hit_count", 0) + 1;

            if (hits >= 3) {
                FiwNBT.setInt(stack, "hit_count", 0);

                if (attacker.getWorld() instanceof ServerWorld serverWorld) {
                    // Grant lightning immunity to player
                    lightningImmune.put(player.getUuid(), System.currentTimeMillis() + 2000L);

                    // Spawn real lightning at target
                    LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
                    bolt.setPosition(target.getX(), target.getY(), target.getZ());
                    serverWorld.spawnEntity(bolt);

                    // Thunder sound
                    serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            } else {
                FiwNBT.setInt(stack, "hit_count", hits);

                // Building-up ELECTRIC_SPARK particles on hit 1 and 2
                if (attacker.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        attacker.getX(), attacker.getY() + 1.0, attacker.getZ(),
                        5 * hits, 0.3, 0.3, 0.3, 0.1);
                    serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS,
                        0.4f + 0.1f * hits, 1.2f + 0.2f * hits);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    // Cancel lightning damage to the player who holds this axe during immunity window
    public static void registerDamageEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getWorld().isClient()) return true;
            if (!(entity instanceof PlayerEntity player)) return true;
            if (!source.isOf(DamageTypes.LIGHTNING_BOLT)) return true;

            Long expiry = lightningImmune.get(player.getUuid());
            if (expiry != null && System.currentTimeMillis() < expiry) {
                return false; // Cancel lightning damage
            }
            return true;
        });
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // No active ability
    }

    // ========== TOOLTIP ==========

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§6§o«Hacha de Relámpago»§r"));
        tooltip.add(Text.literal("§d§oMemoria del Pasado§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Hacha de una vez un dios de truenos,§r"));
        tooltip.add(Text.literal("§7aunque parece que no es de este§r"));
        tooltip.add(Text.literal("§7universo... Perdió gran parte de su§r"));
        tooltip.add(Text.literal("§7poder, aunque sigue siendo un gran arma.§r"));
        tooltip.add(Text.literal(""));

        int hits = stack.hasNbt() ? FiwNBT.getInt(stack, "hit_count", 0) : 0;
        tooltip.add(Text.literal("§e⚡ Golpes: " + hits + "/3 §8(rayo al 3er golpe)§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«Los ecos del trueno nunca callan»§r"));
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
