package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.TrollEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.EnumMap;
import java.util.List;

public class TrollModel extends AnimatedModel<TrollEntity> implements HeadedModel {
    private final AnimatedModelPart hindBody;
    private final AnimatedModelPart hindRightLeg;
    private final AnimatedModelPart hindLeftLeg;
    private final AnimatedModelPart frontBody;
    private final AnimatedModelPart head;
    private final AnimatedModelPart rightEar;
    private final AnimatedModelPart leftEar;
    private final AnimatedModelPart antlers;
    private final AnimatedModelPart frontRightLeg;
    private final AnimatedModelPart frontLeftLeg;
    private final List<AnimatedModelPart> noStunParts;

    public TrollModel(ModelPart root) {
        super(root);
        this.hindBody = (AnimatedModelPart) root.getChild("hind_body");
        this.hindRightLeg = (AnimatedModelPart) hindBody.getChild("hind_right_leg");
        this.hindLeftLeg = (AnimatedModelPart) hindBody.getChild("hind_left_leg");
        this.frontBody = (AnimatedModelPart) root.getChild("front_body");
        this.head = (AnimatedModelPart) frontBody.getChild("head");
        this.rightEar = (AnimatedModelPart) head.getChild("right_ear");
        this.leftEar = (AnimatedModelPart) head.getChild("left_ear");
        this.antlers = (AnimatedModelPart) head.getChild("antlers");
        this.frontRightLeg = (AnimatedModelPart) frontBody.getChild("front_right_leg");
        this.frontLeftLeg = (AnimatedModelPart) frontBody.getChild("front_left_leg");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition hind_body = partdefinition.addOrReplaceChild("hind_body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -4.5F, 0.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(12, 1).addBox(-1.0F, -3.5F, 4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition hind_right_leg = hind_body.addOrReplaceChild("hind_right_leg", CubeListBuilder.create().texOffs(15, 15).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.5F, 2.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition hind_left_leg = hind_body.addOrReplaceChild("hind_left_leg", CubeListBuilder.create().texOffs(15, 15).mirror().addBox(-1.0F, -1.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.5F, 2.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition front_body = partdefinition.addOrReplaceChild("front_body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.5F, -4.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = front_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(13, 5).addBox(-1.5F, -2.0F, -2.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 12).addBox(-1.5F, -1.0F, -3.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -3.5F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(10, 16).addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, -0.5F, 0.0F, 1.1345F, 0.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(10, 16).mirror().addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0F, -2.0F, -0.5F, 0.0F, -1.1345F, 0.0F));

        PartDefinition antlers = head.addOrReplaceChild("antlers", CubeListBuilder.create().texOffs(22, 0).addBox(0.5F, -5.0F, -1.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(22, 0).mirror().addBox(-3.5F, -5.0F, -1.5F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition front_right_leg = front_body.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(0, 8).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -0.5F, -3.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition front_left_leg = front_body.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, -0.5F, -3.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(TrollEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 3.3F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        //limbSwing = time;
        //limbSwingAmount = 1;
        //Idle twitches
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 220F;
            if(idle1 < 2F * 2F) rotateX(rightEar, 5F, 2F, 0, 0, idle1, 1, Easing.inOutSine, false);
            float idle2 = (time + (hash >> 8 & 255)) % 220F;
            if(idle2 < 2F * 2F) rotateX(leftEar, 5F, 2F, 0, 0, idle2, 1, Easing.inOutSine, false);
            float idle3 = (time + (hash >> 16 & 255)) % 310F;
            if(idle3 < 90F) {
                float p = Easing.inOutCubic.apply(idle3 < 4F ? (idle3 / 4F) : (idle3 >= 86F ? (1F - (idle3 - 86F) / 4F) : 1F));
                rightEar.yRot += MathUtil.toRadians(10) * p;
                head.yRot += MathUtil.toRadians(10) * p;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 310F;
            if(idle4 < 90F) {
                float p = Easing.inOutCubic.apply(idle4 < 4F ? (idle4 / 4F) : (idle4 >= 86F ? (1F - (idle4 - 86F) / 4F) : 1F));
                leftEar.yRot += MathUtil.toRadians(-10) * p;
                head.yRot += MathUtil.toRadians(-10) * p;
            }
        }
        //Adjust resting position
        frontRightLeg.z += 0.5F * (1F - limbSwingAmount);
        frontLeftLeg.z += 0.5F * (1F - limbSwingAmount);
        //Hopping
        frontBody.y += -2.5F * limbSwingAmount;
        hindBody.y += -2.5F * limbSwingAmount;
        frontRightLeg.xRot += MathUtil.toRadians(20F) * limbSwingAmount;
        frontLeftLeg.xRot += MathUtil.toRadians(20F) * limbSwingAmount;
        hindRightLeg.xRot += MathUtil.toRadians(30F) * limbSwingAmount;
        hindLeftLeg.xRot += MathUtil.toRadians(30F) * limbSwingAmount;
        translateY(frontBody, -1F, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutCubic, false);
        translateY(hindBody, -1F, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutCubic, false);
        translateZ(frontBody, 0.5F, 1 * speed, MathUtil.PI, 0, limbSwing, limbSwingAmount, Easing.inOutCubic, false);
        translateZ(hindBody, -0.5F, 1 * speed, MathUtil.PI, 0, limbSwing, limbSwingAmount, Easing.inOutCubic, false);
        rotateX(frontRightLeg, 120, 1 * speed, MathUtil.PI + MathUtil.PI/9F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(frontLeftLeg, 120, 1 * speed, MathUtil.PI, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(hindRightLeg, 110, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(hindLeftLeg, 110, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        hindBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.LEG_RIGHT, frontRightLeg.animationData);
        map.put(EntityPart.LEG_LEFT, frontLeftLeg.animationData);
        map.put(EntityPart.LEG_2_RIGHT, hindRightLeg.animationData);
        map.put(EntityPart.LEG_2_LEFT, hindLeftLeg.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.EAR_RIGHT, rightEar.animationData);
        map.put(EntityPart.EAR_LEFT, leftEar.animationData);
        map.put(EntityPart.BODY, frontBody.animationData);
        map.put(EntityPart.BODY_2, hindBody.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, TrollEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        super.animateStun(frame, duration, dir, mag, user, mCalc, mVec, partialTicks);
        AnimationUtil.stunPartToDefaultWithPause(head, head.animationData, frame, duration, partialTicks, -20F * mag, 1);
    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return noStunParts;
    }

    @Override
    public ModelPart getHead() {
        return head;
    }
}
