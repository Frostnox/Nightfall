package frostnox.nightfall.block;

import net.minecraft.world.level.block.state.BlockState;

public interface IBlockChunkLoader {
    boolean keepForceChunk(BlockState state);
}
