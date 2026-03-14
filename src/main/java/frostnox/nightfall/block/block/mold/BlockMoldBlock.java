package frostnox.nightfall.block.block.mold;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockMoldBlock extends ItemMoldBlock implements ICustomPathfindable {
    protected final List<AABB> southFaceUp, northFaceUp, eastFaceUp, westFaceUp, southFaceDown, northFaceDown, eastFaceDown, westFaceDown;

    public BlockMoldBlock(VoxelShape shape, TagKey<Item> matchingItemTag, int maxUnits, Properties properties) {
        super(shape, matchingItemTag, maxUnits, properties);
        this.southFaceUp = southShape.getFaceShape(Direction.UP).toAabbs();
        this.northFaceUp = northShape.getFaceShape(Direction.UP).toAabbs();
        this.eastFaceUp = eastShape.getFaceShape(Direction.UP).toAabbs();
        this.westFaceUp = westShape.getFaceShape(Direction.UP).toAabbs();
        this.southFaceDown = southShape.getFaceShape(Direction.DOWN).toAabbs();
        this.northFaceDown = northShape.getFaceShape(Direction.DOWN).toAabbs();
        this.eastFaceDown = eastShape.getFaceShape(Direction.DOWN).toAabbs();
        this.westFaceDown = westShape.getFaceShape(Direction.DOWN).toAabbs();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.BLOCK_MOLD.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entity) {
        if(!state.getValue(TICKING)) return null;
        else if(level.isClientSide()) return createTickerHelper(entity, BlockEntitiesNF.BLOCK_MOLD.get(), ItemMoldBlockEntity::clientTick);
        else return createTickerHelper(entity, BlockEntitiesNF.BLOCK_MOLD.get(), ItemMoldBlockEntity::serverTick);
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        nodeManager.getNode(pos).partial = true;
        return NodeType.CLOSED;
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.CLOSED;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> northFaceUp;
            case SOUTH -> southFaceUp;
            case WEST -> westFaceUp;
            default -> eastFaceUp;
        };
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return switch(state.getValue(FACING)) {
            case NORTH -> northFaceDown;
            case SOUTH -> southFaceDown;
            case WEST -> westFaceDown;
            default -> eastFaceDown;
        };
    }
}
