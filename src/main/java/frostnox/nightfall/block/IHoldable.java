package frostnox.nightfall.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static frostnox.nightfall.block.BlockStatePropertiesNF.*;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

/**
 * For block entities that can be picked up and held by the player.
 */
public interface IHoldable {
    /**
     * Inventory must be cleared here to avoid duplication.
     * @return all data to store on the server
     */
    CompoundTag writeDataAndClear();

    /**
     * Handle force drop of block (death & forced crawl).
     * This should drop the block itself and its contents.
     */
    void onDrop(Level level, BlockPos pos);

    /**
     * Resolves possible conflicts in properties between the held state and new state from placement context.
     * Assume both states are of the same block class.
     * @return state to put in the world
     */
    default BlockState resolvePutState(BlockState state, BlockState placeState) {
        if(state.hasProperty(WATERLOG_TYPE)) state = state.setValue(WATERLOG_TYPE, placeState.getValue(WATERLOG_TYPE));
        if(state.hasProperty(WATER_LEVEL)) state = state.setValue(WATER_LEVEL, placeState.getValue(WATER_LEVEL));
        if(state.hasProperty(SUPPORT)) state = state.setValue(SUPPORT, placeState.getValue(SUPPORT));
        if(state.hasProperty(FACING_NOT_DOWN)) state = state.setValue(FACING_NOT_DOWN, placeState.getValue(FACING_NOT_DOWN));
        if(state.hasProperty(FACING_NOT_UP)) state = state.setValue(FACING_NOT_UP, placeState.getValue(FACING_NOT_UP));

        if(state.hasProperty(HORIZONTAL_AXIS)) state = state.setValue(HORIZONTAL_AXIS, placeState.getValue(HORIZONTAL_AXIS));
        if(state.hasProperty(AXIS)) state = state.setValue(AXIS, placeState.getValue(AXIS));
        if(state.hasProperty(HORIZONTAL_FACING)) state = state.setValue(HORIZONTAL_FACING, placeState.getValue(HORIZONTAL_FACING));
        if(state.hasProperty(FACING)) state = state.setValue(FACING, placeState.getValue(FACING));
        if(state.hasProperty(FACING_HOPPER)) state = state.setValue(FACING_HOPPER, placeState.getValue(FACING_HOPPER));
        if(state.hasProperty(WATERLOGGED)) state = state.setValue(WATERLOGGED, placeState.getValue(WATERLOGGED));
        if(state.hasProperty(CHEST_TYPE)) state = state.setValue(CHEST_TYPE, placeState.getValue(CHEST_TYPE));
        return state;
    }

    /**
     * Called immediately after the block is put down in the world.
     */
    default void onPut(BlockPos pos, Player player) {

    }

    /**
     * @return true to use the item's renderer in addition to the block model
     */
    default boolean useBlockEntityItemRenderer() {
        return false;
    }

    default double getFirstPersonYOffset() {
        return 0D;
    }

    default double getThirdPersonYOffset() {
        return 0D;
    }

    /**
     * @return true if use interaction succeeds
     */
    default boolean heldUse(BlockPos usePos, Player player) {
        return false;
    }

    /**
     * @return true if use interaction succeeds
     */
    default boolean heldUse(Entity target, Player player) {
        return false;
    }
}