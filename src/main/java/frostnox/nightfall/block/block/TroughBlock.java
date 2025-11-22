package frostnox.nightfall.block.block;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.entity.Diet;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class TroughBlock extends WaterloggedBlock implements IFoodBlock, ICustomPathfindable {
    public enum FoodType implements StringRepresentable {
        HAY, MEAT, MIX;

        private final String name;

        FoodType() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty AMOUNT = IntegerProperty.create("amount", 0, 4);
    public static final EnumProperty<FoodType> FOOD_TYPE = EnumProperty.create("food_type", FoodType.class);
    public static final VoxelShape SHAPE_Z = box(0, 0, 2, 16, 10, 14);
    public static final VoxelShape SHAPE_X = MathUtil.rotate(SHAPE_Z, Rotation.CLOCKWISE_90);

    public TroughBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z).setValue(AMOUNT, 0).setValue(FOOD_TYPE, FoodType.HAY));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!player.isShiftKeyDown()) {
            int amount = state.getValue(AMOUNT);
            if(amount < 4) {
                ItemStack item = player.getItemInHand(hand);
                if(item.is(TagsNF.OMNIVORE_FOOD)) {
                    if(!player.getAbilities().instabuild) item.shrink(1);
                    if(item.is(TagsNF.HERBIVORE_FOOD)) {
                        if(amount == 0 || state.getValue(FOOD_TYPE) == FoodType.HAY) level.setBlockAndUpdate(pos, state.setValue(FOOD_TYPE, FoodType.HAY).setValue(AMOUNT, amount + 1));
                        else level.setBlockAndUpdate(pos, state.setValue(FOOD_TYPE, FoodType.MIX).setValue(AMOUNT, amount + 1));
                    }
                    else if(item.is(TagsNF.CARNIVORE_FOOD)) {
                        if(amount == 0 || state.getValue(FOOD_TYPE) == FoodType.MEAT) level.setBlockAndUpdate(pos, state.setValue(FOOD_TYPE, FoodType.MEAT).setValue(AMOUNT, amount + 1));
                        else level.setBlockAndUpdate(pos, state.setValue(FOOD_TYPE, FoodType.MIX).setValue(AMOUNT, amount + 1));
                    }
                    else level.setBlockAndUpdate(pos, state.setValue(FOOD_TYPE, FoodType.MIX).setValue(AMOUNT, amount + 1));
                    level.playSound(null, pos, SoundsNF.TROUGH_FILL.get(), SoundSource.BLOCKS, 1, 0.95F + level.random.nextFloat() * 0.1F);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return state.getValue(AXIS) == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext).setValue(AXIS, pContext.getHorizontalDirection().getAxis());
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
        pBuilder.add(AXIS, AMOUNT, FOOD_TYPE);
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
    }

    @Override
    public boolean isEatable(BlockState state, Diet diet) {
        if(state.getValue(AMOUNT) == 0) return false;
        else return switch(diet) {
            case HERBIVORE -> state.getValue(FOOD_TYPE) == FoodType.HAY;
            case CARNIVORE -> state.getValue(FOOD_TYPE) == FoodType.MEAT;
            case OMNIVORE, OMNIVORE_SEEDS -> true;
        };
    }

    @Override
    public void eat(Entity eater, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.setBlockAndUpdate(pos, state.setValue(AMOUNT, state.getValue(AMOUNT) - 1));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> level.getFluidState(pos).is(FluidTags.WATER);
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return getTypeForCenteredBottomShape(nodeManager, pos, 10F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return NO_BOXES;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return FULL_BOXES;
    }
}
