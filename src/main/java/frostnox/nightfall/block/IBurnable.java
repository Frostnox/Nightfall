package frostnox.nightfall.block;

import net.minecraft.world.level.block.state.BlockState;

public interface IBurnable {
    BlockState getBurnedState(BlockState state);
}