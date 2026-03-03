package com.fiw.fiwstory.effect;

/**
 * Constantes del sistema de corrupción.
 */
public final class CorruptionConstants {

    private CorruptionConstants() {}

    // ========== COLORES ==========
    public static final int EFFECT_COLOR = 0x4B0082; // Morado índigo

    // ========== INTERVALOS DE TICK ==========
    public static final int TICKS_PER_SECOND = 20;
    public static final int UPDATE_INTERVAL_TICKS = 20; // Cada segundo
    public static final int TICK_PROCESSING_INTERVAL = 5; // Cada 5 ticks (4Hz)

    // ========== TIEMPOS DE CORRUPCIÓN ==========
    public static final int REMOVAL_DELAY_TICKS = TICKS_PER_SECOND * 15; // 15 segundos
    public static final int MIN_ACTIVATION_TICKS = TICKS_PER_SECOND * 60 * 30; // 30 minutos
    public static final int MAX_ACTIVATION_TICKS = TICKS_PER_SECOND * 60 * 90; // 90 minutos

    // ========== INMUNIDAD ==========
    public static final int IMMUNITY_WHISPER_INTERVAL_TICKS = TICKS_PER_SECOND * 60 * 10; // 10 minutos

    // ========== PROBABILIDADES POR SEGUNDO ==========
    public static final float LEVEL1_DAMAGE_CHANCE = 0.05f;
    public static final float LEVEL2_BLINDNESS_CHANCE = 0.01f;
    public static final float LEVEL3_FATIGUE_CHANCE = 0.02f;
    public static final float LEVEL5_DAMAGE_CHANCE = 0.1f;
    public static final float LEVEL5_WHISPER_CHANCE = 0.15f;
    public static final float LEVEL5_NAUSEA_CHANCE = 0.03f;

    // ========== PROBABILIDADES DE SUSURROS ==========
    public static final float WHISPER_BASE_CHANCE = 0.01f;
    public static final float WHISPER_LEVEL_BONUS = 0.005f;
    public static final float HIGH_LEVEL_WHISPER_THRESHOLD = 0.3f; // 30% de frase nivel alto en nivel 4+

    // ========== DAÑO ==========
    public static final float LEVEL1_DAMAGE_BASE = 0.5f;
    public static final float LEVEL1_DAMAGE_RANGE = 1.0f;
    public static final float LEVEL5_DAMAGE_BASE = 1.0f;
    public static final float LEVEL5_DAMAGE_RANGE = 1.0f;

    // ========== DURACIONES DE EFECTOS (ticks) ==========
    public static final int BLINDNESS_DURATION_BASE = 40; // 2 segundos
    public static final int BLINDNESS_DURATION_RANGE = 40; // +0-2 segundos
    public static final int FATIGUE_DURATION_BASE = 100; // 5 segundos
    public static final int FATIGUE_DURATION_RANGE = 100; // +0-5 segundos
    public static final int NAUSEA_DURATION_BASE = 80; // 4 segundos
    public static final int NAUSEA_DURATION_RANGE = 80; // +0-4 segundos

    // ========== REDUCCIÓN DE SALUD ==========
    public static final double HEALTH_REDUCTION_PER_LEVEL = 2.0; // Puntos por nivel sobre 2
    public static final int HEALTH_REDUCTION_MIN_LEVEL = 3;
    public static final int HEALTH_REDUCTION_MAX_LEVEL = 4;

    // ========== SONIDO - SUSURROS NORMALES ==========
    public static final float WHISPER_VOLUME = 0.3f;
    public static final float WHISPER_PITCH_BASE = 1.8f;
    public static final float WHISPER_PITCH_RANGE = 0.4f;

    // ========== SONIDO - SUSURROS DE NIVEL ALTO ==========
    public static final float HIGH_WHISPER_VOLUME = 0.4f;
    public static final float HIGH_WHISPER_PITCH_BASE = 0.5f;
    public static final float HIGH_WHISPER_PITCH_RANGE = 0.5f;

    // ========== SONIDO - SUSURROS DE INMUNIDAD ==========
    public static final float IMMUNE_WHISPER_VOLUME = 0.2f;
    public static final float IMMUNE_WHISPER_PITCH_BASE = 1.5f;
    public static final float IMMUNE_WHISPER_PITCH_RANGE = 0.5f;
}
