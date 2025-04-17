package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FireableBlockEntity extends BlockEntity implements IFireableBlockEntity {
    public int cookTicks;
    public boolean inStructure;

    public FireableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesNF.FIREABLE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cookTicks = tag.getInt("cookTicks");
        inStructure = tag.getBoolean("inStructure");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("cookTicks", cookTicks);
        tag.putBoolean("inStructure", inStructure);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void setCookTicks(int cookTicks) {
        this.cookTicks = cookTicks;
    }

    @Override
    public void setInStructure(boolean inStructure) {
        this.inStructure = inStructure;
    }

    @Override
    public int getCookTicks() {
        return cookTicks;
    }

    @Override
    public boolean inStructure() {
        return inStructure;
    }
}
