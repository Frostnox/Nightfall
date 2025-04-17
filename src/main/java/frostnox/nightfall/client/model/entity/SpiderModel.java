package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.SpiderEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.List;

public class SpiderModel extends AnimatedModel<SpiderEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart head;
    private final AnimatedModelPart rightLeg1, rightLeg2, rightLeg3, rightLeg4;
    private final AnimatedModelPart leftLeg1, leftLeg2, leftLeg3, leftLeg4;
    private final List<AnimatedModelPart> noStunParts;

    public SpiderModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.head = (AnimatedModelPart) root.getChild("head");
        this.rightLeg1 = (AnimatedModelPart) root.getChild("rightJoint1");
        this.rightLeg2 = (AnimatedModelPart) root.getChild("rightJoint2");
        this.rightLeg3 = (AnimatedModelPart) root.getChild("rightJoint3");
        this.rightLeg4 = (AnimatedModelPart) root.getChild("rightJoint4");
        this.leftLeg1 = (AnimatedModelPart) root.getChild("leftJoint1");
        this.leftLeg2 = (AnimatedModelPart) root.getChild("leftJoint2");
        this.leftLeg3 = (AnimatedModelPart) root.getChild("leftJoint3");
        this.leftLeg4 = (AnimatedModelPart) root.getChild("leftJoint4");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 7).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.5F, -2.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.5F, -4.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.5F, -2.0F));

        PartDefinition rightJoint1 = partdefinition.addOrReplaceChild("rightJoint1", CubeListBuilder.create(), PartPose.offset(-3.0F, 21.5F, -1.5F));

        PartDefinition rightLeg1 = rightJoint1.addOrReplaceChild("rightLeg1", CubeListBuilder.create().texOffs(0, 20).addBox(-7.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, -0.9599F, -0.5236F));

        PartDefinition rightJoint2 = partdefinition.addOrReplaceChild("rightJoint2", CubeListBuilder.create(), PartPose.offset(-3.0F, 21.5F, -0.5F));

        PartDefinition rightLeg2 = rightJoint2.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(0, 20).addBox(-7.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3491F, -0.3491F));

        PartDefinition rightJoint3 = partdefinition.addOrReplaceChild("rightJoint3", CubeListBuilder.create(), PartPose.offset(-3.0F, 21.5F, 0.5F));

        PartDefinition rightLeg3 = rightJoint3.addOrReplaceChild("rightLeg3", CubeListBuilder.create().texOffs(0, 20).addBox(-7.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3491F, -0.3491F));

        PartDefinition rightJoint4 = partdefinition.addOrReplaceChild("rightJoint4", CubeListBuilder.create(), PartPose.offset(-3.0F, 21.5F, 1.5F));

        PartDefinition rightLeg4 = rightJoint4.addOrReplaceChild("rightLeg4", CubeListBuilder.create().texOffs(0, 20).addBox(-7.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.9599F, -0.5236F));

        PartDefinition leftJoint1 = partdefinition.addOrReplaceChild("leftJoint1", CubeListBuilder.create(), PartPose.offset(3.0F, 21.5F, -1.5F));

        PartDefinition leftLeg1 = leftJoint1.addOrReplaceChild("leftLeg1", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-0.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.9599F, 0.5236F));

        PartDefinition leftJoint2 = partdefinition.addOrReplaceChild("leftJoint2", CubeListBuilder.create(), PartPose.offset(3.0F, 21.5F, -0.5F));

        PartDefinition leftLeg2 = leftJoint2.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-0.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3491F, 0.3491F));

        PartDefinition leftJoint3 = partdefinition.addOrReplaceChild("leftJoint3", CubeListBuilder.create(), PartPose.offset(3.0F, 21.5F, 0.5F));

        PartDefinition leftLeg3 = leftJoint3.addOrReplaceChild("leftLeg3", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-0.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3491F, 0.3491F));

        PartDefinition leftJoint4 = partdefinition.addOrReplaceChild("leftJoint4", CubeListBuilder.create(), PartPose.offset(3.0F, 21.5F, 1.5F));

        PartDefinition leftLeg4 = leftJoint4.addOrReplaceChild("leftLeg4", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(-0.5F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, -0.9599F, 0.5236F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(SpiderEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 3.5F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        float trackAmount = AnimationUtil.applyEasing(Mth.lerp(ClientEngine.get().getPartialTick(), entity.targetTimeLast, entity.targetTime) / 9F, Easing.inOutSine);
        //limbSwing = time;
        //limbSwingAmount = 1;
        //Idle
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash >> 16 & 255)) % 310F;
            if(idle1 < 90F) {
                float p = Easing.inOutCubic.apply(idle1 < 4F ? (idle1 / 4F) : (idle1 >= 86F ? (1F - (idle1 - 86F) / 4F) : 1F));
                head.yRot += MathUtil.toRadians(5) * p;
            }
            float idle2 = (time + (hash >>> 24 & 255)) % 310F;
            if(idle2 < 90F) {
                float p = Easing.inOutCubic.apply(idle2 < 4F ? (idle2 / 4F) : (idle2 >= 86F ? (1F - (idle2 - 86F) / 4F) : 1F));
                head.yRot += MathUtil.toRadians(-5) * p;
            }
        }
        //Hostile
        body.xRot += MathUtil.toRadians(12) * trackAmount;
        head.z += 0.4F * trackAmount;
        //Walking
        rotateX(rightLeg1, -12, speed / 2, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(rightLeg2, -12, speed / 2, MathUtil.PI * 0.9F * 2, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(rightLeg3, 12, speed / 2, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(rightLeg4, 12, speed / 2, MathUtil.PI * 0.9F * 2, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(leftLeg1, -12, speed / 2, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(leftLeg2, -12, speed / 2, MathUtil.PI * 0.9F * 2, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(leftLeg3, 12, speed / 2, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(leftLeg4, 12, speed / 2, MathUtil.PI * 0.9F * 2, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateY(rightLeg1, 35, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(rightLeg2, 20, speed, MathUtil.PI * 0.9F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(rightLeg3, 20, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(rightLeg4, 35, speed, MathUtil.PI * 0.9F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(leftLeg1, 35, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(leftLeg2, 20, speed, MathUtil.PI * 0.9F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(leftLeg3, 20, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateY(leftLeg4, 35, speed, MathUtil.PI * 0.9F, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.LEG_RIGHT, rightLeg1.animationData);
        map.put(EntityPart.LEG_LEFT, leftLeg1.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.BODY, body.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, SpiderEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
