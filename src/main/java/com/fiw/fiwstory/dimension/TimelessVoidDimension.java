package com.fiw.fiwstory.dimension;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

public class TimelessVoidDimension {
    public static final RegistryKey<DimensionOptions> DIMENSION_KEY = RegistryKey.of(
        RegistryKeys.DIMENSION,
        new Identifier("fiwstory", "timeless_void")
    );
    
    public static final RegistryKey<World> WORLD_KEY = RegistryKey.of(
        RegistryKeys.WORLD,
        new Identifier("fiwstory", "timeless_void")
    );
    
    public static final Identifier DIMENSION_ID = new Identifier("fiwstory", "timeless_void");
    
    public static class Config {
        public static final int MIN_Y = 0;
        public static final int HEIGHT = 304;
        public static final int LOGICAL_HEIGHT = 304;
        public static final boolean HAS_SKYLIGHT = false;
        public static final boolean HAS_CEILING = false;
        public static final float AMBIENT_LIGHT = 0.0f;
        public static final double COORDINATE_SCALE = 1.0;
        public static final boolean BED_WORKS = true;
        public static final boolean RESPAWN_ANCHOR_WORKS = false;
        public static final boolean ULTRAWARM = false;
        public static final boolean NATURAL = false;
    }
}