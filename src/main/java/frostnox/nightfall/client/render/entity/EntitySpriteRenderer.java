package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

public abstract class EntitySpriteRenderer<T extends Entity> extends EntityRenderer<T> {
    protected final float halfSize;

    protected EntitySpriteRenderer(EntityRendererProvider.Context pContext, float shadowRadius, float shadowStrength) {
        this(pContext, 1F, shadowRadius, shadowStrength);
    }
    
    protected EntitySpriteRenderer(EntityRendererProvider.Context pContext, float size, float shadowRadius, float shadowStrength) {
        super(pContext);
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        halfSize = size / 2F;
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
        stack.pushPose();
        Quaternion quat = entityRenderDispatcher.cameraOrientation();
        quat.mul(Vector3f.YP.rotationDegrees(180F));
        //Rotate vertices directly to avoid changing lighting
        Vector3f[] vertices = new Vector3f[]{new Vector3f(-halfSize, -halfSize, 0.0F), new Vector3f(halfSize, -halfSize, 0.0F), new Vector3f(halfSize, halfSize, 0.0F), new Vector3f(-halfSize, halfSize, 0.0F)};
        for(int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.transform(quat);
        }
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.itemEntityTranslucentCull(getTextureLocation(pEntity)));
        PoseStack.Pose pose = stack.last();
        Matrix4f mat = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(buffer, mat, normal, vertices[0], 0, 1, light);
        vertex(buffer, mat, normal, vertices[1], 1, 1, light);
        vertex(buffer, mat, normal, vertices[2], 1, 0, light);
        vertex(buffer, mat, normal, vertices[3], 0, 0, light);
        stack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, stack, bufferSource, light);
    }

    private static void vertex(VertexConsumer pBuffer, Matrix4f pMatrix, Matrix3f pMatrixNormal, Vector3f vertex, float pTexU, float pTexV, int pPackedLight) {
        pBuffer.vertex(pMatrix, vertex.x(), vertex.y(), vertex.z()).color(255, 255, 255, 255).uv(pTexU, pTexV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(pMatrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
