package frostnox.nightfall.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.world.Weather;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class FallingParticle extends TextureSheetParticle {
    protected final BlockPos.MutableBlockPos mutablePos;

    public FallingParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        mutablePos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        pickSprite(sprite);
        gravity = 1F;
        xd = 0F;
        zd = 0F;
        yd = -gravity;
        alpha = 0F;
    }

    protected void onLand() {

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        age++;
        alpha = Math.min(1F, age / 7F);
        xo = x;
        yo = y;
        zo = z;
        yd -= 0.04D * gravity;
        move(xd, yd, zd);
        xd *= friction;
        yd *= friction;
        zd *= friction;
        if(onGround) {
            onLand();
            remove();
        }
        else {
            Entity camEntity = Minecraft.getInstance().cameraEntity;
            if(camEntity != null && camEntity.getEyePosition().distanceToSqr(x, y, z) > Weather.PARTICLE_DESPAWN_RADIUS_SQR) remove();
        }
    }

    @Override
    public void move(double x, double y, double z) {
        double oldY = y;
        Vec3 col = Entity.collideBoundingBox(null, new Vec3(x, y, z), getBoundingBox(), level, List.of());
        x = col.x;
        y = col.y;
        z = col.z;
        if(y != 0.0D) {
            setBoundingBox(getBoundingBox().move(x, y, z));
            setLocationFromBoundingbox();
            if(!level.getFluidState(mutablePos.set(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z))).isEmpty()) {
                onGround = true;
                return;
            }
        }
        onGround = oldY != y;
    }

    @Override
    public void render(VertexConsumer buffer, Camera cam, float partial) {
        Vec3 camPos = cam.getPosition();
        float dX = (float)(Mth.lerp(partial, xo, x) - camPos.x());
        float dY = (float)(Mth.lerp(partial, yo, y) - camPos.y());
        float dZ = (float)(Mth.lerp(partial, zo, z) - camPos.z());
        Quaternion quaternion = new Quaternion(cam.rotation());
        quaternion.mul(Vector3f.XP.rotationDegrees(-cam.getXRot()));
        quaternion.mul(Vector3f.YP.rotation(MathUtil.toRadians(cam.getYRot()) + (float) Math.atan2(dX, dZ)));
        if(roll != 0F || oRoll != 0F) quaternion.mul(Vector3f.ZP.rotation(Mth.lerp(partial, oRoll, roll)));

        Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float quadSize = getQuadSize(partial);

        for(int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.transform(quaternion);
            vertex.mul(quadSize);
            vertex.add(dX, dY, dZ);
        }

        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partial);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FallingParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
