package frostnox.nightfall.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class FadingGlowingParticle extends FadingParticle {
    protected FadingGlowingParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int life, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, life, sprite);
    }

    @Override
    public int getLightColor(float partial) {
        return 240;
    }

    public static class EctoplasmProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public EctoplasmProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingGlowingParticle particle = new FadingGlowingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 160, sprite);
            particle.setColor(0.725F, 0.725F, 0.804F);
            return particle;
        }
    }
}
