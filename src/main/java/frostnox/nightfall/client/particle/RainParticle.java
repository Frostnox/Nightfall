package frostnox.nightfall.client.particle;

import frostnox.nightfall.block.IHeatSource;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.FluidsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RainParticle extends FallingParticle {
    public RainParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        int color = FluidsNF.WATER.get().getAttributes().getColor(level, mutablePos);
        alpha = (color >>> 24 & 0xFF) / 255F;
        rCol = (color >> 16 & 0xFF) / 255F;
        gCol = (color >> 8 & 0xFF) / 255F;
        bCol = (color & 0xFF) / 255F;
        quadSize = 0.3F;
        setSize(0.15F, 0.15F);
        gravity = 0.82F;
        yd = -gravity;
    }

    @Override
    protected void onLand() {
        if(Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL) {
            BlockState state = level.getBlockState(mutablePos.set(x, y - 0.05, z));
            ParticleOptions particle = state.getBlock() instanceof IHeatSource heatSource
                    && heatSource.getHeat(level, mutablePos, state) != TieredHeat.NONE ? ParticleTypes.SMOKE : ParticleTypesNF.RAIN_SPLASH.get();
            level.addParticle(particle, x, y, z, 0, 0, 0);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RainParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
