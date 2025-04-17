package frostnox.nightfall.block.block;

import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

public class MoonEssenceBlock extends AbstractEssenceBlock {
    public MoonEssenceBlock(RegistryObject<SimpleParticleType> particle, Properties properties) {
        super(particle, properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if(LevelUtil.isDay(level)) level.destroyBlock(pos, false);
    }
}
