package com.fiw.fiwstory.item;

import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

/**
 * Clase base para artefactos que son espadas/armas.
 * Combina la funcionalidad de SwordItem con BaseArtifactItem.
 */
public abstract class BaseArtifactSwordItem extends SwordItem {
    
    protected final BaseArtifactItem.ArtifactType artifactType;
    protected final BaseArtifactItem.ArtifactRarity artifactRarity;
    protected final int baseCorruptionRate;
    protected final int maxUses;
    
    /**
     * Constructor para artefactos espada.
     */
    public BaseArtifactSwordItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, 
                                 BaseArtifactItem.ArtifactType type, BaseArtifactItem.ArtifactRarity rarity,
                                 int baseCorruptionRate, int maxUses, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.artifactType = type;
        this.artifactRarity = rarity;
        this.baseCorruptionRate = baseCorruptionRate;
        this.maxUses = maxUses;
    }
    
    /**
     * Obtiene el tipo de artefacto.
     */
    public BaseArtifactItem.ArtifactType getArtifactType() {
        return artifactType;
    }
    
    /**
     * Obtiene la rareza del artefacto.
     */
    public BaseArtifactItem.ArtifactRarity getArtifactRarity() {
        return artifactRarity;
    }
    
    /**
     * Obtiene la tasa base de corrupción.
     */
    public int getBaseCorruptionRate() {
        return baseCorruptionRate;
    }
    
    /**
     * Obtiene el número máximo de usos.
     */
    public int getMaxUses() {
        return maxUses;
    }
    
    // Métodos abstractos que deben ser implementados por las subclases
    public abstract String getArtifactDisplayName();
    public abstract String getArtifactDescription();
    public abstract java.util.List<String> getArtifactFeatures();
    public abstract String getArtifactQuote();
    public abstract void onArtifactUse(net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity player, net.minecraft.item.ItemStack stack, net.minecraft.util.Hand hand);
    
    // Nota: Esta clase sirve como base para artefactos que necesitan
    // funcionalidad de espada. Las subclases deben implementar los métodos
    // abstractos de BaseArtifactItem y los métodos específicos de SwordItem.
}