package frostnox.nightfall.client.particle;

import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.client.render.blockentity.TieredAnvilRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.awt.*;

public class SparkParticle extends TextureSheetParticle {
    protected SparkParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        setParticleSpeed(pXSpeed, Math.abs(pYSpeed), pZSpeed);
        this.pickSprite(sprite);
        this.gravity = 0.9F;
        this.lifetime = this.random.nextInt(12) + 12;
        this.quadSize *= 0.07F + this.random.nextFloat(0.03F);
        this.hasPhysics = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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

    public static class RedProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public RedProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredHeat.RED.color;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }

    public static class OrangeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public OrangeProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredHeat.ORANGE.color;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }

    public static class YellowProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public YellowProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredHeat.YELLOW.color;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }

    public static class WhiteProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WhiteProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredHeat.WHITE.color;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }

    public static class BlueProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public BlueProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredHeat.BLUE.color;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }

    public static class SlagProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SlagProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SparkParticle particle = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            Color color = TieredAnvilRenderer.SLAG_COLOR;
            particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            return particle;
        }
    }
}
