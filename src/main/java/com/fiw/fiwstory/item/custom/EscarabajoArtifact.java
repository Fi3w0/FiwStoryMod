package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EscarabajoArtifact extends BaseArtifactItem {

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
    }

    @Override
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // Accesorio pasivo - NO aplica efectos de corrupción (wither/slowness)
        // Atributos pasivos aplicados via Trinkets getModifiers
    }

    // ========== TRINKETS API ==========
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(EntityAttributes.GENERIC_ARMOR,
            new EntityAttributeModifier(uuid, "Escarabajo armor", 4.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS,
            new EntityAttributeModifier(uuid, "Escarabajo armor toughness", 2.0, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
            new EntityAttributeModifier(uuid, "Escarabajo knockback resistance", 0.2, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }
}
