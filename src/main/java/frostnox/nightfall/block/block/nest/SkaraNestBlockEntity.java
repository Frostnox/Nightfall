package frostnox.nightfall.block.block.nest;

import frostnox.nightfall.entity.entity.ActionableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.BiFunction;

public class SkaraNestBlockEntity extends GuardedNestBlockEntity {
    public SkaraNestBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState, int capacity, int respawnTime, BiFunction<ServerLevel, BlockPos, ActionableEntity> respawnFunc, int listenRange) {
        super(pType, pos, pBlockState, capacity, respawnTime, respawnFunc, listenRange);
    }

    @Override
    protected boolean shouldEventTriggerNest(float range, BlockPos pos, GameEvent event, Entity entity) {
        return super.shouldEventTriggerNest(range, pos, event, entity) &&
                (event == GameEvent.ENTITY_DAMAGED || event == GameEvent.ENTITY_KILLED);
    }
}
