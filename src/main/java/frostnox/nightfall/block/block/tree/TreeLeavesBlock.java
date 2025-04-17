package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.ICustomPathfindable;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.entity.ai.pathfinding.NodeManager;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.world.Season;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.common.IForgeShearable;

import java.util.List;
import java.util.Random;

public class TreeLeavesBlock extends Block implements IForgeShearable, ICustomPathfindable {
    public static final BooleanProperty ALTERNATE = BlockStatePropertiesNF.ALTERNATE;
    public static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 1.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    public final ITree type;
    protected final IBlockRenderProperties renderProperties = new IBlockRenderProperties() {
        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            boolean hasParticle = type.getParticle() != null;
            if(hasParticle && level.getGameTime() % 10L == 0) {
                Vec3 pos = target.getLocation();
                level.addParticle(new BlockParticleOption(type.getParticle().get(), state), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            return hasParticle;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
            boolean hasParticle = type.getParticle() != null;
            if(hasParticle) {
                for(int i = 0; i < 3 + Math.abs(level.random.nextInt() % 4); i++) {
                    double x = pos.getX() + level.random.nextDouble();
                    double y = pos.getY() + level.random.nextDouble();
                    double z = pos.getZ() + level.random.nextDouble();
                    level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
                }
            }
            return hasParticle;
        }
    };

    public TreeLeavesBlock(ITree type, Properties properties) {
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
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) {
            return entityContext.getEntity() instanceof LivingEntity ? COLLISION_SHAPE : Shapes.empty(); //TODO: Let heavy entities sink
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if(level.isRainingAt(pos.above())) {
            if(random.nextInt() % 15 == 0) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);
                if(!blockstate.canOcclude() || !blockstate.isFaceSturdy(level, blockpos, Direction.UP)) {
                    double d0 = (double)pos.getX() + random.nextDouble();
                    double d1 = (double)pos.getY() - 0.05D;
                    double d2 = (double)pos.getZ() + random.nextDouble();
                    level.addParticle(ParticleTypesNF.DRIPPING_WATER.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        if(type.getParticle() != null) {
            int tickMod;
            switch(Season.get(level)) {
                case SPRING: tickMod = 2400; break;
                case SUMMER: tickMod = 1600; break;
                case FALL: tickMod = 550; break;
                default: return;
            }
            if(random.nextInt() % tickMod == 0) {
                double x = (double) pos.getX() + random.nextDouble();
                double y = pos.getY();
                double z = (double) pos.getZ() + random.nextDouble();
                level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(Math.max(0, fallDistance - 1.5F), 1.0F, DamageSource.FALL)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if(!level.isClientSide() || type.getParticle() == null) return;
        Vec3 velocity = entity.getDeltaMovement();
        if(level.getGameTime() % 12L == 0L && !entity.isCrouching() && velocity.lengthSqr() > 0) {
            double x = pos.getX() + level.random.nextDouble();
            double y = pos.getY() + level.random.nextDouble();
            double z = pos.getZ() + level.random.nextDouble();
            level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(BlockTags.LEAVES);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ALTERNATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ALTERNATE, context.getPlayer() != null && context.getPlayer().isShiftKeyDown());
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return renderProperties;
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
