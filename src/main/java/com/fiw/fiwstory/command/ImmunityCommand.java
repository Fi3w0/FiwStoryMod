package com.fiw.fiwstory.command;

import com.fiw.fiwstory.data.ImmunityData;
import com.fiw.fiwstory.data.ModData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class ImmunityCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("fiwimmunity")
            .requires(source -> source.hasPermissionLevel(2)) // Solo ops
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> addImmunity(context, EntityArgumentType.getPlayer(context, "player")))
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> removeImmunity(context, EntityArgumentType.getPlayer(context, "player")))
                )
            )
            .then(CommandManager.literal("list")
                .executes(ImmunityCommand::listImmunePlayers)
            )
            .then(CommandManager.literal("check")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> checkImmunity(context, EntityArgumentType.getPlayer(context, "player")))
                )
            )
        );
    }
    
    private static int addImmunity(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        ImmunityData immunityData = ImmunityData.getServerState(context.getSource().getServer());
        ModData oldData = ModData.getServerState(context.getSource().getServer());
        UUID playerId = player.getUuid();
        
        // Migrar del sistema viejo si es necesario
        boolean wasImmune = oldData.isPlayerImmune(playerId);
        
        // Establecer inmunidad en el nuevo sistema
        immunityData.setPlayerImmune(playerId, true);
        
        // Remover del sistema viejo
        oldData.removeImmunePlayer(playerId);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§a✅ " + player.getName().getString() + " ahora es inmune a objetos corruptos.").formatted(net.minecraft.util.Formatting.GREEN), 
            true
        );
        player.sendMessage(Text.literal("§a✨ Has recibido inmunidad a objetos corruptos.").formatted(net.minecraft.util.Formatting.GREEN), false);
        player.sendMessage(Text.literal("§7§o(Susurros cada 10 minutos si tienes corrupción)").formatted(net.minecraft.util.Formatting.GRAY, net.minecraft.util.Formatting.ITALIC), false);
        return 1;
    }
    
    private static int removeImmunity(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        ImmunityData immunityData = ImmunityData.getServerState(context.getSource().getServer());
        UUID playerId = player.getUuid();
        
        if (immunityData.isPlayerImmune(playerId)) {
            immunityData.setPlayerImmune(playerId, false);
            context.getSource().sendFeedback(() -> 
                Text.literal("§c❌ " + player.getName().getString() + " ya no es inmune a objetos corruptos.").formatted(net.minecraft.util.Formatting.RED), 
                true
            );
            player.sendMessage(Text.literal("§c⚠ Has perdido la inmunidad a objetos corruptos.").formatted(net.minecraft.util.Formatting.RED), false);
            return 1;
        } else {
            context.getSource().sendFeedback(() -> 
                Text.literal("§e⚠ " + player.getName().getString() + " no tenía inmunidad.").formatted(net.minecraft.util.Formatting.YELLOW), 
                true
            );
            return 0;
        }
    }
    
    private static int listImmunePlayers(CommandContext<ServerCommandSource> context) {
        // Este método necesita ser reimplementado ya que ImmunityData no tiene getImmunePlayers()
        // Por ahora, solo mostrar mensaje
        context.getSource().sendFeedback(() -> 
            Text.literal("§7Usa /fiwimmunity check <jugador> para verificar inmunidad.").formatted(net.minecraft.util.Formatting.GRAY), 
            true
        );
        return 0;
    }
    
    private static int checkImmunity(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        ImmunityData immunityData = ImmunityData.getServerState(context.getSource().getServer());
        boolean isImmune = immunityData.isPlayerImmune(player.getUuid());
        String status = isImmune ? "§aINMUNE" : "§cVULNERABLE";
        String info = immunityData.getPlayerImmunityInfo(player.getUuid());
        
        context.getSource().sendFeedback(() -> 
            Text.literal("§7" + player.getName().getString() + ": " + status + "§7 a objetos corruptos.").formatted(net.minecraft.util.Formatting.GRAY), 
            true
        );
        
        if (isImmune) {
            context.getSource().sendFeedback(() -> 
                Text.literal("§7  " + info).formatted(net.minecraft.util.Formatting.GRAY), 
                true
            );
        }
        
        return isImmune ? 1 : 0;
    }
    
    public static boolean isPlayerImmune(ServerCommandSource source, UUID playerId) {
        ImmunityData immunityData = ImmunityData.getServerState(source.getServer());
        return immunityData.isPlayerImmune(playerId);
    }
}