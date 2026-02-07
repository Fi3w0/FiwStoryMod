package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AmethystDropEvent {
    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient() && state.getBlock() == Blocks.AMETHYST_CLUSTER) {
                ServerWorld serverWorld = (ServerWorld) world;
                
                // 10% de probabilidad de dropear cristal corrupto
                if (serverWorld.random.nextFloat() < 0.10f) {
                    ItemStack crystalStack = new ItemStack(ModItems.CORRUPTED_CRYSTAL, 1);
                    Vec3d dropPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    
                    ItemEntity itemEntity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, crystalStack);
                    itemEntity.setVelocity(
                        (serverWorld.random.nextDouble() - 0.5) * 0.1,
                        serverWorld.random.nextDouble() * 0.1,
                        (serverWorld.random.nextDouble() - 0.5) * 0.1
                    );
                    
                    world.spawnEntity(itemEntity);
                }
            }
        });
    }
}