package frostnox.nightfall.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class FlameParticleNF extends RisingParticle {
    private final SpriteSet sprite;

    protected FlameParticleNF(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprite = sprite;
        this.setSpriteFromAge(sprite);
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double x, double pY, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, pY, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float pPartialTick) {
        float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprite);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FlameParticleNF(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
