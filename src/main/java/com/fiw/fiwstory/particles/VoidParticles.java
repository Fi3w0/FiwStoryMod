package com.fiw.fiwstory.particles;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.fiw.fiwstory.dimension.TimelessVoidDimension;

public class VoidParticles {
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isInTimelessVoid(player)) {
                    spawnVoidParticles(player);
                }
            }
        });
    }
    
    private static boolean isInTimelessVoid(ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey() == TimelessVoidDimension.WORLD_KEY;
    }
    
    private static void spawnVoidParticles(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d playerPos = player.getPos();
        
        // Partículas de ceniza alrededor del jugador (radio de 100 bloques)
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() * 200) - 100;
            double offsetY = (Math.random() * 50) - 25;
            double offsetZ = (Math.random() * 200) - 100;
            
            Vec3d particlePos = playerPos.add(offsetX, offsetY, offsetZ);
            
            // Partícula de ceniza (ash)
            world.spawnParticles(
                ParticleTypes.ASH,
                particlePos.x, particlePos.y, particlePos.z,
                1, 0, 0, 0, 0
            );
            
            // Partícula de humo (smoke) - más oscura
            if (Math.random() < 0.3) {
                world.spawnParticles(
                    ParticleTypes.SMOKE,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0.1, 0.1, 0.1, 0.01
                );
            }
            
            // Partícula de lava (lava) - efecto oscuro
            if (Math.random() < 0.1) {
                world.spawnParticles(
                    ParticleTypes.LAVA,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0
                );
            }
            
            // Partícula de portal (portal) - efecto místico oscuro
            if (Math.random() < 0.05) {
                world.spawnParticles(
                    ParticleTypes.PORTAL,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0.2, 0.2, 0.2, 0.1
                );
            }
        }
    }
}