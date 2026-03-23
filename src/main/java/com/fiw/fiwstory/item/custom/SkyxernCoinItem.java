package com.fiw.fiwstory.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkyxernCoinItem extends Item {
    public SkyxernCoinItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7Mineral de origen desconocido. Aparece de forma aleatoria§r"));
        tooltip.add(Text.literal("§7en distintos mundos sin patrón ni explicación. Su escasez§r"));
        tooltip.add(Text.literal("§7extrema lo convirtió en moneda universal por acuerdo tácito§r"));
        tooltip.add(Text.literal("§7— todo el mundo lo acepta porque nadie tiene suficiente.§r"));
    }
}
