package frostnox.nightfall.block.fluid;

import frostnox.nightfall.registry.forge.SoundsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Random;

public abstract class MeltedMetalFluid extends SizedFluid {
    protected MeltedMetalFluid(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, Random random) {
        if(random.nextInt(380) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundsNF.MOLTEN_LIQUID_AMBIENT.get(), SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
        }
    }

    public static class Flowing extends MeltedMetalFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 3));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends MeltedMetalFluid {
        public Source(Properties properties) {
            super(properties);
        }

        public int getAmount(FluidState state) {
            return 4;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
