package frostnox.nightfall.block;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IFallable {
    default void onFall(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity) {

    }

    default void onMovingBlockEntityCreated(BlockState state, Level level, BlockPos pos, MovingBlockEntity entity) {

    }

    default void onLand(Level level, BlockPos pos, BlockState state, BlockState contactState, MovingBlockEntity entity) {

    }

    default void onBrokenAfterFall(Level level, BlockPos pos, MovingBlockEntity movingBlock) {

    }

    default boolean canLand(Level level, BlockPos movingPos, BlockState movingState, BlockState contactState, MovingBlockEntity movingBlock) {
        return true;
    }

    default SoundEvent getFallSound(BlockState state) {
        return null;
    }

    default DamageType getFallDamageType() {
        return DamageType.STRIKING;
    }
}
