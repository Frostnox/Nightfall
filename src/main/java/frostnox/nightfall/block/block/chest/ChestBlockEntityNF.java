package frostnox.nightfall.block.block.chest;

import frostnox.nightfall.block.IDropsItems;
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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChestBlockEntityNF extends ChestBlockEntity implements IHoldable, IDropsItems {
    public int forceEntityRenderTick = -1;
    public int tickCount = 0;

    public ChestBlockEntityNF(BlockPos p_155328_, BlockState p_155329_) {
        super(BlockEntitiesNF.CHEST.get(), p_155328_, p_155329_);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChestBlockEntity pBlockEntity) {
        ChestBlockEntity.lidAnimateTick(level, pos, state, pBlockEntity);
        ((ChestBlockEntityNF) pBlockEntity).tickCount++;
    }

    @Override
    public CompoundTag writeDataAndClear() {
        CompoundTag data = saveWithId();
        data.putInt("state", Block.getId(getBlockState().getBlock().defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH)));
        for(int i = 0; i < getContainerSize(); i++) getItems().set(i, ItemStack.EMPTY);
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = NonNullList.create();
        for(int i = 0; i < getContainerSize(); i++) drops.add(getItems().get(i));
        drops.add(new ItemStack(getBlockState().getBlock().asItem()));
        if(level != null) Containers.dropContents(level, pos, drops);
    }

    @Override
    public NonNullList<ItemStack> getContainerDrops() {
        return getItems();
    }
}
