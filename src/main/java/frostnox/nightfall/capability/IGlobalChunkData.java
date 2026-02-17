package frostnox.nightfall.capability;

import frostnox.nightfall.network.message.world.ChunkDigProgressToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface IGlobalChunkData {
    void tick();

    void setBreakProgress(BlockPos pos, int decay, float progress, BlockState block);

    void setBreakProgress(BlockPos pos, float progress, BlockState block);

    float getBreakProgress(BlockPos pos);

    void removeBreakProgress(BlockPos pos);

    void simulateBreakProgress(long elapsedTime);

    ChunkDigProgressToClient getBreakProgressMessage();

    CompoundTag writeNBT();

    void readNBT(CompoundTag tag);
}
