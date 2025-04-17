package frostnox.nightfall.block;

import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFoodBlock {
    boolean isEatable(BlockState state);

    default void eat(Entity eater, Level level, BlockPos pos) {
        LevelUtil.destroyBlockNoSound(level, pos, true, eater);
    }
}
