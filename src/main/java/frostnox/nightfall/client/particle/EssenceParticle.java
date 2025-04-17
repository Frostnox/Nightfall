package frostnox.nightfall.client.particle;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class EssenceParticle extends TextureSheetParticle {
    private final SpriteSet sprite;

    protected EssenceParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprite = sprite;
        this.setSpriteFromAge(sprite);
        this.gravity = -0.008F;
        this.lifetime = this.random.nextInt(6) + 24;
        this.quadSize *= 0.6F + this.random.nextFloat(0.1F);
        this.hasPhysics = false;
        this.setAlpha(0.9F);
        this.xd *= 0.2D;
        this.yd *= 0.2D;
        this.zd *= 0.2D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float p_106065_) {
        return 240;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprite);
        this.setAlpha(Mth.clamp(1F - ((float) this.age + ClientEngine.get().getPartialTick()) / this.lifetime, 0.12F, 0.5F));
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            EssenceParticle particle = new EssenceParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            particle.setColor(0.781F, 0.796F, 0.831F);
            return particle;
        }
    }

    public static class MoonProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MoonProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            EssenceParticle particle = new EssenceParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            particle.setColor(0.756F, 0.913F, 0.955F);
            return particle;
        }
    }
}
