package com.fiw.fiwstory.item.custom;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.fiw.fiwstory.item.ModItems;
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
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class Fi3w0GlassesArmor extends ArmorItem {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");

    public Fi3w0GlassesArmor(Type type, Settings settings) {
        super(new CustomArmorMaterial(), type, settings.maxCount(1).fireproof());
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
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        
        if (slot == this.type.getEquipmentSlot()) {
            // MOVEMENT_SPEED: +0.35 MULTIPLY_BASE
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "Fi3w0 glasses movement speed", 0.35,
                    EntityAttributeModifier.Operation.MULTIPLY_BASE));
            
            // ARMOR: +4
            modifiers.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_MODIFIER_ID, "Fi3w0 glasses armor", 4.0,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Gafas de Fi3w0»").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Artefacto que no pertenece ni al tiempo ni al espacio").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oLleno de magia corrupta§r").formatted(Formatting.DARK_PURPLE));
        tooltip.add(Text.literal("§7• Dicen que su portador es un ser superior a los dioses§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Nadie sabe si es verdad§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Estas gafas se sienten muy cómodas§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        
        // Sinergia con la lanza
        tooltip.add(Text.literal("§6§oSinergia con la Lanza Devora Almas§r").formatted(Formatting.GOLD, Formatting.ITALIC));
        tooltip.add(Text.literal("§8• Ataques cargados aumentan alcance§r").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("§8• +25% daño con la lanza equipada§r").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal(""));
        
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("§8«La realidad es solo lo que ves a través de estas gafas»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§d§o Ligada al alma de su portador§r").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            // Verificar si está equipada (slot de armadura de cabeza es 3)
            if (slot == 3) {
                // Efectos visuales sutiles
                if (world.getTime() % 20 == 0) {
                    FiwEffects.spawnParticlesAroundEntity(player, 
                        net.minecraft.particle.ParticleTypes.PORTAL, 2, 0.5);
                }
                
                // Sinergia con la lanza
                if (hasSpearEquipped(player)) {
                    // Efectos adicionales cuando tiene ambas
                    if (world.getTime() % 40 == 0) {
                        FiwEffects.spawnParticlesAroundEntity(player,
                            net.minecraft.particle.ParticleTypes.DRAGON_BREATH, 3, 1.0);
                    }
                }
            }
            
            // Verificar si está equipado en la cabeza
            boolean isEquipped = player.getEquippedStack(EquipmentSlot.HEAD) == stack;
            
            if (isEquipped && !hasImmunity(player)) {
                // NO aplicar efectos Wither/Slowness
                // Solo corrupción a través del sistema general
                
                // Los atributos se aplican automáticamente a través de getAttributeModifiers
            }
        }
    }
    
    private boolean hasImmunity(PlayerEntity player) {
        // Usar el nuevo sistema de inmunidad
        if (player.getServer() != null) {
            com.fiw.fiwstory.data.ImmunityData data = com.fiw.fiwstory.data.ImmunityData.getServerState(player.getServer());
            return data.isPlayerImmune(player.getUuid());
        }
        return false;
    }
    
    // Material de armadura personalizado para las gafas
    private static class CustomArmorMaterial implements ArmorMaterial {
        private static final EnumMap<ArmorItem.Type, Integer> BASE_DURABILITY = new EnumMap<>(ArmorItem.Type.class);
        static {
            BASE_DURABILITY.put(ArmorItem.Type.BOOTS, 13);
            BASE_DURABILITY.put(ArmorItem.Type.LEGGINGS, 15);
            BASE_DURABILITY.put(ArmorItem.Type.CHESTPLATE, 16);
            BASE_DURABILITY.put(ArmorItem.Type.HELMET, 11);
        }
        
        @Override
        public int getDurability(ArmorItem.Type type) {
            return Integer.MAX_VALUE; // Durabilidad infinita
        }
        
        @Override
        public int getProtection(ArmorItem.Type type) {
            // Las gafas dan +4 de armadura (ya aplicado en getAttributeModifiers)
            // Aquí solo damos protección base de cuero para que sea visualmente diferente
            return switch (type) {
                case HELMET -> 2; // Protección base baja
                case CHESTPLATE -> 0;
                case LEGGINGS -> 0;
                case BOOTS -> 0;
            };
        }
        
        @Override
        public int getEnchantability() {
            return 0; // No se puede encantar
        }
        
        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER; // Sonido suave
        }
        
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(Items.AIR); // No se puede reparar
        }
        
        @Override
        public String getName() {
            return "fi3w0_glasses";
        }
        
        @Override
        public float getToughness() {
            return 0.0f; // Sin toughness
        }
        
        @Override
        public float getKnockbackResistance() {
            return 0.0f; // Sin resistencia a knockback
        }
    }
    
    // ========== MÉTODOS PERSONALIZADOS ==========
    
    private boolean hasSpearEquipped(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ModItems.CURSED_SPEAR_OF_FI3W0 ||
               player.getOffHandStack().getItem() == ModItems.CURSED_SPEAR_OF_FI3W0;
    }
}