package frostnox.nightfall.capability;

import frostnox.nightfall.network.message.world.ChunkDigProgressToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface IGlobalChunkData {
    void tick();

    void setBreakProgress(BlockPos pos, int decay, float progress);

    void setBreakProgress(BlockPos pos, float progress);

    float getBreakProgress(BlockPos pos);

    void removeBreakProgress(BlockPos pos);

    void simulateBreakProgress(long elapsedTime);

    ChunkDigProgressToClient getBreakProgressMessage();

    CompoundTag writeNBT();

    void readNBT(CompoundTag tag);
}
