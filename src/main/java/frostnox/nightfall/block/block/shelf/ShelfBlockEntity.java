package frostnox.nightfall.block.block.shelf;

import frostnox.nightfall.block.block.VisualContainerBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ShelfBlockEntity extends VisualContainerBlockEntity {
    public ShelfBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.SHELF.get(), pos, pBlockState, 4);
    }

    protected ShelfBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int size) {
        super(pType, pPos, pBlockState, size);
    }

    @Override
    public boolean dropOnFall() {
        return true;
    }
}
