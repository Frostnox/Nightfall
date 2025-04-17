package frostnox.nightfall.block.fluid;

import frostnox.nightfall.block.IMetal;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;

import java.util.function.Supplier;

public abstract class MetalFluid extends SizedFluid {
    public final IMetal metal;

    protected MetalFluid(MetalProperties properties) {
        super(properties);
        this.metal = properties.metal;
    }

    public static class Flowing extends MetalFluid {
        public Flowing(MetalProperties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 3));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends MetalFluid {
        public Source(MetalProperties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 4;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class MetalProperties extends Properties {
        public final IMetal metal;

        public MetalProperties(Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing, IMetal metal, FluidAttributes.Builder attributes) {
            super(still, flowing, attributes);
            this.metal = metal;
        }
    }
}
