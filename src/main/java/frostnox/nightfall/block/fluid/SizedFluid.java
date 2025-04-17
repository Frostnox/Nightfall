package frostnox.nightfall.block.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * Support for custom fluid amounts
 */
public abstract class SizedFluid extends ForgeFlowingFluid {
    protected SizedFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected FluidState getNewLiquid(LevelReader level, BlockPos pos, BlockState pBlockState) {
        FluidState newFluid = super.getNewLiquid(level, pos, pBlockState);
        if(newFluid.getAmount() > getSource().getAmount(newFluid)) { //Vanilla hardcodes the level to 8, so correct it here
            return newFluid.setValue(FlowingFluid.LEVEL, getSource().getAmount(newFluid));
        }
        return newFluid;
    }

    @Override
    public float getOwnHeight(FluidState p_76048_) {
        return (float)p_76048_.getAmount() / (getSource().getAmount(p_76048_) + 1);
    }

    protected int getBlockLevel(FluidState state) {
        return state.isSource() ? 0 : (state.getValue(FALLING) ? getSource().getAmount(state) : state.getAmount());
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        BlockState block = super.createLegacyBlock(state);
        if(block.isAir()) return block;
        else return block.setValue(LiquidBlock.LEVEL, getBlockLevel(state));
    }
}
