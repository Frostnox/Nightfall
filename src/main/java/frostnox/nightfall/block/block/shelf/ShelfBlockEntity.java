package frostnox.nightfall.block.block.shelf;

import frostnox.nightfall.block.ContainerBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ShelfBlockEntity extends ContainerBlockEntity {
    public ShelfBlockEntity(BlockPos pos, BlockState pBlockState) {
        super(BlockEntitiesNF.SHELF.get(), pos, pBlockState, 4);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
    public boolean dropOnFall() {
        return true;
    }
}
