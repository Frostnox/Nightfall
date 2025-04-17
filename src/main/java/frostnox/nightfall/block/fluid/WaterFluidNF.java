package frostnox.nightfall.block.fluid;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class WaterFluidNF extends SizedFluid {
    protected WaterFluidNF(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, Random random) {
        if (!state.isSource() && !state.getValue(FALLING)) {
            if (random.nextInt(64) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        } else if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.UNDERWATER, (double)pos.getX() + random.nextDouble(), (double)pos.getY() + random.nextDouble(), (double)pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypesNF.DRIPPING_WATER.get();
    }

    @Override
    public boolean isSame(Fluid pFluid) {
        //return super.isSame(pFluid);
        return pFluid instanceof WaterFluidNF;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
        return direction == Direction.DOWN && !super.isSame(fluidIn);
    }

    @Override
    protected boolean isSourceBlockOfThisType(FluidState state) {
        return state.isSource() && super.isSame(state.getType());
    }

    @Override
    protected boolean isWaterHole(BlockGetter p_75957_, Fluid p_75958_, BlockPos p_75959_, BlockState p_75960_, BlockPos p_75961_, BlockState p_75962_) {
        if (!this.canPassThroughWall(Direction.DOWN, p_75957_, p_75959_, p_75960_, p_75961_, p_75962_)) {
            return false;
        } else {
            return super.isSame(p_75962_.getFluidState().getType()) || this.canHoldFluid(p_75957_, p_75961_, p_75962_, p_75958_);
        }
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        if(!state.isSource()) {
            FluidState fluidstate = this.getNewLiquid(level, pos, level.getBlockState(pos));
            int i = this.getSpreadDelay(level, pos, state, fluidstate);
            BlockState blockState = level.getBlockState(pos);
            if(fluidstate.isEmpty()) {
                state = fluidstate;
                level.setBlock(pos, blockState.getBlock() instanceof IWaterloggedBlock ? blockState.setValue(BlockStatePropertiesNF.WATER_LEVEL,
                        0).setValue(BlockStatePropertiesNF.WATERLOG_TYPE, IWaterloggedBlock.WaterlogType.fromFluid(fluidstate.getType()))
                        : Blocks.AIR.defaultBlockState(), 3);
            }
            else if(!fluidstate.equals(state)) {
                state = fluidstate;
                BlockState newBlock = blockState.getBlock() instanceof IWaterloggedBlock ? blockState.setValue(BlockStatePropertiesNF.WATER_LEVEL,
                        fluidstate.getAmount()).setValue(BlockStatePropertiesNF.WATERLOG_TYPE, IWaterloggedBlock.WaterlogType.fromFluid(fluidstate.getType()))
                        : fluidstate.createLegacyBlock();
                level.setBlock(pos, newBlock, 2);
                level.scheduleTick(pos, fluidstate.getType(), i);
                level.updateNeighborsAt(pos, newBlock.getBlock());
            }
        }
        this.spread(level, pos, state);
    }

    /**
     * Improve spreading behavior between water fluids, so they mix together more evenly
     */
    @Override
    protected FluidState getNewLiquid(LevelReader level, BlockPos pos, BlockState state) {
        FluidState currentState = state.getFluidState();
        int maxLevel = currentState.getAmount();
        WaterFluidNF maxFluid = this;
        HashMap<WaterFluidNF, Integer> sourceCounts = new HashMap<>();
        int totalSourceCount = 0;

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborBlock = level.getBlockState(neighborPos);
            FluidState neighborFluid = neighborBlock.getFluidState();
            if(neighborFluid.getType() instanceof WaterFluidNF neighborType && this.canPassThroughWall(direction, level, pos, state, neighborPos, neighborBlock)) {
                if(neighborFluid.isSource() && ForgeEventFactory.canCreateFluidSource(level, neighborPos, neighborBlock, this.canConvertToSource())) {
                    sourceCounts.put(neighborType, sourceCounts.getOrDefault(neighborType, 0) + 1);
                    totalSourceCount++;
                }
                int neighborAmount = neighborFluid.getAmount();
                if(neighborAmount > maxLevel) {
                    maxLevel = neighborAmount;
                    maxFluid = neighborType;
                }
                //Prefer own type
                else if(neighborAmount == maxLevel && super.isSame(neighborType)) maxFluid = this;
            }
        }

        if(totalSourceCount >= 2) {
            BlockState belowBlock = level.getBlockState(pos.below());
            FluidState belowFluid = belowBlock.getFluidState();
            //Consistent behavior for case with 2 types and equal source amounts
            if(belowFluid.isSource() && belowFluid.getType() instanceof WaterFluidNF belowType && sourceCounts.getOrDefault(belowType, 0) >= 2) {
                return belowType.getSource(false);
            }
            else if(belowBlock.getMaterial().isSolid()) {
                WaterFluidNF maxSourceFluid = this;
                int maxSourceCount = 0;
                for(Map.Entry<WaterFluidNF, Integer> entry : sourceCounts.entrySet()) {
                    if(entry.getValue() > maxSourceCount) {
                        maxSourceCount = entry.getValue();
                        maxSourceFluid = entry.getKey();
                    }
                    //Prefer own type
                    else if(entry.getValue() == maxSourceCount) maxSourceFluid = this;
                }
                return maxSourceFluid.getSource(false);
            }
        }

        BlockPos abovePos = pos.above();
        BlockState aboveBlock = level.getBlockState(abovePos);
        FluidState aboveFluid = aboveBlock.getFluidState();
        if(!aboveFluid.isEmpty() && aboveFluid.getType() instanceof WaterFluidNF aboveType && this.canPassThroughWall(Direction.UP, level, pos, state, abovePos, aboveBlock)) {
            if(currentState.isSource()) return currentState;
            return aboveType.getFlowing(aboveType.getSource().getAmount(aboveFluid), true);
        }
        else {
            int fluidLevel = maxLevel - maxFluid.getDropOff(level);
            return fluidLevel <= 0 ? Fluids.EMPTY.defaultFluidState() : maxFluid.getFlowing(fluidLevel, false);
        }
    }

    public static class Flowing extends WaterFluidNF {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 6));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends WaterFluidNF {
        public Source(Properties properties) {
            super(properties);
        }

        public int getAmount(FluidState state) {
            return 6;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
