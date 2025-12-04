package frostnox.nightfall.capability;

import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.Weather;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface ILevelData {
    void onLoad(long seed);

    long getSeed();

    void updateWeather();

    double getWeatherPercentageAboveIntensityOverTime(float intensity, long startTime, long endTime);

    void setGlobalWeatherIntensity(float intensity);

    float getTargetWeatherIntensity();

    float getLastWeatherIntensity();

    void updateWind();

    long getWindTime();

    void setWindTime(long time);

    long getSeasonTime();

    void setSeasonTime(long time);

    void tick();

    Season getSeason();

    float getSeasonalTemperature(IChunkData chunkData, BlockPos pos);

    boolean isWaterFrozen(IChunkData chunkData, BlockPos pos);

    float getGlobalWeatherIntensity();

    Weather getGlobalWeather();

    float getGlobalRainLevel();

    float getGlobalThunderLevel();

    float getWeatherIntensity(IChunkData chunkData, BlockPos pos);

    Weather getWeather(IChunkData chunkData, BlockPos pos);

    boolean isRainfallCommonAt(IChunkData chunkData, int x, int y, int z);

    float getFogIntensity();

    float getWindX(float partial);

    float getWindZ(float partial);

    float getVegetationNoise(int worldX, int worldZ);

    CompoundTag writeNBTClientInit(CompoundTag tag);

    CompoundTag writeNBTSync(CompoundTag tag);

    CompoundTag writeNBT();

    void readNBT(CompoundTag tag);
}
