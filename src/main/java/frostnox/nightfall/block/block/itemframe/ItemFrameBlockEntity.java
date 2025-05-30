package frostnox.nightfall.block.block.itemframe;

import frostnox.nightfall.block.block.VisualContainerBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemFrameBlockEntity extends VisualContainerBlockEntity {
    protected int rotation;

    public ItemFrameBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.ITEM_FRAME.get(), pos, pBlockState, 1);
    }

    protected ItemFrameBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int size) {
        super(pType, pPos, pBlockState, size);
    }

    public int getRotation() {
        return rotation;
    }

    public void incrementRotation() {
        rotation++;
        if(rotation > 7) rotation = 0;
        setChanged();
    }

    public void resetRotation() {
        rotation = 0;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rotation = tag.getInt("rotation");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("rotation", rotation);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("rotation", rotation);
        return tag;
    }
}
