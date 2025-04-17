package frostnox.nightfall.block.block;

import com.google.common.collect.Lists;
import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VinesBlockNF extends VineBlock implements IWaterloggedBlock {
    public static final IntegerProperty WATER_LEVEL = BlockStatePropertiesNF.WATER_LEVEL;
    public static final EnumProperty<WaterlogType> WATERLOG_TYPE = BlockStatePropertiesNF.WATERLOG_TYPE;

    public VinesBlockNF(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        tickLiquid(state, currentPos, level);
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return getLiquid(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return addLiquidToPlacement(super.getStateForPlacement(context), context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATER_LEVEL, WATERLOG_TYPE);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(level.random.nextFloat() < 0.01F && state.getValue(WATER_LEVEL) == 0 && level.isAreaLoaded(pos, 2)) {
            Direction dir = Direction.getRandom(random);
            BlockPos dirPos = pos.relative(dir);
            BlockState dirState = level.getBlockState(dirPos);
            //Try to grow inside own block first
            if(dir != Direction.DOWN && !state.getValue(getPropertyForFace(dir)) && isAcceptableNeighbour(level, dirPos, dir)) {
                level.setBlock(pos, state.setValue(getPropertyForFace(dir), true), 2);
                return;
            }
            if(dir == Direction.UP) {
                dir = Direction.DOWN;
                dirPos = pos.below();
                dirState = level.getBlockState(dirPos);
            }
            //Pick a random direction from this vine
            Direction facingDir = null;
            List<Direction> directions = dir == Direction.DOWN ? Lists.newArrayList(Direction.Plane.HORIZONTAL) : Lists.newArrayList(dir.getClockWise(), dir.getCounterClockWise());
            Collections.shuffle(directions, random);
            for(Direction randDir : directions) {
                if(state.getValue(getPropertyForFace(randDir))) {
                    facingDir = randDir;
                    break;
                }
            }
            if(facingDir == null) return;

            BlockState newState = null;
            if(dirState.isAir()) newState = defaultBlockState();
            else if(dirState.is(this)) newState = dirState;

            if(newState != null && !newState.getValue(getPropertyForFace(facingDir))) {
                if(dir == Direction.DOWN) {
                    BlockPos.MutableBlockPos abovePos = pos.mutable();
                    for(int i = 0; true; i++) {
                        abovePos.setY(abovePos.getY() + 1);
                        if(!level.getBlockState(abovePos).is(this)) break;
                        else if(i == 3) return;
                    }
                }
                else if(!isAcceptableNeighbour(level, dirPos.relative(facingDir), facingDir)) return;
                level.setBlock(dirPos, newState.setValue(getPropertyForFace(facingDir), true), 2);
            }
        }
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return !entity.isOnGround();
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }
}
