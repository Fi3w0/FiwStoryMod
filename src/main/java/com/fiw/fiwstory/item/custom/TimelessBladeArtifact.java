package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TimelessBladeArtifact extends BaseArtifactSwordItem {
    
    // UUIDs para modificadores de atributos
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("8B5A5F5E-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    
    // Material personalizado para la espada atemporal (indestructible)
    private static final ToolMaterial TIMELESS_BLADE_MATERIAL = new ToolMaterial() {
        @Override
        public int getDurability() {
            return 0; // Indestructible - forjada con la esencia del Vacío
        }
        
        @Override
        public float getMiningSpeedMultiplier() {
            return 2.5f; // Velocidad mejorada - corta a través del tiempo
        }
        
        @Override
        public float getAttackDamage() {
            return 1f; // Daño base del material (Netherite da 4, Diamond da 3)
        }
        
        @Override
        public int getMiningLevel() {
            return 1; // Nivel de espada (no es un pico ni hacha)
        }
        
        @Override
        public int getEnchantability() {
            return 25; // Encantabilidad excelente - absorbe magia del Vacío
        }
        
        @Override
        public Ingredient getRepairIngredient() {
            // Indestructible, no necesita reparación - se regenera con el tiempo
            return Ingredient.EMPTY;
        }
    };

    public TimelessBladeArtifact(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        // 12 de daño total: material(1) + constructor(10) + 1 base = 12
        // Netherite: material(4) + constructor(5) + 1 base = 10
        super(TIMELESS_BLADE_MATERIAL, attackDamage, attackSpeed, 
              BaseArtifactItem.ArtifactType.WEAPON,
              BaseArtifactItem.ArtifactRarity.MYTHIC, // RARIDAD MÍTICA - superior a legendaria
              2, // Tasa de corrupción reducida - más estable que otros artefactos
              0, // Usos infinitos - poder del Vacío es inagotable
              settings.maxDamage(0)); // Indestructible
    }

    @Override
    public boolean isDamageable() {
        return false; // Indestructible
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true; // Se puede encantar
    }
    
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        
        if (slot == EquipmentSlot.MAINHAND) {
            // ATTACK_DAMAGE: +12 (solo en mainHand)
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Timeless blade attack damage", 12.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // ATTACK_SPEED: +0.1 (solo en mainHand)
            modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Timeless blade attack speed", 0.1,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // MOVEMENT_SPEED: +0.1 (solo en mainHand) - como la Chaos Gem
            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "Timeless blade movement speed", 0.1,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public String getArtifactDisplayName() {
        return "Timeless Blade";
    }
    
    @Override
    public String getArtifactDescription() {
        return "Mezcla entre recuerdos del pasado y presente";
    }
    
    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "En su base está la legendaria daga del dios faraón",
            "Parece que te invita a un mundo donde no hay tiempo",
            "Permite entrar al Vacío Atemporal",
            "Daño de ataque +12 en mano principal",
            "Velocidad de ataque +0.1 en mano principal",
            "Velocidad de movimiento +0.1 en mano principal"
        );
    }
    
    @Override
    public String getArtifactQuote() {
        return "Artefacto único";
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // No llamar a super.appendTooltip para evitar duplicación
        tooltip.add(Text.literal("«Timeless Blade»").formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.BOLD));
        tooltip.add(Text.literal("Mezcla entre recuerdos del pasado y presente").formatted(net.minecraft.util.Formatting.GRAY, net.minecraft.util.Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§5§oEn su base está la legendaria daga del dios faraón§r").formatted(net.minecraft.util.Formatting.DARK_PURPLE));
        tooltip.add(Text.literal("§7• Parece que te invita a un mundo donde no hay tiempo§r").formatted(net.minecraft.util.Formatting.GRAY));
        tooltip.add(Text.literal("§7• Permite entrar al Vacío Atemporal§r").formatted(net.minecraft.util.Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.BOLD));
        tooltip.add(Text.literal("§8«Artefacto único»§r").formatted(net.minecraft.util.Formatting.DARK_GRAY, net.minecraft.util.Formatting.ITALIC));
    }
    
    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            // Habilidad de teletransporte al Timeless Void
            long lastUsed = stack.getOrCreateNbt().getLong("lastVoidUse");
            long currentTime = world.getTime();
            
            // Cooldown de 10 segundos (200 ticks)
            if (currentTime - lastUsed < 200) {
                player.sendMessage(net.minecraft.text.Text.literal("Habilidad en cooldown").formatted(net.minecraft.util.Formatting.YELLOW), true);
                return;
            }
            
            // Verificar si el jugador está en el Void
            boolean inVoid = world.getRegistryKey() == com.fiw.fiwstory.dimension.TimelessVoidDimension.WORLD_KEY;
            
            if (inVoid) {
                // Salir del Void
                com.fiw.fiwstory.dimension.TimelessVoidTeleporter.teleportToOverworld(
                    (net.minecraft.server.network.ServerPlayerEntity) player, 
                    player.getBlockPos()
                );
                player.sendMessage(net.minecraft.text.Text.literal("Saliendo del Timeless Void...").formatted(net.minecraft.util.Formatting.GREEN), true);
            } else {
                // Entrar al Void
                com.fiw.fiwstory.dimension.TimelessVoidTeleporter.teleportToVoid(
                    (net.minecraft.server.network.ServerPlayerEntity) player,
                    player.getBlockPos()
                );
                player.sendMessage(net.minecraft.text.Text.literal("Entrando al Timeless Void...").formatted(net.minecraft.util.Formatting.GREEN), true);
            }
            
            // Establecer cooldown
            stack.getOrCreateNbt().putLong("lastVoidUse", currentTime);
            
            // Efectos visuales
            com.fiw.fiwstory.lib.FiwEffects.spawnExplosionParticles(world, player.getPos(), 
                net.minecraft.particle.ParticleTypes.PORTAL, 30, 2.0);
            com.fiw.fiwstory.lib.FiwEffects.playSoundAtEntity(player, 
                net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Los artefactos legendarios siempre tienen glint
    }
    

}