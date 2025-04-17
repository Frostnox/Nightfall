package frostnox.nightfall.block.block;

import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

public abstract class AbstractEssenceBlock extends HalfTransparentBlock {
    private final RegistryObject<SimpleParticleType> particle;

    public AbstractEssenceBlock(RegistryObject<SimpleParticleType> particle, Properties properties) {
        super(properties);
        this.particle = particle;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter pReader, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        for(int i = 0; i < 2; i++) {
            double x = pos.getX() + level.random.nextDouble();
            double y = pos.getY() + level.random.nextDouble();
            double z = pos.getZ() + level.random.nextDouble();
            level.addParticle(particle.get(), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return BlocksNF.NO_BREAK_PARTICLES;
    }
}
