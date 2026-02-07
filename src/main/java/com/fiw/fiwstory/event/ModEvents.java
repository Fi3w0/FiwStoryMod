package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.item.custom.FallenGodHeartArtifact;
import com.fiw.fiwstory.lib.FiwUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Random;

public class ModEvents {
    
    // Cache para verificaciones de manos (cada 10 ticks)
    private static final ThreadLocal<Long> LAST_HAND_CHECK = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<Boolean> LAST_HOLDING_SPEAR = ThreadLocal.withInitial(() -> false);
    
    // Random thread-safe
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
    
    // Contador de ticks para optimización
    private static int serverTickCounter = 0;
    private static int clientTickCounter = 0;
    
    public static void registerClientEvents() {
        // Partículas mientras sostienes la lanza - OPTIMIZADO
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                clientTickCounter++;
                
                // Solo procesar cada 3 ticks (6.6Hz en lugar de 20Hz)
                if (clientTickCounter % 3 != 0) {
                    return;
                }
                
                PlayerEntity player = client.player;
                long currentTime = client.world.getTime();
                
                // Cachear verificación de manos cada 10 ticks
                boolean holdingSpear;
                if (currentTime - LAST_HAND_CHECK.get() >= 10) {
                    ItemStack mainHand = player.getMainHandStack();
                    ItemStack offHand = player.getOffHandStack();
                    
                    holdingSpear = mainHand.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0 || 
                                  offHand.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0;
                    
                    LAST_HAND_CHECK.set(currentTime);
                    LAST_HOLDING_SPEAR.set(holdingSpear);
                } else {
                    holdingSpear = LAST_HOLDING_SPEAR.get();
                }
                
                if (holdingSpear && client.world.isClient()) {
                    spawnHoldingParticles(client.world, player);
                }
            }
        });
    }
    
    private static void spawnHoldingParticles(World world, PlayerEntity player) {
        if (world.isClient()) {
            ClientWorld clientWorld = (ClientWorld) world;
            Vec3d pos = player.getPos();
            Random random = RANDOM.get();
            
            // Partículas suaves alrededor del jugador (optimizado)
            // Solo generar 1 partícula en lugar de 2, y menos frecuente
            if (world.getTime() % 15 == 0) { // Solo cada 15 ticks (0.75 segundos)
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = 0.5 + random.nextDouble() * 0.3;
                double height = 0.5 + random.nextDouble() * 1.5;
                
                double x = pos.x + Math.cos(angle) * radius;
                double y = pos.y + height;
                double z = pos.z + Math.sin(angle) * radius;
                
                Vector3f color = new Vector3f(0.6f, 0.2f, 0.8f); // Morado
                clientWorld.addParticle(new DustParticleEffect(color, 0.8f),
                    x, y, z, 0, 0, 0);
            }
        }
    }
    
    public static void registerServerEvents() {
        // Explosión de partículas al usar habilidad
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            
            if (stack.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0 && !world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                Vec3d pos = player.getPos();
                
                // Explosión optimizada de partículas moradas
                spawnAbilityExplosionOptimized(serverWorld, pos);
            }
            
            return TypedActionResult.pass(stack);
        });
        
        // Tick del servidor para efectos del Corazón de Dios Caído - OPTIMIZADO
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverTickCounter++;
            
            // Solo procesar cada 5 ticks (4Hz en lugar de 20Hz)
            if (serverTickCounter % 5 != 0) {
                return;
            }
            
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FallenGodHeartArtifact.handleHeartEffects(player, player.getWorld());
            }
        });
    }
    
    private static void spawnAbilityExplosionOptimized(ServerWorld world, Vec3d center) {
        Random random = RANDOM.get();
        
        // Partículas de polvo morado intenso - REDUCIDAS de 30 a 15
        for (int i = 0; i < 15; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = random.nextDouble() * Math.PI;
            double distance = random.nextDouble() * 1.5;
            
            double x = center.x + Math.sin(pitch) * Math.cos(angle) * distance;
            double y = center.y + Math.cos(pitch) * distance;
            double z = center.z + Math.sin(pitch) * Math.sin(angle) * distance;
            
            Vector3f color = new Vector3f(0.7f, 0.1f, 0.9f); // Morado intenso
            world.spawnParticles(new DustParticleEffect(color, 1.2f),
                x, y, z, 1, 0, 0, 0, 0.05);
        }
        
        // Partículas de encantamiento - REDUCIDAS de 15 a 8
        for (int i = 0; i < 8; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = random.nextDouble() * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;
            
            world.spawnParticles(ParticleTypes.ENCHANT,
                center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                1, 0, 0, 0, 0.1);
        }
        
        // Partículas de portal - REDUCIDAS de 10 a 5
        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.5;
            double offsetY = random.nextDouble() * 1.0;
            double offsetZ = (random.nextDouble() - 0.5) * 1.5;
            
            world.spawnParticles(ParticleTypes.PORTAL,
                center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                1, 0, 0, 0, 0.08);
        }
    }
    
    // Método original mantenido para compatibilidad
    private static void spawnAbilityExplosion(ServerWorld world, Vec3d center) {
        spawnAbilityExplosionOptimized(world, center);
    }
}