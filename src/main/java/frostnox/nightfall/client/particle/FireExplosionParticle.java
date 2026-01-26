package frostnox.nightfall.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class FireExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprite;

    protected FireExplosionParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprite = sprite;
        this.setSpriteFromAge(sprite);
        this.gravity = -0.006F;
        this.lifetime = 7;
        this.quadSize = 0.5F + this.random.nextFloat(0.05F);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float partial) {
        return 240;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprite);
    }

    @Override
    public void remove() {
        super.remove();
        for(int i = 0; i < (random.nextBoolean() ? 9 : 10); i++) {
            level.addParticle(ParticleTypes.SMOKE.getType(), x - 0.35 + random.nextFloat() * 0.7F, y - 0.1 + random.nextFloat() * 0.5F, z - 0.35 + random.nextFloat() * 0.7F, 0, 0, 0);
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
            return new FireExplosionParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
