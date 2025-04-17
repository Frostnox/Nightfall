package frostnox.nightfall.block.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MultifaceBlockNF extends MultifaceBlock {
    public MultifaceBlockNF(Properties pProperties) {
        super(pProperties);
    }

    public boolean canReplaceState(BlockState state) {
        return state.isAir() || state.is(this);
    }

    public BlockState getFullyAttachedState(BlockGetter level, BlockPos pos) {
        BlockState state = defaultBlockState();
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        for(Direction dir : Direction.values()) {
            mutablePos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            if(canAttachTo(level, dir, mutablePos, level.getBlockState(mutablePos))) state = state.setValue(getFaceProperty(dir), true);
        }
        return state;
    }

    public boolean canAttachTo(BlockGetter pLevel, Direction pDirection, BlockPos pPos, BlockState pState) {
        return Block.isFaceFull(pState.getCollisionShape(pLevel, pPos), pDirection.getOpposite());
    }

    public boolean canSpreadAt(BlockGetter level, BlockPos pos, BlockState state) {
        if(state.is(this)) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            for(Direction dir : Direction.values()) {
                if(state.getValue(getFaceProperty(dir))) continue;
                mutablePos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
                if(canAttachTo(level, dir, mutablePos, level.getBlockState(mutablePos))) return true;
            }
        }
        else if(canReplaceState(state)) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            for(Direction dir : Direction.values()) {
                mutablePos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
                if(canAttachTo(level, dir, mutablePos, level.getBlockState(mutablePos))) return true;
            }
        }
        return false;
    }
}
