package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Cristal Puro - cae al minar diamante con 0.2% base, 0.7% con Fortune III.
 * Ingrediente para crear Mix Puro que reduce corrupción.
 */
public class PureCrystalItem extends Item {
    
    public PureCrystalItem(Settings settings) {
        super(settings.maxCount(16)); // Stackable
    }
    

    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (world.isClient()) {
            return TypedActionResult.success(stack, true);
        }
        
        // No hace nada al hacer click - como un diamante
        // Solo un efecto visual muy sutil
        com.fiw.fiwstory.lib.FiwEffects.spawnParticlesAroundEntity(player,
            net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, 1, 0.2);
        
        return TypedActionResult.success(stack, false);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("«Cristal Puro»").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Fragmento de pureza latente").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§b§oCristal Sagrado§r").formatted(Formatting.BLUE));
        tooltip.add(Text.literal("§7• Energía de pureza§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7• Calor reconfortante§r").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§8«La pureza duerme, esperando despertar»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // Los cristales puros tienen un brillo sutil
        return true;
    }
    
    // ========== MÉTODOS ESTÁTICOS PARA EVENTOS DE MINERÍA ==========
    
    /**
     * Maneja el drop de Cristal Puro al minar diamante.
     */
    public static void handleDiamondMining(PlayerEntity player, net.minecraft.block.BlockState state) {
        if (player.getWorld().isClient()) return;
        
        // Verificar si es diamante normal o profundo
        boolean isDiamond = state.isOf(net.minecraft.block.Blocks.DIAMOND_ORE) || 
                           state.isOf(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE);
        
        if (!isDiamond) return;
        
        // Calcular chance base
        final float[] baseChance = {0.002f}; // 0.2%
        
        // Aumentar con Fortune
        net.minecraft.item.ItemStack tool = player.getMainHandStack();
        if (tool.hasEnchantments()) {
            net.minecraft.enchantment.EnchantmentHelper.get(tool).forEach((enchantment, level) -> {
                if (enchantment == net.minecraft.enchantment.Enchantments.FORTUNE) {
                    // Fortune III: 0.7% (0.2% base + 0.5% bonus)
                    baseChance[0] += level * 0.00167f; // Aproximadamente 0.167% por nivel
                }
            });
        }
        
        // Verificar drop
        if (player.getWorld().random.nextFloat() < baseChance[0]) {
            // Dar cristal al jugador
            ItemStack crystalStack = new ItemStack(
                com.fiw.fiwstory.item.ModItems.PURE_CRYSTAL, 
                1 + player.getWorld().random.nextInt(2) // 1-2 cristales
            );
            
            if (!player.giveItemStack(crystalStack)) {
                player.dropItem(crystalStack, false);
            }
            
            // Efectos visuales
            net.minecraft.util.math.BlockPos pos = player.getBlockPos();
            com.fiw.fiwstory.lib.FiwEffects.spawnExplosionParticles(
                player.getWorld(),
                net.minecraft.util.math.Vec3d.ofCenter(pos),
                net.minecraft.particle.ParticleTypes.END_ROD,
                8,
                1.0
            );
            
            // NO mensajes - los jugadores deben descubrir
            // Solo efectos visuales sutiles
        }
    }
}