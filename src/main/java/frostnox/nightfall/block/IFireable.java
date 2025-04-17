package frostnox.nightfall.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * For blocks that can be fired into other blocks.
 * Includes pottery.
 */
public interface IFireable {
    /**
     * @return blockstate to replace this block with once it is fired
     */
    BlockState getFiredBlock();

    /**
     * @return current amount of cook ticks
     */
    int getCookTicks(BlockPos pos, Level level);

    /**
     * @return time in ticks to fire
     */
    int getCookTicksTotal();
}
