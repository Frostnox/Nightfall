package frostnox.nightfall.block.block.furnacechannel;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceChannelBlockEntity extends BlockEntity {
    public FurnaceChannelBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.FURNACE_CHANNEL.get(), pos, state);
    }

    protected FurnaceChannelBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FurnaceChannelBlockEntity entity) {

    }
}
