package frostnox.nightfall.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class FragmentParticle extends ConstantCollidingParticle {
    protected FragmentParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pickSprite(sprite);
        this.gravity = 1F;
        this.lifetime = this.random.nextInt(5) + 140;
        this.quadSize *= 0.9F + this.random.nextFloat(0.2F);
        this.hasPhysics = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class BoneProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public BoneProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FragmentParticle particle = new FragmentParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            particle.setColor(0.725F, 0.729F, 0.804F);
            return particle;
        }
    }

    public static class CreeperProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CreeperProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FragmentParticle particle = new FragmentParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            particle.setColor(0.183F, 0.451F, 0.129F);
            return particle;
        }
    }
}
