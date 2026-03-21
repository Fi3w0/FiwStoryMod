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

public class EspadaFrostmornArtifact extends BaseArtifactSwordItem {

    private static final int ABILITY_COOLDOWN_MS = 15000; // 15 segundos

    private static final UUID ARMOR_UUID = UUID.fromString("B8C9D0E1-F2A3-4567-BC89-0123456789DE");
    private static final UUID SPEED_UUID = UUID.fromString("C9D0E1F2-A3B4-5678-CD90-123456789EF0");
    private static final UUID KB_UUID    = UUID.fromString("D0E1F2A3-B4C5-6789-DE01-23456789F012");

    private static final ToolMaterial FROSTMORN_MATERIAL = new ToolMaterial() {
        @Override public int getDurability()              { return 3000; }
        @Override public float getMiningSpeedMultiplier() { return 1.5f; }
        @Override public float getAttackDamage()          { return 0f;   }
        @Override public int getMiningLevel()             { return 3;    }
        @Override public int getEnchantability()          { return 10;   }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
        }
    };

    public EspadaFrostmornArtifact(ToolMaterial ignored, int attackDamage, float attackSpeed, Settings settings) {
        super(FROSTMORN_MATERIAL, attackDamage, attackSpeed,
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.LEGENDARY,
              3, 0,
              settings.maxDamage(3000));
    }

    @Override public String getArtifactDisplayName() { return "FrostMorn"; }
    @Override public String getArtifactDescription()  { return "Espada de los terrenos congelados"; }
    @Override public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "El alma del frío nunca muere",
            "Sientes frío al tenerla en la mano",
            "No existe calor que derrita esta hoja"
        );
    }
    @Override public String getArtifactQuote() { return "No existe calor que derrita esta hoja"; }

    // ========== ATTRIBUTE MODIFIERS: -1 Armor, +8% Speed, +20% KB Resist en mainhand ==========

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            HashMultimap<EntityAttribute, EntityAttributeModifier> map =
                HashMultimap.create(super.getAttributeModifiers(slot));
            map.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_UUID, "Frostmorn armor", -1.0,
                    EntityAttributeModifier.Operation.ADDITION));
            map.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(SPEED_UUID, "Frostmorn speed", 0.08,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            map.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                new EntityAttributeModifier(KB_UUID, "Frostmorn kb resist", 0.20,
                    EntityAttributeModifier.Operation.ADDITION));
            return map;
        }
        return super.getAttributeModifiers(slot);
    }

    // ========== FROST BEAM: right click, 15s CD ==========

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);
        if (!(world instanceof ServerWorld serverWorld)) return TypedActionResult.fail(stack);

        if (!FiwNBT.isCooldownOver(stack, "frost_beam")) {
            long remaining = FiwNBT.getCooldownRemaining(stack, "frost_beam");
            FiwUtils.sendErrorMessage(player, "Frost Beam en cooldown: " +
                FiwUtils.formatTimeSeconds(remaining / 1000.0));
            return TypedActionResult.fail(stack);
        }

        FiwEffects.executeFrostBeam(serverWorld, player);
        player.sendMessage(Text.literal("§b§l❄ FROST BEAM ❄§r"), true);
        FiwNBT.setCooldown(stack, "frost_beam", ABILITY_COOLDOWN_MS);
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
        tooltip.add(Text.literal("§b§o«FrostMorn»§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Espada de los terrenos congelados,§r"));
        tooltip.add(Text.literal("§7sientes frío al tenerla en la mano...§r"));
        tooltip.add(Text.literal("§7Parece que el alma del frío nunca muere.§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§3§oEl invierno no perdona§r"));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c[Click D] §7Frost Beam §8(15s CD)"));

        long cd = FiwNBT.getCooldownRemaining(stack, "frost_beam");
        if (cd > 0) {
            tooltip.add(Text.literal("§7Frost Beam: " + FiwUtils.formatTimeSeconds(cd / 1000.0) + "§r"));
            tooltip.add(Text.literal(""));
        }

        tooltip.add(Text.literal("§8«No existe calor que derrita esta hoja»§r"));
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
