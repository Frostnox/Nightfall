package frostnox.nightfall.world.spawngroup;

import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.Season;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class WolfSpawnGroup extends VariedLandSpawnGroup {
    public WolfSpawnGroup(int weight, boolean friendly, int yMin, int yMax, int lightMin, int lightMax, float tempMin, float tempMax, float humidityMin, float humidityMax, TagKey<Block> spawnBlocks) {
        super(weight, friendly, yMin, yMax, lightMin, lightMax, tempMin, tempMax, humidityMin, humidityMax, spawnBlocks);
    }

    @Override
    protected boolean checkClimate(float temperature, float humidity) {
        return super.checkClimate(temperature, humidity) && humidity > temperature - 0.75F;
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        int size;
        switch(Season.get(level)) {
            case SPRING -> {
                if(level.random.nextFloat() < 0.25F) size = 1;
                else size = 2 + level.random.nextInt(3);
            }
            case SUMMER -> {
                if(level.random.nextFloat() < 0.4F) size = 1;
                else size = level.random.nextFloat() < 0.35F ? 3 : 2;
            }
            default -> {
                if(level.random.nextFloat() < 0.1F) size = 1;
                else size = 2 + level.random.nextInt(3);
            }
            case WINTER -> {
                if(level.random.nextFloat() < 0.05F) size = 1;
                else size = 3 + level.random.nextInt(3);
            }
        }
        EntityType<?>[] group = new EntityType[size];
        Arrays.fill(group, EntitiesNF.WOLF.get());
        return group;
    }
}
