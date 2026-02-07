package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.custom.PhilosopherStoneUpgradedArtifact;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PhilosopherStoneEvents {
    
    public static void registerEvents() {
        // Evento para detectar cuando se rompe un bloque
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient()) {
                PhilosopherStoneUpgradedArtifact.onBlockBreak(player, state, pos, world);
            }
        });
    }
}