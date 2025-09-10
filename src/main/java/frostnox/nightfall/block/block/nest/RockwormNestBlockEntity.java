package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.registry.ActionsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class RockwormNestBlockEntity extends GuardedNestBlockEntity {
    private static final Direction[] EXIT_UP = new Direction[] {Direction.UP};
    protected long lastDigTime = 0;

    public RockwormNestBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState, int capacity, int respawnTime, BiFunction<ServerLevel, BlockPos, ActionableEntity> respawnFunc, int listenRange) {
        super(pType, pos, pBlockState, capacity, respawnTime, respawnFunc, listenRange);
    }

    public BlockPos getEmergePos() {
        BlockPos pos = getBlockPos();
        for(int i = 0; i < 5; i++) {
            pos = pos.above();
            BlockState block = level.getBlockState(pos);
            if(block.is(TagsNF.STONE_TUNNELS) && block.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y) continue;
            else return pos;
        }
        return pos;
    }

    @Override
    public @Nullable Entity removeEntity(int index, boolean forgetHome) {
        Entity entity = super.removeEntity(index, forgetHome);
        if(entity instanceof RockwormEntity rockworm) {
            rockworm.retreatCooldown = 6;
            rockworm.startAction(ActionsNF.ROCKWORM_EMERGE.getId());
        }
        return entity;
    }

    @Override
    protected Direction[] getExitDirections() {
        return EXIT_UP;
    }

    @Override
    public BlockPos removeEntityToSafePos(@Nullable Entity entity) {
        if(entity == null) {
            if(entityData.isEmpty()) return null;
            CompoundTag data = entityData.get(entityData.size() - 1);
            entity = EntityType.loadEntityRecursive(data, level, (e) -> e);
        }
        if(entity.getSoundSource() == SoundSource.HOSTILE && level.getDifficulty() == Difficulty.PEACEFUL) return null;
        BlockPos pos = getBlockPos();
        int randYRot = level.random.nextInt(360);
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, randYRot, 0);
        for(int i = 0; i < 5; i++) {
            pos = pos.above();
            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            if(canPlaceEntityAt(entity, pos, level)) return pos;
            else {
                BlockState block = level.getBlockState(pos);
                if(!block.is(TagsNF.STONE_TUNNELS) || block.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) return null;
            }
        }
        return null;
    }

    @Override
    protected boolean shouldEventTriggerNest(float range, BlockPos pos, GameEvent event, Entity entity) {
        double blockDistSqr = getEmergePos().above().distSqr(pos);
        return blockDistSqr <= range * range &&  blockDistSqr < 3.1 * 3.1;
    }

    @Override
    protected boolean handleGameEvent(Level level, GameEvent event, @Nullable Entity entity, BlockPos pos) {
        if(super.handleGameEvent(level, event, entity, pos)) {
            if(hasAnyEntities() && level.getGameTime() - lastDigTime >= 15L) {
                BlockPos digPos = getBlockPos();
                for(int i = 0; i < 5; i++) {
                    digPos = digPos.above();
                    BlockState block = level.getBlockState(digPos);
                    if(!block.is(TagsNF.STONE_TUNNELS) || block.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) break;
                }
                dummy.mineBlock(level, digPos);
                lastDigTime = level.getGameTime();
            }
            return true;
        }
        else return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastDigTime = tag.getLong("lastDigTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("lastDigTime", lastDigTime);
    }
}
