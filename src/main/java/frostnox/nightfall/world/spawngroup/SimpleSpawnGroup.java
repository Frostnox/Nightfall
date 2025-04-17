package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;

public class SimpleSpawnGroup extends SpawnGroup {
    private final EntityType<?> type;
    private final int yMin, yMax, lightMin, lightMax, sizeMin, sizeRand;
    private final float tempMin, tempMax, humidityMin, humidityMax;
    private final TagKey<Block> spawnBlocks;

    public SimpleSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, float tempMin, float tempMax, float humidityMin, float humidityMax, TagKey<Block> spawnBlocks) {
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
        this.spawnBlocks = spawnBlocks;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return pos.getY() >= yMin && pos.getY() <= yMax && temperature >= tempMin && temperature <= tempMax && skyLight >= lightMin && skyLight <= lightMax &&
                humidity >= humidityMin && humidity <= humidityMax && block.is(spawnBlocks);
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level) {
        EntityType<?>[] group = new EntityType<?>[sizeRand == 1 ? sizeMin : (sizeMin + level.random.nextInt(sizeRand))];
        Arrays.fill(group, type);
        return group;
    }

    @Override
    public @Nullable SpawnGroupData getGroupData(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity, int groupSize) {
        return null;
    }
}
