package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.block.BlockEventListener;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.registry.vanilla.GameEventsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiFunction;

public class GuardedNestBlockEntity extends NestBlockEntity {
    protected final GameEventListener eventListener;
    public @Nullable UUID scout;
    protected @Nullable ActionableEntity dummy;

    public GuardedNestBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState, int capacity, int respawnTime, BiFunction<ServerLevel, BlockPos, ActionableEntity> respawnFunc, int listenRange) {
        super(pType, pos, pBlockState, capacity, respawnTime, respawnFunc);
        this.eventListener = new BlockEventListener(new BlockPositionSource(pos), listenRange) {
            @Override
            public boolean handleGameEvent(Level level, GameEvent event, @Nullable Entity entity, BlockPos pos) {
                return GuardedNestBlockEntity.this.handleGameEvent(level, event, entity, pos);
            }
        };
    }

    public GameEventListener getEventListener() {
        return eventListener;
    }

    protected boolean handleGameEvent(Level level, GameEvent event, @Nullable Entity entity, BlockPos pos) {
        if(!hasAnyEntities() || !(entity instanceof LivingEntity livingEntity) || (entity instanceof Player player && player.isCreative())) return false;
        float range = GameEventsNF.getEventRange(event, entity);
        double blockDistSqr = pos.distSqr(worldPosition);
        if(blockDistSqr <= range * range) {
            if(dummy == null) dummy = respawnFunc.apply((ServerLevel) level, pos);
            if(dummy.canTargetFromSound(livingEntity)) {
                if(canSafelyRemoveEntity()) removeAllEntities(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("scoutUUID")) scout = tag.getUUID("scoutUUID");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(scout != null) tag.putUUID("scoutUUID", scout);
    }
}
