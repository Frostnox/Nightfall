package frostnox.nightfall.world.spawngroup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public abstract class SpawnGroup extends ForgeRegistryEntry<SpawnGroup> {
    private final int weight;
    private final boolean friendly;

    public SpawnGroup(int weight, boolean friendly) {
        this.friendly = friendly;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isFriendly() {
        return friendly;
    }

    /**
     * @param temperature at world position, season independent
     * @param humidity at world position, season independent
     */
    public abstract boolean canSpawnAt(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity);

    /**
     * @return array of entities to spawn with size of at least 1
     */
    public abstract EntityType<?>[] createGroup(ServerLevel level);

    public abstract @Nullable SpawnGroupData getGroupData(ServerLevel level, BlockPos pos, BlockState block, int skyLight, float temperature, float humidity, int groupSize);
}
