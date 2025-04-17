package frostnox.nightfall.item.item;

import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SulfurItem extends ItemNF {
    public SulfurItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isOnFire() && !entity.level.isClientSide) {
            entity.playSound(SoundsNF.FIRE_WHOOSH.get(), 1F, 0.9F + entity.level.random.nextFloat() * 0.2F);
            BlockPos pos = entity.blockPosition();
            if(entity.level.getBlockState(pos).isAir()) {
                BlockState fire = BlocksNF.FIRE.get().getPlacementState(entity.level, pos);
                if(fire.canSurvive(entity.level, pos)) entity.level.setBlock(pos, fire, 11);
            }
            for(Direction dir : Direction.values()) {
                BlockPos adjPos = pos.relative(dir);
                if(entity.level.getBlockState(adjPos).isAir()) {
                    BlockState fire = BlocksNF.FIRE.get().getPlacementState(entity.level, adjPos);
                    if(fire.canSurvive(entity.level, adjPos)) entity.level.setBlock(adjPos, fire, 11);
                }
            }
            entity.discard();
            return true;
        }
        return false;
    }
}
