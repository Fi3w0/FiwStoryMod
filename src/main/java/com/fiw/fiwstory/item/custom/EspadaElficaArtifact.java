package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EspadaElficaArtifact extends BaseArtifactSwordItem {

    private static final int ABILITY_COOLDOWN_MS = 15000; // 15 segundos

    private static final UUID ARMOR_UUID = UUID.fromString("C3D4E5F6-A7B8-9012-CD34-EF5678901234");
    private static final UUID KB_UUID    = UUID.fromString("D4E5F6A7-B8C9-0123-DE45-F67890123456");

    private static final ToolMaterial ELFICA_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()              { return 3000; }
        @Override public float getMiningSpeedMultiplier() { return 1.5f; }
        @Override public float getAttackDamage()          { return 0f;   }
        @Override public int getMiningLevel()             { return 3;    }
        @Override public int getEnchantability()          { return 15;   }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
        }
    };

    public EspadaElficaArtifact(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(ELFICA_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.LEGENDARY,
              3, 0,
              settings.maxDamage(3000));
    }

    @Override public String getArtifactDisplayName() { return "Espada de Elfo Real"; }
    @Override public String getArtifactDescription()  { return "Espada legendaria de una familia élfica noble"; }
    @Override public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Energía sagrada que fluye por el filo",
            "Más de mil años de historia élfica",
            "La nobleza no se hereda, se forja"
        );
    }
    @Override public String getArtifactQuote() { return "La nobleza no se hereda, se forja"; }

    // ========== ATTRIBUTE MODIFIERS: +2 Armor, +10% KB Resist en mainhand ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            HashMultimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_UUID, "Elfica armor", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            map.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                new EntityAttributeModifier(KB_UUID, "Elfica kb resist", 0.10,
                    EntityAttributeModifier.Operation.ADDITION));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== BENDICIÓN ÉLFICA: Regeneration II (2s) + Chain Lightning ==========

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);
        if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.fail(stack);

        if (!FiwNBT.isCooldownOver(stack, "bendicion")) {
            long remaining = FiwNBT.getCooldownRemaining(stack, "bendicion");
            FiwUtils.sendErrorMessage(player, "Bendición Élfica en cooldown: " +
                FiwUtils.formatTimeSeconds(remaining / 1000.0));
            return TypedActionResult.fail(stack);
        }

        // Regeneration II for 2 seconds (40 ticks)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, false, true));

        // Chain lightning
        FiwEffects.executeChainLightning(serverWorld, player);

        player.sendMessage(Text.literal("§a§l✦ BENDICIÓN ÉLFICA ✦§r"), true);
        FiwNBT.setCooldown(stack, "bendicion", ABILITY_COOLDOWN_MS);
        FiwNBT.incrementUses(stack);
        FiwNBT.setLong(stack, FiwNBT.LAST_USED, System.currentTimeMillis());
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Handled by use()
    }

    // ========== TOOLTIP ==========

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§5§o«Espada de Elfo Real»§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Espada legendaria de la tercera§r"));
        tooltip.add(Text.literal("§7generación de una familia élfica noble.§r"));
        tooltip.add(Text.literal("§7Parece que tiene más de mil años...§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§a§oEnergía sagrada fluye por el filo§r"));
        tooltip.add(Text.literal("§a§ode esta espada§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c[Click D] §7Bendición Élfica §8(15s CD)"));

        long cd = FiwNBT.getCooldownRemaining(stack, "bendicion");
        if (cd > 0) {
            tooltip.add(Text.literal("§7Bendición: " + FiwUtils.formatTimeSeconds(cd / 1000.0) + "§r"));
            tooltip.add(Text.literal(""));
        }

        tooltip.add(Text.literal("§8«La nobleza no se hereda, se forja»§r"));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Has enchant glint
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantability() { return 15; }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.getItem() == com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL;
    }
}
