package com.fiw.fiwstory.command;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * Comando para vincular artefactos a jugadores.
 * Sintaxis: /fiw bind <hand|item_id> <jugador>
 * Permisos: OPs level 2+
 */
public class BindCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                               CommandRegistryAccess registryAccess, 
                               CommandManager.RegistrationEnvironment environment) {
        
        dispatcher.register(CommandManager.literal("fiw")
            .requires(source -> source.hasPermissionLevel(2)) // OP level 2+
            .then(CommandManager.literal("bind")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(context -> bindItemInHand(context, EntityArgumentType.getPlayer(context, "target"))))
                .then(CommandManager.argument("player", StringArgumentType.word())
                    .executes(context -> bindItemInHandToPlayerName(context, StringArgumentType.getString(context, "player"))))
            )
            .then(CommandManager.literal("unbind")
                .executes(BindCommand::unbindItemInHand)
            )
            .then(CommandManager.literal("bindinfo")
                .executes(BindCommand::showBindInfo)
            )
        );
    }
    
    /**
     * Vincula el ítem en la mano del ejecutor al jugador especificado.
     */
    private static int bindItemInHand(CommandContext<ServerCommandSource> context, ServerPlayerEntity targetPlayer) 
            throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        ItemStack itemInHand = executor.getMainHandStack();
        
        return bindItemToPlayer(source, executor, itemInHand, targetPlayer);
    }
    
    /**
     * Vincula el ítem en la mano del ejecutor al jugador por nombre.
     */
    private static int bindItemInHandToPlayerName(CommandContext<ServerCommandSource> context, String playerName) 
            throws CommandSyntaxException {
        
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        ItemStack itemInHand = executor.getMainHandStack();
        
        // Buscar jugador por nombre
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);
        if (targetPlayer == null) {
            FiwUtils.sendErrorMessage(executor, "Jugador no encontrado: " + playerName);
            return 0;
        }
        
        return bindItemToPlayer(source, executor, itemInHand, targetPlayer);
    }
    
    /**
     * Lógica común para vincular un ítem a un jugador.
     */
    private static int bindItemToPlayer(ServerCommandSource source, ServerPlayerEntity executor, 
                                       ItemStack item, ServerPlayerEntity targetPlayer) {
        
        // Verificar que el ejecutor tenga un ítem en la mano
        if (item.isEmpty()) {
            FiwUtils.sendErrorMessage(executor, "Debes sostener un artefacto en tu mano principal.");
            return 0;
        }
        
        // Verificar que el ítem sea un artefacto (opcional, pero recomendado)
        if (!(item.getItem() instanceof BaseArtifactItem)) {
            FiwUtils.sendWarningMessage(executor, "Este ítem no es un artefacto, pero se vinculará de todos modos.");
        }
        
        // Obtener información
        UUID targetUuid = targetPlayer.getUuid();
        String targetName = targetPlayer.getName().getString();
        String executorName = executor.getName().getString();
        
        // Vincular el ítem
        FiwNBT.bindTo(item, targetUuid, executorName);
        
        // Mensajes de feedback
        String itemName = item.getName().getString();
        FiwUtils.sendSuccessMessage(executor, "Artefacto vinculado a " + targetName);
        FiwUtils.sendInfoMessage(targetPlayer, executorName + " ha vinculado un artefacto a ti: " + itemName);
        
        // Log en consola del servidor
        source.getServer().sendMessage(Text.literal(
            "[FiwStory] " + executorName + " vinculó " + itemName + " a " + targetName
        ));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * Desvincula el ítem en la mano del ejecutor.
     */
    private static int unbindItemInHand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        ItemStack itemInHand = executor.getMainHandStack();
        
        // Verificar que el ejecutor tenga un ítem en la mano
        if (itemInHand.isEmpty()) {
            FiwUtils.sendErrorMessage(executor, "Debes sostener un artefacto en tu mano principal.");
            return 0;
        }
        
        // Verificar que el ítem esté vinculado
        if (!FiwNBT.isBound(itemInHand)) {
            FiwUtils.sendErrorMessage(executor, "Este artefacto no está vinculado.");
            return 0;
        }
        
        // Obtener información del vínculo anterior
        UUID previousOwner = FiwNBT.getBoundTo(itemInHand);
        String itemName = itemInHand.getName().getString();
        String executorName = executor.getName().getString();
        
        // Desvincular
        FiwNBT.unbind(itemInHand);
        
        // Mensajes de feedback
        FiwUtils.sendSuccessMessage(executor, "Artefacto desvinculado.");
        
        // Notificar al dueño anterior si está en línea
        if (previousOwner != null) {
            ServerPlayerEntity previousOwnerPlayer = source.getServer().getPlayerManager().getPlayer(previousOwner);
            if (previousOwnerPlayer != null && !previousOwnerPlayer.getUuid().equals(executor.getUuid())) {
                FiwUtils.sendWarningMessage(previousOwnerPlayer, 
                    executorName + " ha desvinculado tu artefacto: " + itemName);
            }
        }
        
        // Log en consola del servidor
        source.getServer().sendMessage(Text.literal(
            "[FiwStory] " + executorName + " desvinculó " + itemName
        ));
        
        return Command.SINGLE_SUCCESS;
    }
    
    /**
     * Muestra información sobre el vínculo del ítem en la mano.
     */
    private static int showBindInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        ItemStack itemInHand = executor.getMainHandStack();
        
        // Verificar que el ejecutor tenga un ítem en la mano
        if (itemInHand.isEmpty()) {
            FiwUtils.sendErrorMessage(executor, "Debes sostener un artefacto en tu mano principal.");
            return 0;
        }
        
        // Verificar que el ítem esté vinculado
        if (!FiwNBT.isBound(itemInHand)) {
            FiwUtils.sendInfoMessage(executor, "Este artefacto no está vinculado.");
            return Command.SINGLE_SUCCESS;
        }
        
        // Obtener información del vínculo
        UUID boundTo = FiwNBT.getBoundTo(itemInHand);
        long bindDate = FiwNBT.getLong(itemInHand, FiwNBT.BIND_DATE, 0);
        String bindBy = FiwNBT.getString(itemInHand, FiwNBT.BIND_BY, "Desconocido");
        int bindLevel = FiwNBT.getInt(itemInHand, FiwNBT.BIND_LEVEL, 1);
        
        // Buscar nombre del jugador vinculado
        String boundPlayerName = "Desconocido";
        if (boundTo != null) {
            ServerPlayerEntity boundPlayer = source.getServer().getPlayerManager().getPlayer(boundTo);
            if (boundPlayer != null) {
                boundPlayerName = boundPlayer.getName().getString();
            } else {
                // Intentar obtener del cache o mostrar UUID
                boundPlayerName = "UUID: " + boundTo.toString().substring(0, 8) + "...";
            }
        }
        
        // Formatear fecha
        String bindDateStr = "Desconocida";
        if (bindDate > 0) {
            long daysAgo = (System.currentTimeMillis() - bindDate) / (1000 * 60 * 60 * 24);
            if (daysAgo == 0) {
                bindDateStr = "Hoy";
            } else if (daysAgo == 1) {
                bindDateStr = "Ayer";
            } else {
                bindDateStr = "Hace " + daysAgo + " días";
            }
        }
        
        // Mostrar información
        FiwUtils.sendInfoMessage(executor, "=== Información de Vínculo ===");
        FiwUtils.sendInfoMessage(executor, "Artefacto: " + itemInHand.getName().getString());
        FiwUtils.sendInfoMessage(executor, "Vinculado a: " + boundPlayerName);
        FiwUtils.sendInfoMessage(executor, "Vinculado por: " + bindBy);
        FiwUtils.sendInfoMessage(executor, "Fecha: " + bindDateStr);
        FiwUtils.sendInfoMessage(executor, "Nivel de vínculo: " + bindLevel);
        
        return Command.SINGLE_SUCCESS;
    }
}