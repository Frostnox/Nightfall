package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class TreeBranchesBlock extends Block implements ICustomPathfindable {
    public static final BooleanProperty ALTERNATE = BlockStatePropertiesNF.ALTERNATE;
    public static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 1.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    public final ITree type;

    public TreeBranchesBlock(ITree type, Properties properties) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(ALTERNATE, false));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!TreeTrunkBlockEntity.updating && !pNewState.is(this)) {
            TreeGenerator gen = type.getGenerator();
            if(gen.maxLeavesRadius <= 1) return; //Leaves connected only to stems
            int minX = pos.getX() - gen.maxLeavesDistXZ, maxX = pos.getX() + gen.maxLeavesDistXZ;
            int minZ = pos.getZ() - gen.maxLeavesDistXZ, maxZ = pos.getZ() + gen.maxLeavesDistXZ;
            List<TreeTrunkBlockEntity> nearbyTrunks = TreeTrunkBlockEntity.getNearbyTrunks(level, type, pos, minX, maxX, minZ, maxZ);
            for(TreeTrunkBlockEntity nearbyTrunk : nearbyTrunks) {
                var simulatedData = gen.getTree((WorldGenLevel) level, nearbyTrunk, true);
                if(simulatedData.branchLeaves.contains(pos) || simulatedData.trunkLeaves.contains(pos)) {
                    nearbyTrunk.updateBlocks(pos, simulatedData, true);
                }
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALL)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
            if(entityContext.getEntity() instanceof LivingEntity || entityContext.getEntity() instanceof Boat) {
                if(entityContext.getEntity() instanceof ActionableEntity actionable && actionable.getPushResistance() >= ActionableEntity.PUSH_HIGH) return Shapes.empty();
                else return COLLISION_SHAPE;
            }
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(TagsNF.BRANCHES_OR_LEAVES);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ALTERNATE);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ALTERNATE, context.getPlayer() != null && context.getPlayer().isShiftKeyDown());
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return switch(pType) {
            case LAND, AIR -> true;
            case WATER -> false;
        };
    }

    @Override
    public NodeType getRawNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return getTypeForBottomClosedShape(nodeManager, level, pos, 0.75F);
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
