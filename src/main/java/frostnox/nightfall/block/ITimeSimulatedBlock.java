package frostnox.nightfall.block;

import frostnox.nightfall.capability.IChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.TickPriority;

import java.util.Random;

public interface ITimeSimulatedBlock {
    void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random);

    default TickPriority getTickPriority() {
        return TickPriority.NORMAL;
    }
}
