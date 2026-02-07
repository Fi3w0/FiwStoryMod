package com.fiw.fiwstory.effect;

import com.fiw.fiwstory.FiwstoryMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registro de efectos de estado personalizados para Fiw Story.
 */
public class ModStatusEffects {
    
    public static final StatusEffect CORRUPTION = new CorruptionStatusEffect();
    
    public static void registerStatusEffects() {
        Registry.register(Registries.STATUS_EFFECT, 
            new Identifier(FiwstoryMod.MOD_ID, "corruption"), 
            CORRUPTION);
        
        FiwstoryMod.LOGGER.info("Registering mod status effects for " + FiwstoryMod.MOD_ID);
    }
}