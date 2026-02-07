package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PhilosopherStoneUpgradedArtifact extends BaseArtifactItem {
    
    // Random thread-safe para efectos
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
    
    public PhilosopherStoneUpgradedArtifact(Settings settings) {
        super(ArtifactType.SPECIAL,
              ArtifactRarity.EPIC,
              1, // Baja tasa de corrupción
              0, // Usos infinitos
              settings.maxCount(1).fireproof());
    }
    
    // ========== MÉTODOS DE ARTEFACTO ==========
    
    @Override
    public String getArtifactDisplayName() {
        return "Piedra Filosófica Mejorada";
    }
    
    @Override
    public String getArtifactDescription() {
        return "Artefacto de Dios Faraón - Versión Mejorada";
    }
    
    @Override
    public List<String> getArtifactFeatures() {
        return Arrays.asList(
            "Parece que este artefacto puede convertir materia",
            "Ves el verdadero potencial de la piedra",
            "1% de chance al minar piedra/pizarra profunda",
            "Genera minerales en offhand",
            "Oro/Hierro: 1-5, Diamante: 1-3, Esmeralda: 1-2"
        );
    }
    
    @Override
    public String getArtifactQuote() {
        return "La alquimia verdadera transforma no solo la materia, sino también al alquimista";
    }
    
    @Override
    public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
        if (!world.isClient()) {
            // Efecto visual al usar (partículas doradas)
            FiwEffects.spawnExplosionParticles(world, player.getPos(), 
                net.minecraft.particle.ParticleTypes.ENCHANT, 10, 2.0);
            FiwEffects.playSoundAtEntity(player, net.minecraft.sound.SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 
                0.8f, 1.2f);
            
            FiwUtils.sendInfoMessage(player, "✨ La Piedra Filosófica pulsa con energía alquímica.");
        }
    }
    
    // ========== MÉTODOS DE ITEM OVERRIDES ==========
    
    @Override
    public boolean isDamageable() {
        return false;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    
    // ========== MÉTODOS DE MINERÍA (ESTÁTICOS PARA EVENTOS) ==========
    
    /**
     * Método para verificar si el jugador está minando con la piedra en offhand.
     * Llamado desde eventos externos.
     */
    public static void onBlockBreak(PlayerEntity player, BlockState state, BlockPos pos, World world) {
        if (world.isClient()) return;
        
        // Verificar si tiene la piedra mejorada en offhand
        ItemStack offhandStack = player.getOffHandStack();
        if (offhandStack.getItem() instanceof PhilosopherStoneUpgradedArtifact) {
            // Verificar si está minando piedra o pizarra profunda
            String blockName = state.getBlock().getTranslationKey().toLowerCase();
            boolean isStoneBlock = blockName.contains("stone") || 
                                  blockName.contains("deepslate") ||
                                  blockName.contains("cobblestone") ||
                                  blockName.contains("andesite") ||
                                  blockName.contains("diorite") ||
                                  blockName.contains("granite");
            
            if (isStoneBlock) {
                // 1% de chance de activar el efecto
                Random rand = RANDOM.get();
                if (rand.nextFloat() < 0.01f) {
                    generateOres(player, world, pos);
                    
                    // Incrementar corrupción en la piedra
                    FiwNBT.incrementUses(offhandStack);
                    int currentCorruption = FiwNBT.getInt(offhandStack, FiwNBT.CORRUPTION_LEVEL, 0);
                    FiwNBT.setInt(offhandStack, FiwNBT.CORRUPTION_LEVEL, Math.min(100, currentCorruption + 1));
                }
            }
        }
    }
    
    private static void generateOres(PlayerEntity player, World world, BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld) world;
        Random rand = RANDOM.get();
        
        // Elegir qué mineral generar
        int oreType = rand.nextInt(100);
        ItemStack oreStack = null;
        
        if (oreType < 40) { // 40% - Oro (1-5)
            int amount = 1 + rand.nextInt(5);
            oreStack = new ItemStack(Items.GOLD_NUGGET, amount);
        } else if (oreType < 70) { // 30% - Hierro (1-5)
            int amount = 1 + rand.nextInt(5);
            oreStack = new ItemStack(Items.IRON_NUGGET, amount);
        } else if (oreType < 90) { // 20% - Diamante (1-3)
            int amount = 1 + rand.nextInt(3);
            oreStack = new ItemStack(Items.DIAMOND, amount);
        } else { // 10% - Esmeralda (1-2)
            int amount = 1 + rand.nextInt(2);
            oreStack = new ItemStack(Items.EMERALD, amount);
        }
        
        // Dar el mineral al jugador
        if (!player.giveItemStack(oreStack)) {
            // Si el inventario está lleno, dropear en el mundo
            player.dropItem(oreStack, false);
        }
        
        // Efectos visuales usando FiwEffects
        FiwEffects.spawnExplosionParticles(world, 
            net.minecraft.util.math.Vec3d.ofCenter(pos),
            net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, 15, 1.5);
        
        FiwEffects.playSoundAtPosition(world, net.minecraft.util.math.Vec3d.ofCenter(pos),
            net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.5f);
        
        // Mensaje al jugador (genérico, no especifica qué mineral)
        FiwUtils.sendInfoMessage(player, "✨ La Piedra Filosófica transforma la piedra en un mineral valioso!");
    }
    
    // ========== TOOLTIP PERSONALIZADO ==========
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Usar el sistema de tooltips unificado
        tooltip.add(FiwUtils.formatArtifactName(getArtifactDisplayName()));
        tooltip.add(FiwUtils.formatArtifactDescription(getArtifactDescription()));
        tooltip.add(Text.literal(""));
        
        tooltip.add(FiwUtils.formatArtifactType(
            getArtifactType().getDisplayName() + " • " + getArtifactRarity().getDisplayName()));
        
        for (String feature : getArtifactFeatures()) {
            tooltip.add(FiwUtils.formatArtifactFeature(feature));
        }
        
        tooltip.add(Text.literal(""));
        
        // Información especial para este artefacto
        tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal(""));
        
        // Información de bind (solo si está vinculado)
        if (FiwNBT.isBound(stack)) {
            tooltip.add(Text.literal("§6§oVinculado a su dueño§r").formatted(Formatting.GOLD, Formatting.ITALIC));
            tooltip.add(Text.literal(""));
        }
        
        // Información de corrupción
        int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
        if (corruptionLevel > 0) {
            String corruptionText = "Corrupción: " + corruptionLevel + "%";
            Formatting corruptionColor = corruptionLevel < 30 ? Formatting.YELLOW : 
                                        corruptionLevel < 70 ? Formatting.RED : Formatting.DARK_RED;
            tooltip.add(Text.literal("§7" + corruptionText + "§r").formatted(corruptionColor));
            tooltip.add(Text.literal(""));
        }
        
        // Información de usos (transformaciones exitosas)
        int uses = FiwNBT.getUses(stack);
        if (uses > 0) {
            String usesText = "Transformaciones: " + uses;
            tooltip.add(Text.literal("§7" + usesText + "§r").formatted(Formatting.GRAY));
            tooltip.add(Text.literal(""));
        }
        
        // Cita misteriosa
        tooltip.add(FiwUtils.formatArtifactQuote(getArtifactQuote()));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // La piedra filosófica siempre tiene glint
        return true;
    }
}