package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PharaohDaggerArtifact extends SwordItem {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID LUCK_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");
    
    // Material personalizado para la daga con 3000 de durabilidad
    private static final ToolMaterial PHARAOH_DAGGER_MATERIAL = new ToolMaterial() {
        @Override
        public int getDurability() {
            return 3000; // Durabilidad muy alta
        }
        
        @Override
        public float getMiningSpeedMultiplier() {
            return 1.5f; // Velocidad de minería decente
        }
        
        @Override
        public float getAttackDamage() {
            return 0f; // El daño base se define en el constructor de SwordItem
        }
        
        @Override
        public int getMiningLevel() {
            return 2; // Nivel de minería de hierro
        }
        
        @Override
        public int getEnchantability() {
            return 15; // Encantabilidad media
        }
        
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(net.minecraft.item.Items.GOLD_INGOT); // Se repara con oro
        }
    };

    public PharaohDaggerArtifact(Settings settings) {
        // 4 de daño base (como una espada de hierro) y -2.4 de velocidad de ataque (más rápido)
        super(PHARAOH_DAGGER_MATERIAL, 4, -2.4f, settings.maxCount(1).fireproof());
    }

    @Override
    public boolean isDamageable() {
        return true; // Ahora tiene durabilidad
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true; // Se puede encantar
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        // Crear un nuevo Multimap mutable en lugar de modificar el inmutable
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        
        // Primero agregar los modificadores base de SwordItem
        Multimap<EntityAttribute, EntityAttributeModifier> baseModifiers = super.getAttributeModifiers(stack, slot);
        if (baseModifiers != null) {
            modifiers.putAll(baseModifiers);
        }
        
        if (slot == EquipmentSlot.MAINHAND) {
            // MOVEMENT_SPEED: +5% (ágil como una daga)
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "Dagger movement speed", 0.05,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // LUCK: +5 (daga de la fortuna)
            modifiers.put(EntityAttributes.GENERIC_LUCK,
                new EntityAttributeModifier(LUCK_MODIFIER_ID, "Dagger luck", 5.0,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }
    
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Consumir 1 punto de durabilidad al golpear
        stack.damage(1, attacker, (e) -> {
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Daga del Faraón»").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Hoja sagrada que corta el destino").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§6§oArtefacto Legendario - Arma§r").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("§7• Daño: 4§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Velocidad: Muy rápida§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Durabilidad: 3000§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Fortuna en cada golpe§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Velocidad de las arenas§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La daga que corta más que carne»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}