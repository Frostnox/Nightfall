package frostnox.nightfall.block.block.liquid;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.Random;
import java.util.function.Supplier;

public class LavaLiquidBlock extends SizedLiquidBlock implements IHeatSource {
    public LavaLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if(shouldSpreadLiquid(level, pos, state)) super.onPlace(state, level, pos, pOldState, pIsMoving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if(shouldSpreadLiquid(level, pos, state)) super.neighborChanged(state, level, pos, pBlock, pFromPos, pIsMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean pIsMoving) {
        if(!newState.is(this)) spreadHeat(level, pos, TieredHeat.NONE);
    }

    protected boolean shouldSpreadLiquid(Level level, BlockPos pos, BlockState state) {
        for(Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos blockpos = pos.relative(direction.getOpposite());
            if(level.getFluidState(blockpos).is(FluidTags.WATER)) {
                Block block = level.getFluidState(pos).isSource() ? BlocksNF.OBSIDIAN.get() : BlocksNF.STONE_BLOCKS.get(Stone.PUMICE).get();
                level.setBlockAndUpdate(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, block.defaultBlockState()));
                this.fizz(level, pos);
                return false;
            }
        }
        return true;
    }

    protected void fizz(LevelAccessor level, BlockPos pos) {
        level.levelEvent(1501, pos, 0);
    }

    @Override
    public TieredHeat getHeat(Level level, BlockPos pos, BlockState state) {
        return TieredHeat.ORANGE;
    }
}
