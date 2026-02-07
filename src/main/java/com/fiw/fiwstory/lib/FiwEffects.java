package com.fiw.fiwstory.lib;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Utilidades para efectos visuales, sonoros y de estado.
 * Proporciona métodos estáticos para aplicar efectos de forma consistente.
 */
public class FiwEffects {
    
    // Random thread-safe para efectos
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
    
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
        
        // 1. Setup inicial
        worldBarrageSetup(player);
        
        // 2. Ejecutar slashes rotativos (en secuencia)
        // Usaremos un sistema de ticks para la secuencia
        scheduleWorldBarrageSequence(player, target);
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
     * Programa la secuencia de slashes.
     */
    private static void scheduleWorldBarrageSequence(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();
        
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            // Ángulos de rotación para los slashes (12 slashes como en MythicMobs)
            int[] slashAngles = {0, 30, 60, 90, 120, 150, 180, -30, -60, -90, -120, -150};
            
            // Usar un sistema de ticks para programar las tareas
            for (int i = 0; i < slashAngles.length; i++) {
                final int angle = slashAngles[i];
                final int delayTicks = 8 + (i * 3); // 8 ticks inicial + 3 por cada slash
                
                // Programar tarea con delay usando el scheduler del server
                scheduleDelayedTask(serverWorld, delayTicks, () -> {
                    if (player.isAlive() && target.isAlive()) {
                        executeRotatingSlash(player, target, angle);
                    }
                });
            }
            
            // Programar teletransporte final y daño
            final int finalDelay = 8 + (slashAngles.length * 3) + 5;
            scheduleDelayedTask(serverWorld, finalDelay, () -> {
                if (player.isAlive() && target.isAlive()) {
                    worldBarrageFinale(player, target);
                }
            });
        }
    }
    
    /**
     * Programa una tarea con delay en ticks.
     * Usa un enfoque simple con ejecución inmediata para testing.
     * En producción, usaríamos un sistema de tick scheduler.
     */
    private static void scheduleDelayedTask(ServerWorld serverWorld, int delayTicks, Runnable task) {
        // Ejecutar inmediatamente para testing
        // En una implementación real, usaríamos serverWorld.getServer().executeDelayed()
        serverWorld.getServer().execute(task);
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
    

}