package frostnox.nightfall.block.block.pot;

import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.block.WaterloggedEntityBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PotBlock extends WaterloggedEntityBlock implements ICustomPathfindable {
    protected static final VoxelShape COLLISION_SHAPE = Block.box(3, 0, 3, 13, 13, 13);
    protected static final VoxelShape SHAPE = Shapes.or(COLLISION_SHAPE,
            Block.box(6, 13, 6, 10, 14, 10),
            Block.box(5, 14, 5, 11, 16, 11));
    protected static final List<AABB> AABB = COLLISION_SHAPE.toAabbs();

    public PotBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.POT.get().create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        else {
            BlockPos abovePos = pos.above();
            if(!level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN, SupportType.CENTER) &&
                    level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
                NetworkHooks.openGui((ServerPlayer) pPlayer, pot, pos);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return COLLISION_SHAPE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!state.is(pNewState.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pStack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof BaseContainerBlockEntity container) {
            container.setCustomName(pStack.getHoverName());
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALL)) {
            //Shatter now before this block is gone
            if(entity instanceof MovingBlockEntity movingBlock && movingBlock.getBlockState().is(TagsNF.SHATTER_ON_FALL)) {
                movingBlock.tryPlacement();
            }
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        if(!level.isClientSide && pProjectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && pProjectile.getDeltaMovement().length() >= 1.4D) {
            level.destroyBlock(pHit.getBlockPos(), true);
        }
    }

    @Override
    public int getExcludedWaterLevel(BlockState state) {
        return 0;
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
        return getTypeForCenteredBottomShape(nodeManager, pos, 12F/16F);
    }

    @Override
    public NodeType getFloorNodeType(NodeManager nodeManager, BlockState state, BlockGetter level, BlockPos pos) {
        return NodeType.OPEN;
    }

    @Override
    public List<AABB> getTopFaceShape(BlockState state) {
        return AABB;
    }

    @Override
    public List<AABB> getBottomFaceShape(BlockState state) {
        return AABB;
    }
}
