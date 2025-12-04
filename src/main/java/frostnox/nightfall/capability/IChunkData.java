package frostnox.nightfall.capability;

import frostnox.nightfall.network.message.world.ChunkClimateToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;
import java.util.UUID;

public interface IChunkData {
    LevelChunk getChunk();

    boolean isNew();

    void setOld();

    ChunkClimateToClient createClimateMessageToClient();

    float getTemperature(BlockPos pos);

    float getBaseTemperature(int x, int z);

    float getHumidity(BlockPos pos);

    float getHumidity(int x, int z);

    float getWeatherAddend(int x, int z);

    void setTemperature(int x, int z, float temperature);

    void setHumidity(int x, int z, float humidity);

    boolean hasSpawnedUndead();

    void setSpawnedUndead(boolean spawned);

    long getLastTickingGameTime();

    void setLastTickingGameTime(long time);

    long getLastLoadedDayTime();

    void setLastLoadedDayTime(long time);

    void tickPhysics();

    void schedulePhysicsTick(BlockPos pos);

    void schedulePhysicsTickAround(BlockPos pos);

    void tryEntitySpawn(boolean spawnFriendlies, boolean spawnEnemies);

    void addWardingEffigy(BlockPos pos);

    void removeWardingEffigy(BlockPos pos);

    boolean isUndeadSpawnBlocked(BlockPos spawnPos);

    void simulateTime(long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random);

    void addSimulatableBlock(TickPriority priority, BlockPos pos);

    void removeSimulatableBlock(TickPriority priority, BlockPos pos);

    void addUndeadUUID(UUID id);

    void clearUndeadUUIDs();

    boolean areUndeadLoaded();

    CompoundTag writeNBT();

    void readNBT(CompoundTag tag);
}
