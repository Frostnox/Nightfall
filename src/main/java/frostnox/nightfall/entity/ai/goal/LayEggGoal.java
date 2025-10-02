package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.block.block.eggnest.EggNestBlock;
import frostnox.nightfall.block.block.eggnest.EggNestBlockEntity;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.animal.DrakefowlEntity;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class LayEggGoal extends Goal {
    protected final DrakefowlEntity mob;
    protected final Supplier<? extends Block> nestBlock;
    protected final double speedModifier;
    protected @Nullable ReversePath path;
    protected @Nullable Vec3 pos;
    protected @Nullable BlockPos blockPos;
    protected int lookX, lookZ;

    public LayEggGoal(DrakefowlEntity mob, Supplier<? extends Block> nestBlock, double speedModifier) {
        this.mob = mob;
        this.nestBlock = nestBlock;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    protected boolean isNestSpotValid(BlockPos pos) {
        return !LevelUtil.isSkyUnobstructed(mob.level, pos);
    }

    @Override
    public boolean canUse() {
        pos = null;
        blockPos = null;
        List<BlockEntity> nests = LevelUtil.getBlockEntities(mob.level, mob.blockPosition(), 16, (entity) -> entity instanceof EggNestBlockEntity);
        nests.sort(Comparator.comparingDouble(entity -> {
            BlockPos pos = entity.getBlockPos();
            return mob.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        }));
        for(BlockEntity entity : nests) {
            EggNestBlockEntity nest = (EggNestBlockEntity) entity;
            if(!nest.occupied) {
                BlockPos nestPos = nest.getBlockPos();
                if(!LevelUtil.isSkyUnobstructed(mob.level, nestPos)) {
                    if(mob.distanceToSqr(nestPos.getX() + 0.5, nestPos.getY(), nestPos.getZ() + 0.5) < 0.5 * 0.5) {
                        pos = new Vec3(nestPos.getX() + 0.5, nestPos.getY(), nestPos.getZ() + 0.5);
                        blockPos = new BlockPos(pos);
                        break;
                    }
                    else {
                        path = mob.getNavigator().findPath(nestPos, 0);
                        if(path != null && path.reachesGoal()) {
                            pos = new Vec3(nestPos.getX() + 0.5, nestPos.getY(), nestPos.getZ() + 0.5);
                            blockPos = new BlockPos(pos);
                            nest.occupied = true;
                            break;
                        }
                    }
                }
            }
        }
        if(pos != null) return true;
        else if(mob.getGestationTime() > 0) {
            BlockPos randPos = mob.blockPosition().offset(mob.getRandom().nextInt(25) - 12, 0, mob.getRandom().nextInt(25) - 12);
            LevelChunk chunk = mob.level.getChunkAt(randPos);
            if(chunk.getBlockState(randPos).isAir()) {
                BlockPos.MutableBlockPos belowPos = randPos.mutable();
                for(int i = 0; i < 8; i++) {
                    if(!chunk.getBlockState(belowPos.setY(belowPos.getY() - 1)).isAir()) {
                        blockPos = belowPos.above();
                        pos = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                        break;
                    }
                }
            }
            else {
                BlockPos.MutableBlockPos abovePos = randPos.mutable();
                for(int i = 0; i < 8; i++) {
                    if(chunk.getBlockState(abovePos.setY(abovePos.getY() + 1)).isAir()) {
                        blockPos = abovePos.immutable();
                        pos = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                        break;
                    }
                }
            }
            if(pos == null) return false;
            blockPos = new BlockPos(pos);
            if(!isNestSpotValid(blockPos)) return false;
            path = mob.getNavigator().findPath(blockPos, 0);
            return path != null && path.reachesGoal();
        }
        else return false;
    }

    @Override
    public boolean canContinueToUse() {
         return !mob.getNavigator().isDone() || mob.level.getBlockEntity(blockPos) instanceof EggNestBlockEntity || isNestSpotValid(blockPos);
    }

    @Override
    public void start() {
        mob.getNavigator().moveTo(path, speedModifier);
        path = null;
    }

    @Override
    public void stop() {
        lookX = 0;
        lookZ = 0;
        if(mob.level.getBlockEntity(blockPos) instanceof EggNestBlockEntity nest) nest.occupied = false;
    }

    @Override
    public void tick() {
        if(mob.refreshPath || mob.randTickCount % 20 < 2) {
            mob.getNavigator().moveTo(pos.x, pos.y, pos.z, speedModifier, 0);
        }
        if(mob.distanceToSqr(pos) < 0.25 * 0.25) {
            if(mob.randTickCount % 100 < 2) {
                BlockPos.MutableBlockPos adjPos = blockPos.mutable();
                lookX = 0;
                lookZ = 0;
                for(Direction dir : LevelUtil.HORIZONTAL_DIRECTIONS) {
                    if(!mob.level.getBlockState(adjPos.setWithOffset(blockPos, dir)).isAir()) {
                        lookX -= dir.getStepX();
                        lookZ -= dir.getStepZ();
                    }
                }
            }
            mob.getLookControl().setLookAt(mob.getX() + lookX, mob.getEyeY(), mob.getZ() + lookZ);
            if(mob.getGestationTime() > 0 && mob.isComfortable() && mob.getRandom().nextInt(20 * 80) == 0) {
                if(mob.level.getBlockEntity(blockPos) instanceof EggNestBlockEntity nest) {
                    for(int i = 0; i < nest.hatchTimes.length; i++) {
                        if(nest.hatchTimes[i] == 0) {
                            mob.level.setBlockAndUpdate(blockPos, nest.getBlockState().setValue(EggNestBlock.EGGS, nest.getBlockState().getValue(EggNestBlock.EGGS) + 1));
                            nest.hatchTimes[i] = mob.fatherType == null ? -1 : ((EggNestBlock) nest.getBlockState().getBlock()).hatchDuration;
                            if(mob.fatherType != null) nest.eggData[0] = mob.getDrakefowlType() == mob.fatherType ? mob.fatherType.ordinal()
                                    : (mob.getRandom().nextBoolean() ? mob.getDrakefowlType().ordinal() : mob.fatherType.ordinal());
                            break;
                        }
                    }
                }
                else if(isNestSpotValid(blockPos)) {
                    mob.level.setBlockAndUpdate(blockPos, nestBlock.get().defaultBlockState().setValue(EggNestBlock.EGGS, 1));
                    if(mob.level.getBlockEntity(blockPos) instanceof EggNestBlockEntity nest) {
                        nest.hatchTimes[0] = mob.fatherType == null ? -1 : ((EggNestBlock) nest.getBlockState().getBlock()).hatchDuration;
                        if(mob.fatherType != null) nest.eggData[0] = mob.getDrakefowlType() == mob.fatherType ? mob.fatherType.ordinal()
                                : (mob.getRandom().nextBoolean() ? mob.getDrakefowlType().ordinal() : mob.fatherType.ordinal());
                        nest.occupied = true;
                    }
                }
                mob.endGestation();
            }
        }
    }
}