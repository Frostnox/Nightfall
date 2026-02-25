package frostnox.nightfall.block.block.anvil;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.IWaterloggedBlock;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class MetalAnvilBlock extends TieredAnvilBlock implements ICustomPathfindable, IWaterloggedBlock {
    private static final VoxelShape BASE = Block.box(2.0D, 0.0D, 3.0D, 14.0D, 3.0D, 13.0D);
    private static final VoxelShape LEG1 = Block.box(3.0D, 3.0D, 4.0D, 13.0D, 5.0D, 12.0D);
    private static final VoxelShape LEG2 = Block.box(5.0D, 5.0D, 6.0D, 11.0D, 10.0D, 10.0D);
    private static final VoxelShape HORN = Block.box(0.0D, 10.0D, 5.0D, 4.0D, 15.0D, 11.0D);
    private static final VoxelShape FLAT = Block.box(4.0D, 10.0D, 3.0D, 12.0D, 16.0D, 13.0D);
    private static final VoxelShape EDGE = Block.box(12.0D, 13.0D, 3.0D, 16.0D, 16.0D, 13.0D);
    private static final VoxelShape SOUTH_SHAPE = MathUtil.rotate(Shapes.or(BASE, LEG1, LEG2, HORN, FLAT, EDGE), Rotation.CLOCKWISE_90);
    private static final VoxelShape NORTH_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_180);
    private static final VoxelShape EAST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    private static final VoxelShape WEST_SHAPE = MathUtil.rotate(SOUTH_SHAPE, Rotation.CLOCKWISE_90);
    private static final List<AABB> NORTH_FACE_Y = NORTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> SOUTH_FACE_Y = SOUTH_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> WEST_FACE_Y = WEST_SHAPE.getFaceShape(Direction.UP).toAabbs();
    private static final List<AABB> EAST_FACE_Y = EAST_SHAPE.getFaceShape(Direction.UP).toAabbs();

    public MetalAnvilBlock(int tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> WEST_SHAPE;
        };
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState contactState, MovingBlockEntity entity) {
        if(!entity.isSilent()) {
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.4F, level.random.nextFloat() * 0.1F + 0.8F);
        }
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> NORTH_FACE_Y;
            case SOUTH -> SOUTH_FACE_Y;
            case WEST -> WEST_FACE_Y;
            default -> EAST_FACE_Y;
        };
    }
}