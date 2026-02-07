package com.fiw.fiwstory.command;

import net.minecraft.util.math.BlockPos;

public class VoidTeleportData {
    private final BlockPos entryPos;
    private final long entryTime;
    
    public VoidTeleportData(BlockPos entryPos, long entryTime) {
        this.entryPos = entryPos;
        this.entryTime = entryTime;
    }
    
    public BlockPos getEntryPos() {
        return entryPos;
    }
    
    public long getEntryTime() {
        return entryTime;
    }
    
    public boolean isTimeExpired(long currentTime, boolean hasTimelessBlade) {
        // 5 minutos sin Timeless Blade = 5 * 60 * 1000 = 300,000 ms
        // 10 minutos con Timeless Blade = 10 * 60 * 1000 = 600,000 ms
        long timeLimit = hasTimelessBlade ? 600000L : 300000L;
        return (currentTime - entryTime) >= timeLimit;
    }
    
    public long getRemainingTime(long currentTime, boolean hasTimelessBlade) {
        long timeLimit = hasTimelessBlade ? 600000L : 300000L;
        long elapsed = currentTime - entryTime;
        return Math.max(0, timeLimit - elapsed);
    }
    
    public BlockPos getRandomExitPos() {
        // Posición aleatoria alrededor de 2000 bloques del punto de entrada
        int offsetX = (int) ((Math.random() * 4000) - 2000);
        int offsetZ = (int) ((Math.random() * 4000) - 2000);
        return entryPos.add(offsetX, 0, offsetZ);
    }
    
    public static BlockPos convertToOverworld(BlockPos voidPos) {
        // Multiplicar coordenadas por 4 para el mundo normal
        return new BlockPos(voidPos.getX() * 4, voidPos.getY(), voidPos.getZ() * 4);
    }
    
    public static BlockPos convertToVoid(BlockPos overworldPos) {
        // Dividir coordenadas por 4 para el void
        return new BlockPos(overworldPos.getX() / 4, overworldPos.getY(), overworldPos.getZ() / 4);
    }
}