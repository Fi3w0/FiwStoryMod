package com.fiw.fiwstory.effect;

import com.fiw.fiwstory.command.ImmunityCommand;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Efecto de corrupción personalizado.
 * Niveles 1-5 con efectos progresivos:
 * - Pantalla morada (tint)
 * - Daño gradual ignorando armadura
 * - Susurros aleatorios
 * - Ceguera momentánea
 * - Fatiga en fases altas
 * - Reducción de vida máxima en fases 3-4
 */
public class CorruptionStatusEffect extends StatusEffect {
    
    // Random thread-safe para efectos
    private static final ThreadLocal<net.minecraft.util.math.random.Random> RANDOM = 
        ThreadLocal.withInitial(() -> net.minecraft.util.math.random.Random.create());
    
    // UUID para modificador de salud
    private static final java.util.UUID CORRUPTION_HEALTH_MODIFIER_ID = 
        java.util.UUID.fromString("c0ff0000-0000-0000-0000-000000000000");
    
    // Lista de susurros (42 frases)
    private static final List<String> WHISPERS = new ArrayList<>();
    private static final List<String> HIGH_LEVEL_WHISPERS = new ArrayList<>();
    
    static {
        // Frases para todos los niveles (30 frases)
        WHISPERS.addAll(List.of(
            "Te pertenecemos...",
            "La corrupción es inevitable...",
            "Siente el vacío...",
            "No hay escape...",
            "Somos uno...",
            "La oscuridad te llama...",
            "Abraza la nada...",
            "Tus pensamientos son nuestros...",
            "La verdad duele...",
            "No hay purificación...",
            "Eres nuestro ahora...",
            "La sombra crece...",
            "Olvida quién eras...",
            "El fin se acerca...",
            "Nada importa...",
            "Déjate llevar...",
            "La mentira de la luz...",
            "En la oscuridad está la verdad...",
            "Tus miedos nos alimentan...",
            "No hay retorno...",
            "La corrupción es sabiduría...",
            "Ve lo que otros no ven...",
            "La pureza es una ilusión...",
            "Somos eternos...",
            "Tu alma es nuestra...",
            "La decadencia es belleza...",
            "No luches...",
            "Todo se desmorona...",
            "La quietud te espera...",
            "Eres polvo en el viento..."
        ));
        
        // Frases solo para niveles altos (12 frases)
        HIGH_LEVEL_WHISPERS.addAll(List.of(
            "TU CARNE SE DESHACE...",
            "LOS DIOSES TE HAN ABANDONADO...",
            "EL VACÍO TE CONSUME...",
            "NO HAY MÁS ALMA QUE CORROMPER...",
            "LA REALIDAD SE DESGARRA...",
            "ERES NUESTRA CREACIÓN...",
            "EL FINAL ES DULCE...",
            "LA LOCURA ES CLARIDAD...",
            "TODO SE DESINTEGRA...",
            "LA ETERNIDAD TE ESPERA...",
            "NO HAY MÁS LUZ...",
            "LA CORRUPCIÓN ES TU DESTINO..."
        ));
    }
    
    public CorruptionStatusEffect() {
        super(
            StatusEffectCategory.HARMFUL, // Efecto dañino
            0x4B0082 // Color morado índigo para partículas
        );
    }
    
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Aplicar efectos cada segundo (20 ticks)
        return duration % 20 == 0;
    }
    
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }
        
        if (entity.getWorld().isClient()) {
            // Efectos visuales/sonoros en cliente
            return;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Random random = RANDOM.get();
        
        // Nivel de corrupción (amplifier + 1)
        int corruptionLevel = amplifier + 1;
        
        // Verificar si el jugador es inmune
        boolean isImmune = ImmunityCommand.isPlayerImmune(serverPlayer.getServer().getCommandSource(), player.getUuid());
        
        // ========== EFECTOS POR NIVEL ==========
        
        // Nivel 1-3: Jugable pero desagradable
        if (corruptionLevel >= 1) {
            // Daño gradual ignorando armadura (0.5-1.5 corazones por minuto)
            // Solo si NO es inmune
            if (!isImmune && random.nextFloat() < 0.05f) { // 5% chance por segundo
                float damage = 0.5f + (random.nextFloat() * 1.0f);
                // Daño que ignora armadura
                player.damage(player.getDamageSources().magic(), damage);
            }
            
            // Susurros ocasionales (siempre, incluso para inmunes)
            if (random.nextFloat() < getWhisperChance(corruptionLevel)) {
                sendWhisper(player, corruptionLevel);
            }
        }
        
        // Nivel 2+: Ceguera momentánea (solo si NO es inmune)
        if (!isImmune && corruptionLevel >= 2 && random.nextFloat() < 0.01f) { // 1% chance por segundo
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.BLINDNESS,
                40 + (random.nextInt(40)), // 2-4 segundos
                0,
                false,
                false
            ));
        }
        
        // Nivel 3+: Fatiga (solo si NO es inmune)
        if (!isImmune && corruptionLevel >= 3 && random.nextFloat() < 0.02f) { // 2% chance por segundo
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE,
                100 + (random.nextInt(100)), // 5-10 segundos
                Math.min(2, corruptionLevel - 3), // Nivel 0-2
                false,
                false
            ));
        }
        
        // Nivel 3-4: Reducción de vida máxima (solo si NO es inmune)
        if (!isImmune && corruptionLevel >= 3 && corruptionLevel <= 4) {
            adjustMaxHealth(player, corruptionLevel);
        }
        
        // Nivel 5: Efectos extremos (solo si NO es inmune)
        if (!isImmune && corruptionLevel >= 5) {
            // Más daño
            if (random.nextFloat() < 0.1f) { // 10% chance por segundo
                player.damage(player.getDamageSources().magic(), 1.0f + random.nextFloat());
            }
            
            // Susurros más frecuentes y fuertes (siempre)
            if (random.nextFloat() < 0.15f) {
                sendHighLevelWhisper(player);
            }
            
            // Náusea ocasional
            if (random.nextFloat() < 0.03f) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA,
                    80 + (random.nextInt(80)), // 4-8 segundos
                    0,
                    false,
                    false
                ));
            }
        }
    }
    
    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        
        if (entity instanceof PlayerEntity player && !player.getWorld().isClient()) {
            int corruptionLevel = amplifier + 1;
            
            // Ajustar vida máxima si es nivel 3-4
            if (corruptionLevel >= 3 && corruptionLevel <= 4) {
                adjustMaxHealth(player, corruptionLevel);
            }
            
            // NO mensajes - los jugadores deben descubrir
            // Solo efectos visuales sutiles
        }
    }
    
    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        
        if (entity instanceof PlayerEntity player && !player.getWorld().isClient()) {
            // Restaurar vida máxima si fue reducida
            restoreMaxHealth(player);
            
            // NO mensajes - los jugadores deben descubrir
            // Solo efectos visuales sutiles
        }
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private float getWhisperChance(int corruptionLevel) {
        // Chance base aumenta con nivel
        return 0.01f + (corruptionLevel * 0.005f); // 1% + 0.5% por nivel
    }
    
    private void sendWhisper(PlayerEntity player, int corruptionLevel) {
        Random random = RANDOM.get();
        String whisper;
        
        if (corruptionLevel >= 4 && random.nextFloat() < 0.3f) {
            // 30% chance de frase de nivel alto en nivel 4+
            whisper = HIGH_LEVEL_WHISPERS.get(random.nextInt(HIGH_LEVEL_WHISPERS.size()));
        } else {
            whisper = WHISPERS.get(random.nextInt(WHISPERS.size()));
        }
        
        // Formatear mensaje
        Text message = Text.literal("§8«" + whisper + "»§r")
            .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        
        player.sendMessage(message, false);
        
        // Sonido de susurro (usar sonido de enderman o fantasma)
        if (!player.getWorld().isClient()) {
            player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                net.minecraft.sound.SoundEvents.ENTITY_ENDERMAN_STARE,
                net.minecraft.sound.SoundCategory.AMBIENT,
                0.3f,
                1.8f + (random.nextFloat() * 0.4f)
            );
        }
    }
    
    private void sendHighLevelWhisper(PlayerEntity player) {
        Random random = RANDOM.get();
        String whisper = HIGH_LEVEL_WHISPERS.get(random.nextInt(HIGH_LEVEL_WHISPERS.size()));
        
        Text message = Text.literal("§4§o«" + whisper + "»§r")
            .formatted(Formatting.DARK_RED, Formatting.ITALIC);
        
        player.sendMessage(message, false);
        
        // Sonido más intenso
        if (!player.getWorld().isClient()) {
            player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                net.minecraft.sound.SoundEvents.ENTITY_WITHER_AMBIENT,
                net.minecraft.sound.SoundCategory.AMBIENT,
                0.4f,
                0.5f + (random.nextFloat() * 0.5f)
            );
        }
    }
    
    private void adjustMaxHealth(PlayerEntity player, int corruptionLevel) {
        EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        // Reducción basada en nivel (1-3 corazones)
        double reduction = 2.0 * (corruptionLevel - 2); // 2, 4, 6 puntos (1-3 corazones)
        
        // Verificar si ya aplicamos la reducción
        net.minecraft.entity.attribute.EntityAttributeModifier existingModifier = 
            maxHealthAttr.getModifier(CORRUPTION_HEALTH_MODIFIER_ID);
        
        if (existingModifier == null && reduction > 0) {
            maxHealthAttr.addPersistentModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                CORRUPTION_HEALTH_MODIFIER_ID,
                "corruption_health_reduction",
                -reduction,
                net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION
            ));
            
            // Ajustar vida actual si es necesario
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }
    
    private void restoreMaxHealth(PlayerEntity player) {
        EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        // Remover modificador de corrupción
        maxHealthAttr.removeModifier(CORRUPTION_HEALTH_MODIFIER_ID);
        
        // Asegurar que la vida no exceda el máximo
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
    
    // ========== MÉTODOS PÚBLICOS ESTÁTICOS ==========
    
    /**
     * Obtiene una instancia del efecto de corrupción.
     */
    public static StatusEffectInstance getInstance(int level, int duration) {
        return new StatusEffectInstance(
            ModStatusEffects.CORRUPTION,
            duration,
            Math.max(0, level - 1), // Convertir nivel 1-5 a amplifier 0-4
            false,
            true,
            true
        );
    }
    
    /**
     * Aplica el efecto de corrupción a un jugador.
     */
    public static void applyToPlayer(PlayerEntity player, int level, int duration) {
        if (player.getWorld().isClient()) return;
        
        StatusEffectInstance instance = getInstance(level, duration);
        player.addStatusEffect(instance);
    }
    
    /**
     * Remueve el efecto de corrupción de un jugador.
     */
    public static void removeFromPlayer(PlayerEntity player) {
        player.removeStatusEffect(ModStatusEffects.CORRUPTION);
    }
    
    /**
     * Obtiene el nivel actual de corrupción de un jugador.
     * Retorna 0 si no tiene el efecto.
     */
    public static int getPlayerCorruptionLevel(PlayerEntity player) {
        StatusEffectInstance effect = player.getStatusEffect(ModStatusEffects.CORRUPTION);
        if (effect == null) return 0;
        return effect.getAmplifier() + 1; // Convertir amplifier 0-4 a nivel 1-5
    }
}