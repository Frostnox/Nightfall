package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;

public abstract class BiomeTagSpawnGroup extends SpawnGroup {
    protected final EntityType<?> type;
    protected final int yMin, yMax, lightMin, lightMax, sizeMin, sizeRand;
    protected final TagKey<Biome> biomeTag;

    public BiomeTagSpawnGroup(int weight, boolean friendly, EntityType<?> type, int yMin, int yMax, int lightMin, int lightMax, int sizeMin, int sizeMax, TagKey<Biome> biomeTag) {
        super(weight, friendly);
        this.type = type;
        this.yMin = yMin;
        this.yMax = yMax;
        this.lightMin = lightMin;
        this.lightMax = lightMax;
        this.sizeMin = sizeMin;
        this.biomeTag = biomeTag;
        this.sizeRand = sizeMax - sizeMin + 1;
    }

    @Override
    public boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity) {
        return pos.getY() >= yMin && pos.getY() <= yMax && skyLight >= lightMin && skyLight <= lightMax && level.getBiome(pos).is(biomeTag);
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
