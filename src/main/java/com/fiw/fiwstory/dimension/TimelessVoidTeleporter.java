package com.fiw.fiwstory.dimension;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;

public class TimelessVoidTeleporter {
    
    public static void teleportToVoid(ServerPlayerEntity player, BlockPos overworldPos) {
        ServerWorld voidWorld = player.getServer().getWorld(TimelessVoidDimension.WORLD_KEY);
        if (voidWorld == null) {
            player.sendMessage(net.minecraft.text.Text.literal("Error: Dimensión Timeless Void no encontrada").formatted(net.minecraft.util.Formatting.RED), false);
            return;
        }
        
        // Convertir coordenadas overworld a void (÷4)
        BlockPos voidPos = convertToVoid(overworldPos);
        
        // Encontrar posición segura arriba del todo
        BlockPos safePos = findTopSafePosition(voidWorld, voidPos);
        
        // Teletransportar directamente
        player.teleport(voidWorld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, player.getYaw(), player.getPitch());
        
        player.sendMessage(net.minecraft.text.Text.literal("Entrando al Timeless Void...").formatted(net.minecraft.util.Formatting.GREEN), false);
    }
    
    public static void teleportToOverworld(ServerPlayerEntity player, BlockPos voidPos) {
        ServerWorld overworld = player.getServer().getOverworld();
        
        // Convertir coordenadas void a overworld (×4)
        BlockPos overworldPos = convertToOverworld(voidPos);
        
        // Asegurar posición segura en overworld (buscar posición segura)
        BlockPos safePos = findSafePosition(overworld, overworldPos);
        
        // Teletransportar directamente
        player.teleport(overworld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, player.getYaw(), player.getPitch());
        
        player.sendMessage(net.minecraft.text.Text.literal("Saliendo del Timeless Void...").formatted(net.minecraft.util.Formatting.GREEN), false);
    }
    
    public static BlockPos convertToVoid(BlockPos overworldPos) {
        // Dividir coordenadas por 4 para el void
        return new BlockPos(overworldPos.getX() / 4, overworldPos.getY(), overworldPos.getZ() / 4);
    }
    
    public static BlockPos convertToOverworld(BlockPos voidPos) {
        // Multiplicar coordenadas por 4 para el overworld
        return new BlockPos(voidPos.getX() * 4, voidPos.getY(), voidPos.getZ() * 4);
    }
    
    public static BlockPos getRandomExitPos(BlockPos entryPos) {
        // Posición aleatoria alrededor de 2000 bloques del punto de entrada
        int offsetX = (int) ((Math.random() * 4000) - 2000);
        int offsetZ = (int) ((Math.random() * 4000) - 2000);
        return entryPos.add(offsetX, 0, offsetZ);
    }
    
    public static BlockPos findSafePosition(ServerWorld world, BlockPos pos) {
        // Buscar posición segura en el overworld
        int y = world.getTopY();
        BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
        
        // Buscar hacia abajo hasta encontrar un bloque sólido
        while (y > world.getBottomY() && !world.getBlockState(checkPos).isSolid()) {
            y--;
            checkPos = new BlockPos(pos.getX(), y, pos.getZ());
        }
        
        // Retornar posición encima del bloque sólido
        return new BlockPos(pos.getX(), y + 1, pos.getZ());
    }
    
    public static BlockPos findTopSafePosition(ServerWorld world, BlockPos pos) {
        // En el Timeless Void, terreno de 272 a 304 con variación suave
        // Altura máxima: 304 (múltiplo de 16)
        // 3 bloques igualmente mezclados: Deepslate, Basalt, Smooth Basalt
        // Buscar desde arriba del todo (Y=303) hacia abajo
        int y = 303;
        BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
        
        // Buscar hacia abajo hasta encontrar un bloque sólido
        while (y > world.getBottomY() && !world.getBlockState(checkPos).isSolid()) {
            y--;
            checkPos = new BlockPos(pos.getX(), y, pos.getZ());
        }
        
        // Si encontramos un bloque sólido, poner al jugador encima
        if (y > world.getBottomY() && world.getBlockState(checkPos).isSolid()) {
            return new BlockPos(pos.getX(), y + 1, pos.getZ());
        }
        
        // Si no hay bloques sólidos, usar Y=282 como fallback
        return new BlockPos(pos.getX(), 282, pos.getZ());
    }
}