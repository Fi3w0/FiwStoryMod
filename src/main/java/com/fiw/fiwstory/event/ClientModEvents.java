package com.fiw.fiwstory.event;

import com.fiw.fiwstory.item.ModItems;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Random;

/**
 * Eventos client-side separados de ModEvents para evitar que el servidor
 * intente cargar clases client-only (ClientWorld, ClientTickEvents, etc.).
 */
@Environment(EnvType.CLIENT)
public class ClientModEvents {

    private static final ThreadLocal<Long>    LAST_HAND_CHECK   = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<Boolean> LAST_HOLDING_SPEAR = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Random>  RANDOM            = ThreadLocal.withInitial(Random::new);
    private static int clientTickCounter = 0;

    public static void registerClientEvents() {
        // Partículas mientras sostienes la lanza — optimizado
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            clientTickCounter++;
            if (clientTickCounter % 3 != 0) return;

            PlayerEntity player = client.player;
            long currentTime = client.world.getTime();

            boolean holdingSpear;
            if (currentTime - LAST_HAND_CHECK.get() >= 10) {
                ItemStack main = player.getMainHandStack();
                ItemStack off  = player.getOffHandStack();
                holdingSpear = main.getItem() == ModItems.CURSED_SPEAR_OF_FI3W0
                            || off.getItem()  == ModItems.CURSED_SPEAR_OF_FI3W0;
                LAST_HAND_CHECK.set(currentTime);
                LAST_HOLDING_SPEAR.set(holdingSpear);
            } else {
                holdingSpear = LAST_HOLDING_SPEAR.get();
            }

            if (holdingSpear) {
                spawnHoldingParticles(client.world, player);
            }
        });
    }

    private static void spawnHoldingParticles(World world, PlayerEntity player) {
        if (!world.isClient()) return;
        ClientWorld clientWorld = (ClientWorld) world;
        if (world.getTime() % 15 != 0) return;

        Vec3d pos = player.getPos();
        Random random = RANDOM.get();
        double angle  = random.nextDouble() * Math.PI * 2;
        double radius = 0.5 + random.nextDouble() * 0.3;
        double height = 0.5 + random.nextDouble() * 1.5;

        clientWorld.addParticle(
            new DustParticleEffect(new Vector3f(0.6f, 0.2f, 0.8f), 0.8f),
            pos.x + Math.cos(angle) * radius,
            pos.y + height,
            pos.z + Math.sin(angle) * radius,
            0, 0, 0);
    }
}
