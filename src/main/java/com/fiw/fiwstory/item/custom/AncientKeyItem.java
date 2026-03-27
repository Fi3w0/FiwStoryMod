package com.fiw.fiwstory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AncientKeyItem extends Item {

    public AncientKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Una llave de origen desconocido.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("Se siente más antigua que cualquier cosa").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("que hayas visto antes.").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("§7Abre algo. Pero ¿qué?§r").formatted(Formatting.DARK_GRAY));
    }
}
