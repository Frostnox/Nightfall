package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.RockwormModel;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

public class RockwormRenderer extends AnimatedMobRenderer<RockwormEntity, RockwormModel> {
    public static final ResourceLocation ROCKWORM_0 = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/entity/rockworm/rockworm_0.png");

    public RockwormRenderer(EntityRendererProvider.Context renderer) {
        super(renderer, new RockwormModel(renderer.bakeLayer(ModelRegistryNF.ROCKWORM)), 0.3F);
    }

    @Override
    protected void setupRotations(RockwormEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if(this.isShaking(pEntityLiving)) {
            pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }
        Pose pose = pEntityLiving.getPose();
        if(pose != Pose.SLEEPING) pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        if(isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0.0D, pEntityLiving.getBbHeight() + 0.1F, 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(RockwormEntity pEntity) {
        return ROCKWORM_0;
    }
}
