package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FireableHoldableBlockEntity extends BlockEntity implements IFireableBlockEntity, IHoldable {
    public int cookTicks;
    public boolean inStructure;

    public FireableHoldableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesNF.FIREABLE_POTTERY.get(), pos, state);
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

    @Override
    public CompoundTag writeDataAndClear() {
        cookTicks = 0;
        inStructure = false;
        CompoundTag data = this.saveWithId();
        data.putInt("state", Block.getId(this.getBlockState().setValue(BlockStateProperties.LIT, false)));
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        if(level != null) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this.getBlockState().getBlock().asItem()));
    }
}
