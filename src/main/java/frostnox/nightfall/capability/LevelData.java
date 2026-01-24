package frostnox.nightfall.capability;

import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.math.noise.FractalSimplexNoiseFast;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.Weather;
import frostnox.nightfall.world.generation.ContinentalChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LevelData implements ILevelData {
    private record WeatherData(float intensity, int duration) {}
    public static final Capability<ILevelData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {}); //Reference to manager instance
    public static final float WATER_FREEZE_TEMP = 0.2F, SEAWATER_FREEZE_TEMP = 0.05F,
            FRAZIL_TEMP = WATER_FREEZE_TEMP + 0.02F, SEA_FRAZIL_TEMP = SEAWATER_FREEZE_TEMP + 0.02F, SNOW_TEMP = 0.3F;
    private static final int HISTORY_LENGTH = 150;
    private final Level level;
    private final WeatherData[] weatherHistory = new WeatherData[HISTORY_LENGTH];
    private int historyIndex = -1;
    private int weatherDuration = 1, weatherTicks = 2;
    private long windTime, seasonTime = Season.SPRING.getMiddleTime(), seed;
    private float rawWeatherIntensity, weatherIntensity, lastWeatherIntensity, windX, windZ, lastWindX = -10F, lastWindZ;
    private FractalSimplexNoiseFast vegetationNoise;

    private LevelData(Level level) {
        this.level = level;
    }

    private void generateNewWeather() {
        float lastLastWeatherIntensity = lastWeatherIntensity;
        lastWeatherIntensity = weatherIntensity;
        //Let fog hang around for extra time
        if(Weather.get(lastLastWeatherIntensity) != Weather.FOG && Weather.get(lastWeatherIntensity) == Weather.FOG) {
            int time = (int) (20 * 60 * 3 * -weatherIntensity);
            weatherDuration = time + level.random.nextInt(time);
        }
        else {
            //Modeled after realistic rainfall distribution in a temperate zone
            weatherIntensity = (float) MathUtil.gammaSample(1.2, level.random) / 4F;
            Season season = getSeason();
            if(level.random.nextFloat() < (season == Season.FALL ? 0.5F : 0.3F)) {
                weatherIntensity = -Math.min(weatherIntensity, 1F);
            }
            else {
                if(season == Season.SUMMER) weatherIntensity *= 0.8F;
                else if(season == Season.SPRING) weatherIntensity *= 1.2F;
                weatherIntensity = Math.min(weatherIntensity, 1F);
            }
            weatherDuration = 20 * 60 * 8 + level.random.nextInt(20 * 60 * 12);
        }
        //Record weather data
        weatherHistory[historyIndex] = new WeatherData(weatherIntensity, weatherDuration);
        historyIndex = (historyIndex + 1) % HISTORY_LENGTH;
    }

    @Override
    public void onLoad(long seed) {
        this.seed = seed;
        XoroshiroRandomSource random = new XoroshiroRandomSource(seed);
        vegetationNoise = new FractalSimplexNoiseFast(random.nextLong(), 0.0045F, 2, 0.5F, 2.0F);
        if(historyIndex == -1) {
            historyIndex = 0;
            for(int i = 0; i < HISTORY_LENGTH; i++) {
                //Ensure that weather starts from 0
                if(i == HISTORY_LENGTH - 2) {
                    lastWeatherIntensity = weatherIntensity;
                    weatherIntensity = 0F;
                    weatherDuration = 20 * 60 * 8 + level.random.nextInt(20 * 60 * 12);
                    weatherHistory[historyIndex] = new WeatherData(weatherIntensity, weatherDuration);
                    historyIndex = (historyIndex + 1) % HISTORY_LENGTH;
                }
                else generateNewWeather();
            }
        }
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void updateWeather() {
        if(!level.isClientSide && weatherTicks > weatherDuration) {
            generateNewWeather();
            weatherTicks = 0;
        }
        rawWeatherIntensity = Mth.lerp((float) weatherTicks / weatherDuration, lastWeatherIntensity, weatherIntensity);
    }

    @Override
    public double getWeatherPercentageAboveIntensityOverTime(float intensity, long startTime, long endTime) {
        double totalDuration = 0, targetDuration = 0;
        //Special handling for first entry since it's always in progress
        if(startTime < weatherTicks) {
            WeatherData cur = weatherHistory[historyIndex];
            WeatherData prev = weatherHistory[Math.floorMod(historyIndex - 1, HISTORY_LENGTH)];
            float curIntensity = Mth.lerp((float) weatherTicks / cur.duration, prev.intensity, cur.intensity);
            float prevIntensity = endTime >= weatherTicks ? prev.intensity : Mth.lerp((float) endTime / cur.duration, cur.intensity, prev.intensity);
            double duration = Math.min(endTime, weatherTicks) - startTime;
            totalDuration += duration;
            if((intensity >= prevIntensity && intensity <= curIntensity) || (intensity >= curIntensity && intensity <= prevIntensity)) {
                targetDuration += duration - ((intensity - prevIntensity) / (curIntensity - prevIntensity) * duration);
            }
            if(endTime <= weatherTicks) return targetDuration / totalDuration;
        }
        else totalDuration += weatherTicks;
        //Remaining past entries
        int fromIndex = Math.floorMod(historyIndex - 1, HISTORY_LENGTH), toIndex;
        for(int i = 2; i < HISTORY_LENGTH; i++) {
            toIndex = fromIndex;
            fromIndex = Math.floorMod(historyIndex - i, HISTORY_LENGTH);
            WeatherData cur = weatherHistory[toIndex];
            double fullDuration = totalDuration + cur.duration;
            if(startTime < fullDuration) {
                WeatherData prev = weatherHistory[fromIndex];
                double intervalCur = Math.max(startTime - totalDuration, 0);
                double intervalPrev = Math.min(fullDuration, endTime) - totalDuration;
                double curIntensity = Mth.lerp(1D - (intervalCur / cur.duration), prev.intensity, cur.intensity);
                double prevIntensity = Mth.lerp(intervalPrev / cur.duration, cur.intensity, prev.intensity);
                double duration = intervalPrev - intervalCur;
                totalDuration += duration;
                if((intensity >= prevIntensity && intensity <= curIntensity) || (intensity >= curIntensity && intensity <= prevIntensity)) {
                    targetDuration += duration - ((intensity - prevIntensity) / (curIntensity - prevIntensity) * duration);
                }
                if(endTime <= fullDuration) return targetDuration / totalDuration;
            }
            else totalDuration += cur.duration;
        }
        return targetDuration / totalDuration;
    }

    @Override
    public void setGlobalWeatherIntensity(float intensity) {
        weatherIntensity = intensity;
        weatherTicks = weatherDuration + 1;
        updateWeather();
    }

    @Override
    public float getTargetWeatherIntensity() {
        return weatherIntensity;
    }

    @Override
    public float getLastWeatherIntensity() {
        return lastWeatherIntensity;
    }

    @Override
    public void updateWind() {
        float v = windTime * 0.00011F;
        float f = Mth.sin(MathUtil.SQRT_2 * 0.2F * v) + Mth.sin(MathUtil.PI * 0.2F * v)
                + Mth.cos(0.1F * v) + Mth.cos(0.15F * MathUtil.PI * v);
        float windIntensity = Math.min(1F, Math.max(0F, (f - Mth.cos(0.2F * MathUtil.PI_SQRT * v)) / 5F) +
                Math.max(0, ((f - Mth.sin(0.2F * MathUtil.PI_SQRT * v)) / 5F) / 4F));
        if(lastWindX != 10F) {
            lastWindX = windX;
            lastWindZ = windZ;
        }
        if(windIntensity == 0F) {
            windX = 0F;
            windZ = 0F;
        }
        else {
            float rot = (1F + (Mth.sin(MathUtil.SQRT_2 * 0.1F * v) + Mth.sin(MathUtil.PI * 0.1F * v) + Mth.cos(0.05F * v)
                    + Mth.cos(0.075F * MathUtil.PI * v) - Mth.sin(0.1F * MathUtil.PI_SQRT * v)) / 5F) * MathUtil.PI;
            windX = Mth.cos(rot) * windIntensity;
            windZ = Mth.sin(rot) * windIntensity;
        }
        if(lastWindX == 10F) {
            lastWindX = windX;
            lastWindZ = windZ;
        }
    }

    @Override
    public long getWindTime() {
        return windTime;
    }

    @Override
    public void setWindTime(long time) {
        windTime = time;
        updateWind();
    }

    @Override
    public long getSeasonTime() {
        return seasonTime;
    }

    @Override
    public void setSeasonTime(long time) {
        seasonTime = time;
    }

    @Override
    public void tick() {
        windTime++;
        seasonTime++;
        weatherTicks++;
        updateWind();
        updateWeather();
    }

    @Override
    public Season getSeason() {
        return Season.get(seasonTime);
    }

    @Override
    public float getSeasonalTemperature(IChunkData chunkData, BlockPos pos) {
        if(pos.getY() < ContinentalChunkGenerator.SEA_LEVEL - 64) return 0.5F;
        else if(pos.getY() < ContinentalChunkGenerator.SEA_LEVEL - 32) {
            return chunkData.getTemperature(pos) + Mth.lerp(Math.abs(pos.getY() - (ContinentalChunkGenerator.SEA_LEVEL - 32)) / 32F, Season.getTemperatureInfluence(seasonTime), 0F);
        }
        else return chunkData.getTemperature(pos) + Season.getTemperatureInfluence(seasonTime);
    }

    @Override
    public boolean isWaterFrozen(IChunkData chunkData, BlockPos pos) {
        return getSeasonalTemperature(chunkData, pos) <= WATER_FREEZE_TEMP;
    }

    @Override
    public float getGlobalWeatherIntensity() {
        return rawWeatherIntensity;
    }

    @Override
    public Weather getGlobalWeather() {
        return Weather.get(rawWeatherIntensity);
    }

    @Override
    public float getGlobalRainLevel() {
        return rawWeatherIntensity < Weather.GLOBAL_CLEAR_THRESHOLD ? 0F : Math.min((rawWeatherIntensity - Weather.GLOBAL_CLEAR_THRESHOLD) * 3F, 1F);
    }

    @Override
    public float getGlobalThunderLevel() {
        return rawWeatherIntensity < 0.3F ? 0F : Math.min((rawWeatherIntensity - 0.3F) * 3F, 1F);
    }

    /**
     * @return -1F <= intensity <= 1.3F
     */
    @Override
    public float getWeatherIntensity(IChunkData chunkData, BlockPos pos) {
        if(rawWeatherIntensity < 0F) return rawWeatherIntensity;
        else return rawWeatherIntensity + chunkData.getWeatherAddend(pos.getX(), pos.getZ());
    }

    @Override
    public Weather getWeather(IChunkData chunkData, BlockPos pos) {
        Weather weather = Weather.get(getWeatherIntensity(chunkData, pos));
        if(weather == Weather.RAIN && getSeasonalTemperature(chunkData, pos) <= SNOW_TEMP) weather = Weather.SNOW;
        return weather;
    }

    @Override
    public boolean isRainfallCommonAt(IChunkData chunkData, int x, int y, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) < y
                && Weather.get(0.5F + chunkData.getWeatherAddend(x, z)).isPrecipitation;
    }

    @Override
    public float getFogIntensity() {
        if(rawWeatherIntensity > Weather.FOG_THRESHOLD) return 0F;
        else return -(rawWeatherIntensity - Weather.FOG_THRESHOLD) / 0.25F;
    }

    @Override
    public float getWindX(float partial) {
        return Mth.lerp(partial, lastWindX, windX);
    }

    @Override
    public float getWindZ(float partial) {
        return Mth.lerp(partial, lastWindZ, windZ);
    }

    @Override
    public float getVegetationNoise(int worldX, int worldZ) {
        if(vegetationNoise == null) return 0;
        else return vegetationNoise.noise2D(worldX, worldZ);
    }

    @Override
    public CompoundTag writeNBTClientInit(CompoundTag tag) {
        tag.putLong("seed", seed);
        return tag;
    }

    @Override
    public CompoundTag writeNBTSync(CompoundTag tag) {
        tag.putLong("windTime", windTime);
        tag.putLong("seasonTime", seasonTime);
        tag.putInt("weatherDuration", weatherDuration);
        tag.putInt("weatherTicks", weatherTicks);
        tag.putFloat("weatherIntensity", weatherIntensity);
        tag.putFloat("lastWeatherIntensity", lastWeatherIntensity);
        return tag;
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("historyIndex", historyIndex);
        ListTag intensities = new ListTag(), durations = new ListTag();
        for(WeatherData data : weatherHistory) {
            intensities.add(FloatTag.valueOf(data.intensity));
            durations.add(IntTag.valueOf(data.duration));
        }
        tag.put("intensities", intensities);
        tag.put("durations", durations);
        return writeNBTSync(tag);
    }

    @Override
    public void readNBT(CompoundTag tag) {
        if(tag.contains("windTime")) windTime = tag.getLong("windTime");
        if(tag.contains("seasonTime")) seasonTime = tag.getLong("seasonTime");
        if(tag.contains("weatherDuration")) weatherDuration = tag.getInt("weatherDuration");
        if(tag.contains("weatherTicks")) weatherTicks = tag.getInt("weatherTicks");
        if(tag.contains("weatherIntensity")) weatherIntensity = tag.getFloat("weatherIntensity");
        if(tag.contains("lastWeatherIntensity")) lastWeatherIntensity = tag.getFloat("lastWeatherIntensity");
        if(tag.contains("historyIndex")) historyIndex = tag.getInt("historyIndex");
        if(tag.contains("intensities") && tag.contains("durations")) {
            ListTag intensities = tag.getList("intensities", ListTag.TAG_FLOAT);
            ListTag durations = tag.getList("durations", ListTag.TAG_INT);
            if(intensities.size() != durations.size()) throw new IllegalStateException("Recorded weather intensities and durations are not equal in size, likely corrupted data");
            for(int i = 0; i < HISTORY_LENGTH; i++) {
                weatherHistory[i] = new WeatherData(intensities.getFloat(i), durations.getInt(i));
            }
        }
    }

    public static ILevelData get(Level level) {
        return level.getCapability(CAPABILITY, null).orElseThrow(() -> new IllegalArgumentException("Null in LazyOptional."));
    }

    public static boolean isPresent(Level level) {
        return level.getCapability(CAPABILITY).isPresent();
    }

    public static LazyOptional<ILevelData> getOptional(Level level) {
        return level.getCapability(CAPABILITY, null);
    }

    public static class LevelDataCapability implements ICapabilitySerializable<CompoundTag> {
        private final LevelData cap;
        private final LazyOptional<ILevelData> holder;

        public LevelDataCapability(Level level) {
            cap = new LevelData(level);
            holder = LazyOptional.of(() -> cap);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> c, Direction side) {
            return CAPABILITY == c ? (LazyOptional<T>) holder : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return cap.writeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag NBT) {
            cap.readNBT(NBT);
        }
    }
}
