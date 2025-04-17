package frostnox.nightfall.block;

import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

/**
 * For blocks that can provide heat to neighboring blocks.
 */
public interface IHeatSource {
    TieredHeat getHeat(Level level, BlockPos pos, BlockState state);

    default void scheduleHeatTick(LevelAccessor level, BlockPos pos, Block block) {
        level.scheduleTick(pos, block, 4 + level.getRandom().nextInt(9), TickPriority.HIGH);
    }

    default float getTemperature(Level level, BlockPos pos, BlockState state) {
        return getHeat(level, pos, state).getBaseTemp();
    }

    default void spreadHeat(Level level, BlockPos pos, TieredHeat heat) {
        for(Direction dir : Direction.values()) {
            BlockPos spreadPos = pos.relative(dir);
            BlockState spreadState = level.getBlockState(spreadPos);
            LevelUtil.spreadHeat(level, spreadPos, spreadState, heat, dir.getOpposite());
        }
    }
}
