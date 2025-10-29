package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.MerborBabyEntity;
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

public class MerborBabyModel extends AnimatedModel<MerborBabyEntity> implements HeadedModel {
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

    public MerborBabyModel(ModelPart root) {
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

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(6, 5).addBox(-2.0F, -2.0F, -3.5F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, -3.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -2.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(13, 0).addBox(-1.0F, -0.5F, -3.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(0, 7).addBox(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -1.0F, -0.5F));

        PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(0, 7).addBox(-1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -1.0F, -0.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(3, 7).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 3.5F, 0.2618F, 0.0F, 0.0F));

        PartDefinition frontRightLeg = partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(0, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 22.0F, -3.0F));

        PartDefinition frontLeftLeg = partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 22.0F, -3.0F));

        PartDefinition hindRightLeg = partdefinition.addOrReplaceChild("hindRightLeg", CubeListBuilder.create().texOffs(0, 12).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 22.0F, 3.0F));

        PartDefinition hindLeftLeg = partdefinition.addOrReplaceChild("hindLeftLeg", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 22.0F, 3.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    @Override
    public void setupAnim(MerborBabyEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 4.5F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        //limbSwing = time/2F;
        //limbSwingAmount = 1F;
        float sprintAmount = AnimationUtil.applyEasing(Mth.lerp(ClientEngine.get().getPartialTick(), 0, 0) / 9F, Easing.inOutSine);
        //sprintAmount = 1F;
        float walkAmount = 1F - sprintAmount;
        sprintAmount *= limbSwingAmount;
        walkAmount *= limbSwingAmount;
        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        if(netHeadYaw > 45F || netHeadYaw < -45F) {
            float yaw = Mth.clamp(netHeadYaw, -45F, 45F);
            head.yRot += MathUtil.toRadians(netHeadYaw - yaw);
            neck.yRot += MathUtil.toRadians(yaw);
        }
        else neck.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle
        if(!entity.isDeadOrDying() && walkAmount > 0F) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 220F;
            if(idle1 < 4F * 2F) rotateZ(tail, 7.5F, 4F, 0, 0, idle1 + 2F, walkAmount, Easing.inOutSine, true);
            float idle3 = (time + (hash >> 16 & 255)) % 310F;
            if(idle3 < 90F) {
                float p = Easing.inOutCubic.apply(idle3 < 4F ? (idle3 / 4F) : (idle3 >= 86F ? (1F - (idle3 - 86F) / 4F) : 1F));
                rightEar.yRot += MathUtil.toRadians(12.5F) * p * walkAmount;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 310F;
            if(idle4 < 90F) {
                float p = Easing.inOutCubic.apply(idle4 < 4F ? (idle4 / 4F) : (idle4 >= 86F ? (1F - (idle4 - 86F) / 4F) : 1F));
                leftEar.yRot += MathUtil.toRadians(-12.5F) * p * walkAmount;
            }
        }
        rotateX(tail, 7.5F, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        //Sprint
        if(sprintAmount > 0F) {
            speed = 3.5F;
            rotateX(body, 5, 1 * speed, 0, 0, limbSwing, sprintAmount, Easing.inOutSine, true);
            rotateX(head, -5, 1 * speed, 0, 0, limbSwing, sprintAmount, Easing.inOutSine, true);
            for(AnimatedModelPart part : new AnimatedModelPart[] {body, frontRightLeg, frontLeftLeg, hindRightLeg, hindLeftLeg}) {
                translateY(part, -1.5F, 1 * speed, MathUtil.PI, 0, limbSwing, sprintAmount, Easing.inOutSine, false);
            }
            frontRightLeg.xRot += MathUtil.toRadians(-10F) * sprintAmount;
            frontLeftLeg.xRot += MathUtil.toRadians(-10F) * sprintAmount;
            rotateX(frontRightLeg, 42, 1 * speed, 0, 0, limbSwing, sprintAmount, Easing.inOutCubic, true);
            rotateX(frontLeftLeg, 42, 1 * speed, MathUtil.PI/16F, 0, limbSwing, sprintAmount, Easing.inOutCubic, true);
            translateY(frontRightLeg, -0.75F, speed, MathUtil.PI/2F, 0F, limbSwing, sprintAmount, Easing.inOutSine, false);
            translateY(frontLeftLeg, -0.75F, speed, MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, sprintAmount, Easing.inOutSine, false);
            hindRightLeg.xRot += MathUtil.toRadians(10F) * sprintAmount;
            hindLeftLeg.xRot += MathUtil.toRadians(10F) * sprintAmount;
            rotateX(hindLeftLeg, -42, 1 * speed, 0, 0, limbSwing, sprintAmount, Easing.inOutCubic, true);
            rotateX(hindRightLeg, -42, 1 * speed, MathUtil.PI/16F, 0, limbSwing, sprintAmount, Easing.inOutCubic, true);
            translateY(hindLeftLeg, -1.5F, speed, -MathUtil.PI/2F, 0F, limbSwing, sprintAmount, Easing.inOutSine, false);
            translateY(hindRightLeg, -1.5F, speed, -MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, sprintAmount, Easing.inOutSine, false);
        }
        //Walk
        if(walkAmount > 0F) {
            speed = 4.5F;
            frontRightLeg.xRot += MathUtil.toRadians(-5F) * walkAmount;
            frontLeftLeg.xRot += MathUtil.toRadians(-5F) * walkAmount;
            hindRightLeg.xRot += MathUtil.toRadians(5F) * walkAmount;
            hindLeftLeg.xRot += MathUtil.toRadians(5F) * walkAmount;
            rotateX(frontRightLeg, 42, 1 * speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(frontLeftLeg, -42, 1 * speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindRightLeg, -42, 1 * speed, MathUtil.PI/16F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindLeftLeg, 42, 1 * speed, MathUtil.PI/16F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            translateY(frontRightLeg, -0.5F, speed, MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(frontLeftLeg, -0.5F, speed, -MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindLeftLeg, -0.5F, speed, MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindRightLeg, -0.5F, speed, -MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
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
    public void animateStun(int frame, int duration, int dir, float mag, MerborBabyEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
