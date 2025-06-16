package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;

public abstract class SimpleSpawnGroup extends SpawnGroup {
    protected final EntityType<?> type;
    protected final int yMin, yMax, lightMin, lightMax, sizeMin, sizeRand;
    protected final float tempMin, tempMax, humidityMin, humidityMax;

    public SimpleSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, float tempMin, float tempMax, float humidityMin, float humidityMax) {
        super(weight, friendly);
        this.type = type;
        this.yMin = yMin;
        this.yMax = yMax;
        this.lightMin = lightMin;
        this.lightMax = lightMax;
        this.sizeMin = sizeMin;
        this.sizeRand = sizeMax - sizeMin + 1;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return pos.getY() >= yMin && pos.getY() <= yMax && temperature >= tempMin && temperature <= tempMax && skyLight >= lightMin && skyLight <= lightMax &&
                humidity >= humidityMin && humidity <= humidityMax;
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        EntityType<?>[] group = new EntityType<?>[sizeRand == 1 ? sizeMin : (sizeMin + level.random.nextInt(sizeRand))];
        Arrays.fill(group, type);
        return group;
    }

    @Override
    public @Nullable SpawnGroupData getGroupData(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity, int groupSize) {
        return null;
    }
}
