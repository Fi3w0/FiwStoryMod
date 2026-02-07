package com.fiw.fiwstory.lib;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;
import java.util.UUID;

/**
 * Utilidades generales para Fiw Story Mod.
 * Funciones de propósito general y helpers comunes.
 */
public class FiwUtils {
    
    // Random thread-safe para utilidades
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
    
    // Formateo de texto
    public static final Formatting ARTIFACT_NAME_FORMAT = Formatting.DARK_PURPLE;
    public static final Formatting ARTIFACT_DESC_FORMAT = Formatting.GRAY;
    public static final Formatting ARTIFACT_TYPE_FORMAT = Formatting.LIGHT_PURPLE;
    public static final Formatting ARTIFACT_FEATURE_FORMAT = Formatting.GRAY;
    public static final Formatting ARTIFACT_QUOTE_FORMAT = Formatting.DARK_GRAY;
    
    /**
     * Genera un Identifier con el namespace del mod.
     */
    public static Identifier id(String path) {
        return new Identifier("fiwstory", path);
    }
    
    /**
     * Verifica si un jugador es admin (OP level 2+).
     */
    public static boolean isAdmin(PlayerEntity player) {
        if (player == null || !(player instanceof ServerPlayerEntity)) {
            return false;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        return serverPlayer.hasPermissionLevel(2); // OP level 2+
    }
    
    /**
     * Verifica si un jugador puede usar un artefacto vinculado.
     */
    public static boolean canUseBoundArtifact(PlayerEntity player, ItemStack artifact) {
        if (player == null || artifact.isEmpty()) {
            return false;
        }
        
        // Si el artefacto no está vinculado, cualquiera puede usarlo
        if (!FiwNBT.isBound(artifact)) {
            return true;
        }
        
        // Admins pueden usar cualquier artefacto
        if (isAdmin(player)) {
            return true;
        }
        
        // Verificar si el artefacto está vinculado a este jugador
        UUID boundTo = FiwNBT.getBoundTo(artifact);
        return player.getUuid().equals(boundTo);
    }
    
    /**
     * Verifica si el jugador tiene las gafas de Fi3w0 equipadas.
     */
    public static boolean hasFi3w0GlassesEquipped(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        
        ItemStack headSlot = player.getInventory().getArmorStack(3); // Slot 3 = cabeza
        return headSlot.getItem() instanceof com.fiw.fiwstory.item.custom.Fi3w0GlassesArmor;
    }
    
    /**
     * Envía un mensaje de error a un jugador.
     */
    public static void sendErrorMessage(PlayerEntity player, String message) {
        if (player == null) {
            return;
        }
        
        player.sendMessage(
            Text.literal("✗ " + message)
                .formatted(Formatting.RED),
            false
        );
    }
    
    /**
     * Envía un mensaje de éxito a un jugador.
     */
    public static void sendSuccessMessage(PlayerEntity player, String message) {
        if (player == null) {
            return;
        }
        
        player.sendMessage(
            Text.literal("✓ " + message)
                .formatted(Formatting.GREEN),
            false
        );
    }
    
    /**
     * Envía un mensaje de información a un jugador.
     */
    public static void sendInfoMessage(PlayerEntity player, String message) {
        if (player == null) {
            return;
        }
        
        player.sendMessage(
            Text.literal("ℹ " + message)
                .formatted(Formatting.AQUA),
            false
        );
    }
    
    /**
     * Envía un mensaje de advertencia a un jugador.
     */
    public static void sendWarningMessage(PlayerEntity player, String message) {
        if (player == null) {
            return;
        }
        
        player.sendMessage(
            Text.literal("⚠ " + message)
                .formatted(Formatting.YELLOW),
            false
        );
    }
    
    /**
     * Formatea un texto como nombre de artefacto.
     */
    public static Text formatArtifactName(String name) {
        return Text.literal("«" + name + "»")
            .formatted(ARTIFACT_NAME_FORMAT, Formatting.BOLD);
    }
    
    /**
     * Formatea un texto como descripción de artefacto.
     */
    public static Text formatArtifactDescription(String description) {
        return Text.literal(description)
            .formatted(ARTIFACT_DESC_FORMAT, Formatting.ITALIC);
    }
    
    /**
     * Formatea un texto como tipo de artefacto.
     */
    public static Text formatArtifactType(String type) {
        return Text.literal("§5§o" + type + "§r")
            .formatted(ARTIFACT_TYPE_FORMAT);
    }
    
    /**
     * Formatea un texto como característica de artefacto.
     */
    public static Text formatArtifactFeature(String feature) {
        return Text.literal("§7• " + feature + "§r")
            .formatted(ARTIFACT_FEATURE_FORMAT);
    }
    
    /**
     * Formatea un texto como cita misteriosa.
     */
    public static Text formatArtifactQuote(String quote) {
        return Text.literal("§8«" + quote + "»§r")
            .formatted(ARTIFACT_QUOTE_FORMAT, Formatting.ITALIC);
    }
    
    /**
     * Calcula la distancia horizontal entre dos posiciones.
     */
    public static double horizontalDistance(BlockPos pos1, BlockPos pos2) {
        double dx = pos1.getX() - pos2.getX();
        double dz = pos1.getZ() - pos2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Calcula la distancia horizontal entre dos vectores.
     */
    public static double horizontalDistance(Vec3d pos1, Vec3d pos2) {
        double dx = pos1.x - pos2.x;
        double dz = pos1.z - pos2.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Verifica si dos posiciones están dentro de un rango horizontal.
     */
    public static boolean withinHorizontalRange(BlockPos pos1, BlockPos pos2, double range) {
        return horizontalDistance(pos1, pos2) <= range;
    }
    
    /**
     * Verifica si dos vectores están dentro de un rango horizontal.
     */
    public static boolean withinHorizontalRange(Vec3d pos1, Vec3d pos2, double range) {
        return horizontalDistance(pos1, pos2) <= range;
    }
    
    /**
     * Obtiene un valor aleatorio dentro de un rango.
     */
    public static int randomInRange(int min, int max) {
        if (min >= max) {
            return min;
        }
        return RANDOM.get().nextInt(max - min + 1) + min;
    }
    
    /**
     * Obtiene un valor aleatorio double dentro de un rango.
     */
    public static double randomInRange(double min, double max) {
        if (min >= max) {
            return min;
        }
        return min + (RANDOM.get().nextDouble() * (max - min));
    }
    
    /**
     * Verifica si un chance aleatorio ocurre.
     */
    public static boolean randomChance(double chance) {
        if (chance <= 0) {
            return false;
        }
        if (chance >= 1) {
            return true;
        }
        return RANDOM.get().nextDouble() < chance;
    }
    
    /**
     * Obtiene el nombre del mundo (simplificado).
     */
    public static String getWorldName(World world) {
        if (world == null) {
            return "unknown";
        }
        
        // Para mundos del servidor
        if (world.getRegistryKey() == World.OVERWORLD) {
            return "overworld";
        } else if (world.getRegistryKey() == World.NETHER) {
            return "nether";
        } else if (world.getRegistryKey() == World.END) {
            return "end";
        }
        
        return world.getRegistryKey().getValue().getPath();
    }
    
    /**
     * Verifica si es de noche en el mundo.
     */
    public static boolean isNightTime(World world) {
        if (world == null) {
            return false;
        }
        
        long time = world.getTimeOfDay() % 24000;
        return time >= 13000 && time < 23000;
    }
    
    /**
     * Verifica si está lloviendo en el mundo.
     */
    public static boolean isRaining(World world) {
        if (world == null) {
            return false;
        }
        
        return world.isRaining();
    }
    
    /**
     * Verifica si hay tormenta en el mundo.
     */
    public static boolean isThundering(World world) {
        if (world == null) {
            return false;
        }
        
        return world.isThundering();
    }
    
    /**
     * Obtiene el ItemStack en la mano principal de un jugador.
     */
    public static ItemStack getMainHandStack(PlayerEntity player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        return player.getMainHandStack();
    }
    
    /**
     * Obtiene el ItemStack en la mano secundaria de un jugador.
     */
    public static ItemStack getOffHandStack(PlayerEntity player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        return player.getOffHandStack();
    }
    
    /**
     * Verifica si un jugador tiene un ItemStack específico en alguna mano.
     */
    public static boolean hasItemInHands(PlayerEntity player, ItemStack itemToCheck) {
        if (player == null || itemToCheck.isEmpty()) {
            return false;
        }
        
        ItemStack mainHand = getMainHandStack(player);
        ItemStack offHand = getOffHandStack(player);
        
        return ItemStack.areItemsEqual(mainHand, itemToCheck) ||
               ItemStack.areItemsEqual(offHand, itemToCheck);
    }
    
    /**
     * Convierte ticks a segundos.
     */
    public static double ticksToSeconds(int ticks) {
        return ticks / 20.0;
    }
    
    /**
     * Convierte segundos a ticks.
     */
    public static int secondsToTicks(double seconds) {
        return (int) (seconds * 20);
    }
    
    /**
     * Convierte milisegundos a ticks.
     */
    public static int millisecondsToTicks(long milliseconds) {
        return (int) (milliseconds / 50);
    }
    
    /**
     * Convierte ticks a milisegundos.
     */
    public static long ticksToMilliseconds(int ticks) {
        return ticks * 50L;
    }
    
    /**
     * Formatea un tiempo en segundos a texto legible.
     */
    public static String formatTimeSeconds(double seconds) {
        if (seconds < 60) {
            return String.format("%.1fs", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            double remainingSeconds = seconds % 60;
            return String.format("%dm %.1fs", minutes, remainingSeconds);
        } else {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            return String.format("%dh %dm", hours, minutes);
        }
    }
    
    /**
     * Formatea un tiempo en ticks a texto legible.
     */
    public static String formatTimeTicks(int ticks) {
        return formatTimeSeconds(ticksToSeconds(ticks));
    }
    
    /**
     * Capitaliza la primera letra de un string.
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Convierte snake_case a PascalCase.
     */
    public static String snakeToPascal(String snake) {
        if (snake == null || snake.isEmpty()) {
            return snake;
        }
        
        String[] parts = snake.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(capitalize(part));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convierte PascalCase a snake_case.
     */
    public static String pascalToSnake(String pascal) {
        if (pascal == null || pascal.isEmpty()) {
            return pascal;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < pascal.length(); i++) {
            char c = pascal.charAt(i);
            
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            
            result.append(Character.toLowerCase(c));
        }
        
        return result.toString();
    }
}