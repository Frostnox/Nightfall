package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.IMetal;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.registries.RegistryObject;

public class FireableMetalAxisBlock extends FireableMetalBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public FireableMetalAxisBlock(IMetal metalType, Properties properties) {
        this(metalType, 400, false, null, properties);
    }

    public FireableMetalAxisBlock(IMetal metalType, int metalUnits, boolean hasSlag, RegistryObject<? extends Block> firedBlock, Properties properties) {
        super(metalType, metalUnits, hasSlag, firedBlock, properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state != null) return state.setValue(AXIS, context.getHorizontalDirection().getAxis());
        else return state;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation pRot) {
        return switch(pRot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch(state.getValue(AXIS)) {
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
