package frostnox.nightfall.world;

import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.data.Vec2f;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Locale;

public enum Season {
    SPRING(),
    SUMMER(),
    FALL(),
    WINTER();

    public static final long SEASON_LENGTH = ContinentalWorldType.DAY_LENGTH * 7; //4 hours 40 min
    public static final long YEAR_LENGTH = SEASON_LENGTH * 4L;
    public static final long SPRING_START = 0;
    public static final long SUMMER_START = SEASON_LENGTH;
    public static final long FALL_START = SEASON_LENGTH * 2L;
    public static final long WINTER_START = SEASON_LENGTH * 3L;
    public static final float TEMP_INFLUENCE = 0.25F;
    private final TranslatableComponent translatable;

    Season() {
        translatable = new TranslatableComponent("season." + toString().toLowerCase(Locale.ROOT));
    }

    public float getProgress(long time) {
        return (time % SEASON_LENGTH) / (float) SEASON_LENGTH;
    }

    public long getStartTime() {
        return ordinal() * SEASON_LENGTH;
    }

    public long getMiddleTime() {
        return ordinal() * SEASON_LENGTH + SEASON_LENGTH / 2;
    }

    public TranslatableComponent toTranslatable() {
        return translatable;
    }

    public static Season get(Level level) {
        return LevelData.isPresent(level) ? get(LevelData.get(level).getSeasonTime()) : SUMMER;
    }

    public static Season get(long time) {
        return switch((int) (time / SEASON_LENGTH) % 4) {
            case 0 -> SPRING;
            case 1 -> SUMMER;
            case 2 -> FALL;
            default -> WINTER;
        };
    }

    public static float getNormalizedProgress(Level level) {
        return LevelData.isPresent(level) ? getNormalizedProgress(LevelData.get(level).getSeasonTime()) : SUMMER.getMiddleTime();
    }

    /**
     * @return 0 - 1 where each quartile corresponds to a season in chronological order
     */
    public static float getNormalizedProgress(long time) {
        return (time % YEAR_LENGTH) / (float) YEAR_LENGTH;
    }

    public static LongLongPair getTimesAtTemperatureInfluence(float tempInfluence) {
        //Solve for start/end times based on temp influence equation
        double acos = 4 * Math.acos(-tempInfluence / TEMP_INFLUENCE);
        long startTime = (long) (-(YEAR_LENGTH / 8L * (Math.PI + acos) / Math.PI));
        long endTime = (long) (YEAR_LENGTH / 8L * (-Math.PI + acos) / Math.PI);
        return LongLongPair.of(startTime, endTime);
    }

    public static long getTimePassedWithin(long seasonTime, long elapsedTime, long startTime, long endTime) {
        long fullDays = elapsedTime / YEAR_LENGTH;
        long activeTime = endTime - startTime;
        if(activeTime != YEAR_LENGTH) activeTime = Math.floorMod(activeTime, YEAR_LENGTH);
        //Count time for full years
        long timePassed = activeTime * fullDays;
        //Count time for partial years
        long currentPartialTime = Math.floorMod(seasonTime, YEAR_LENGTH);
        long pastPartialTime = currentPartialTime - (elapsedTime % YEAR_LENGTH);
        if(currentPartialTime == pastPartialTime) return timePassed;
        //Wrap around case
        if(endTime < startTime) {
            if(pastPartialTime < endTime) timePassed += Math.min(endTime, currentPartialTime) - pastPartialTime;
            if(currentPartialTime > startTime) timePassed += currentPartialTime - Math.max(pastPartialTime, startTime);
        }
        //Standard case
        else {
            if(currentPartialTime > startTime && pastPartialTime < endTime) {
                timePassed += Math.min(currentPartialTime, endTime) - Math.max(pastPartialTime, startTime);
            }
        }
        return timePassed;
    }

    /**
     * @return additive temperature influence of season
     */
    public static float getTemperatureInfluence(long time) {
        float progress = time / (float) YEAR_LENGTH * MathUtil.PI * 2F;
        return TEMP_INFLUENCE * Mth.sin(progress - MathUtil.PI / 4F);
    }

    public static float getPlantColorMultiplier(Level level) {
        float progress = getNormalizedProgress(level);
        if(progress < 0.25F) return 0.9F + (progress / 0.25F) * 0.1F;
        else if(progress < 0.5F) return 1;
        else if(progress < 0.6F) return 1F - ((progress - 0.5F) / 0.1F);
        else if(progress < 0.625F) return 0;
        else if(progress < 0.75F) return 0.2F * ((progress - 0.625F) / 0.125F);
        else if(progress < 0.9F) return 0.2F;
        else return 0.2F + 0.7F * ((progress - 0.9F) / 0.1F);
    }

    public static Vec2f getMinMaxTemperatureInfluence(long startTime, long endTime) {
        long timeElapsed = endTime - startTime;
        if(timeElapsed >= YEAR_LENGTH) return new Vec2f(-TEMP_INFLUENCE, TEMP_INFLUENCE);
        long c1 = Math.round(startTime / (YEAR_LENGTH / 2D)) * (YEAR_LENGTH / 2L);
        long c2 = Math.round(endTime / (YEAR_LENGTH / 2D)) * (YEAR_LENGTH / 2L);
        //Single point so invalidate one
        if(c1 == c2) c2 = -1L;
        //Points are functionally identical so move one up to other unique critical point
        else if((Math.abs(c1) + Math.abs(c2)) % YEAR_LENGTH == 0L) c1 += YEAR_LENGTH / 2L;
        c1 -= YEAR_LENGTH / 8L;
        c2 -= YEAR_LENGTH / 8L;
        boolean hasC1 = c1 >= startTime && c1 <= endTime;
        boolean hasC2 = c2 >= startTime && c2 <= endTime;
        if(hasC1 && hasC2) {
            float c1Temp = getTemperatureInfluence(c1), c2Temp = getTemperatureInfluence(c2);
            return new Vec2f(Math.min(c1Temp, c2Temp), Math.max(c1Temp, c2Temp));
        }
        else {
            float startTemp = getTemperatureInfluence(startTime), endTemp = getTemperatureInfluence(endTime);
            if(hasC1) {
                float cTemp = getTemperatureInfluence(c1);
                return new Vec2f(Math.min(cTemp, Math.min(startTemp, endTemp)), Math.max(cTemp, Math.max(startTemp, endTemp)));
            }
            else if(hasC2) {
                float cTemp = getTemperatureInfluence(c2);
                return new Vec2f(Math.min(cTemp, Math.min(startTemp, endTemp)), Math.max(cTemp, Math.max(startTemp, endTemp)));
            }
            else return new Vec2f(Math.min(startTemp, endTemp), Math.max(startTemp, endTemp));
        }
    }

    /**
     * @return seasonal growth modifier for plants: max increase at summer and max decrease at winter
     */
    public static float getGrowthMultiplier(long time) {
        float progress = time / (float) YEAR_LENGTH * MathUtil.PI * 2F;
        float sin = Mth.sin(progress - MathUtil.PI / 4F);
        if(sin > 0F) sin *= 0.5F;
        return 0.5F * sin;
    }
}
