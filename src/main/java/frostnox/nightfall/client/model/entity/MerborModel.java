package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

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
                .texOffs(45, 12).mirror().addBox(-2.0F, -1.5F, -10.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 3).addBox(2.0F, 0.5F, -9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 3).mirror().addBox(-4.0F, 0.5F, -9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tusks = head.addOrReplaceChild("tusks", CubeListBuilder.create().texOffs(15, 1).addBox(2.0F, -5.0F, -16.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(21, 1).addBox(2.0F, -6.0F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 1).mirror().addBox(-3.0F, -5.0F, -16.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(21, 1).mirror().addBox(-3.0F, -6.0F, -16.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 6.5F, 4.5F));

        PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.5F, -2.0F, 0.0F, -0.1745F, 0.0F));

        PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-2.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, -2.5F, -2.0F, 0.0F, 0.1745F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.5F, 8.0F, -0.9599F, 0.0F, 0.0F));

        PartDefinition frontRightLeg = partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 18.0F, -6.5F));

        PartDefinition hindRightLeg = partdefinition.addOrReplaceChild("hindRightLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 18.0F, 6.0F));

        PartDefinition hindLeftLeg = partdefinition.addOrReplaceChild("hindLeftLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 18.0F, 6.0F));

        PartDefinition frontLeftLeg = partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(1, 13).mirror().addBox(-2.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, 18.0F, -6.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(MerborEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        tusks.visible = entity.sex == Sex.MALE;
        float speed = 4F;
        float runAmount = Math.max(0, (limbSwingAmount - 0.6F) / 0.4F);
        float walkAmount = Math.min(1F, limbSwingAmount * 2) * (1F - runAmount);
        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        head.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 220F;
            if(idle1 < 6F * 4F) rotateZ(tail, 20F, 6F, 0, 0, idle1 + 3F, 1F - (idle1 / (6F * 4F)), Easing.inOutSine, true);
            float idle2 = (time + (hash >> 8 & 255));
            rotateZ(tail, 2F, 20F, 0, 0, idle2, 1, Easing.inOutSine, true);
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
            translateY(body, -0.25F, speed / 2, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            frontRightLeg.xRot += MathUtil.toRadians(-5F) * walkAmount;
            frontLeftLeg.xRot += MathUtil.toRadians(-5F) * walkAmount;
            hindRightLeg.xRot += MathUtil.toRadians(5F) * walkAmount;
            hindLeftLeg.xRot += MathUtil.toRadians(5F) * walkAmount;
            rotateX(tail, -9F, speed / 2, MathUtil.PI/6F, 0, limbSwing, walkAmount, Easing.inOutSine, false);
            rotateX(frontRightLeg, 46, 1 * speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(frontLeftLeg, -46, 1 * speed, 0, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindRightLeg, -46, 1 * speed, MathUtil.PI/16F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            rotateX(hindLeftLeg, 46, 1 * speed, MathUtil.PI/16F, 0, limbSwing, walkAmount, Easing.inOutSine, true);
            translateY(frontRightLeg, 0.25F, speed, MathUtil.PI + MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(frontLeftLeg, 0.25F, speed, MathUtil.PI + -MathUtil.PI/2F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindLeftLeg, 0.25F, speed, MathUtil.PI + MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
            translateY(hindRightLeg, 0.25F, speed, MathUtil.PI + -MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, walkAmount, Easing.inOutSine, false);
        }
        //Run
        if(runAmount > 0) {
            rotateX(body, 5, 1 * speed, 0, 0, limbSwing, runAmount, Easing.inOutSine, true);
            rotateX(head, -3, 1 * speed, 0, 0, limbSwing, runAmount, Easing.inOutSine, true);
            for(AnimatedModelPart part : new AnimatedModelPart[] {body, frontRightLeg, frontLeftLeg, hindRightLeg, hindLeftLeg}) {
                translateY(part, -1.7F, 1 * speed, MathUtil.PI, 0, limbSwing, runAmount, Easing.inOutSine, false);
            }
            frontRightLeg.xRot += MathUtil.toRadians(-5F) * runAmount;
            frontLeftLeg.xRot += MathUtil.toRadians(-5F) * runAmount;
            rotateX(frontRightLeg, 52, 1 * speed, 0, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            rotateX(frontLeftLeg, 52, 1 * speed, MathUtil.PI/16F, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(frontRightLeg, -1.5F, speed, MathUtil.PI/2F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            translateY(frontLeftLeg, -1.5F, speed, MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            hindRightLeg.xRot += MathUtil.toRadians(5F) * runAmount;
            hindLeftLeg.xRot += MathUtil.toRadians(5F) * runAmount;
            rotateX(hindLeftLeg, -52, 1 * speed, 0, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            rotateX(hindRightLeg, -52, 1 * speed, MathUtil.PI/16F, 0, limbSwing, runAmount, Easing.inOutCubic, true);
            translateY(hindLeftLeg, -1.5F, speed, -MathUtil.PI/2F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            translateY(hindRightLeg, -1.5F, speed, -MathUtil.PI/2F + MathUtil.PI/16F, 0F, limbSwing, runAmount, Easing.inOutSine, false);
            rotateX(tail, -18F, speed, MathUtil.PI * 0.4F, 0, limbSwing, runAmount, Easing.inOutSine, true);
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
        AnimationUtil.stunPartToDefaultWithPause(head, head.animationData, frame, duration, partialTicks, -12F * mag, 1);
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
