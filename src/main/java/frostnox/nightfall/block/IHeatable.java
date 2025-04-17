package frostnox.nightfall.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IHeatable {
    void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir);
}
