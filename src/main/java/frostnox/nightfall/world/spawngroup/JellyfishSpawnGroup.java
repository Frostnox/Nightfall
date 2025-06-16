package frostnox.nightfall.world.spawngroup;

import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;

public class JellyfishSpawnGroup extends OceanSpawnGroup {
    public JellyfishSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, float tempMin, float tempMax, float humidityMin, float humidityMax) {
        super(weight, friendly, type, yMin, yMax, lightMin, lightMax, sizeMin, sizeMax, tempMin, tempMax, humidityMin, humidityMax);
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        int adjustedRand = sizeRand - (int) ((1F - temperature) * 5);
        EntityType<?>[] group = new EntityType<?>[adjustedRand <= 1 ? sizeMin : (sizeMin + level.random.nextInt(adjustedRand))];
        Arrays.fill(group, type);
        return group;
    }

    @Override
    public @Nullable SpawnGroupData getGroupData(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity, int groupSize) {
        int[] weight = new int[]{1, 1, 1, 1};
        MoonPhase moonPhase = MoonPhase.get(level);
        //Moon
        if(temperature < 0.4F) weight[0]++;
        if(moonPhase == MoonPhase.FULL) weight[0] += 2;
        else if(moonPhase.fullness >= 0.75F) weight[0] += 1;
        //Amber
        if(temperature < 0.55F) weight[1] += 1;
        //Rose
        if(temperature > 0.75F) weight[2] += 1;
        //Scarlet
        if(temperature > 0.6F) weight[3] += 1;
        int sum = 0;
        for(int w : weight) sum += w;
        int rand = level.random.nextInt(sum);
        int cumulativeWeight = 0;
        JellyfishEntity.Type type = JellyfishEntity.Type.MOON;
        for(int i = 0; i < weight.length; i++) {
            cumulativeWeight += weight[i];
            if(rand < cumulativeWeight) {
                type = JellyfishEntity.Type.values()[i];
                break;
            }
        }
        return new JellyfishEntity.GroupData(type, level.random.nextInt());
    }
}
