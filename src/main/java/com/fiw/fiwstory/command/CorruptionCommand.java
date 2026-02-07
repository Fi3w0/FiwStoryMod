package com.fiw.fiwstory.command;

import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.item.custom.PureMixItem;
import com.fiw.fiwstory.lib.FiwUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

/**
 * Comando para controlar la corrupción de jugadores.
 * Sintaxis: /corruption <subcomando> <jugador> [nivel|cantidad]
 * Permisos: OPs level 2+
 */
public class CorruptionCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                               CommandRegistryAccess registryAccess, 
                               CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(CommandManager.literal("corruption")
            .requires(source -> source.hasPermissionLevel(2)) // OP level 2+
            
            // /corruption set <jugador> <nivel>
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 5))
                        .executes(context -> setCorruptionLevel(
                            context, 
                            EntityArgumentType.getPlayers(context, "targets"),
                            IntegerArgumentType.getInteger(context, "level")
                        ))
                    )
                )
            )
            
            // /corruption add <jugador> <cantidad>
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(-5, 5))
                        .executes(context -> addCorruption(
                            context, 
                            EntityArgumentType.getPlayers(context, "targets"),
                            IntegerArgumentType.getInteger(context, "amount")
                        ))
                    )
                )
            )
            
            // /corruption clear <jugador>
            .then(CommandManager.literal("clear")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                    .executes(context -> clearCorruption(
                        context, 
                        EntityArgumentType.getPlayers(context, "targets")
                    ))
                )
            )
            
            // /corruption info <jugador>
            .then(CommandManager.literal("info")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(context -> showCorruptionInfo(
                        context, 
                        EntityArgumentType.getPlayer(context, "target")
                    ))
                )
                .executes(context -> showSelfCorruptionInfo(context))
            )
            
            // /corruption resetprogress <jugador>
            .then(CommandManager.literal("resetprogress")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(context -> resetPurificationProgress(
                        context,
                        EntityArgumentType.getPlayer(context, "target")
                    ))
                )
            )
            
            // /corruption help
            .then(CommandManager.literal("help")
                .executes(CorruptionCommand::showHelp)
            )
        );
    }
    
    // ========== MÉTODOS DE SUBCOMANDOS ==========
    
    private static int setCorruptionLevel(CommandContext<ServerCommandSource> context, 
                                         Collection<ServerPlayerEntity> targets, 
                                         int level) throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        int successCount = 0;
        
        for (ServerPlayerEntity player : targets) {
            // Remover efecto actual si existe
            CorruptionStatusEffect.removeFromPlayer(player);
            
            if (level > 0) {
                // Aplicar nuevo nivel
                CorruptionStatusEffect.applyToPlayer(player, level, Integer.MAX_VALUE);
                FiwUtils.sendInfoMessage(player, 
                    String.format("Tu nivel de corrupción ha sido establecido a %d por un admin.", level));
                
                if (level >= 3) {
                    FiwUtils.sendWarningMessage(player, "¡Cuidado! Niveles altos de corrupción son peligrosos.");
                }
            } else {
                FiwUtils.sendInfoMessage(player, "Tu corrupción ha sido purificada por un admin.");
            }
            
            successCount++;
        }
        
        if (successCount == 1) {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
                String.format("Nivel de corrupción establecido a %d para 1 jugador.", level));
        } else {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
                String.format("Nivel de corrupción establecido a %d para %d jugadores.", level, successCount));
        }
        
        return successCount > 0 ? Command.SINGLE_SUCCESS : 0;
    }
    
    private static int addCorruption(CommandContext<ServerCommandSource> context,
                                    Collection<ServerPlayerEntity> targets,
                                    int amount) throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        int successCount = 0;
        
        for (ServerPlayerEntity player : targets) {
            int currentLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(player);
            int newLevel = Math.max(0, Math.min(5, currentLevel + amount));
            
            // Remover efecto actual
            CorruptionStatusEffect.removeFromPlayer(player);
            
            if (newLevel > 0) {
                // Aplicar nuevo nivel
                CorruptionStatusEffect.applyToPlayer(player, newLevel, Integer.MAX_VALUE);
                
                if (amount > 0) {
                    FiwUtils.sendWarningMessage(player, 
                        String.format("Has ganado %d nivel(es) de corrupción. Ahora: %d", amount, newLevel));
                } else if (amount < 0) {
                    FiwUtils.sendInfoMessage(player, 
                        String.format("Has perdido %d nivel(es) de corrupción. Ahora: %d", -amount, newLevel));
                }
            } else {
                FiwUtils.sendInfoMessage(player, "¡Estás completamente purificado!");
            }
            
            successCount++;
        }
        
        if (successCount == 1) {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
                String.format("Corrupción modificada en %d para 1 jugador.", amount));
        } else {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
                String.format("Corrupción modificada en %d para %d jugadores.", amount, successCount));
        }
        
        return successCount > 0 ? Command.SINGLE_SUCCESS : 0;
    }
    
    private static int clearCorruption(CommandContext<ServerCommandSource> context,
                                      Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        int successCount = 0;
        
        for (ServerPlayerEntity player : targets) {
            int currentLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(player);
            
            if (currentLevel > 0) {
                CorruptionStatusEffect.removeFromPlayer(player);
                FiwUtils.sendInfoMessage(player, "¡Tu corrupción ha sido completamente purificada por un admin!");
                successCount++;
            }
        }
        
        if (successCount == 1) {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), "Corrupción limpiada para 1 jugador.");
        } else {
            FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
                String.format("Corrupción limpiada para %d jugadores.", successCount));
        }
        
        return successCount > 0 ? Command.SINGLE_SUCCESS : 0;
    }
    
    private static int showCorruptionInfo(CommandContext<ServerCommandSource> context,
                                         ServerPlayerEntity target) throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        
        int currentLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(target);
        String progress = PureMixItem.getPlayerPurificationProgress(target.getUuid());
        
        // Información básica
        FiwUtils.sendInfoMessage(executor, "=== Información de Corrupción ===");
        FiwUtils.sendInfoMessage(executor, "Jugador: " + target.getName().getString());
        FiwUtils.sendInfoMessage(executor, "Nivel actual: " + currentLevel + "/5");
        FiwUtils.sendInfoMessage(executor, "Progreso de purificación: " + progress);
        
        // Descripción del nivel
        if (currentLevel == 0) {
            FiwUtils.sendInfoMessage(executor, "Estado: §aPuro§r");
        } else if (currentLevel <= 2) {
            FiwUtils.sendInfoMessage(executor, "Estado: §eLeve - Jugable pero desagradable§r");
        } else if (currentLevel <= 4) {
            FiwUtils.sendInfoMessage(executor, "Estado: §6Moderado - Efectos significativos§r");
        } else {
            FiwUtils.sendInfoMessage(executor, "Estado: §cSevero - Extremadamente peligroso§r");
        }
        
        // Efectos actuales
        FiwUtils.sendInfoMessage(executor, "Efectos activos:");
        if (currentLevel >= 1) {
            FiwUtils.sendInfoMessage(executor, "  • Daño gradual ignorando armadura");
            FiwUtils.sendInfoMessage(executor, "  • Susurros corruptos");
        }
        if (currentLevel >= 2) {
            FiwUtils.sendInfoMessage(executor, "  • Ceguera momentánea ocasional");
        }
        if (currentLevel >= 3) {
            FiwUtils.sendInfoMessage(executor, "  • Fatiga de minería");
            FiwUtils.sendInfoMessage(executor, "  • Reducción de vida máxima");
        }
        if (currentLevel >= 5) {
            FiwUtils.sendInfoMessage(executor, "  • Náusea ocasional");
            FiwUtils.sendInfoMessage(executor, "  • Susurros intensos");
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int showSelfCorruptionInfo(CommandContext<ServerCommandSource> context) 
            throws CommandSyntaxException {
        
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        return showCorruptionInfo(context, player);
    }
    
    private static int resetPurificationProgress(CommandContext<ServerCommandSource> context,
                                                ServerPlayerEntity target) throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        
        PureMixItem.resetPlayerPurificationProgress(target.getUuid());
        FiwUtils.sendSuccessMessage(source.getPlayerOrThrow(), 
            "Progreso de purificación reseteado para " + target.getName().getString());
        FiwUtils.sendInfoMessage(target, "Tu progreso de purificación ha sido reseteado por un admin.");
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int showHelp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        
        FiwUtils.sendInfoMessage(player, "=== Comandos de Corrupción ===");
        FiwUtils.sendInfoMessage(player, "/corruption set <jugador> <0-5> - Establecer nivel");
        FiwUtils.sendInfoMessage(player, "/corruption add <jugador> <-5 a 5> - Añadir/restar nivel");
        FiwUtils.sendInfoMessage(player, "/corruption clear <jugador> - Limpiar corrupción");
        FiwUtils.sendInfoMessage(player, "/corruption info [jugador] - Ver información");
        FiwUtils.sendInfoMessage(player, "/corruption resetprogress <jugador> - Resetear progreso de purificación");
        FiwUtils.sendInfoMessage(player, "/corruption help - Mostrar esta ayuda");
        FiwUtils.sendInfoMessage(player, "");
        FiwUtils.sendInfoMessage(player, "=== Niveles de Corrupción ===");
        FiwUtils.sendInfoMessage(player, "0: §aPuro§r - Sin efectos");
        FiwUtils.sendInfoMessage(player, "1-2: §eLeve§r - Daño gradual, susurros");
        FiwUtils.sendInfoMessage(player, "3-4: §6Moderado§r - +Fatiga, -Vida máxima");
        FiwUtils.sendInfoMessage(player, "5: §cSevero§r - +Náusea, susurros intensos");
        FiwUtils.sendInfoMessage(player, "");
        FiwUtils.sendInfoMessage(player, "=== Purificación ===");
        FiwUtils.sendInfoMessage(player, "• Mix Puro: 6-15 para bajar 1 nivel");
        FiwUtils.sendInfoMessage(player, "• No cura completamente, solo reduce nivel");
        FiwUtils.sendInfoMessage(player, "• No funciona bajo nivel 1");
        
        return Command.SINGLE_SUCCESS;
    }
}