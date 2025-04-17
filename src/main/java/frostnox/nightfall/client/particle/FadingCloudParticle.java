package frostnox.nightfall.client.particle;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class FadingCloudParticle extends TextureSheetParticle {
    protected FadingCloudParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pickSprite(sprite);
        this.gravity = -0.0025F;
        this.lifetime = this.random.nextInt(16) + 60;
        this.quadSize *= 0.8F + this.random.nextFloat(0.2F);
        this.xd *= 0.1;
        this.yd = 0;
        this.zd *= 0.1;
        this.hasPhysics = true;
        this.setAlpha(0.4F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        int fadeTime = this.lifetime * 4 / 5;
        if(this.age >= fadeTime) {
            this.setAlpha(Mth.clamp(0.4F - ((float) this.age + ClientEngine.get().getPartialTick() - fadeTime) / (this.lifetime - fadeTime) * 0.4F, 0F, 0.4F));
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
            FadingCloudParticle particle = new FadingCloudParticle(worldIn, x, y, z, 0, 0, 0, sprite);
            particle.setColor((float) xSpeed, (float) ySpeed, (float) zSpeed);
            return particle;
        }
    }
}
