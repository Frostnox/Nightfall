package frostnox.nightfall.client.particle;

import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class ColoredWaterDropParticle extends WaterDropParticle {
    public ColoredWaterDropParticle(ClientLevel level, double x, double pY, double z) {
        super(level, x, pY, z);
        int color = FluidsNF.WATER.get().getAttributes().getColor(level, new BlockPos(Mth.floor(x), Mth.floor(pY), Mth.floor(z)));
        alpha = (color >>> 24 & 0xFF) / 255F;
        rCol = (color >> 16 & 0xFF) / 255F;
        gCol = (color >> 8 & 0xFF) / 255F;
        bCol = (color & 0xFF) / 255F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ColoredWaterDropParticle particle = new ColoredWaterDropParticle(worldIn, x, y, z);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
