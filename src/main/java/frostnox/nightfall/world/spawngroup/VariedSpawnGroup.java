package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class VariedSpawnGroup extends SpawnGroup {
    protected final int yMin, yMax, lightMin, lightMax;
    protected final float tempMin, tempMax, humidityMin, humidityMax;

    public VariedSpawnGroup(int weight, boolean friendly, int yMin, int yMax, int lightMin, int lightMax, float tempMin, float tempMax, float humidityMin, float humidityMax) {
        super(weight, friendly);
        this.yMin = yMin;
        this.yMax = yMax;
        this.lightMin = lightMin;
        this.lightMax = lightMax;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
    }

    protected boolean checkClimate(float temperature, float humidity) {
        return temperature >= tempMin && temperature <= tempMax && humidity >= humidityMin && humidity <= humidityMax;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return pos.getY() >= yMin && pos.getY() <= yMax && skyLight >= lightMin && skyLight <= lightMax && checkClimate(temperature, humidity);
    }

    @Override
    public @Nullable SpawnGroupData getGroupData(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity, int groupSize) {
        return null;
    }
}
