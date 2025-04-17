package frostnox.nightfall.block.block.bowl;

import frostnox.nightfall.block.IHoldable;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BowlBlockEntity extends BlockEntity implements IHoldable {
    public ItemStack item = ItemStack.EMPTY;
    public float itemAngle = 0F;
    public int crushes = 0;

    public BowlBlockEntity(BlockPos pos, BlockState pBlockState) {
        super(BlockEntitiesNF.BOWL.get(), pos, pBlockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("item", Tag.TAG_COMPOUND)) item = ItemStack.of(tag.getCompound("item"));
        else item = ItemStack.EMPTY;
        itemAngle = tag.getFloat("angle");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(!item.isEmpty()) tag.put("item", item.save(new CompoundTag()));
        tag.putFloat("angle", itemAngle); //Always save so update packet isn't discarded if empty
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public CompoundTag writeDataAndClear() {
        itemAngle = 0F;
        CompoundTag data = saveWithId();
        data.putInt("state", Block.getId(getBlockState().getBlock().defaultBlockState()));
        item = ItemStack.EMPTY;
        return data;
    }

    @Override
    public void onDrop(Level level, BlockPos pos) {
        if(level != null) {
            Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, item, new ItemStack(getBlockState().getBlock().asItem())));
        }
    }

    @Override
    public void onPut(BlockPos pos, Player player) {
        if(!item.isEmpty()) {
            itemAngle = Math.round((-player.getViewYRot(1F)) / 45F) * 45F;
        }
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
