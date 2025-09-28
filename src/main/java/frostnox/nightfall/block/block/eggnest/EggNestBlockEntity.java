package frostnox.nightfall.block.block.eggnest;

import frostnox.nightfall.block.block.TimeDataBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class EggNestBlockEntity extends TimeDataBlockEntity {
    public final int[] hatchTimes = new int[4], eggData = new int[4];
    public boolean occupied;

    public EggNestBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesNF.EGG_NEST.get(), pPos, pBlockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for(int i = 0; i < hatchTimes.length; i++) hatchTimes[i] = tag.getInt("hatch" + i);
        for(int i = 0; i < eggData.length; i++) eggData[i] = tag.getInt("data" + i);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for(int i = 0; i < hatchTimes.length; i++) tag.putInt("hatch" + i, hatchTimes[i]);
        for(int i = 0; i < eggData.length; i++) tag.putInt("data" + i, eggData[i]);
    }
}
