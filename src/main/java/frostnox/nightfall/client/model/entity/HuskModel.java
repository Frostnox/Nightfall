package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.entity.monster.HuskEntity;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

public class HuskModel extends AnimatedHumanoidModel<HuskEntity> {
    public HuskModel(ModelPart model) {
        super(model);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 2.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-6.0F, -10.0F, 0.0F));

        PartDefinition right_hand = right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0524F, 0.0524F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(6.0F, -10.0F, 0.0F));

        PartDefinition left_hand = left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, -0.0524F, -0.0524F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.09F, 0.0F, -2.09F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 2.1F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.91F, 0.0F, -2.09F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 2.1F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(HuskEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        float partialTicks = ClientEngine.get().getPartialTick();
        float degree = 1;
        float speed = 4.2F;
        if(limbSwingAmount > 0.5F) limbSwingAmount = 0.5F;

        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        head.yRot += MathUtil.toRadians(netHeadYaw);

        int hash = entity.getUUID().hashCode();
        head.zRot += MathUtil.toRadians(hash % 8 * (hash % 2 == 0 ? 1 : -1));

        //Idle
        if(!entity.isDeadOrDying()) {
            rotateX(rightHand, 2F, 50F, 0, 0, entity.tickCount + partialTicks, 1, Easing.inOutSine, true);
            rotateZ(rightHand, 2.5F, 50F, 0.5F, 0, entity.tickCount + partialTicks, 1, Easing.inOutSine, true);
            rotateX(leftHand, 2F, 50F, 0.75F, 0, entity.tickCount + partialTicks, 1, Easing.inOutSine, true);
            rotateZ(leftHand, -2.5F, 50F, 0.6F, 0, entity.tickCount + partialTicks, 1, Easing.inOutSine, true);
        }

        //Walk
        translateY(body, 1.0F, 0.5F * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        translateY(rightLeg, 1.0F, 0.5F * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        translateY(leftLeg, 1.0F, 0.5F * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateZ(body, 3F * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        //rotateX(body, 6F * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount);
        //rotateX(head, -3.5F * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount);
        rotateX(head, -2 * degree, 1 * speed, 0, MathUtil.toRadians(-2), limbSwing, limbSwingAmount, Easing.inOutSine, true);

        rotateX(rightLeg, 90 * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(leftLeg, -90 * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);

        rotateX(rightArm, -30 * degree, 1 * speed, 0.2F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rightArm.zRot += Math.toRadians(10) * limbSwingAmount;
        rotateX(leftArm, 33 * degree, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        //rightArm.xRot += MathUtil.toRadians(-30);
        //leftArm.xRot += MathUtil.toRadians(-30);
        leftArm.zRot += Math.toRadians(-10) * limbSwingAmount;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack stack) {
        super.translateToHand(side, stack);
        stack.translate(0, 0.625D, -0.125D);
    }
}
