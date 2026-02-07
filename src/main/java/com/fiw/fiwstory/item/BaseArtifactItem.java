package com.fiw.fiwstory.item;

import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Clase base abstracta para todos los artefactos del mod Fiw Story.
 * Proporciona funcionalidad común como sistema de bind, tooltips unificados,
 * efectos de corrupción y manejo de cooldowns.
 */
public abstract class BaseArtifactItem extends Item {
    
    // Tipos de artefactos según el PLAN MAESTRO
    public enum ArtifactType {
        WEAPON("Arma"),
        ARMOR("Armadura"),
        ACCESSORY("Accesorio"),
        CONSUMABLE("Consumible"),
        RITUAL("Ritual"),
        SPECIAL("Especial");
        
        private final String displayName;
        
        ArtifactType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Niveles de rareza
    public enum ArtifactRarity {
        COMMON("Común", Formatting.GRAY),
        UNCOMMON("Poco Común", Formatting.GREEN),
        RARE("Raro", Formatting.BLUE),
        EPIC("Épico", Formatting.DARK_PURPLE),
        LEGENDARY("Legendario", Formatting.GOLD),
        MYTHIC("Mítico", Formatting.LIGHT_PURPLE);
        
        private final String displayName;
        private final Formatting color;
        
        ArtifactRarity(String displayName, Formatting color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Formatting getColor() {
            return color;
        }
    }
    
    // Propiedades del artefacto
    protected final ArtifactType artifactType;
    protected final ArtifactRarity artifactRarity;
    protected final int baseCorruptionRate; // Tasa base de corrupción por uso
    protected final int maxUses; // Usos máximos antes de degradación (0 = infinito)
    
    /**
     * Constructor base para artefactos.
     */
    public BaseArtifactItem(ArtifactType type, ArtifactRarity rarity, int baseCorruptionRate, int maxUses, Settings settings) {
        super(settings);
        this.artifactType = type;
        this.artifactRarity = rarity;
        this.baseCorruptionRate = baseCorruptionRate;
        this.maxUses = maxUses;
    }
    
    /**
     * Método abstracto para obtener el nombre de visualización del artefacto.
     */
    public abstract String getArtifactDisplayName();
    
    /**
     * Método abstracto para obtener la descripción del artefacto.
     */
    public abstract String getArtifactDescription();
    
    /**
     * Método abstracto para obtener las características del artefacto.
     * Debe devolver una lista de strings con características vagas/misteriosas.
     */
    public abstract List<String> getArtifactFeatures();
    
    /**
     * Método abstracto para obtener la cita misteriosa del artefacto.
     */
    public abstract String getArtifactQuote();
    
    /**
     * Método que se llama cuando el artefacto es usado exitosamente.
     * Las subclases deben implementar este método para definir el comportamiento específico.
     */
    public abstract void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand);
    
    /**
     * Método que se llama cada tick mientras el artefacto está en el inventario.
     * Las subclases pueden sobrescribir para comportamiento específico.
     */
    public void onArtifactTick(ItemStack stack, World world, LivingEntity entity, int slot, boolean selected) {
        // Comportamiento base: aplicar efectos de corrupción si está seleccionado
        if (!world.isClient() && entity instanceof PlayerEntity player && selected) {
            handleCorruptionEffects(player, stack);
        }
    }
    
    /**
     * Método que se llama cuando el artefacto es usado por un jugador no autorizado.
     * Las subclases pueden sobrescribir para comportamiento personalizado.
     */
    public void onUnauthorizedUse(PlayerEntity player, ItemStack stack) {
        FiwEffects.applyUnauthorizedUsePenalty(player);
        FiwUtils.sendErrorMessage(player, "Este artefacto no te pertenece...");
    }
    
    /**
     * Maneja los efectos de corrupción para el artefacto.
     */
    protected void handleCorruptionEffects(PlayerEntity player, ItemStack stack) {
        // Verificar inmunidad (sistema existente)
        if (hasImmunity(player)) {
            return;
        }
        
        // Aplicar efectos corruptos base
        int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
        FiwEffects.applyCorruptionEffects(player, corruptionLevel);
        
        // Incrementar corrupción con el tiempo
        if (player.getWorld().getTime() % 100 == 0) { // Cada 5 segundos
            incrementCorruption(stack, 1);
        }
    }
    
    /**
     * Incrementa el nivel de corrupción del artefacto.
     */
    protected void incrementCorruption(ItemStack stack, int amount) {
        int currentCorruption = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
        int newCorruption = Math.min(100, currentCorruption + amount);
        FiwNBT.setInt(stack, FiwNBT.CORRUPTION_LEVEL, newCorruption);
    }
    
    /**
     * Verifica si un jugador puede usar este artefacto.
     */
    public boolean canPlayerUse(PlayerEntity player, ItemStack stack) {
        // Verificar bind
        if (!FiwUtils.canUseBoundArtifact(player, stack)) {
            return false;
        }
        
        // Verificar cooldowns
        if (!isCooldownReady(stack, "use")) {
            return false;
        }
        
        // Verificar usos máximos
        if (maxUses > 0) {
            int uses = FiwNBT.getUses(stack);
            if (uses >= maxUses) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Verifica si un cooldown específico está listo.
     */
    protected boolean isCooldownReady(ItemStack stack, String cooldownType) {
        return FiwNBT.isCooldownOver(stack, cooldownType);
    }
    
    /**
     * Establece un cooldown para el artefacto.
     */
    protected void setCooldown(ItemStack stack, String cooldownType, long durationMs) {
        FiwNBT.setCooldown(stack, cooldownType, durationMs);
    }
    
    /**
     * Obtiene el tiempo restante de cooldown.
     */
    protected long getCooldownRemaining(ItemStack stack, String cooldownType) {
        return FiwNBT.getCooldownRemaining(stack, cooldownType);
    }
    
    /**
     * Verifica si el jugador tiene inmunidad a efectos corruptos.
     * Método de compatibilidad con sistema existente.
     */
    protected boolean hasImmunity(PlayerEntity player) {
        // Este método debería integrarse con el sistema existente de inmunidad
        // Por ahora, retornamos false como placeholder
        return false;
    }
    
    // ========== MÉTODOS DE ITEM OVERRIDES ==========
    
    @Override
    public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        
        if (entity instanceof LivingEntity livingEntity) {
            onArtifactTick(stack, world, livingEntity, slot, selected);
        }
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (world.isClient()) {
            return TypedActionResult.success(stack, true);
        }
        
        // Verificar si el jugador puede usar el artefacto
        if (!canPlayerUse(player, stack)) {
            if (!FiwUtils.canUseBoundArtifact(player, stack)) {
                onUnauthorizedUse(player, stack);
                return TypedActionResult.fail(stack);
            }
            
            // Cooldown o usos máximos
            FiwUtils.sendErrorMessage(player, "El artefacto no está listo para ser usado.");
            return TypedActionResult.fail(stack);
        }
        
        // Ejecutar uso específico del artefacto
        onArtifactUse(world, player, stack, hand);
        
        // Registrar uso
        FiwNBT.incrementUses(stack);
        FiwNBT.setLong(stack, FiwNBT.LAST_USED, System.currentTimeMillis());
        
        // Aplicar corrupción por uso
        incrementCorruption(stack, baseCorruptionRate);
        
        return TypedActionResult.success(stack, false);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        // Nombre del artefacto
        tooltip.add(FiwUtils.formatArtifactName(getArtifactDisplayName()));
        
        // Descripción
        tooltip.add(FiwUtils.formatArtifactDescription(getArtifactDescription()));
        tooltip.add(Text.literal(""));
        
        // Tipo y rareza
        tooltip.add(FiwUtils.formatArtifactType(artifactType.getDisplayName() + " • " + artifactRarity.getDisplayName()));
        
        // Características
        for (String feature : getArtifactFeatures()) {
            tooltip.add(FiwUtils.formatArtifactFeature(feature));
        }
        
        tooltip.add(Text.literal(""));
        
        // Información de bind (solo si está vinculado)
        if (FiwNBT.isBound(stack)) {
            UUID boundTo = FiwNBT.getBoundTo(stack);
            String bindInfo = "Vinculado a su dueño";
            tooltip.add(Text.literal("§6§o" + bindInfo + "§r").formatted(Formatting.GOLD, Formatting.ITALIC));
            tooltip.add(Text.literal(""));
        }
        
        // Información de corrupción (solo si hay corrupción)
        int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
        if (corruptionLevel > 0) {
            String corruptionText = "Corrupción: " + corruptionLevel + "%";
            Formatting corruptionColor = corruptionLevel < 30 ? Formatting.YELLOW : 
                                        corruptionLevel < 70 ? Formatting.RED : Formatting.DARK_RED;
            tooltip.add(Text.literal("§7" + corruptionText + "§r").formatted(corruptionColor));
            tooltip.add(Text.literal(""));
        }
        
        // Información de usos (solo si tiene usos máximos)
        if (maxUses > 0) {
            int uses = FiwNBT.getUses(stack);
            int remaining = maxUses - uses;
            String usesText = "Usos restantes: " + remaining + "/" + maxUses;
            tooltip.add(Text.literal("§7" + usesText + "§r").formatted(Formatting.GRAY));
            tooltip.add(Text.literal(""));
        }
        
        // Cita misteriosa
        tooltip.add(FiwUtils.formatArtifactQuote(getArtifactQuote()));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // Los artefactos legendarios y míticos siempre tienen glint
        return artifactRarity == ArtifactRarity.LEGENDARY || 
               artifactRarity == ArtifactRarity.MYTHIC ||
               super.hasGlint(stack);
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
        // La mayoría de artefactos usan animación de bloqueo (como escudos)
        return UseAction.BLOCK;
    }
    
    @Override
    public int getMaxUseTime(ItemStack stack) {
        // Tiempo máximo de uso (para animación)
        return 72000; // Valor alto para uso continuo
    }
    
    // ========== MÉTODOS DE UTILIDAD PÚBLICOS ==========
    
    /**
     * Vincula este artefacto a un jugador.
     */
    public void bindToPlayer(ItemStack stack, UUID playerId, String adminName) {
        FiwNBT.bindTo(stack, playerId, adminName);
    }
    
    /**
     * Desvincula este artefacto.
     */
    public void unbindFromPlayer(ItemStack stack) {
        FiwNBT.unbind(stack);
    }
    
    /**
     * Verifica si el artefacto está vinculado.
     */
    public boolean isBound(ItemStack stack) {
        return FiwNBT.isBound(stack);
    }
    
    /**
     * Obtiene el UUID del jugador al que está vinculado.
     */
    public UUID getBoundPlayerId(ItemStack stack) {
        return FiwNBT.getBoundTo(stack);
    }
    
    /**
     * Obtiene el nivel de corrupción actual.
     */
    public int getCorruptionLevel(ItemStack stack) {
        return FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
    }
    
    /**
     * Establece el nivel de corrupción.
     */
    public void setCorruptionLevel(ItemStack stack, int level) {
        FiwNBT.setInt(stack, FiwNBT.CORRUPTION_LEVEL, Math.min(100, Math.max(0, level)));
    }
    
    /**
     * Obtiene el número de usos acumulados.
     */
    public int getUsesCount(ItemStack stack) {
        return FiwNBT.getUses(stack);
    }
    
    /**
     * Reinicia los usos acumulados.
     */
    public void resetUses(ItemStack stack) {
        FiwNBT.setInt(stack, FiwNBT.ARTIFACT_USES, 0);
    }
    
    /**
     * Obtiene el tipo de artefacto.
     */
    public ArtifactType getArtifactType() {
        return artifactType;
    }
    
    /**
     * Obtiene la rareza del artefacto.
     */
    public ArtifactRarity getArtifactRarity() {
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
}