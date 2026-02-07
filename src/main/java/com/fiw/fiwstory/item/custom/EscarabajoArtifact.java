package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EscarabajoArtifact extends BaseArtifactItem {
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4F5C-BE3C-9C1A8E5B5F5D");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("D8499B04-0E66-4F0E-BD22-7C9F6B5A5F5E");

    public EscarabajoArtifact(Settings settings) {
        super(ArtifactType.ACCESSORY, ArtifactRarity.LEGENDARY, 2, 0, settings.maxCount(1).fireproof());
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
        
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            // ARMOR: +4 puntos de armadura
            modifiers.put(EntityAttributes.GENERIC_ARMOR,
                new EntityAttributeModifier(ARMOR_MODIFIER_ID, "Escarabajo armor", 4.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // ARMOR_TOUGHNESS: +2 dureza de armadura
            modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
                new EntityAttributeModifier(ARMOR_TOUGHNESS_MODIFIER_ID, "Escarabajo armor toughness", 2.0,
                    EntityAttributeModifier.Operation.ADDITION));
            
            // KNOCKBACK_RESISTANCE: +0.2 resistencia a empujón
            modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                new EntityAttributeModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID, "Escarabajo knockback resistance", 0.2,
                    EntityAttributeModifier.Operation.ADDITION));
        }
        
        return modifiers;
    }

    @Override
    public String getArtifactDisplayName() {
        return "Escarabajo de Plata del Faraón";
    }
    
    @Override
    public String getArtifactDescription() {
        return "Uno de los artefactos legendarios del Dios Faraón";
    }
    
    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Protección divina del desierto",
            "Bendición de defensa eterna"
        );
    }
    
    @Override
    public String getArtifactQuote() {
        return "La plata del desierto protege a su portador";
    }
    
    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        // Accesorio pasivo, no tiene uso activo
        // Los atributos se aplican automáticamente a través de getAttributeModifiers
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        super.onArtifactTick(stack, world, entity, slot, selected);
        
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            // Verificar si está en mano principal o secundaria
            boolean inMainHand = player.getMainHandStack() == stack;
            boolean inOffHand = player.getOffHandStack() == stack;
            
            if (inMainHand || inOffHand) {
                // Los atributos se aplican automáticamente a través de getAttributeModifiers
                // No necesitamos hacer nada adicional aquí
            }
        }
    }
}