package frostnox.nightfall.block.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SpiderWebBlock extends MultifaceBlockNF {
    public SpiderWebBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canReplaceState(BlockState state) {
        return super.canReplaceState(state) || state.getMaterial().isReplaceable();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }
}
