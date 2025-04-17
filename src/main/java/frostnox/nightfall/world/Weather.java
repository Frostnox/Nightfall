package frostnox.nightfall.world;

import net.minecraft.network.chat.TranslatableComponent;

import java.util.Locale;

public enum Weather {
    CLEAR(false),
    CLOUDS(false),
    RAIN(true),
    SNOW(true),
    FOG(false);

    public static final double PARTICLE_SPAWN_RADIUS = 32D;
    public static final double PARTICLE_DESPAWN_RADIUS_SQR = (PARTICLE_SPAWN_RADIUS + 4) * (PARTICLE_SPAWN_RADIUS + 4);
    public static final float FOG_THRESHOLD = -0.45F;
    public static final float GLOBAL_CLEAR_THRESHOLD = 0.15F;
    public static final float GLOBAL_CLOUDS_THRESHOLD = 0.45F;

    public final boolean isPrecipitation;
    private final TranslatableComponent translatable;

    Weather(boolean isPrecipitation) {
        this.isPrecipitation = isPrecipitation;
        translatable = new TranslatableComponent("weather." + toString().toLowerCase(Locale.ROOT));
    }

    public static Weather get(float intensity) {
        if(intensity < FOG_THRESHOLD) return Weather.FOG;
        else if(intensity < GLOBAL_CLEAR_THRESHOLD) return Weather.CLEAR;
        else if(intensity < GLOBAL_CLOUDS_THRESHOLD) return Weather.CLOUDS;
        else return Weather.RAIN;
    }

    public TranslatableComponent toTranslatable() {
        return translatable;
    }
}
