package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.TieredHeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.registries.RegistryObject;

public class FireableFacingPartialBlock extends FireablePartialBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FireableFacingPartialBlock(int cookTicks, TieredHeat cookHeat, RegistryObject<? extends Block> firedBlock, int excludedWaterLevel, Properties properties) {
        super(cookTicks, cookHeat, firedBlock, excludedWaterLevel, properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getFiredBlock(Level level, BlockPos pos, BlockState state) {
        return firedBlock.get().defaultBlockState().setValue(FACING, state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state != null) return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        else return state;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRotation) {
        return state.setValue(FACING, pRotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror pMirror) {
        return state.rotate(pMirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }
}
