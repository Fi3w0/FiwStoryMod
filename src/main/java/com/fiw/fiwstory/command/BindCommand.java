package com.fiw.fiwstory.command;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comandos para gestión de vínculos de artefactos.
 *
 * /fiw bind <jugador>    — vincula todos los artefactos del jugador a él mismo
 * /fiw unbind            — desvincula el artefacto en tu mano
 * /fiw unbind <jugador>  — desvincula todos los artefactos de un jugador
 * /fiw list              — lista todos los jugadores online con sus artefactos y estado de bind
 */
public class BindCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("fiw")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("bind")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(context -> bindAllArtifacts(context, EntityArgumentType.getPlayer(context, "target"))))
            )
            .then(CommandManager.literal("unbind")
                .executes(BindCommand::unbindFromHand)
                .then(CommandManager.argument("target", EntityArgumentType.player())
                    .executes(context -> unbindAllFromPlayer(context, EntityArgumentType.getPlayer(context, "target"))))
            )
            .then(CommandManager.literal("list")
                .executes(BindCommand::listArtifacts)
            )
        );
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private static boolean isArtifact(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof BaseArtifactItem || item instanceof BaseArtifactSwordItem;
    }

    /**
     * Recoge todos los ItemStack de artefactos de un jugador:
     * inventario principal, slots de trinket y ender chest.
     * Devuelve referencias directas a los stacks para que modificarlos
     * sea efectivo sobre el inventario real.
     */
    private static List<ItemStack> collectArtifacts(ServerPlayerEntity player) {
        List<ItemStack> result = new ArrayList<>();

        // Inventario principal (hotbar + main + armor + offhand)
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (isArtifact(stack)) result.add(stack);
        }

        // Slots de Trinkets (separados del inventario principal)
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            for (var entry : component.getAllEquipped()) {
                ItemStack stack = entry.getRight();
                if (isArtifact(stack)) result.add(stack);
            }
        });

        // Ender chest
        SimpleInventory enderChest = player.getEnderChestInventory();
        for (int i = 0; i < enderChest.size(); i++) {
            ItemStack stack = enderChest.getStack(i);
            if (isArtifact(stack)) result.add(stack);
        }

        return result;
    }

    // =========================================================================
    //  /fiw bind <target>
    // =========================================================================

    private static int bindAllArtifacts(CommandContext<ServerCommandSource> context,
                                        ServerPlayerEntity target) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String executorName = source.getName();
        String targetName = target.getName().getString();
        UUID targetUuid = target.getUuid();

        List<ItemStack> artifacts = collectArtifacts(target);

        if (artifacts.isEmpty()) {
            source.sendFeedback(() -> Text.literal(
                "§e[FiwStory] " + targetName + " no tiene artefactos en inventario, trinkets o ender chest."
            ), false);
            return 0;
        }

        StringBuilder names = new StringBuilder();
        for (ItemStack stack : artifacts) {
            FiwNBT.bindTo(stack, targetUuid, executorName);
            if (names.length() > 0) names.append(", ");
            names.append(stack.getName().getString());
        }

        int count = artifacts.size();
        String namesList = names.toString();
        source.sendFeedback(() -> Text.literal(
            "§a[FiwStory] Vinculados §f" + count + "§a artefactos a §f" + targetName + "§a: " + namesList
        ), true);

        FiwUtils.sendInfoMessage(target,
            executorName + " ha vinculado tus artefactos (" + count + "): " + namesList);

        source.getServer().sendMessage(Text.literal(
            "[FiwStory] " + executorName + " vinculó " + count + " artefactos a " + targetName
        ));
        return Command.SINGLE_SUCCESS;
    }

    // =========================================================================
    //  /fiw unbind  (item en mano)
    // =========================================================================

    private static int unbindFromHand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity executor = source.getPlayerOrThrow();
        ItemStack itemInHand = executor.getMainHandStack();

        if (itemInHand.isEmpty()) {
            FiwUtils.sendErrorMessage(executor, "Debes sostener un artefacto en tu mano principal.");
            return 0;
        }

        if (!FiwNBT.isBound(itemInHand)) {
            FiwUtils.sendErrorMessage(executor, "Este artefacto no está vinculado.");
            return 0;
        }

        UUID previousOwner = FiwNBT.getBoundTo(itemInHand);
        String itemName = itemInHand.getName().getString();
        String executorName = executor.getName().getString();

        FiwNBT.unbind(itemInHand);
        FiwUtils.sendSuccessMessage(executor, "Artefacto desvinculado: " + itemName);

        if (previousOwner != null) {
            ServerPlayerEntity prev = source.getServer().getPlayerManager().getPlayer(previousOwner);
            if (prev != null && !prev.getUuid().equals(executor.getUuid())) {
                FiwUtils.sendWarningMessage(prev,
                    executorName + " ha desvinculado tu artefacto: " + itemName);
            }
        }

        source.getServer().sendMessage(Text.literal(
            "[FiwStory] " + executorName + " desvinculó " + itemName
        ));
        return Command.SINGLE_SUCCESS;
    }

    // =========================================================================
    //  /fiw unbind <target>
    // =========================================================================

    private static int unbindAllFromPlayer(CommandContext<ServerCommandSource> context,
                                           ServerPlayerEntity target) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String executorName = source.getName();
        String targetName = target.getName().getString();

        List<ItemStack> artifacts = collectArtifacts(target);
        int unbound = 0;
        for (ItemStack stack : artifacts) {
            if (FiwNBT.isBound(stack)) {
                FiwNBT.unbind(stack);
                unbound++;
            }
        }

        if (unbound == 0) {
            source.sendFeedback(() -> Text.literal(
                "§e[FiwStory] " + targetName + " no tiene artefactos vinculados."
            ), false);
            return 0;
        }

        int finalUnbound = unbound;
        source.sendFeedback(() -> Text.literal(
            "§a[FiwStory] Desvinculados §f" + finalUnbound + "§a artefactos de §f" + targetName + "§a."
        ), true);

        FiwUtils.sendWarningMessage(target,
            executorName + " ha desvinculado " + finalUnbound + " de tus artefactos.");
        return Command.SINGLE_SUCCESS;
    }

    // =========================================================================
    //  /fiw list
    // =========================================================================

    private static int listArtifacts(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§e[FiwStory] No hay jugadores en línea."), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§6§l=== Artefactos (online) ==="), false);
        source.sendFeedback(() -> Text.literal("§7Jugador | Artefacto | Bind"), false);

        for (ServerPlayerEntity player : players) {
            String playerName = player.getName().getString();
            List<ItemStack> artifacts = collectArtifacts(player);

            if (artifacts.isEmpty()) {
                source.sendFeedback(() -> Text.literal("§f" + playerName + " §8| §7(sin artefactos)"), false);
                continue;
            }

            for (ItemStack stack : artifacts) {
                String itemName = stack.getName().getString();
                boolean bound = FiwNBT.isBound(stack);

                String bindStr;
                if (bound) {
                    UUID boundTo = FiwNBT.getBoundTo(stack);
                    ServerPlayerEntity boundPlayer = source.getServer().getPlayerManager().getPlayer(boundTo);
                    String boundName = boundPlayer != null
                        ? boundPlayer.getName().getString()
                        : boundTo.toString().substring(0, 8) + "...";
                    bindStr = "§aSí §7(" + boundName + ")";
                } else {
                    bindStr = "§cNo";
                }

                // Lambda needs effectively-final vars
                final String pn = playerName;
                final String in = itemName;
                final String bs = bindStr;
                source.sendFeedback(() -> Text.literal("§f" + pn + " §8| §e" + in + " §8| §7Bind: " + bs), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
