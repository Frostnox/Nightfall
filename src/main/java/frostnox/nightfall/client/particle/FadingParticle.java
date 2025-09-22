package frostnox.nightfall.client.particle;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class FadingParticle extends ConstantCollidingParticle {
    protected FadingParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int life, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pickSprite(sprite);
        this.gravity = 1F;
        this.lifetime = this.random.nextInt(10) + life;
        this.quadSize *= 0.9F + this.random.nextFloat(0.2F);
        this.hasPhysics = true;
        this.setAlpha(0.85F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if(!level.getBlockState(new BlockPos(x, y, z)).getFluidState().isEmpty()) this.gravity = -0.001F;
        else this.gravity = 1F;
        int fadeTime = this.lifetime * 4 / 5;
        if(this.age >= fadeTime) {
            this.setAlpha(Mth.clamp(0.85F - ((float) this.age + ClientEngine.get().getPartialTick() - fadeTime) / (this.lifetime - fadeTime) * 0.85F, 0F, 0.85F));
        }
    }

    public static class RedProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public RedProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingParticle particle = new FadingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 160, sprite);
            particle.setColor(0.606F, 0.098F, 0.098F);
            return particle;
        }
    }

    public static class DarkRedProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DarkRedProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingParticle particle = new FadingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 160, sprite);
            particle.setColor(0.420F, 0.18F, 0.16F);
            return particle;
        }
    }

    public static class PaleBlueProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public PaleBlueProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingParticle particle = new FadingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 160, sprite);
            particle.setColor(0.442F, 0.598F, 0.652F);
            return particle;
        }
    }

    public static class GreenProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GreenProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingParticle particle = new FadingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 160, sprite);
            particle.setColor(0.350F, 0.493F, 0.271F);
            return particle;
        }
    }

    public static class PoisonSpitProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public PoisonSpitProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FadingParticle particle = new FadingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 18, sprite);
            float scale = 0.7F + particle.random.nextFloat() * 0.6F;
            particle.setColor(0.592F * scale, 0.749F * scale, 0.110F * scale);
            return particle;
        }
    }
}
