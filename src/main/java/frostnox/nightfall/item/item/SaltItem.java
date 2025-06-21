package frostnox.nightfall.item.item;

import frostnox.nightfall.block.block.MeltableBlock;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SaltItem extends ItemNF {
    public SaltItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(!entity.level.isClientSide && entity.isOnGround()) {
            BlockPos meltPos = null;
            BlockState block = entity.getFeetBlockState();
            if(block.is(TagsNF.SALT_MELTS)) meltPos = entity.blockPosition();
            else {
                BlockPos onPos = entity.getOnPos();
                block = entity.level.getBlockState(onPos);
                if(block.is(TagsNF.SALT_MELTS)) meltPos = onPos;
            }
            if(meltPos != null) {
                if(block.getBlock() instanceof MeltableBlock meltable) {
                    if(block.is(BlockTags.ICE)) entity.level.playSound(null, meltPos, SoundsNF.ICE_CRACKLE.get(), SoundSource.BLOCKS, 1F, 0.95F + entity.level.random.nextFloat() * 0.1F);
                    else entity.level.levelEvent(2001, meltPos, Block.getId(block));
                    entity.level.setBlockAndUpdate(meltPos, meltable.meltBlock.get().defaultBlockState());
                }
                else entity.level.destroyBlock(meltPos, false);
                entity.discard();
                return true;
            }
        }
        return false;
    }
}
