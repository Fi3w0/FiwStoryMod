package com.fiw.fiwstory.dimension;

import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;

public class SimpleTimelessVoid {
    public static final Identifier DIMENSION_ID = new Identifier("fiwstory", "timeless_void");
    
    // Para Fabric 1.20.1, la forma más simple es usar el sistema de datos
    // La dimensión se configurará a través de datapacks
    
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
    
    // Los comandos usarán teletransporte normal por ahora
    // La dimensión real se implementaría con datapacks
}