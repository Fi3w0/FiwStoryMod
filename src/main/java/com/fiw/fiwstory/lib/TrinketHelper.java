package com.fiw.fiwstory.lib;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utilidades para integración con Trinkets.
 * Provee métodos para verificar si un jugador tiene un artefacto equipado
 * en cualquier slot (mano, offhand o trinket).
 */
public class TrinketHelper {

    /**
     * Verifica si el jugador tiene un item específico equipado en algún slot de Trinkets.
     */
    public static boolean hasTrinketEquipped(PlayerEntity player, Item item) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            return component.get().isEquipped(item);
        }
        return false;
    }

    /**
     * Obtiene el ItemStack de un trinket equipado específico.
     * Retorna ItemStack.EMPTY si no está equipado.
     */
    public static ItemStack getTrinketStack(PlayerEntity player, Item item) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            List<Pair<dev.emi.trinkets.api.SlotReference, ItemStack>> equipped = component.get().getEquipped(item);
            if (!equipped.isEmpty()) {
                return equipped.get(0).getRight();
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Verifica si el jugador tiene un artefacto equipado en cualquier parte:
     * - Mainhand
     * - Offhand
     * - Trinket slot
     */
    public static boolean hasArtifactAnywhere(PlayerEntity player, Item item) {
        // Verificar manos
        if (player.getMainHandStack().getItem() == item) return true;
        if (player.getOffHandStack().getItem() == item) return true;

        // Verificar Trinkets
        return hasTrinketEquipped(player, item);
    }

    /**
     * Verifica si un jugador tiene un artefacto de una clase específica equipado
     * en cualquier slot (manos o trinket).
     */
    public static boolean hasArtifactOfType(PlayerEntity player, Class<? extends Item> itemClass) {
        // Verificar manos
        if (itemClass.isInstance(player.getMainHandStack().getItem())) return true;
        if (itemClass.isInstance(player.getOffHandStack().getItem())) return true;

        // Verificar Trinkets
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            for (var entry : component.get().getAllEquipped()) {
                if (itemClass.isInstance(entry.getRight().getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Obtiene el ItemStack de un artefacto de una clase específica,
     * buscando en manos y trinkets.
     */
    public static ItemStack getArtifactStackOfType(PlayerEntity player, Class<? extends Item> itemClass) {
        // Verificar manos
        if (itemClass.isInstance(player.getMainHandStack().getItem())) {
            return player.getMainHandStack();
        }
        if (itemClass.isInstance(player.getOffHandStack().getItem())) {
            return player.getOffHandStack();
        }

        // Verificar Trinkets
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            for (var entry : component.get().getAllEquipped()) {
                if (itemClass.isInstance(entry.getRight().getItem())) {
                    return entry.getRight();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Obtiene todos los ItemStacks de trinkets equipados.
     */
    public static List<ItemStack> getAllEquippedTrinkets(PlayerEntity player) {
        List<ItemStack> result = new ArrayList<>();
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            for (var entry : component.get().getAllEquipped()) {
                result.add(entry.getRight());
            }
        }
        return result;
    }
}
