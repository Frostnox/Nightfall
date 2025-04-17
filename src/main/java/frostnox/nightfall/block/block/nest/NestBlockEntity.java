package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.entity.IHomeEntity;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public class NestBlockEntity extends BlockEntity implements Clearable {
    public final int capacity, respawnTime;
    protected final BiFunction<ServerLevel, BlockPos, ActionableEntity> respawnFunc;
    protected final List<CompoundTag> entityData;
    protected final Set<UUID> trackedEntities;
    public long lastFullTime = Long.MIN_VALUE;

    public NestBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState, int capacity, int respawnTime, BiFunction<ServerLevel, BlockPos, ActionableEntity> respawnFunc) {
        super(pType, pos, pBlockState);
        this.capacity = capacity;
        this.respawnTime = respawnTime;
        this.respawnFunc = respawnFunc;
        entityData = new ObjectArrayList<>(capacity);
        trackedEntities = new ObjectArraySet<>(capacity);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ListTag entitiesList = tag.getList("entities", ListTag.TAG_COMPOUND);
        for(int i = 0; i < entitiesList.size(); i++) {
            CompoundTag data = entitiesList.getCompound(i);
            entityData.add(data);
        }
        if(tag.contains("trackedEntities")) {
            ListTag ids = tag.getList("trackedEntities", ListTag.TAG_INT_ARRAY);
            for(Tag idTag : ids) trackedEntities.add(NbtUtils.loadUUID(idTag));
        }
        lastFullTime = tag.getLong("lastFullTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag entitiesList = new ListTag();
        for(CompoundTag data : entityData) {
            CompoundTag dataCopy = data.copy();
            dataCopy.remove("UUID");
            entitiesList.add(dataCopy);
        }
        tag.put("entities", entitiesList);
        if(!trackedEntities.isEmpty()) {
            ListTag ids = new ListTag();
            for(UUID id : trackedEntities) ids.add(NbtUtils.createUUID(id));
            tag.put("trackedEntities", ids);
        }
        tag.putLong("lastFullTime", lastFullTime);
    }

    @Override
    public void clearContent() {
        entityData.clear(); //Clear for when block falls since nested entities are preserved
    }

    public boolean hasAnyEntities() {
        return !entityData.isEmpty();
    }

    public boolean canRespawn() {
        return entityData.size() + trackedEntities.size() < capacity;
    }

    public void respawnEntity(BlockPos pos) {
        if(level instanceof ServerLevel serverLevel) addEntity(respawnFunc.apply(serverLevel, pos));
    }

    public void addEntity(Entity entity) {
        if(entityData.size() < capacity) {
            entity.stopRiding();
            entity.ejectPassengers();
            CompoundTag tag = new CompoundTag();
            entity.save(tag);
            entityData.add(tag);
            trackedEntities.remove(entity.getUUID());
            entity.discard();
            setChanged();
        }
    }

    public @Nullable Entity popEntity(boolean forgetHome) {
        return removeEntity(entityData.size() - 1, forgetHome);
    }

    public boolean canSafelyRemoveEntity() {
        if(entityData.isEmpty()) return true;
        CompoundTag data = entityData.get(entityData.size() - 1);
        Entity entity = EntityType.loadEntityRecursive(data, level, (e) -> e);
        if(entity.getSoundSource() == SoundSource.HOSTILE && level.getDifficulty() == Difficulty.PEACEFUL) return false;
        BlockPos pos = getBlockPos();
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if(canPlaceEntityAt(entity, getBlockPos(), level)) return true;
        if(getBlockState().getMaterial().blocksMotion()) {
            for(Direction direction : LevelUtil.HORIZONTAL_DIRECTIONS) {
                pos = getBlockPos().relative(direction);
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if(canPlaceEntityAt(entity, pos, level)) return true;
            }
        }
        return false;
    }

    public static boolean canPlaceEntityAt(Entity entity, BlockPos pos, BlockGetter level) {
        BlockState pState = level.getBlockState(pos);
        VoxelShape blockShape = pState.getCollisionShape(level, pos, CollisionContext.of(entity)).move(pos.getX(), pos.getY(), pos.getZ());
        return !Shapes.joinIsNotEmpty(blockShape, Shapes.create(entity.getBoundingBox()), BooleanOp.AND);
    }

    public @Nullable Entity removeEntity(int index, boolean forgetHome) {
        if(entityData.isEmpty() || index < 0 || index >= entityData.size()) return null;
        CompoundTag data = entityData.remove(index);
        Entity entity = EntityType.loadEntityRecursive(data, level, (e) -> e);
        setChanged();
        if(entity != null) {
            BlockPos pos = getBlockPos();
            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextInt(360), 0);
            if(getBlockState().getMaterial().blocksMotion() && !canPlaceEntityAt(entity, pos, level)) {
                List<Direction> directions = new ObjectArrayList<>(LevelUtil.HORIZONTAL_DIRECTIONS);
                Collections.shuffle(directions, level.random);
                while(!directions.isEmpty()) {
                    Direction direction = directions.remove(0);
                    pos = getBlockPos().relative(direction);
                    entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, direction.toYRot() % 360, 0);
                    if(canPlaceEntityAt(entity, pos, level)) break;
                }
            }
            if(entity instanceof IHomeEntity homeEntity) {
                if(forgetHome) homeEntity.setHomePos(null);
                else trackedEntities.add(entity.getUUID());
                homeEntity.onExitHome();
            }
            entity.setYBodyRot(entity.getYRot());
            entity.setYHeadRot(entity.getYRot());
            level.addFreshEntity(entity);
            entity.setYRot(entity.getYRot());
            return entity;
        }
        else return null;
    }

    public void removeAllEntities(boolean forgetHome) {
        while(!entityData.isEmpty()) popEntity(forgetHome);
    }

    public void startTrackingEntity(UUID id) {
        if(trackedEntities.add(id)) setChanged();
    }

    public void stopTrackingEntity(UUID id) {
        if(trackedEntities.remove(id)) setChanged();
    }

    public void randomTick(ServerLevel level, BlockPos pos) {
        if(canRespawn()) {
            if(level.getGameTime() - lastFullTime >= respawnTime) {
                addEntity(respawnFunc.apply(level, pos));
                setChanged();
            }
        }
        else {
            lastFullTime = level.getGameTime();
            setChanged();
        }
    }
}
