package frostnox.nightfall.world;

import net.minecraft.world.level.LevelTimeAccess;

public enum MoonPhase {
    FULL(1),
    WANING_GIBBOUS(0.75F),
    LAST_QUARTER(0.5F),
    WANING_CRESCENT(0.25F),
    NEW(0),
    WAXING_CRESCENT(0.25F),
    FIRST_QUARTER(0.5F),
    WAXING_GIBBOUS(0.75F);

    public final float fullness;

    MoonPhase(float fullness) {
        this.fullness = fullness;
    }

    public static MoonPhase get(LevelTimeAccess level) {
        return values()[level.getMoonPhase()];
    }
}