package frostnox.nightfall.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class FloatingParticle extends TextureSheetParticle {
    protected FloatingParticle(ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pickSprite(sprite);
        this.gravity = -0.024F;
        this.lifetime = this.random.nextInt(8) + 28;
        this.quadSize *= 0.6F + this.random.nextFloat(0.2F);
        this.xd *= 0.2D;
        this.yd *= 0.2D;
        this.zd *= 0.2D;
    }

    public void tick() {
        super.tick();
    }

    protected boolean isCloseToPlayer() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        return player != null && player.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 0.6D && mc.options.getCameraType().isFirstPerson();
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
            return new FloatingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
