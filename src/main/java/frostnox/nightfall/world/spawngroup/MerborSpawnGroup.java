package frostnox.nightfall.world.spawngroup;

import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.Season;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MerborSpawnGroup extends VariedLandSpawnGroup {
    public MerborSpawnGroup(int weight, boolean friendly, int yMin, int yMax, int lightMin, int lightMax, float tempMin, float tempMax, float humidityMin, float humidityMax, TagKey<Block> spawnBlocks) {
        super(weight, friendly, yMin, yMax, lightMin, lightMax, tempMin, tempMax, humidityMin, humidityMax, spawnBlocks);
    }

    @Override
    protected boolean checkClimate(float temperature, float humidity) {
        return super.checkClimate(temperature, humidity) && humidity - temperature > -0.05F;
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        EntityType<?>[] group;
        switch(Season.get(level)) {
            case SPRING -> {
                if(level.random.nextInt() % 3 == 0) {
                    group = new EntityType[]{EntitiesNF.MERBOR_TUSKER.get()};
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(3)];
                    group[0] = EntitiesNF.MERBOR_TUSKER.get();
                    for(int i = 1; i < group.length; i++) group[i] = EntitiesNF.MERBOR_SOW.get();
                }
            }
            case SUMMER -> {
                if(level.random.nextInt() % 3 == 0) {
                    group = new EntityType[]{EntitiesNF.MERBOR_TUSKER.get()};
                }
                else {
                    group = new EntityType[3 + level.random.nextInt(3)];
                    group[0] = EntitiesNF.MERBOR_TUSKER.get();
                    group[1] = EntitiesNF.MERBOR_SOW.get();
                    for(int i = 2; i < group.length; i++) group[i] = level.random.nextInt() % 3 == 0 ? EntitiesNF.MERBOR_TUSKER.get() : EntitiesNF.MERBOR_SOW.get();
                }
            }
            default -> {
                group = new EntityType[3 + level.random.nextInt(3)];
                group[0] = EntitiesNF.MERBOR_TUSKER.get();
                group[1] = EntitiesNF.MERBOR_SOW.get();
                for(int i = 2; i < group.length; i++) group[i] = level.random.nextBoolean() ? EntitiesNF.MERBOR_TUSKER.get() : EntitiesNF.MERBOR_SOW.get();
            }
            case WINTER -> {
                if(level.random.nextBoolean()) {
                    group = new EntityType[]{EntitiesNF.MERBOR_TUSKER.get()};
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(2)];
                    group[0] = level.random.nextBoolean() ? EntitiesNF.MERBOR_TUSKER.get() : EntitiesNF.MERBOR_SOW.get();
                    for(int i = 1; i < group.length; i++) group[i] = EntitiesNF.MERBOR_SOW.get();
                }
            }
        }
        return group;
    }
}
