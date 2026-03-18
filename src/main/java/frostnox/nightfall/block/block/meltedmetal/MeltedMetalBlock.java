package frostnox.nightfall.block.block.meltedmetal;

import frostnox.nightfall.block.*;
import frostnox.nightfall.block.fluid.MeltedMetalFluid;
import frostnox.nightfall.entity.ai.pathfinding.NodeType;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MeltedMetalBlock extends BaseEntityBlock implements IAdjustableNodeType, IHeatable, IBlockChunkLoader {
    public static final IntegerProperty HEAT = BlockStatePropertiesNF.HEAT;
    public static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public MeltedMetalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HEAT, 2));
    }

    public TieredHeat getHeat(BlockState state) {
        return TieredHeat.values()[state.getValue(HEAT)];
    }

    @Override
    public void applyHeat(Level level, BlockPos pos, BlockState state, TieredHeat heat, Direction fromDir) {
        if(fromDir == Direction.DOWN && level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity metal) {
            metal.targetTemperature = heat.getUpperTemp();
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.lavaHurt();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext entityContext && entityContext.getEntity() != null) return COLLISION_SHAPE;
        else return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        double x = (double)pos.getX() + 0.5D;
        double y = pos.getY();
        double z = (double)pos.getZ() + 0.5D;
        if(rand.nextInt(380) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundsNF.MOLTEN_LIQUID_AMBIENT.get(), SoundSource.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
        }
        if(rand.nextDouble() < 0.55D) {
            BlockPos blockpos = pos.above();
            if(level.isRainingAt(blockpos)) {
                level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.175F, 2.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F, false);
            }
        }
    }

    protected boolean tryDestabilize(BlockState state, ServerLevel level, BlockPos pos) {
        boolean unstable = false;
        for(Direction dir : Direction.Plane.HORIZONTAL) {
            if(!level.getBlockState(pos.relative(dir)).getMaterial().blocksMotion()) {
                unstable = true;
                break;
            }
        }
        if(unstable) unstable = LevelUtil.getNearbyFurnaceTier(level, pos) == 0;
        if(unstable) {
            MeltedMetalFluid fluid = FluidsNF.MELTED_METAL.get(getHeat(state)).get();
            level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock().setValue(LiquidBlock.LEVEL, 3), 11);
            fluid.tick(level, pos, fluid.defaultFluidState());
        }
        return unstable;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @javax.annotation.Nullable BlockEntity pTe, ItemStack pStack) {
        MeltedMetalFluid fluid = FluidsNF.MELTED_METAL.get(getHeat(state)).get();
        level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock().setValue(LiquidBlock.LEVEL, 3), 11);
        fluid.tick(level, pos, fluid.defaultFluidState());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        tryDestabilize(state, level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        level.scheduleTick(pos, state.getBlock(), 40 + level.random.nextInt(16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HEAT);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(state, level, pos, pBlock, pFromPos, pIsMoving);
        if(!level.isClientSide) {
            BlockState neighbor = level.getBlockState(pFromPos);
            if(neighbor.getFluidState().is(FluidTags.WATER)) {
                level.setBlockAndUpdate(pos, BlocksNF.SLAG.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.375F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.25D, (double)pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.0D);
            }
            else if(pFromPos.getY() == pos.getY() && !neighbor.getMaterial().blocksMotion() && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 40 + level.random.nextInt(16));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntitiesNF.MELTED_METAL.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        return level.isClientSide() ? null : createTickerHelper(blockEntity, BlockEntitiesNF.MELTED_METAL.get(), MeltedMetalBlockEntity::serverTick);
    }

    @Override
    public NodeType adjustNodeType(NodeType type, BlockState state, LivingEntity entity) {
        return entity.fireImmune() ? type : NodeType.IMPASSABLE_DANGER_MAJOR;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if(level.getBlockEntity(pos) instanceof MeltedMetalBlockEntity metal) return new ItemStack(metal.originalState.getBlock());
        else return ItemStack.EMPTY;
    }

    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        Level level = (Level) levelReader;
        if(!level.isClientSide && !oldState.is(this)) LevelUtil.forceTickingChunk(level, pos, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(state, level, pos, pNewState, pIsMoving);
        if(!pNewState.is(this)) LevelUtil.forceTickingChunk(level, pos, false);
    }

    @Override
    public boolean keepForceChunk(BlockState state) {
        return true;
    }
}
