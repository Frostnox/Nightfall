package frostnox.nightfall.client.particle;

import frostnox.nightfall.client.ClientEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class SteamParticle extends TextureSheetParticle {
    protected final SpriteSet spriteSet;
    protected final boolean flip;

    protected SteamParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        gravity = -0.04F;
        lifetime = this.random.nextInt(8) + 36;
        this.quadSize = 0.5F + this.random.nextFloat(0.05F);
        this.spriteSet = sprite;
        this.xd *= 0.2D;
        this.yd *= 0.2D;
        this.zd *= 0.2D;
        setSpriteFromAge(sprite);
        flip = random.nextBoolean();
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(spriteSet);
        int fadeTime = this.lifetime * 3 / 4;
        if(this.age >= fadeTime) {
            this.setAlpha(Mth.clamp(1F - ((float) this.age + ClientEngine.get().getPartialTick() - fadeTime) / (this.lifetime - fadeTime) * 1F, 0F, 1F));
        }
    }

    @Override
    protected float getU0() {
        return flip ? sprite.getU1() : sprite.getU0();
    }

    @Override
    protected float getU1() {
        return flip ? sprite.getU0() : sprite.getU1();
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
            return new SteamParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
