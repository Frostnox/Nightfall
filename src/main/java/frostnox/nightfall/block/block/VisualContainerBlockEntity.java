package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IDropsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VisualContainerBlockEntity extends BlockEntity implements Container, IDropsItems {
    public final NonNullList<ItemStack> items;

    public VisualContainerBlockEntity(BlockEntityType<?> pType, BlockPos pos, BlockState pBlockState, int size) {
        super(pType, pos, pBlockState);
        items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for(int i = 0; i < items.size(); i++) {
            if(tag.contains("item" + i, Tag.TAG_COMPOUND)) items.set(i, ItemStack.of(tag.getCompound("item" + i)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for(int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if(!item.isEmpty()) tag.put("item" + i, item.save(new CompoundTag()));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        for(int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            tag.put("item" + i, item.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NonNullList<ItemStack> getContainerDrops() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        if(items.isEmpty()) return true;
        for(ItemStack item : items) if(!item.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return items.get(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return items.get(pSlot).split(pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        ItemStack item = items.get(pSlot);
        items.set(pSlot, ItemStack.EMPTY);
        return item;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        items.set(pSlot, pStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockEntity(getBlockPos()) == this && getBlockPos().distToCenterSqr(player.getEyePosition()) <= 36D;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
