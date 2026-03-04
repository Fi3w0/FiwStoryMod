package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.item.custom.FallenGodHeartArtifact;
import com.fiw.fiwstory.item.custom.PlainCopperRingArtifact;
import com.fiw.fiwstory.lib.TrinketHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Random;

public class ModEvents {

    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
    private static int serverTickCounter = 0;

    public static void registerServerEvents() {
        // Explosión de partículas al usar habilidad
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0 && !world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                Vec3d pos = player.getPos();
                spawnAbilityExplosionOptimized(serverWorld, pos);
            }

            return TypedActionResult.pass(stack);
        });

        // Tick del servidor para efectos del Corazón de Dios Caído
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverTickCounter++;

            // Solo procesar cada 5 ticks (4Hz)
            if (serverTickCounter % 5 != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FallenGodHeartArtifact.handleHeartEffects(player, player.getWorld());

                if (serverTickCounter % 10 == 0) {
                    checkNearbyLightning(player);
                }
            }
        });
    }

    private static void spawnAbilityExplosionOptimized(ServerWorld world, Vec3d center) {
        Random random = RANDOM.get();

        for (int i = 0; i < 15; i++) {
            double angle    = random.nextDouble() * Math.PI * 2;
            double pitch    = random.nextDouble() * Math.PI;
            double distance = random.nextDouble() * 1.5;

            double x = center.x + Math.sin(pitch) * Math.cos(angle) * distance;
            double y = center.y + Math.cos(pitch) * distance;
            double z = center.z + Math.sin(pitch) * Math.sin(angle) * distance;

            world.spawnParticles(new DustParticleEffect(new Vector3f(0.7f, 0.1f, 0.9f), 1.2f),
                x, y, z, 1, 0, 0, 0, 0.05);
        }

        for (int i = 0; i < 8; i++) {
            world.spawnParticles(ParticleTypes.ENCHANT,
                center.x + (random.nextDouble() - 0.5) * 2.0,
                center.y + random.nextDouble() * 1.5,
                center.z + (random.nextDouble() - 0.5) * 2.0,
                1, 0, 0, 0, 0.1);
        }

        for (int i = 0; i < 5; i++) {
            world.spawnParticles(ParticleTypes.PORTAL,
                center.x + (random.nextDouble() - 0.5) * 1.5,
                center.y + random.nextDouble() * 1.0,
                center.z + (random.nextDouble() - 0.5) * 1.5,
                1, 0, 0, 0, 0.08);
        }
    }

    private static void checkNearbyLightning(ServerPlayerEntity player) {
        World world = player.getWorld();
        if (!world.isThundering()) return;

        if (!TrinketHelper.hasArtifactOfType(player, PlainCopperRingArtifact.class)) return;

        net.minecraft.util.math.Box searchBox = player.getBoundingBox().expand(12.0);
        for (net.minecraft.entity.Entity entity : world.getOtherEntities(player, searchBox)) {
            if (entity instanceof net.minecraft.entity.LightningEntity lightning) {
                if (lightning.age < 5) {
                    PlainCopperRingArtifact.onNearbyLightningStrike(player, world, lightning.getPos());
                    break;
                }
            }
        }
    }
}
