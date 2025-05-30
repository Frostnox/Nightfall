package frostnox.nightfall.block.block.rack;

import frostnox.nightfall.block.block.VisualContainerBlockEntity;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RackBlockEntity extends VisualContainerBlockEntity {
    public RackBlockEntity(BlockPos pos, BlockState pBlockState) {
        this(BlockEntitiesNF.RACK.get(), pos, pBlockState, 3);
    }

    protected RackBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, int size) {
        super(pType, pPos, pBlockState, size);
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
