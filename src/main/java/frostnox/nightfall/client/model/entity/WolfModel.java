package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.animal.WolfEntity;
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

public class WolfModel extends AnimatedModel<WolfEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart hindRightLeg;
    private final AnimatedModelPart hindLeftLeg;
    private final AnimatedModelPart neck;
    private final AnimatedModelPart head;
    private final AnimatedModelPart rightEar;
    private final AnimatedModelPart leftEar;
    private final AnimatedModelPart tail;
    private final AnimatedModelPart frontRightLeg;
    private final AnimatedModelPart frontLeftLeg;
    private final List<AnimatedModelPart> noStunParts;

    public WolfModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.hindRightLeg = (AnimatedModelPart) root.getChild("hindRightLeg");
        this.hindLeftLeg = (AnimatedModelPart) root.getChild("hindLeftLeg");
        this.neck = (AnimatedModelPart) body.getChild("neck");
        this.head = (AnimatedModelPart) neck.getChild("head");
        this.rightEar = (AnimatedModelPart) head.getChild("rightEar");
        this.leftEar = (AnimatedModelPart) head.getChild("leftEar");
        this.tail = (AnimatedModelPart) body.getChild("tail");
        this.frontRightLeg = (AnimatedModelPart) root.getChild("frontRightLeg");
        this.frontLeftLeg = (AnimatedModelPart) root.getChild("frontLeftLeg");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-3.5F, -3.0F, -7.0F, 7.0F, 6.0F, 14.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-4.0F, -2.5F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.5F, -6.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(27, 23).mirror().addBox(-3.0F, -3.5F, -2.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.01F)).mirror(false)
                .texOffs(48, 26).mirror().addBox(-1.5F, -1.5F, -5.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, -1.0F, -1.5F));

        PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(45, 22).addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -2.5F, 0.5F));

        PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(45, 22).mirror().addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -2.5F, 0.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(44, 10).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.8727F, 0.0F, 0.0F));

        PartDefinition frontRightLeg = partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 17.0F, -6.0F));

        PartDefinition hindRightLeg = partdefinition.addOrReplaceChild("hindRightLeg", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 17.0F, 6.0F));

        PartDefinition frontLeftLeg = partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 17.0F, -6.0F));

        PartDefinition hindLeftLeg = partdefinition.addOrReplaceChild("hindLeftLeg", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 17.0F, 6.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(WolfEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 4F;
//        limbSwingAmount = 0.7F;
//        limbSwing = time / 1.5F;
        float runAmount = Mth.clamp((limbSwingAmount - 0.6F) / 0.1F, 0, 1);
        float walkAmount = Math.min(1F, limbSwingAmount * 2) * (1F - runAmount);
        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        head.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 440F;
            if(idle1 < 180F) {
                float p = Easing.inOutCubic.apply(idle1 < 5F ? (idle1 / 5F) : (idle1 >= 175F ? (1F - (idle1 - 175F) / 5F) : 1F));
                head.yRot += MathUtil.toRadians(7) * p;
            }
            float idle2 = (time + (hash >> 8 & 255)) % 440F;
            if(idle2 < 180F) {
                float p = Easing.inOutCubic.apply(idle2 < 5F ? (idle2 / 5F) : (idle2 >= 175F ? (1F - (idle2 - 175F) / 5F) : 1F));
                head.yRot += MathUtil.toRadians(-7) * p;
            }
            float idle3 = (time + (hash >> 16 & 255)) % 310F;
            if(idle3 < 90F) {
                float p = Easing.inOutCubic.apply(idle3 < 6F ? (idle3 / 6F) : (idle3 >= 84F ? (1F - (idle3 - 84F) / 6F) : 1F));
                rightEar.yRot += MathUtil.toRadians(15F) * p;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 310F;
            if(idle4 < 90F) {
                float p = Easing.inOutCubic.apply(idle4 < 6F ? (idle4 / 6F) : (idle4 >= 84F ? (1F - (idle4 - 84F) / 6F) : 1F));
                leftEar.yRot += MathUtil.toRadians(-15F) * p;
            }
        }
        //Walk
        if(walkAmount > 0) {
            rotateX(body, 2.5F, speed / 2, 0, 0, limbSwing, walkAmount, Easing.inOutSine, false);
            rotateX(neck, -2.5F, speed / 2, 0, 0, limbSwing, walkAmount, Easing.inOutSine, false);
            rotateX(frontRightLeg, 50, speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(frontLeftLeg, -50, speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindRightLeg, -50, speed, MathUtil.PI/12F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindLeftLeg, 50, speed, MathUtil.PI/12F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            translateY(frontRightLeg, 0.4F, speed, MathUtil.PI + MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(frontLeftLeg, 0.4F, speed, MathUtil.PI + -MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindLeftLeg, 0.4F, speed, MathUtil.PI + MathUtil.PI/2F + MathUtil.PI/12F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindRightLeg, 0.4F, speed, MathUtil.PI + -MathUtil.PI/2F + MathUtil.PI/12F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            rotateZ(tail, 10F, speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
        }
        //Run
        if(runAmount > 0) {
            rotateX(body, 5, speed, 0, 0, limbSwing, runAmount, Easing.inOutSine, true);
            rotateX(neck, -3, speed, 0, 0, limbSwing, runAmount, Easing.inOutSine, true);
            for(AnimatedModelPart part : new AnimatedModelPart[] {body, frontRightLeg, frontLeftLeg, hindRightLeg, hindLeftLeg}) {
                translateY(part, -1.25F, speed, MathUtil.PI * 7F/8F, 0, limbSwing, runAmount, Easing.inOutSine, false);
                part.y += 1F * runAmount;
            }
            translateY(head, 1F, speed, MathUtil.PI * 10/8F, 0, limbSwing, runAmount, Easing.inOutSine, false);
            tail.xRot += MathUtil.toRadians(5F) * runAmount;
            rotateX(tail, -14F, speed, MathUtil.PI * 0.4F, 0, limbSwing, runAmount, Easing.inOutSine, true);
            rotateZ(tail, 7F, speed * 2, 0, 0, limbSwing, runAmount, Easing.inOutSine, true);

            frontRightLeg.xRot += MathUtil.toRadians(-10F) * runAmount;
            rotateX(frontRightLeg, 57, speed, 0, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(frontRightLeg, -1.5F, speed, MathUtil.PI, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            frontLeftLeg.xRot += MathUtil.toRadians(-10F) * runAmount;
            rotateX(frontLeftLeg, 57, speed, -MathUtil.PI/7F, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(frontLeftLeg, -1.5F, speed, MathUtil.PI + MathUtil.PI/7F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            rotateX(hindRightLeg, -52, speed, -MathUtil.PI/8F, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(hindRightLeg, -1F, speed, MathUtil.PI/4F - MathUtil.PI/8F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            rotateX(hindLeftLeg, -52, speed, MathUtil.PI/7F - MathUtil.PI/8F, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(hindLeftLeg, -1F, speed, MathUtil.PI/7F + MathUtil.PI/4F - MathUtil.PI/8F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        hindRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        hindLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.LEG_RIGHT, frontRightLeg.animationData);
        map.put(EntityPart.LEG_LEFT, frontLeftLeg.animationData);
        map.put(EntityPart.LEG_2_RIGHT, hindRightLeg.animationData);
        map.put(EntityPart.LEG_2_LEFT, hindLeftLeg.animationData);
        map.put(EntityPart.NECK, neck.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.EAR_RIGHT, rightEar.animationData);
        map.put(EntityPart.EAR_LEFT, leftEar.animationData);
        map.put(EntityPart.BODY, body.animationData);
        map.put(EntityPart.TAIL, tail.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, WolfEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        super.animateStun(frame, duration, dir, mag, user, mCalc, mVec, partialTicks);
        AnimationUtil.stunPartToDefaultWithPause(head, head.animationData, frame, duration, partialTicks, -25F * mag, 1);
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
