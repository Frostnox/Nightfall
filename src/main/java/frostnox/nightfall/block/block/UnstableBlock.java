package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.LevelData;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.entity.MovingBlockToClient;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Like falling block but can also slide off ledges
 */
public class UnstableBlock extends BlockNF implements IFallable {
    public final Supplier<SoundEvent> slideSound;

    public UnstableBlock(Supplier<SoundEvent> slideSound, Properties properties) {
        super(properties);
        this.slideSound = slideSound;
    }

    public boolean trySlideOnce(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        //Try sliding in a random direction once
        if(!level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above()) && !LevelUtil.canFallThrough(level.getBlockState(pos.below()))) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos slidePos = pos.relative(dir);
            if(LevelUtil.canFallThrough(level.getBlockState(slidePos)) && LevelUtil.canFallThrough(level.getBlockState(slidePos.below()))) {
                onFall(state, level, pos, null);
                MovingBlockEntity.slide(level, pos, dir, state);
                return true;
            }
        }
        return false;
    }

    public boolean trySlide(Level level, BlockPos movingPos, MovingBlockEntity movingBlock) {
        Direction oppDir = movingBlock.slideDir.getOpposite();
        if(oppDir.getAxis() == Direction.Axis.Y) oppDir = null;
        List<Direction> directions = Lists.newArrayList(Direction.Plane.HORIZONTAL.iterator());
        directions.remove(oppDir);
        //Try sliding in any direction
        while(!directions.isEmpty() || oppDir != null) {
            Direction dir;
            //Try opposite direction last
            if(directions.isEmpty()) {
                dir = oppDir;
                oppDir = null;
            }
            else dir = directions.remove((level.random.nextInt() & Integer.MAX_VALUE) % directions.size());
            BlockPos slidePos = movingPos.relative(dir);
            if(LevelUtil.canFallThrough(level.getBlockState(slidePos)) && LevelUtil.canFallThrough(level.getBlockState(slidePos.below()))) {
                movingBlock.slideDir = dir;
                movingBlock.setSlideTime(0);
                movingBlock.setDeltaMovement(0, 0, 0);
                movingBlock.setPos(movingPos.getX() + 0.5, movingPos.getY() + 0.005D, movingPos.getZ() + 0.5);
                if(!level.isClientSide()) {
                    NetworkHandler.toAllTracking(movingBlock, new MovingBlockToClient(0, dir, true, movingBlock.getId()));
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState contactState, MovingBlockEntity entity) {
        trySlide(level, pos, entity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if(level instanceof ServerLevel serverLevel) {
            if(!trySlideOnce(state, serverLevel, pos, level.random)) level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public boolean canLand(Level level, BlockPos movingPos, BlockState movingState, BlockState contactState, MovingBlockEntity movingBlock) {
        return trySlide(level, movingPos, movingBlock);
    }

    @Override
    public SoundEvent getFallSound(BlockState state) {
        return slideSound.get();
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        if(!level.isClientSide() && LevelData.isPresent(level)) ChunkData.get(level.getChunkAt(pos)).schedulePhysicsTick(pos);
    }

    /*@Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random pRand) {
        if(pRand.nextInt(16) == 0) {
            BlockPos blockpos = pos.below();
            if(isFree(level.getBlockState(blockpos))) {
                double d0 = (double)pos.getX() + pRand.nextDouble();
                double d1 = (double)pos.getY() - 0.05D;
                double d2 = (double)pos.getZ() + pRand.nextDouble();
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }*/
}
