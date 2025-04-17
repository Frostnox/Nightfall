package frostnox.nightfall.item.item;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class LogItem extends BlockItemNF {
    public LogItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.level.isClientSide && entity.isInWater() && stack.is(TagsNF.LUMBER_TANNIN) && entity.tickCount % 40 == 0) {
            entity.level.addParticle(ParticleTypesNF.FADING_CLOUD.get(), entity.getRandomX(entity.getBbWidth()), entity.getRandomY(), entity.getRandomZ(entity.getBbWidth()),
                    0.517, 0.360, 0.226);
        }
        return false;
    }
}
