package frostnox.nightfall.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class SnowParticle extends FallingParticle{
    protected final float rollSpeed;

    public SnowParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        quadSize = 0.15F;
        setSize(0.075F, 0.075F);
        rollSpeed = random.nextFloat() * 0.08F;
        gravity = 0.11F;
        yd = -gravity;
    }

    @Override
    public void tick() {
        oRoll = roll;
        roll += rollSpeed;
        super.tick();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SnowParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
