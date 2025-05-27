package frostnox.nightfall.block.block;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TimeDataBlockEntity extends BlockEntity {
    public long lastProcessedTick;

    public TimeDataBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.TIME_DATA.get(), pos, pBlockState);
    }

    protected TimeDataBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(lastProcessedTick == 0) lastProcessedTick = level.getGameTime();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastProcessedTick = tag.getLong("lastProcessedTick");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("lastProcessedTick", lastProcessedTick);
    }
}
