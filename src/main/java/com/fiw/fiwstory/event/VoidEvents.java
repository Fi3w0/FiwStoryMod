package com.fiw.fiwstory.event;

import com.fiw.fiwstory.command.VoidCommand;
// La dimensión se manejará con datapacks por ahora
// import com.fiw.fiwstory.dimension.TimelessVoidDimensionType;
import com.fiw.fiwstory.item.custom.TimelessBladeArtifact;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VoidEvents {
    
    public static void register() {
        // Verificar límite de tiempo cada tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                VoidCommand.checkTimeLimit(player);
            }
        });
        
        // Prevenir romper bloques en el Void
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.getRegistryKey() == com.fiw.fiwstory.dimension.TimelessVoidDimension.WORLD_KEY) {
                player.sendMessage(Text.literal("No puedes romper bloques en el Timeless Void").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        
        // Habilidad de la Timeless Blade para entrar al Void
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) {
                return TypedActionResult.pass(ItemStack.EMPTY);
            }
            
            ItemStack stack = player.getStackInHand(hand);
            
            // Verificar si es la Timeless Blade
            if (stack.getItem() instanceof TimelessBladeArtifact) {
                // Verificar cooldown (10 segundos)
                long lastUsed = stack.getOrCreateNbt().getLong("lastVoidUse");
                long currentTime = world.getTime();
                
                if (currentTime - lastUsed < 200) { // 10 segundos = 200 ticks
                    player.sendMessage(Text.literal("Habilidad en cooldown").formatted(Formatting.YELLOW), true);
                    return TypedActionResult.fail(stack);
                }
                
                // La habilidad ahora está implementada directamente en TimelessBladeArtifact
                // Este callback ya no es necesario
                
                // El cooldown se maneja en TimelessBladeArtifact
                return TypedActionResult.pass(stack);
            }
            
            return TypedActionResult.pass(stack);
        });
    }
    
    // Los métodos de teletransporte ahora están en TimelessVoidTeleporter
}