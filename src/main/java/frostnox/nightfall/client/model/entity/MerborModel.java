package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.entity.animal.MerborEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.world.Season;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.List;

public class MerborModel extends AnimatedModel<MerborEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart hindRightLeg;
    private final AnimatedModelPart hindLeftLeg;
    private final AnimatedModelPart neck;
    private final AnimatedModelPart head;
    private final AnimatedModelPart tusks;
    private final AnimatedModelPart rightEar;
    private final AnimatedModelPart leftEar;
    private final AnimatedModelPart tail;
    private final AnimatedModelPart frontRightLeg;
    private final AnimatedModelPart frontLeftLeg;
    private final List<AnimatedModelPart> noStunParts;

    public MerborModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.hindRightLeg = (AnimatedModelPart) root.getChild("hindRightLeg");
        this.hindLeftLeg = (AnimatedModelPart) root.getChild("hindLeftLeg");
        this.neck = (AnimatedModelPart) body.getChild("neck");
        this.head = (AnimatedModelPart) neck.getChild("head");
        this.tusks = (AnimatedModelPart) head.getChild("tusks");
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

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-5.0F, -4.5F, -8.0F, 10.0F, 9.0F, 16.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.0F, 13.5F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -7.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(37, 0).mirror().addBox(-4.0F, -3.5F, -5.0F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(45, 12).mirror().addBox(-2.0F, -1.5F, -10.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tusks = head.addOrReplaceChild("tusks", CubeListBuilder.create().texOffs(15, 1).addBox(2.0F, -5.0F, -16.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(21, 1).addBox(2.0F, -6.0F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 1).mirror().addBox(-3.0F, -5.0F, -16.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(21, 1).mirror().addBox(-3.0F, -6.0F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 6.5F, 4.5F));

        PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.5F, -2.0F, 0.0F, -0.1745F, 0.0F));

        PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-2.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -2.5F, -2.0F, 0.0F, 0.1745F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 8.0F, -0.9599F, 0.0F, 0.0F));

        PartDefinition frontLeftLeg = partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 18.0F, -6.5F));

        PartDefinition hindLeftLeg = partdefinition.addOrReplaceChild("hindLeftLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 18.0F, 6.0F));

        PartDefinition hindRightLeg = partdefinition.addOrReplaceChild("hindRightLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 18.0F, 6.0F));

        PartDefinition frontRightLeg = partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-2.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, 18.0F, -6.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(MerborEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        tusks.visible = entity.sex == Sex.MALE;
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
    public void animateStun(int frame, int duration, int dir, float mag, MerborEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
