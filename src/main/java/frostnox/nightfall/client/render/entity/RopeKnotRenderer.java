package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.entity.entity.RopeKnotEntity;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RopeKnotRenderer extends EntityRenderer<RopeKnotEntity> {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rope_knot/rope_knot.png");
   private final LeashKnotModel<RopeKnotEntity> model;

   public RopeKnotRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      model = new LeashKnotModel<>(pContext.bakeLayer(ModelLayers.LEASH_KNOT));
   }

   @Override
   public void render(RopeKnotEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
      pMatrixStack.pushPose();
      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      model.setupAnim(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(model.renderType(TEXTURE));
      model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pMatrixStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   @Override
   public ResourceLocation getTextureLocation(RopeKnotEntity pEntity) {
      return TEXTURE;
   }
}