package frostnox.nightfall.block.block.fuel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BurningHorizontalFuelBlock extends BurningFuelBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public BurningHorizontalFuelBlock(int burnTicks, float burnTemp, @Nullable BiFunction<Level, BlockPos, Block> cookChecker, Supplier<? extends Block> baseFuel, Properties properties) {
        super(burnTicks, burnTemp, cookChecker, baseFuel, properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z));
    }

    public BurningHorizontalFuelBlock(int burnTicks, float burnTemp, Supplier<? extends Block> baseFuel, Properties properties) {
        this(burnTicks, burnTemp, null, baseFuel, properties);
    }

    @Override
    protected BlockState createBurningState(BlockState originalState) {
        return super.createBurningState(originalState).setValue(AXIS, originalState.getValue(AXIS));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return defaultBlockState().setValue(AXIS, pContext.getHorizontalDirection().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRot) {
        return switch(pRot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(AXIS)) {
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(AXIS);
    }
}
