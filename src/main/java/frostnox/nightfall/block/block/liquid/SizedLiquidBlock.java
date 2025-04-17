package frostnox.nightfall.block.block.liquid;

import com.google.common.collect.Lists;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.IBlockRenderProperties;

import java.util.List;
import java.util.function.Supplier;

public class SizedLiquidBlock extends LiquidBlock {
    protected final List<FluidState> stateCache;
    protected int size;
    private boolean fluidStateCacheInitialized = false;
    protected final IBlockRenderProperties renderProperties = new IBlockRenderProperties() { //Make sure particle engine doesn't try to access shape
        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
            return true;
        }
    };

    public SizedLiquidBlock(Supplier<? extends FlowingFluid> p_54694_, Properties p_54695_) {
        super(p_54694_, p_54695_);
        this.stateCache = Lists.newArrayList();
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        Fluid type = pAdjacentBlockState.getFluidState().getType();
        FlowingFluid selfFluid = getFluid();
        return type == selfFluid.getFlowing() || type == selfFluid.getSource();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int i = state.getValue(LEVEL);
        if(!fluidStateCacheInitialized) initFluidStateCache();
        return this.stateCache.get(Math.min(i, size));
    }

    @Override
    protected synchronized void initFluidStateCache() {
        if(!fluidStateCacheInitialized) {
            this.stateCache.add(getFluid().getSource(false));
            size = getFluid().getSource().getAmount(Fluids.EMPTY.defaultFluidState());

            for (int i = 1; i < size; ++i)
                this.stateCache.add(getFluid().getFlowing(i, false));

            this.stateCache.add(getFluid().getFlowing(size, true));
            fluidStateCacheInitialized = true;
        }
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        level.scheduleTick(pos, state.getFluidState().getType(), getFluid().getTickDelay(level));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        level.scheduleTick(pos, state.getFluidState().getType(), getFluid().getTickDelay(level));
    }
}
