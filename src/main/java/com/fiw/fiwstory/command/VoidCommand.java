package com.fiw.fiwstory.command;

import com.fiw.fiwstory.dimension.TimelessVoidDimension;
import com.fiw.fiwstory.dimension.TimelessVoidTeleporter;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;

import java.util.*;

public class VoidCommand {
    
    private static final Map<UUID, VoidTeleportData> playerTeleportData = new HashMap<>();
    private static final Set<UUID> whitelist = new HashSet<>();
    private static final Map<UUID, Long> expulsionCooldowns = new HashMap<>(); // UUID -> tiempo de expulsión
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
        registerCommands(dispatcher, registryAccess);
    }
    
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("v")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("enter")
                .executes(context -> enterVoid(context.getSource())))
            .then(CommandManager.literal("leave")
                .executes(context -> leaveVoid(context.getSource())))
            .then(CommandManager.literal("whitelist")
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> addToWhitelist(context, EntityArgumentType.getPlayer(context, "player")))))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> removeFromWhitelist(context, EntityArgumentType.getPlayer(context, "player")))))
                 .then(CommandManager.literal("show")
                    .executes(context -> showWhitelist(context.getSource()))))
             .then(CommandManager.literal("time")
                .executes(context -> simulateTimeExpulsion(context.getSource())))
             .then(CommandManager.literal("timeskip")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> skipCooldown(context, EntityArgumentType.getPlayer(context, "player")))))
        );
    }
    
    private static int enterVoid(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Solo jugadores pueden usar este comando").formatted(Formatting.RED));
            return 0;
        }
        
        // Prevenir entrada desde End o Nether
        if (player.getWorld().getRegistryKey() == net.minecraft.world.World.END ||
            player.getWorld().getRegistryKey() == net.minecraft.world.World.NETHER) {
            source.sendError(Text.literal("§4§l¡NO PUEDES ACCEDER DESDE AQUÍ!").formatted(Formatting.DARK_RED, Formatting.BOLD));
            player.sendMessage(Text.literal("§cEl Vacío solo se puede alcanzar desde el mundo terrenal").formatted(Formatting.RED), true);
            return 0;
        }
        
        if (!isPlayerAllowed(player)) {
            return 0; // El mensaje de error ya se muestra en isPlayerAllowed
        }
        
        BlockPos currentPos = player.getBlockPos();
        
        // Guardar datos de teletransporte
        playerTeleportData.put(player.getUuid(), 
            new VoidTeleportData(currentPos, System.currentTimeMillis()));
        
        // Aplicar efectos al entrar (3 segundos = 60 ticks)
        applyVoidTransitionEffects(player);
        
        // Teletransportar al jugador a la dimensión Timeless Void
        TimelessVoidTeleporter.teleportToVoid(player, currentPos);
        
        source.sendFeedback(() -> Text.literal("Entrando al Timeless Void...").formatted(Formatting.GREEN), false);
        player.sendMessage(Text.literal("Tienes " + (hasTimelessBladeInHand(player) ? "10" : "5") + " minutos en el Void").formatted(Formatting.GOLD), true);
        return 1;
    }
    
    private static int leaveVoid(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Solo jugadores pueden usar este comando").formatted(Formatting.RED));
            return 0;
        }
        
        if (!playerTeleportData.containsKey(player.getUuid())) {
            source.sendError(Text.literal("No estás en el Void").formatted(Formatting.RED));
            return 0;
        }
        
        VoidTeleportData data = playerTeleportData.get(player.getUuid());
        
        // Aplicar efectos al salir (3 segundos = 60 ticks)
        applyVoidTransitionEffects(player);
        
        // Teletransportar de vuelta al overworld
        TimelessVoidTeleporter.teleportToOverworld(player, player.getBlockPos());
        
        // Eliminar datos
        playerTeleportData.remove(player.getUuid());
        
        source.sendFeedback(() -> Text.literal("Saliendo del Timeless Void...").formatted(Formatting.GREEN), false);
        return 1;
    }
    
    private static int addToWhitelist(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer) {
        whitelist.add(targetPlayer.getUuid());
        context.getSource().sendFeedback(() -> 
            Text.literal("Jugador " + targetPlayer.getName().getString() + " agregado a la whitelist")
                .formatted(Formatting.GREEN), true);
        return 1;
    }
    
    private static int removeFromWhitelist(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer) {
        whitelist.remove(targetPlayer.getUuid());
        context.getSource().sendFeedback(() -> 
            Text.literal("Jugador " + targetPlayer.getName().getString() + " removido de la whitelist")
                .formatted(Formatting.YELLOW), true);
        return 1;
    }
    
    private static int showWhitelist(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        
        if (whitelist.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Whitelist vacía").formatted(Formatting.YELLOW), false);
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("Jugadores en whitelist:").formatted(Formatting.GOLD), false);
        
        for (UUID playerId : whitelist) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                source.sendFeedback(() -> 
                    Text.literal(" - " + player.getName().getString()).formatted(Formatting.WHITE), false);
            }
        }
        
        return whitelist.size();
    }
    
    private static int simulateTimeExpulsion(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Solo jugadores pueden usar este comando").formatted(Formatting.RED));
            return 0;
        }
        
        if (!playerTeleportData.containsKey(player.getUuid())) {
            source.sendError(Text.literal("No estás en el Void").formatted(Formatting.RED));
            return 0;
        }
        
        // Simular que el tiempo ha expirado
        applyExpulsionPenalties(player);
        VoidTeleportData data = playerTeleportData.get(player.getUuid());
        BlockPos randomPos = TimelessVoidTeleporter.getRandomExitPos(data.getEntryPos());
        TimelessVoidTeleporter.teleportToOverworld(player, randomPos);
        playerTeleportData.remove(player.getUuid());
        
        source.sendFeedback(() -> Text.literal("§6§l[PRUEBA]§r §eExpulsión forzada del Void aplicada").formatted(Formatting.GOLD), false);
        return 1;
    }
    
    private static int skipCooldown(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer) {
        UUID playerId = targetPlayer.getUuid();
        
        if (expulsionCooldowns.containsKey(playerId)) {
            expulsionCooldowns.remove(playerId);
            context.getSource().sendFeedback(() -> 
                Text.literal("§a§l¡COOLDOWN SALTADO!§r §eCooldown de 5 horas eliminado para " + targetPlayer.getName().getString())
                    .formatted(Formatting.GREEN), true);
            targetPlayer.sendMessage(Text.literal("§a§l¡EL TIEMPO SE ACELERA!§r §eTu cooldown del Void ha sido saltado por un administrador")
                .formatted(Formatting.GREEN), true);
            return 1;
        } else {
            context.getSource().sendFeedback(() -> 
                Text.literal("§cEl jugador " + targetPlayer.getName().getString() + " no tiene cooldown activo")
                    .formatted(Formatting.YELLOW), true);
            return 0;
        }
    }
    
    private static boolean isPlayerAllowed(ServerPlayerEntity player) {
        // Verificar cooldown de expulsión
        if (isOnExpulsionCooldown(player)) {
            player.sendMessage(Text.literal(getCooldownMessage(player)), true);
            return false;
        }
        
        // Verificar si está en whitelist o tiene la Timeless Blade
        if (whitelist.contains(player.getUuid())) {
            return true;
        }
        
        // Verificar si tiene la Timeless Blade en la mano
        return player.getMainHandStack().getItem() == com.fiw.fiwstory.item.ModItems.TIMELESS_BLADE_ARTIFACT ||
               player.getOffHandStack().getItem() == com.fiw.fiwstory.item.ModItems.TIMELESS_BLADE_ARTIFACT;
    }
    
    // Los métodos de teletransporte ahora están en TimelessVoidTeleporter
    
    public static void checkTimeLimit(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        
        if (playerTeleportData.containsKey(playerId)) {
            VoidTeleportData data = playerTeleportData.get(playerId);
            boolean hasTimelessBlade = hasTimelessBladeInHand(player);
            
            // Aplicar efectos mientras está en el Void
            applyVoidEffects(player);
            
            if (data.isTimeExpired(System.currentTimeMillis(), hasTimelessBlade)) {
                // Tiempo expirado, aplicar penalizaciones y expulsar
                applyExpulsionPenalties(player);
                BlockPos randomPos = TimelessVoidTeleporter.getRandomExitPos(data.getEntryPos());
                TimelessVoidTeleporter.teleportToOverworld(player, randomPos);
                playerTeleportData.remove(playerId);
                
                player.sendMessage(Text.literal("§4§l¡EL VOID TE RECHAZA!").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
                player.sendMessage(Text.literal("§cLa realidad se desgarra a tu alrededor...").formatted(Formatting.RED), true);
                player.sendMessage(Text.literal("§6El tiempo se agota, la esencia se desvanece").formatted(Formatting.GOLD), true);
            } else {
                // Mostrar tiempo restante cada 30 segundos
                long remainingTime = data.getRemainingTime(System.currentTimeMillis(), hasTimelessBlade);
                if (remainingTime > 0 && remainingTime <= 30000) { // Últimos 30 segundos
                    int seconds = (int) (remainingTime / 1000);
                    if (seconds % 10 == 0 || seconds <= 10) { // Cada 10 segundos o últimos 10 segundos
                        player.sendMessage(Text.literal("§eTiempo restante en el Vacío: " + seconds + " segundos").formatted(Formatting.YELLOW), true);
                    }
                }
            }
        }
    }
    
    private static void applyVoidEffects(ServerPlayerEntity player) {
        // Regeneración nivel 1 sin partículas (40 ticks = 2 segundos, infinito mientras esté en Void)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.REGENERATION, 40, 0, false, false)); // Sin partículas, sin icono
        
        // Fatiga minera nivel 1 sin partículas (40 ticks = 2 segundos, infinito mientras esté en Void)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE, 40, 0, false, false));
    }
    
    private static boolean hasTimelessBladeInHand(ServerPlayerEntity player) {
        // Verificar si tiene la Timeless Blade en la mano
        return player.getMainHandStack().getItem() == com.fiw.fiwstory.item.ModItems.TIMELESS_BLADE_ARTIFACT ||
               player.getOffHandStack().getItem() == com.fiw.fiwstory.item.ModItems.TIMELESS_BLADE_ARTIFACT;
    }
    
    private static void applyExpulsionPenalties(ServerPlayerEntity player) {
        // Reducir vida al 50%
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float newHealth = maxHealth * 0.5f;
        player.setHealth(newHealth);
        
        // Aplicar efectos negativos por 2 minutos (2400 ticks)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.NAUSEA, 2400, 0, false, true)); // Sin partículas, mostrar icono
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 2400, 0, false, true));
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SLOWNESS, 2400, 1, false, true));
        
        // Aplicar cooldown de 5 horas (5 * 60 * 60 * 1000 = 18,000,000 ms)
        expulsionCooldowns.put(player.getUuid(), System.currentTimeMillis() + 18000000L);
        
        player.sendMessage(Text.literal("§4§l¡LA FALTA DE TIEMPO TE AFECTA!").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
        player.sendMessage(Text.literal("§cTu esencia se debilita, la realidad te marca").formatted(Formatting.RED), true);
    }
    
    private static boolean isOnExpulsionCooldown(ServerPlayerEntity player) {
        Long cooldownEnd = expulsionCooldowns.get(player.getUuid());
        if (cooldownEnd == null) return false;
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            expulsionCooldowns.remove(player.getUuid());
            return false;
        }
        
        return true;
    }
    
    private static String getCooldownMessage(ServerPlayerEntity player) {
        Long cooldownEnd = expulsionCooldowns.get(player.getUuid());
        if (cooldownEnd == null) return "";
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        long hours = remaining / 3600000L;
        long minutes = (remaining % 3600000L) / 60000L;
        
        return String.format("§4§l¡ADVERTENCIA DEL VOID!§r\n§6El tiempo aún no se estabiliza...\n§cDebes esperar §e%d§c horas y §e%d§c minutos\n§7\"No deberías entrar de nuevo por ahora, el vacío es peligroso\"", hours, minutes);
    }
    
    private static void applyVoidTransitionEffects(ServerPlayerEntity player) {
        // Aplicar efectos de transición (3 segundos = 60 ticks)
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.NAUSEA, 60, 0));
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.BLINDNESS, 60, 0));
        
        player.sendMessage(Text.literal("La transición dimensional te causa mareo y ceguera temporal").formatted(Formatting.YELLOW), true);
    }
    
    public static boolean isPlayerInVoid(UUID playerId) {
        return playerTeleportData.containsKey(playerId);
    }
    
    public static void removePlayerFromVoid(UUID playerId) {
        playerTeleportData.remove(playerId);
    }
}