package frostnox.nightfall.block.block.rack;

import frostnox.nightfall.block.ContainerBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RackBlockEntity extends ContainerBlockEntity {
    public RackBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.RACK.get(), pos, pBlockState, 3);
    }

    protected RackBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int size) {
        super(pType, pPos, pBlockState, size);
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
    public AABB getRenderBoundingBox() {
        AABB box = getBlockState().getShape(level, getBlockPos()).bounds().move(getBlockPos());
        if(getBlockState().getValue(RackBlock.FACING).getAxis() == Direction.Axis.X) {
            box = box.inflate(0, 0.35, 0.5);
        }
        else box = box.inflate(0.5, 0.35, 0);
        return box;
    }
}
