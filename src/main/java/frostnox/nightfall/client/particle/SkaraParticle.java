package frostnox.nightfall.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SkaraParticle extends ConstantCollidingParticle {
    protected static final double SPEED = 0.075D;
    protected final float yOffset, rollOffset, baseRoll;
    protected final @Nullable LivingEntity owner;
    protected final SpriteSet spriteSet;
    protected final int spriteIndex;
    protected boolean wasHurt;

    protected SkaraParticle(ClientLevel worldIn, double x, double y, double z, double rotation, double isMoving, double ownerId, SpriteSet sprite) {
        super(worldIn, x, y, z, 0, 0, 0);
        Entity entity = worldIn.getEntity((int) ownerId);
        owner = (entity instanceof LivingEntity livingEntity) ? livingEntity : null;
        lifetime = 22 + random.nextInt(8);
        friction = 1;
        xd = 0;
        yd = 0;
        zd = 0;
        spriteIndex = random.nextBoolean() ? 0 : 1;
        spriteSet = sprite;
        boolean hurt = owner != null && (owner.hurtTime > 0 || owner.deathTime > 0);
        wasHurt = hurt;
        setSprite(sprite.get(hurt ? (spriteIndex + 2) : spriteIndex, 3));
        quadSize = 0.125F * (0.95F + random.nextFloat(0.1F));
        hasPhysics = true;
        gravity = 0.4F;
        yOffset = 1F/16F + (random.nextFloat() - 0.5F) / 32F;
        rollOffset = random.nextFloat() * MathUtil.PI * 2F;
        if(isMoving == 1) baseRoll = (float) rotation + (random.nextFloat() - 0.5F) * MathUtil.PI * 0.25F;
        else baseRoll = random.nextFloat() * MathUtil.PI * 2;
        roll = baseRoll + Mth.sin(rollOffset) * MathUtil.PI / 3;
        oRoll = roll;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if(age++ >= lifetime) remove();
        else {
            boolean hurt = owner != null && (owner.hurtTime > 0 || owner.deathTime > 0);
            if(hurt != wasHurt) setSprite(spriteSet.get(hurt ? (spriteIndex + 2) : spriteIndex, 3));
            wasHurt = hurt;
            oRoll = roll;
            roll = baseRoll + Mth.sin(rollOffset + age * 0.17F) * MathUtil.PI / 3;
            if(onGround) stoppedByCollision = false;
            yd -= 0.04D * gravity;
            xd = -Mth.sin(roll) * SPEED;
            zd = Mth.cos(roll) * SPEED;
            move(xd, yd, zd);
        }
    }

    @Override
    public void remove() {
        if(onGround) {
            BlockState state;
            if(y % 1D == 0) state = level.getBlockState(new BlockPos(x, y - 1, z));
            else state = level.getBlockState(new BlockPos(x, y, z));
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y, z, 0, 0, 0);
        }
        super.remove();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float pPartialTicks) {
        Vec3 camPos = camera.getPosition();
        float x = (float)(Mth.lerp(pPartialTicks, xo, this.x) - camPos.x());
        float y = (float)(Mth.lerp(pPartialTicks, yo, this.y) - camPos.y());
        float z = (float)(Mth.lerp(pPartialTicks, zo, this.z) - camPos.z());
        Quaternion rotation = new Quaternion(Vector3f.XP.rotationDegrees(90));
        float f3 = Mth.lerp(pPartialTicks, oRoll, roll);
        rotation.mul(Vector3f.ZP.rotation(f3));

        Vector3f[] positions = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float size = getQuadSize(pPartialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = positions[i];
            vector3f.transform(rotation);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(pPartialTicks);
        buffer.vertex(positions[0].x(), yOffset + positions[0].y(), positions[0].z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(positions[1].x(), yOffset + positions[1].y(), positions[1].z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(positions[2].x(), yOffset + positions[2].y(), positions[2].z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(positions[3].x(), yOffset + positions[3].y(), positions[3].z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SkaraParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
