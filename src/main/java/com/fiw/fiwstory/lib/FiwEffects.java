package com.fiw.fiwstory.lib;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.joml.Vector3f;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Utilidades para efectos visuales, sonoros y de estado.
 * Proporciona métodos estáticos para aplicar efectos de forma consistente.
 */
public class FiwEffects {
    
    // Random thread-safe para efectos
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

    // ── Tick Scheduler ──────────────────────────────────────────────────────────
    private static final CopyOnWriteArrayList<ScheduledTask> TASK_QUEUE = new CopyOnWriteArrayList<>();
    private static boolean tickListenerRegistered = false;

    private static class ScheduledTask {
        final long runAtMs;
        final Runnable task;
        ScheduledTask(long runAtMs, Runnable task) {
            this.runAtMs = runAtMs;
            this.task = task;
        }
    }

    private static void ensureTickListenerRegistered() {
        if (!tickListenerRegistered) {
            tickListenerRegistered = true;
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                long now = System.currentTimeMillis();
                TASK_QUEUE.removeIf(t -> {
                    if (t == null) return true;
                    if (t.runAtMs <= now) {
                        try { t.task.run(); } catch (Exception e) { /* swallow */ }
                        return true;
                    }
                    return false;
                });
            });
        }
    }
    
    /**
     * Aplica un efecto de estado a una entidad con duración y nivel.
     */
    public static void applyStatusEffect(LivingEntity entity, StatusEffect effect, int duration, int amplifier) {
        if (entity == null || effect == null || entity.isDead()) {
            return;
        }
        
        StatusEffectInstance instance = new StatusEffectInstance(
            effect,
            duration,  // ticks (20 ticks = 1 segundo)
            amplifier,
            false,     // ambient (partículas menos visibles)
            true,      // show particles
            true       // show icon
        );
        
        entity.addStatusEffect(instance);
    }
    
    /**
     * Aplica efectos corruptos estándar (Wither + Slowness).
     */
    public static void applyCorruptionEffects(LivingEntity entity, int corruptionLevel) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        // Wither: duración y nivel basados en nivel de corrupción
        int witherDuration = 200 + (corruptionLevel * 40); // 10s + 2s por nivel
        int witherAmplifier = Math.min(2, corruptionLevel / 3); // Máximo Wither II
        
        applyStatusEffect(entity, StatusEffects.WITHER, witherDuration, witherAmplifier);
        
        // Slowness: duración y nivel basados en nivel de corrupción
        int slownessDuration = 200 + (corruptionLevel * 30); // 10s + 1.5s por nivel
        int slownessAmplifier = Math.min(2, corruptionLevel / 2); // Máximo Slowness II
        
        applyStatusEffect(entity, StatusEffects.SLOWNESS, slownessDuration, slownessAmplifier);
    }
    
    /**
     * Aplica efectos de penalidad por uso no autorizado de artefacto vinculado.
     */
    public static void applyUnauthorizedUsePenalty(PlayerEntity player) {
        if (player == null || player.isDead()) {
            return;
        }
        
        // Blindness + Wither 3 como especificado en el PLAN MAESTRO
        applyStatusEffect(player, StatusEffects.BLINDNESS, 200, 0); // 10 segundos
        applyStatusEffect(player, StatusEffects.WITHER, 300, 2);    // 15 segundos, Wither III
        
        // Sonido de advertencia
        playSoundAtEntity(player, SoundEvents.ENTITY_WITHER_AMBIENT, 0.8f, 0.5f);
        
        // Partículas de advertencia
        spawnParticlesAroundEntity(player, ParticleTypes.SMOKE, 20, 0.5);
    }
    
    /**
     * Genera partículas alrededor de una entidad.
     */
    public static void spawnParticlesAroundEntity(LivingEntity entity, ParticleEffect particle, int count, double radius) {
        if (entity == null || entity.getWorld().isClient()) {
            return;
        }
        
        World world = entity.getWorld();
        Vec3d pos = entity.getPos();
        Random random = RANDOM.get();
        
        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2 * radius;
            double offsetY = random.nextDouble() * entity.getHeight();
            double offsetZ = (random.nextDouble() - 0.5) * 2 * radius;
            
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    particle,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1, // count
                    0, 0, 0, // delta
                    0.0 // speed
                );
            }
        }
    }
    
    /**
     * Genera partículas en forma de explosión.
     */
    public static void spawnExplosionParticles(World world, Vec3d center, ParticleEffect particle, int count, double radius) {
        if (world.isClient()) {
            return;
        }
        
        Random random = RANDOM.get();
        
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radius;
            double height = (random.nextDouble() - 0.5) * radius;
            
            double x = center.x + Math.cos(angle) * distance;
            double y = center.y + height;
            double z = center.z + Math.sin(angle) * distance;
            
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    particle,
                    x, y, z,
                    1, // count
                    0, 0, 0, // delta
                    0.1 // speed
                );
            }
        }
    }
    
    /**
     * Genera partículas en línea entre dos puntos.
     */
    public static void spawnLineParticles(World world, Vec3d start, Vec3d end, ParticleEffect particle, int count) {
        if (world.isClient()) {
            return;
        }
        
        Vec3d direction = end.subtract(start);
        double length = direction.length();
        Vec3d step = direction.multiply(1.0 / count);
        
        for (int i = 0; i < count; i++) {
            Vec3d point = start.add(step.multiply(i));
            
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    particle,
                    point.x, point.y, point.z,
                    1, // count
                    0, 0, 0, // delta
                    0.0 // speed
                );
            }
        }
    }
    
    /**
     * Reproduce un sonido en la posición de una entidad.
     */
    public static void playSoundAtEntity(LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        if (entity == null || entity.getWorld().isClient()) {
            return;
        }
        
        World world = entity.getWorld();
        world.playSound(
            null, // player - null para todos los jugadores
            entity.getX(), entity.getY(), entity.getZ(),
            sound,
            SoundCategory.PLAYERS,
            volume,
            pitch
        );
    }
    
    /**
     * Reproduce un sonido en una posición específica.
     */
    public static void playSoundAtPosition(World world, Vec3d pos, SoundEvent sound, float volume, float pitch) {
        if (world.isClient()) {
            return;
        }
        
        world.playSound(
            null, // player - null para todos los jugadores
            pos.x, pos.y, pos.z,
            sound,
            SoundCategory.PLAYERS,
            volume,
            pitch
        );
    }
    
    /**
     * Aplica efectos visuales de "screen cracks" para corrupción.
     * Nota: Esto requiere implementación en el lado del cliente.
     */
    public static void applyScreenCrackEffect(PlayerEntity player, int corruptionLevel) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        // El efecto visual real se manejaría en el cliente
        // Por ahora, solo aplicamos efectos de estado relacionados
        
        if (corruptionLevel >= 3) {
            // Nivel 3+: Náusea ocasional
            if (RANDOM.get().nextFloat() < 0.01f) { // 1% chance por tick
                applyStatusEffect(player, StatusEffects.NAUSEA, 100, 0); // 5 segundos
            }
        }
        
        if (corruptionLevel >= 4) {
            // Nivel 4+: Ceguera momentánea ocasional
            if (RANDOM.get().nextFloat() < 0.005f) { // 0.5% chance por tick
                applyStatusEffect(player, StatusEffects.BLINDNESS, 40, 0); // 2 segundos
            }
        }
    }
    
    /**
     * Aplica efectos de "susurros" para corrupción alta.
     */
    public static void applyWhisperEffects(PlayerEntity player, int corruptionLevel) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        Random random = RANDOM.get();
        
        // Chance de susurro basada en nivel de corrupción
        float whisperChance = corruptionLevel * 0.001f; // 0.1% por nivel
        
        if (random.nextFloat() < whisperChance) {
            // Reproducir sonido de susurro
            playSoundAtEntity(player, SoundEvents.ENTITY_ENDERMAN_STARE, 0.3f, 1.8f);
            
            // Partículas de susurro
            spawnParticlesAroundEntity(player, ParticleTypes.SOUL_FIRE_FLAME, 5, 1.0);
        }
    }
    
    /**
     * Aplica todos los efectos de corrupción combinados.
     */
    public static void applyAllCorruptionEffects(PlayerEntity player, int corruptionLevel) {
        if (player == null || corruptionLevel <= 0) {
            return;
        }
        
        // Efectos de estado corruptos
        applyCorruptionEffects(player, corruptionLevel);
        
        // Efectos visuales de screen cracks
        applyScreenCrackEffect(player, corruptionLevel);
        
        // Efectos de susurros
        applyWhisperEffects(player, corruptionLevel);
        
        // Partículas de corrupción
        if (corruptionLevel >= 2) {
            ParticleEffect particle = corruptionLevel >= 4 ? 
                ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMOKE;
            int particleCount = Math.min(10, corruptionLevel * 2);
            spawnParticlesAroundEntity(player, particle, particleCount, 1.0);
        }
    }
    
    /**
     * Limpia todos los efectos de corrupción de un jugador.
     */
    public static void clearCorruptionEffects(PlayerEntity player) {
        if (player == null) {
            return;
        }
        
        // Remover efectos relacionados con corrupción
        player.removeStatusEffect(StatusEffects.WITHER);
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.NAUSEA);
        player.removeStatusEffect(StatusEffects.BLINDNESS);
        
        // Sonido de purificación
        playSoundAtEntity(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        
        // Partículas de purificación
        spawnParticlesAroundEntity(player, ParticleTypes.HAPPY_VILLAGER, 30, 1.5);
    }
    
    // ========== EFECTOS ÉPICOS PARA LANZA DE FI3W0 ==========
    
    /**
     * Efectos visuales para carga de ataque.
     */
    public static void spawnChargeParticles(PlayerEntity player, float chargeLevel) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        World world = player.getWorld();
        Vec3d pos = player.getPos();
        Random random = RANDOM.get();
        
        // Partículas que giran alrededor del jugador
        int particleCount = (int) (10 * chargeLevel);
        double radius = 1.0 + (chargeLevel * 0.5);
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (world.getTime() * 0.1) + (i * Math.PI * 2 / particleCount);
            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + 1.0 + (random.nextDouble() * 0.5);
            double z = pos.z + Math.sin(angle) * radius;
            
            if (world instanceof ServerWorld serverWorld) {
                // Usar partículas moradas de Minecraft
                serverWorld.spawnParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    1, 0, 0, 0, 0
                );
            }
        }
        
        // Sonido de carga (aumenta con nivel)
        if (chargeLevel > 0.5f) {
            playSoundAtEntity(player, SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 
                chargeLevel * 0.3f, 0.8f + (chargeLevel * 0.4f));
        }
    }
    
    /**
     * Efecto visual para ataque cargado nivel 2 (+50% daño).
     */
    public static void spawnChargedAttackEffect(PlayerEntity player, float multiplier, boolean hasGlasses) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        World world = player.getWorld();
        Vec3d pos = player.getPos();
        
        // Explosión de partículas moradas
        spawnExplosionParticles(world, pos, ParticleTypes.DRAGON_BREATH, 
            hasGlasses ? 50 : 30, hasGlasses ? 3.0 : 2.0);
        
        // Sonido épico
        playSoundAtEntity(player, SoundEvents.ENTITY_WITHER_SHOOT, 
            0.8f, 0.5f);
        
        // Onda de choque visual (partículas en cono)
        if (hasGlasses) {
            spawnConeParticles(player, 20, 5.0);
        }
    }
    
    /**
     * Efecto visual para ataque cargado nivel 3 (+100% daño, área).
     */
    public static void spawnUltimateAttackEffect(PlayerEntity player, boolean hasGlasses) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        World world = player.getWorld();
        Vec3d pos = player.getPos();
        
        // Explosión masiva
        spawnExplosionParticles(world, pos, ParticleTypes.SOUL_FIRE_FLAME, 
            hasGlasses ? 100 : 60, hasGlasses ? 4.0 : 3.0);
        
        // Anillo de partículas
        spawnRingParticles(world, pos, 30, hasGlasses ? 4.0 : 3.0);
        
        // Sonido definitivo
        playSoundAtEntity(player, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, 
            0.6f, 0.8f);
        
        // Efecto de screen shake (solo cliente)
        if (world.isClient()) {
            applyScreenShake(hasGlasses ? 1.5f : 1.0f);
        }
    }
    
    /**
     * Partículas en forma de cono (para ataque con alcance).
     */
    private static void spawnConeParticles(PlayerEntity player, int count, double range) {
        if (player.getWorld().isClient()) return;
        
        World world = player.getWorld();
        Vec3d pos = player.getPos();
        Vec3d look = player.getRotationVec(1.0f);
        Random random = RANDOM.get();
        
        for (int i = 0; i < count; i++) {
            double distance = random.nextDouble() * range;
            double spread = random.nextDouble() * 0.5;
            
            Vec3d particlePos = pos.add(
                look.x * distance + (random.nextDouble() - 0.5) * spread,
                look.y * distance + (random.nextDouble() - 0.5) * spread + 1.0,
                look.z * distance + (random.nextDouble() - 0.5) * spread
            );
            
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0
                );
            }
        }
    }
    
    /**
     * Partículas en forma de anillo.
     */
    private static void spawnRingParticles(World world, Vec3d center, int count, double radius) {
        if (world.isClient()) return;
        
        Random random = RANDOM.get();
        
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 1.0 + (random.nextDouble() * 2.0);
            
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    1, 0, 0.1, 0, 0.05
                );
            }
        }
    }
    
    /**
     * Aplica efecto de temblor de pantalla (solo cliente).
     */
    private static void applyScreenShake(float intensity) {
        // Esto se implementaría con un mixin o packet al cliente
        // Por ahora es un placeholder
    }
    
    /**
     * Efectos visuales para buffos activos.
     */
    public static void spawnBuffParticles(PlayerEntity player, String buffType, float intensity) {
        if (player == null || player.getWorld().isClient()) {
            return;
        }
        
        ParticleEffect particle;
        switch (buffType) {
            case "corruption":
                particle = ParticleTypes.DRAGON_BREATH; // Morado
                break;
            case "kill":
                particle = ParticleTypes.FLAME; // Rojo
                break;
            case "end":
                particle = ParticleTypes.PORTAL; // Azul End
                break;
            default:
                particle = ParticleTypes.SMOKE;
        }
        
        // Aura sutil alrededor del jugador
        int particleCount = (int) (5 * intensity);
        spawnParticlesAroundEntity(player, particle, 
            particleCount, 1.0 + intensity);
    }
    
    // ========== HABILIDAD WORLD BARRAGE (SUKUNA STYLE) ==========
    
    /**
     * Ejecuta la habilidad World Barrage (estilo Sukuna).
     * Solo disponible cuando se tienen las gafas de Fi3w0 equipadas.
     */
    public static void executeWorldBarrage(PlayerEntity player, LivingEntity target) {
        if (player == null || target == null || player.getWorld().isClient()) {
            return;
        }

        World world = player.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;

        // 1. Mensaje de activación
        worldBarrageSetup(player);

        // 2. Teletransporte inmediato detrás del objetivo
        Vec3d targetLook = target.getRotationVec(1.0f);
        Vec3d behindPos = target.getPos().subtract(targetLook.multiply(2.0));
        behindPos = new Vec3d(behindPos.x, target.getY(), behindPos.z);
        player.requestTeleport(behindPos.x, behindPos.y, behindPos.z);
        playSoundAtEntity(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        spawnParticlesAroundEntity(player, ParticleTypes.DRAGON_BREATH, 25, 1.0);

        // 3. Programar 16 slashes estilo Dismantle (Sukuna)
        scheduleWorldBarrageSequence(serverWorld, player, target);
    }
    
    /**
     * Setup inicial de la habilidad.
     */
    private static void worldBarrageSetup(PlayerEntity player) {
        World world = player.getWorld();
        
        // Mensaje de habilidad
        player.sendMessage(
            net.minecraft.text.Text.literal("§d§lYou're in my way...§r")
                .formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.BOLD),
            false
        );
        
        // Sonido de carga
        playSoundAtEntity(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.0f, 0.5f);
        
        // Partículas iniciales
        spawnParticlesAroundEntity(player, ParticleTypes.DRAGON_BREATH, 30, 2.0);
        
        // Efecto visual de screen (podría implementarse con shader)
        // Por ahora, partículas alrededor
    }
    
    /**
     * Programa la secuencia de slashes del World Barrage (Dismantle reworked).
     * 10 ondas × 2 slashes = 20 slashes en 4 segundos. Cada slash es un mini arc visual.
     * Daño: 3.5f por slash (~70 raw total). En práctica 4-6 conectan por posición aleatoria.
     */
    private static void scheduleWorldBarrageSequence(ServerWorld serverWorld, PlayerEntity player, LivingEntity target) {
        final int WAVES = 10;
        final int SLASHES_PER_WAVE = 2;
        final int WAVE_DELAY_TICKS = 8; // 0.4s entre ondas → 4s en total

        for (int wave = 0; wave < WAVES; wave++) {
            final int w = wave;
            scheduleDelayedTask(serverWorld, w * WAVE_DELAY_TICKS, () -> {
                if (!player.isAlive() || !target.isAlive()) return;

                Random random = RANDOM.get();
                Vec3d targetPos = target.getPos();

                for (int s = 0; s < SLASHES_PER_WAVE; s++) {
                    // Posición aleatoria alrededor del objetivo (±2 bloques)
                    double ox = (random.nextDouble() * 4.0) - 2.0;
                    double oz = (random.nextDouble() * 4.0) - 2.0;
                    Vec3d slashPos = targetPos.add(ox, 0, oz);

                    // Ángulo de roll aleatorio (0-360°) para slashes caóticos y diagonales
                    double rollDeg = random.nextDouble() * 360.0;

                    // Mini arc slash visual (mismo estilo que Arc Slash de la Espada del Caos)
                    spawnMiniArcSlash(serverWorld, slashPos, rollDeg);

                    // Sonido por slash con pitch ligeramente aleatorio
                    serverWorld.playSound(null, slashPos.x, slashPos.y, slashPos.z,
                        SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                        0.5f, 0.8f + random.nextFloat() * 0.5f);

                    // Daño: 3.5f raw por slash, penetración de armadura del 25% contemplada
                    Box hitBox = new Box(slashPos.x - 1.5, slashPos.y - 0.3, slashPos.z - 1.5,
                                        slashPos.x + 1.5, slashPos.y + 2.0, slashPos.z + 1.5);
                    for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class, hitBox,
                            e -> e != player && e.isAlive())) {
                        victim.damage(player.getDamageSources().playerAttack(player), 3.5f);
                        serverWorld.spawnParticles(ParticleTypes.CRIT,
                            victim.getX(), victim.getY() + victim.getHeight() / 2, victim.getZ(),
                            8, 0.3, 0.3, 0.3, 0.15);
                    }
                }
            });
        }

        // Tras el último slash: Wither I (4s) + sonido de impacto final
        scheduleDelayedTask(serverWorld, WAVES * WAVE_DELAY_TICKS + 2, () -> {
            if (!player.isAlive()) return;

            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.5f, 0.6f);

            if (target.isAlive()) {
                target.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WITHER, 80, 0, false, true // Wither I, 4 segundos
                ));
            }
        });
    }
    
    /**
     * Programa una tarea con delay en ticks.
     * Usa un enfoque simple con ejecución inmediata para testing.
     * En producción, usaríamos un sistema de tick scheduler.
     */
    private static void scheduleDelayedTask(ServerWorld serverWorld, int delayTicks, Runnable task) {
        if (task == null) return;
        ensureTickListenerRegistered();
        long delayMs = delayTicks * 50L; // 1 tick = 50ms a 20 TPS
        TASK_QUEUE.add(new ScheduledTask(System.currentTimeMillis() + delayMs, task));
    }
    
    /**
     * Ejecuta un slash rotativo.
     */
    private static void executeRotatingSlash(PlayerEntity player, LivingEntity target, int rollAngle) {
        World world = player.getWorld();
        Vec3d targetPos = target.getPos();
        
        // Calcular posición del slash (delante del objetivo)
        Vec3d look = target.getRotationVec(1.0f);
        Vec3d slashPos = targetPos.add(look.multiply(2.0));
        
        // 1. Partículas del slash
        spawnSlashParticles(world, slashPos, rollAngle);
        
        // 2. Sonido del slash
        playSoundAtPosition(world, slashPos, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);
        
        // 3. Daño en área (cono rotado)
        applyRotatingSlashDamage(player, target, slashPos, rollAngle);
        
        // 4. Partículas de hit si hay daño
        spawnHitParticles(target);
    }
    
    /**
     * Partículas para el slash con efecto visual de corte.
     */
    private static void spawnSlashParticles(World world, Vec3d position, int rollAngle) {
        // Solo generar partículas en el lado del servidor
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Convertir ángulo roll a radianes
            double rollRad = Math.toRadians(rollAngle);
            
            // Crear slash visual alrededor del objetivo
            double radius = 2.0;
            int slashPoints = 8; // Puntos para formar el slash
            
            for (int i = 0; i < slashPoints; i++) {
                double progress = (double) i / (slashPoints - 1);
                double angle = progress * Math.PI; // Medio círculo
                
                // Coordenadas del slash (rotadas según rollAngle)
                double x = position.x + Math.cos(angle + rollRad) * radius;
                double y = position.y + 1.0 + Math.sin(progress * Math.PI) * 0.5;
                double z = position.z + Math.sin(angle + rollRad) * radius;
                
                // Partículas de corte (crit)
                serverWorld.spawnParticles(
                    ParticleTypes.CRIT,
                    x, y, z,
                    3,
                    0.05, 0.05, 0.05,
                    0.0
                );
                
                // Partículas de bruja (witch) - color púrpura
                serverWorld.spawnParticles(
                    ParticleTypes.WITCH,
                    x, y, z,
                    2,
                    0.1, 0.1, 0.1,
                    0.0
                );
                
                // Partículas de chispas en los extremos
                if (i == 0 || i == slashPoints - 1) {
                    serverWorld.spawnParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x, y, z,
                        5,
                        0.2, 0.2, 0.2,
                        0.0
                    );
                }
            }
            
            // Línea central del slash
            for (int i = 0; i < 5; i++) {
                double offset = (i - 2) * 0.3;
                double x = position.x + Math.cos(rollRad) * offset;
                double y = position.y + 1.0;
                double z = position.z + Math.sin(rollRad) * offset;
                
                serverWorld.spawnParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    2,
                    0.05, 0.05, 0.05,
                    0.0
                );
            }
            
            // Efecto de explosión en el centro
            serverWorld.spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                position.x, position.y + 1.0, position.z,
                10,
                0.3, 0.3, 0.3,
                0.0
            );
        }
    }
    
    /**
     * Aplica daño en un cono rotado.
     */
    private static void applyRotatingSlashDamage(PlayerEntity player, LivingEntity target, Vec3d slashPos, int rollAngle) {
        World world = player.getWorld();
        double range = 4.0; // Ancho del slash (reducido para balance)
        double height = 1.0; // Altura del slash
        double angle = 90; // Ángulo del cono (reducido)
        
        // Buscar entidades en el área del slash
        Box slashBox = new Box(
            slashPos.x - range, slashPos.y - height, slashPos.z - range,
            slashPos.x + range, slashPos.y + height, slashPos.z + range
        );
        
        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, slashBox, e -> e != player && e.isAlive())) {
            // Verificar si está dentro del cono rotado
            if (isInRotatedCone(slashPos, entity.getPos(), rollAngle, angle, range)) {
                // Daño del slash (balanceado - ajustado para PvP)
                float damage = entity instanceof PlayerEntity ? 3.0f : 5.0f; // Reducido contra jugadores
                boolean wasAlive = entity.isAlive();
                entity.damage(player.getDamageSources().playerAttack(player), damage);
                
                // Verificar si se mató a un jugador (para buffos)
                if (!entity.isAlive() && wasAlive && entity instanceof PlayerEntity) {
                    // Notificar al jugador para aplicar buffos
                    // Esto se manejará en el evento de muerte del jugador
                }
                
                // Efectos adicionales (reducidos para PvP)
                if (!world.isClient()) {
                    int witherDuration = entity instanceof PlayerEntity ? 40 : 60; // Reducido contra jugadores
                    int slownessDuration = entity instanceof PlayerEntity ? 20 : 40; // Reducido contra jugadores
                    int slownessLevel = entity instanceof PlayerEntity ? 2 : 4; // Nivel reducido contra jugadores
                    
                    entity.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.WITHER, witherDuration, 0, false, false
                    ));
                    
                    // "Freeze" effect (slowness extremo)
                    entity.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SLOWNESS, slownessDuration, slownessLevel, false, false
                    ));
                }
            }
        }
    }
    
    /**
     * Verifica si una posición está dentro de un cono rotado.
     */
    private static boolean isInRotatedCone(Vec3d coneOrigin, Vec3d point, int rollAngle, double coneAngle, double range) {
        Vec3d direction = point.subtract(coneOrigin);
        double distance = direction.length();
        
        if (distance > range) return false;
        
        // Normalizar dirección
        direction = direction.multiply(1.0 / distance);
        
        // Para simplificar, asumimos cono hacia adelante en eje Z
        // En una implementación real, rotaríamos según el ángulo roll
        Vec3d forward = new Vec3d(0, 0, 1);
        
        // Calcular ángulo entre dirección y forward
        double dot = direction.dotProduct(forward);
        double angle = Math.acos(dot) * (180.0 / Math.PI);
        
        return angle <= (coneAngle / 2.0);
    }
    
    /**
     * Partículas de hit.
     */
    private static void spawnHitParticles(LivingEntity entity) {
        World world = entity.getWorld();
        Vec3d pos = entity.getPos();
        
        if (!world.isClient()) {
            // Partículas de hit encantado
            spawnParticlesAtPosition(world, pos, ParticleTypes.ENCHANTED_HIT, 20, 0.5);
        }
    }
    
    /**
     * Finale de la habilidad (teletransporte + daño final).
     */
    private static void worldBarrageFinale(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();
        
        // 1. Teletransporte detrás del objetivo
        Vec3d targetLook = target.getRotationVec(1.0f);
        Vec3d behindPos = target.getPos().subtract(targetLook.multiply(2.0));
        
        // Ajustar altura
        behindPos = new Vec3d(behindPos.x, target.getY(), behindPos.z);
        
        // Teletransportar jugador
        player.requestTeleport(behindPos.x, behindPos.y, behindPos.z);
        
        // 2. Sonido de teletransporte
        playSoundAtEntity(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        
        // 3. Partículas de teletransporte
        spawnParticlesAroundEntity(player, ParticleTypes.DRAGON_BREATH, 50, 1.0);
        
        // 4. Mensaje final
        player.sendMessage(
            net.minecraft.text.Text.literal("§c§lBehind you.§r")
                .formatted(net.minecraft.util.Formatting.RED, net.minecraft.util.Formatting.BOLD),
            false
        );
        
        // 5. Daño final (más fuerte)
        if (!world.isClient()) {
            float finalDamage = target instanceof PlayerEntity ? 5.0f : 8.0f; // Reducido contra jugadores
            target.damage(player.getDamageSources().playerAttack(player), finalDamage);
            
            // Efectos finales (reducidos para PvP)
            int witherDuration = target instanceof PlayerEntity ? 60 : 100; // Reducido contra jugadores
            int witherLevel = target instanceof PlayerEntity ? 0 : 1; // Nivel reducido contra jugadores
            
            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.WITHER, witherDuration, witherLevel, false, false
            ));
        }
    }
    
    /**
     * Helper para spawnear partículas en una posición.
     */
    public static void spawnParticlesAtPosition(World world, Vec3d pos, ParticleEffect particle, int count, double spread) {
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                particle,
                pos.x, pos.y + 1.0, pos.z,
                count,
                spread, spread, spread,
                0.0
            );
        }
    }

    /**
     * Genera un mini arc slash visual de 90° centrado en {@code center} con roll aleatorio.
     * Usa las mismas capas de partículas que el Arc Slash de la Espada del Caos.
     */
    private static void spawnMiniArcSlash(ServerWorld world, Vec3d center, double rollDeg) {
        double rollRad = Math.toRadians(rollDeg);
        Vec3d fwd   = new Vec3d(Math.cos(rollRad), 0, Math.sin(rollRad));
        Vec3d right = new Vec3d(-fwd.z, 0, fwd.x);

        final float arc     = 90f;
        final float radius  = 1.5f;
        final float yOffset = 0.9f;
        final float height  = 0.35f;
        final int   points  = 10;

        for (int pi = 0; pi <= points; pi++) {
            double progress = (double) pi / points;
            double thetaDeg = -arc / 2.0 + progress * arc; // -45 a +45
            Vec3d pos = miniArcPoint(center, fwd, right, thetaDeg, progress, radius, yOffset, height);

            world.spawnParticles(ParticleTypes.SWEEP_ATTACK,   pos.x, pos.y, pos.z, 1, 0,    0,    0,    0);
            world.spawnParticles(ParticleTypes.CRIT,           pos.x, pos.y, pos.z, 3, 0.05, 0.05, 0.05, 0.15);
            world.spawnParticles(ParticleTypes.ENCHANTED_HIT,  pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.10);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE,    pos.x, pos.y, pos.z, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    /**
     * Calcula un punto en el mini arco en coordenadas world-space.
     * Misma lógica que {@link #playerArcPoint} pero parametrizada para el mini slash.
     */
    private static Vec3d miniArcPoint(Vec3d center, Vec3d fwd, Vec3d right,
                                      double thetaDeg, double t,
                                      float radius, float yOffset, float height) {
        double theta = Math.toRadians(thetaDeg);
        double hx = center.x + radius * (Math.cos(theta) * fwd.x + Math.sin(theta) * right.x);
        double hz = center.z + radius * (Math.cos(theta) * fwd.z + Math.sin(theta) * right.z);
        double vertArc = Math.sin(Math.PI * t); // parábola 0→1→0
        double hy = center.y + yOffset + height * vertArc;
        return new Vec3d(hx, hy, hz);
    }

    // ========== HABILIDAD CRIMSON SLASH (ESPADA MGSHTRAKLAR) ==========

    /**
     * Lanza 3 garras de energía carmesí consecutivas en la dirección del jugador.
     * Adaptado de CrimsonSlashGoal para uso por jugadores.
     * Daño: 10.0 por garra (magic, bypass de armadura). Explosión final: 15.0 con falloff.
     */
    public static void executeCrimsonSlash(ServerWorld serverWorld, PlayerEntity player) {
        if (player == null || !player.isAlive()) return;

        Vec3d look = player.getRotationVec(1.0f);
        final Vec3d clawDir = new Vec3d(look.x, 0, look.z).normalize();
        final Vec3d startPos = player.getPos().add(0, 0.3, 0);

        final double clawSpeed   = 1.2;  // bloques/tick
        final int ticksPerClaw   = 10;   // 12 bloques / 1.2 = 10 ticks
        final int delayBetween   = 10;   // ticks entre garras
        final float clawDamage   = 10.0f;
        final float explosionDmg = 15.0f;
        final float explosionRad = 5.0f;
        final int clawCount      = 3;

        // Sonido inicial
        serverWorld.playSound(null, startPos.x, startPos.y, startPos.z,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5f, 0.6f);

        for (int c = 0; c < clawCount; c++) {
            final int clawIndex  = c;
            final double clawSz  = 1.0 + c * 0.5;
            final int clawStart  = c * delayBetween;
            final boolean isLast = (c == clawCount - 1);

            // Sonido de lanzamiento para garras 2 y 3
            if (c > 0) {
                scheduleDelayedTask(serverWorld, clawStart, () -> {
                    if (!player.isAlive()) return;
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                        1.5f, 0.6f + clawIndex * 0.1f);
                });
            }

            // Posición mutable de la garra (array de 1 elemento para mutabilidad en lambda)
            final Vec3d[] clawPos = {startPos};
            final Set<UUID> hitByThisClaw = new HashSet<>();

            for (int tick = 1; tick <= ticksPerClaw; tick++) {
                final boolean isLastTick = (tick == ticksPerClaw);
                scheduleDelayedTask(serverWorld, clawStart + tick, () -> {
                    if (!player.isAlive()) return;

                    clawPos[0] = clawPos[0].add(clawDir.multiply(clawSpeed));
                    Vec3d pos = clawPos[0];

                    // Partículas de la garra
                    spawnCrimsonClawParticles(serverWorld, pos, clawDir, clawSz);

                    // Daño (magic — 10% armor bypass como en el jefe original)
                    double hw = clawSz * 1.0;
                    Box hitBox = new Box(pos.x - hw, pos.y - 0.5, pos.z - hw,
                                        pos.x + hw, pos.y + 2.5, pos.z + hw);
                    for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class, hitBox,
                            e -> e != player && e.isAlive() && !hitByThisClaw.contains(e.getUuid()))) {
                        victim.damage(serverWorld.getDamageSources().magic(), clawDamage);
                        hitByThisClaw.add(victim.getUuid());
                    }

                    // Explosión al final de la última garra
                    if (isLastTick && isLast) {
                        spawnCrimsonExplosion(serverWorld, pos, explosionRad, explosionDmg, player);
                    }
                });
            }
        }
    }

    /**
     * Partículas de la garra carmesí: suelo + abanico vertical (de CrimsonSlashGoal).
     */
    private static void spawnCrimsonClawParticles(ServerWorld world, Vec3d pos, Vec3d clawDir, double clawSize) {
        Vec3d perp = new Vec3d(-clawDir.z, 0, clawDir.x);
        double halfWidth = clawSize * 0.8;

        // Rastro en el suelo
        for (double w = -halfWidth; w <= halfWidth; w += 0.4) {
            Vec3d p = pos.add(perp.multiply(w));
            world.spawnParticles(ParticleTypes.CRIT,
                p.x, p.y + 0.05, p.z, 1, 0.05, 0.0, 0.05, 0.0);
            world.spawnParticles(new DustParticleEffect(new Vector3f(0.8f, 0.0f, 0.3f), 1.2f),
                p.x, p.y + 0.1, p.z, 1, 0.05, 0.0, 0.05, 0.0);
        }

        // Abanico vertical
        double fanHeight = 1.5 + clawSize * 0.4;
        for (double h = 0.5; h <= fanHeight; h += 0.4) {
            world.spawnParticles(new DustParticleEffect(new Vector3f(0.9f, 0.0f, 0.2f), 1.0f),
                pos.x, pos.y + h, pos.z, 1, 0.05, 0.0, 0.05, 0.0);
            world.spawnParticles(ParticleTypes.CRIT,
                pos.x, pos.y + h, pos.z, 1, 0.04, 0.0, 0.04, 0.0);
        }
    }

    /**
     * Explosión carmesí al final del Crimson Slash (de CrimsonSlashGoal.triggerExplosion).
     */
    private static void spawnCrimsonExplosion(ServerWorld world, Vec3d pos,
                                               float radius, float damage, PlayerEntity attacker) {
        // Visuals
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
            pos.x, pos.y, pos.z, 2, 0.3, 0.3, 0.3, 0.0);
        double fs = radius * 0.4;
        world.spawnParticles(ParticleTypes.FLAME,
            pos.x, pos.y, pos.z, 50, fs, radius * 0.5, fs, 0.15);
        world.spawnParticles(new DustParticleEffect(new Vector3f(0.7f, 0.0f, 0.2f), 2.0f),
            pos.x, pos.y, pos.z, 30, radius * 0.3, radius * 0.3, radius * 0.3, 0.0);
        world.spawnParticles(ParticleTypes.LAVA,
            pos.x, pos.y, pos.z, 15, radius * 0.2, radius * 0.2, radius * 0.2, 0.0);

        // Anillos de FLAME ascendentes
        for (double h = 0; h <= radius; h += 0.5) {
            double ringR = radius * Math.sin(Math.PI * h / radius);
            int N = Math.max(4, (int)(ringR * 4));
            for (int i = 0; i < N; i++) {
                double angle = Math.toRadians(360.0 / N * i);
                world.spawnParticles(ParticleTypes.FLAME,
                    pos.x + Math.cos(angle) * ringR, pos.y + h, pos.z + Math.sin(angle) * ringR,
                    1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Sonidos
        world.playSound(null, pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0f, 0.7f);
        world.playSound(null, pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.2f, 0.5f);

        // Daño con falloff por distancia
        Box blastBox = new Box(pos.x - radius, pos.y - radius, pos.z - radius,
                               pos.x + radius, pos.y + radius, pos.z + radius);
        for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, blastBox,
                e -> e != attacker && e.isAlive())) {
            double dist = victim.getPos().distanceTo(pos);
            if (dist > radius) continue;
            float falloff = (float)(1.0 - dist / radius);
            victim.damage(world.getDamageSources().magic(), damage * falloff);
            Vec3d knock = victim.getPos().subtract(pos).normalize().multiply(1.8 * falloff);
            victim.addVelocity(knock.x, 0.8 * falloff, knock.z);
            victim.velocityModified = true;
        }
    }

    // ========== HABILIDAD ARC SLASH (ESPADA DEL CAOS) ==========

    /**
     * Ejecuta el Arc Slash — barre un arco de 180° frente al jugador en 6 ticks.
     * Daño: 8.0 por entidad (cada entidad solo golpeada una vez por slash).
     * Adaptado de ArcSlashGoal para uso por jugadores.
     */
    public static void executeArcSlash(ServerWorld serverWorld, PlayerEntity player) {
        if (player == null || !player.isAlive()) return;

        final float arc = 180f;
        final float radius = 4.0f;
        final float damage = 8.0f;
        final int duration = 6;
        final int points = 28;
        final float yOffset = 1.1f;
        final float height = 0.8f;
        final float hitRadius = 1.0f;

        // Bloquear orientación al inicio del slash
        Vec3d fwd = player.getRotationVec(1.0f);
        final Vec3d forward = new Vec3d(fwd.x, 0, fwd.z).normalize();
        final Vec3d right = new Vec3d(-forward.z, 0, forward.x);
        final Vec3d origin = player.getPos();

        // Set de entidades ya golpeadas (compartido entre ticks del barrido)
        Set<UUID> alreadyHit = new HashSet<>();

        // Sonido de inicio del slash
        serverWorld.playSound(null, origin.x, origin.y, origin.z,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.5f, 0.85f);

        // Programar cada tick del barrido
        for (int tick = 1; tick <= duration; tick++) {
            final int t = tick;
            scheduleDelayedTask(serverWorld, tick, () -> {
                if (!player.isAlive()) return;

                double prevT = (double)(t - 1) / duration;
                double currT = (double) t / duration;
                int iStart = (int)(prevT * points);
                int iEnd   = Math.min(points, (int)(currT * points) + 1);

                for (int pi = iStart; pi <= iEnd; pi++) {
                    double progress = (double) pi / points;
                    double thetaDeg = -arc / 2.0 + progress * arc;
                    Vec3d pos = playerArcPoint(origin, forward, right,
                                               thetaDeg, progress, radius, yOffset, height);

                    // Partículas del arco (mismo estilo que ArcSlashGoal)
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                        pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    serverWorld.spawnParticles(ParticleTypes.CRIT,
                        pos.x, pos.y, pos.z, 3, 0.07, 0.07, 0.07, 0.18);
                    serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                        pos.x, pos.y, pos.z, 2, 0.07, 0.07, 0.07, 0.12);

                    // Detección de golpe
                    Box hitBox = new Box(
                        pos.x - hitRadius, pos.y - hitRadius - 0.5, pos.z - hitRadius,
                        pos.x + hitRadius, pos.y + hitRadius + 0.5, pos.z + hitRadius);

                    for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class, hitBox,
                            e -> e != player && e.isAlive() && !alreadyHit.contains(e.getUuid()))) {
                        victim.damage(player.getDamageSources().playerAttack(player), damage);
                        alreadyHit.add(victim.getUuid());

                        // Knockback desde el jugador
                        Vec3d knock = victim.getPos().subtract(origin).normalize();
                        victim.addVelocity(knock.x * 0.8, 0.35, knock.z * 0.8);
                        victim.velocityModified = true;

                        // Sonido e impacto de golpe
                        serverWorld.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 0.85f);
                        serverWorld.spawnParticles(ParticleTypes.CRIT,
                            victim.getX(), victim.getY() + victim.getHeight() / 2, victim.getZ(),
                            14, 0.4, 0.4, 0.4, 0.25);
                        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                            victim.getX(), victim.getY() + victim.getHeight() / 2, victim.getZ(),
                            1, 0, 0, 0, 0);
                    }
                }

                // Flash al final del barrido
                if (t == duration) {
                    Vec3d endPos = playerArcPoint(origin, forward, right,
                        arc / 2.0, 1.0, radius, yOffset, height);
                    serverWorld.spawnParticles(ParticleTypes.FLASH,
                        endPos.x, endPos.y, endPos.z, 1, 0, 0, 0, 0);
                    serverWorld.spawnParticles(ParticleTypes.CRIT,
                        endPos.x, endPos.y, endPos.z, 8, 0.3, 0.3, 0.3, 0.3);
                    serverWorld.playSound(null, origin.x, origin.y, origin.z,
                        SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 1.8f);
                }
            });
        }
    }

    /**
     * Calcula un punto en el arco en coordenadas world-space.
     * Adaptado de ArcSlashGoal#arcPoint para uso por jugadores.
     *
     * @param thetaDeg  ángulo en grados desde el centro del arco (-arc/2 a +arc/2)
     * @param t         progreso normalizado 0→1 a lo largo del arco
     */
    private static Vec3d playerArcPoint(Vec3d origin, Vec3d forward, Vec3d right,
                                        double thetaDeg, double t,
                                        float radius, float yOffset, float height) {
        double theta = Math.toRadians(thetaDeg);
        double hx = origin.x + radius * (Math.cos(theta) * forward.x + Math.sin(theta) * right.x);
        double hz = origin.z + radius * (Math.cos(theta) * forward.z + Math.sin(theta) * right.z);
        double vertArc = Math.sin(Math.PI * t); // parabola 0→1→0
        double hy = origin.y + yOffset + height * vertArc;
        return new Vec3d(hx, hy, hz);
    }

}