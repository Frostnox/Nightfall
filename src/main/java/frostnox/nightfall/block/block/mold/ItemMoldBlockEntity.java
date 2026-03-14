package frostnox.nightfall.block.block.mold;

import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ItemMoldBlockEntity extends BlockMoldBlockEntity implements IHoldable {
    public ItemMoldBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntitiesNF.ITEM_MOLD.get(), pos, state);
    }

    protected ItemMoldBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public CompoundTag writeDataAndClear() {
        CompoundTag data = this.saveWithId();
        data.putInt("state", Block.getId(this.getBlockState().setValue(ItemMoldBlock.FACING, Direction.NORTH)));
        inputFluid = FluidStack.EMPTY;
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = getContainerDrops();
        drops.add(new ItemStack(this.getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }

    @Override
    public boolean useBlockEntityItemRenderer() {
        return true;
    }

    @Override
    public double getFirstPersonYOffset() {
        return 0.4D;
    }
}
