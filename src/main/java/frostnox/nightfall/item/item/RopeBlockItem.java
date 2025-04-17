package frostnox.nightfall.item.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class RopeBlockItem extends BlockItemNF {
    public RopeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos.MutableBlockPos pos = context.getHitResult().getBlockPos().mutable();
        if(context.getHitResult().getDirection() == Direction.DOWN) {
            pos.setY(pos.getY() - 1);
        }
        Level level = context.getLevel();
        while(level.getBlockState(pos).is(getBlock())) pos.setY(pos.getY() - 1);
        if(pos.getY() > level.getMinBuildHeight()) {
            BlockPlaceContext newContext = new BlockPlaceContext(context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false));
            if(level.getBlockState(pos).canBeReplaced(newContext)) {
                return newContext;
            }
        }
        return context;
    }
}
