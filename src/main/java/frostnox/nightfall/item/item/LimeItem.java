package frostnox.nightfall.item.item;

import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class LimeItem extends ItemNF {
    public LimeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(entity.isInWater() && entity.level instanceof ServerLevel level) {
            entity.playSound(SoundsNF.SIZZLE.get(), 1.5F, 0.95F + level.random.nextFloat() * 0.1F);
            boolean waterAbove = entity.level.getFluidState(entity.blockPosition().above()).is(FluidTags.WATER);
            ParticleOptions particle = waterAbove ? ParticleTypes.BUBBLE : ParticleTypesNF.STEAM.get();
            level.sendParticles(particle, entity.getX(), waterAbove ? (entity.getEyeY()) : (Mth.floor(entity.getY()) + 1), entity.getZ(),
                    10, 0.2, 0, 0.2, 0);
            entity.discard();
            return true;
        }
        return false;
    }
}
