package frostnox.nightfall.world.spawngroup;

import frostnox.nightfall.registry.forge.EntitiesNF;
import frostnox.nightfall.world.Season;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DrakefowlSpawnGroup extends VariedLandSpawnGroup {
    public DrakefowlSpawnGroup(int weight, boolean friendly, int yMin, int yMax, int lightMin, int lightMax, float tempMin, float tempMax, float humidityMin, float humidityMax, TagKey<Block> spawnBlocks) {
        super(weight, friendly, yMin, yMax, lightMin, lightMax, tempMin, tempMax, humidityMin, humidityMax, spawnBlocks);
    }

    @Override
    public EntityType<?>[] createGroup(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        EntityType<?>[] group;
        switch(Season.get(level)) {
            case SPRING -> {
                if(level.random.nextInt() % 3 == 0) {
                    if(level.random.nextInt() % 10 == 0) group = new EntityType[]{EntitiesNF.DRAKEFOWL_HEN.get()};
                    else group = new EntityType[]{EntitiesNF.DRAKEFOWL_ROOSTER.get()};
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(3)];
                    group[0] = EntitiesNF.DRAKEFOWL_ROOSTER.get();
                    for(int i = 1; i < group.length; i++) group[i] = EntitiesNF.DRAKEFOWL_HEN.get();
                }
            }
            case SUMMER -> {
                if(level.random.nextInt() % 5 == 0) {
                    if(level.random.nextBoolean()) group = new EntityType[]{EntitiesNF.DRAKEFOWL_HEN.get()};
                    else group = new EntityType[]{EntitiesNF.DRAKEFOWL_ROOSTER.get()};
                }
                else if(level.random.nextBoolean()) {
                    group = new EntityType[1 + level.random.nextInt(4)];
                    group[0] = EntitiesNF.DRAKEFOWL_HEN.get();
                    for(int i = 1; i < group.length; i++) group[i] = EntitiesNF.DRAKEFOWL_CHICK.get();
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(4)];
                    group[0] = EntitiesNF.DRAKEFOWL_HEN.get();
                    group[1] = EntitiesNF.DRAKEFOWL_ROOSTER.get();
                    for(int i = 2; i < group.length; i++) group[i] = EntitiesNF.DRAKEFOWL_CHICK.get();
                }
            }
            default -> {
                if(level.random.nextInt() % 10 == 0) {
                    if(level.random.nextBoolean()) group = new EntityType[]{EntitiesNF.DRAKEFOWL_HEN.get()};
                    else group = new EntityType[]{EntitiesNF.DRAKEFOWL_ROOSTER.get()};
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(3)];
                    group[0] = EntitiesNF.DRAKEFOWL_HEN.get();
                    group[1] = EntitiesNF.DRAKEFOWL_ROOSTER.get();
                    for(int i = 2; i < group.length; i++) group[i] = level.random.nextInt() % 3 == 0 ? EntitiesNF.DRAKEFOWL_ROOSTER.get() : EntitiesNF.DRAKEFOWL_HEN.get();
                }
            }
            case WINTER -> {
                if(level.random.nextInt() % 20 == 0) {
                    if(level.random.nextBoolean()) group = new EntityType[]{EntitiesNF.DRAKEFOWL_HEN.get()};
                    else group = new EntityType[]{EntitiesNF.DRAKEFOWL_ROOSTER.get()};
                }
                else {
                    group = new EntityType[2 + level.random.nextInt(3)];
                    group[0] = EntitiesNF.DRAKEFOWL_HEN.get();
                    group[1] = EntitiesNF.DRAKEFOWL_ROOSTER.get();
                    for(int i = 2; i < group.length; i++) group[i] = level.random.nextInt() % 8 == 0 ? EntitiesNF.DRAKEFOWL_ROOSTER.get() : EntitiesNF.DRAKEFOWL_HEN.get();
                }
            }
        }
        return group;
    }
}
