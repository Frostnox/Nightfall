package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.entity.entity.monster.DregEntity;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

public class DregModel extends AnimatedHumanoidModel<DregEntity> {
    public DregModel(ModelPart model) {
        super(model);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.5F, -12.0F, -2.0F, 7.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.5F, 3.5F, 0.4363F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, -10.0F, 0.0F, -0.5236F, 0.0524F, 0.0524F));

        PartDefinition right_hand = right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(34, 16).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, -10.0F, 0.0F, -0.5236F, -0.0524F, -0.0524F));

        PartDefinition left_hand = left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(34, 16).mirror().addBox(-1.5F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.99F, 12.0F, 3.51F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(22, 16).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.99F, 12.0F, 3.51F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(DregEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        float degree = 1F;
        float slow = 5F;
        if(limbSwingAmount > 0.5F) limbSwingAmount = 0.5F;

        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        head.yRot += MathUtil.toRadians(netHeadYaw);

        int hash = entity.getUUID().hashCode();
        head.zRot += MathUtil.toRadians(hash % 4 * (hash % 2 == 0 ? 1 : -1));

        //Idle
        rotateY(body, 0.8F * degree, 1.3F, 0, 0, entity.tickCount, 1, Easing.none, true);
        rotateY(rightLeg, 0.8F * degree, 1.3F, 0, 0, entity.tickCount, 1, Easing.none, true);
        rotateY(leftLeg, 0.8F * degree, 1.3F, 0, 0, entity.tickCount, 1, Easing.none, true);
        //Walk
        translateY(body, 1.2F, 0.5F * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        translateY(rightLeg, 1.2F, 0.5F * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        translateY(leftLeg, 1.2F, 0.5F * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(rightLeg, 90 * degree, 1 * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(leftLeg, -90 * degree, 1 * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(rightArm, -40 * degree, 1 * slow, 0.125F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rightArm.zRot += Math.toRadians(10) * limbSwingAmount;
        rotateX(leftArm, 40 * degree, 1 * slow, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        leftArm.zRot += Math.toRadians(-10) * limbSwingAmount;
        /*rotateX(rightArm, -10 * degree, 1 * slow, 0, MathUtil.toRadians(-10F), limbSwing, 0.5F);
        translateY(rightArm, -2F, slow, 0, -2F, limbSwing, 0.5F);
        rotateX(leftArm, -10 * degree, 1 * slow, MathUtil.PI, MathUtil.toRadians(-10F), limbSwing, 0.5F);
        translateY(leftArm, -2F, slow, MathUtil.PI, -2F, limbSwing, 0.5F);
        translateZ(rightLeg, -2.3F, slow, 0, -2.3F, limbSwing, 0.5F);
        rotateY(rightLeg, -11 * degree, 1 * slow, MathUtil.PI/2F, MathUtil.toRadians(-11F), limbSwing, 0.5F);
        translateZ(leftLeg, -2.3F, slow, MathUtil.PI, -2.3F, limbSwing, 0.5F);
        rotateY(leftLeg, 11 * degree, 1 * slow, MathUtil.PI*1.5F, MathUtil.toRadians(11F), limbSwing, 0.5F);*/
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack stack) {
        super.translateToHand(side, stack);
        stack.translate(0, 0.625D, -0.125D);
    }
}
