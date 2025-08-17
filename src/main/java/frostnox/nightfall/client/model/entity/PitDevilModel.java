package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.PitDevilEntity;
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

public class PitDevilModel extends AnimatedModel<PitDevilEntity> implements HeadedModel {
    private final AnimatedModelPart hindRightLeg;
    private final AnimatedModelPart hindLeftLeg;
    private final AnimatedModelPart body;
    private final AnimatedModelPart neck;
    private final AnimatedModelPart head;
    private final AnimatedModelPart rightEar;
    private final AnimatedModelPart leftEar;
    private final AnimatedModelPart tail;
    private final AnimatedModelPart frontRightLeg;
    private final AnimatedModelPart frontLeftLeg;
    private final List<AnimatedModelPart> noStunParts;

    public PitDevilModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.neck = (AnimatedModelPart) this.body.getChild("neck");
        this.head = (AnimatedModelPart) this.neck.getChild("head");
        this.leftEar = (AnimatedModelPart) this.head.getChild("leftEar");
        this.rightEar = (AnimatedModelPart) this.head.getChild("rightEar");
        this.tail = (AnimatedModelPart) this.body.getChild("tail");
        this.frontLeftLeg = (AnimatedModelPart) root.getChild("frontLeftLeg");
        this.frontRightLeg = (AnimatedModelPart) root.getChild("frontRightLeg");
        this.hindRightLeg = (AnimatedModelPart) root.getChild("hindRightLeg");
        this.hindLeftLeg = (AnimatedModelPart) root.getChild("hindLeftLeg");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -5.5F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 21).addBox(-3.0F, -3.0F, -4.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 27).addBox(-2.0F, -1.0F, -6.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftEar = head.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(19, 21).addBox(0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -2.0F, -0.5F));

        PartDefinition rightEar = head.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(19, 21).mirror().addBox(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -2.0F, -0.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 0).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 6.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition frontLeftLeg = partdefinition.addOrReplaceChild("frontLeftLeg", CubeListBuilder.create().texOffs(42, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 19.0F, -5.0F));

        PartDefinition frontRightLeg = partdefinition.addOrReplaceChild("frontRightLeg", CubeListBuilder.create().texOffs(42, 12).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 19.0F, -5.0F));

        PartDefinition hindRightLeg = partdefinition.addOrReplaceChild("hindRightLeg", CubeListBuilder.create().texOffs(42, 12).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 19.0F, 5.0F));

        PartDefinition hindLeftLeg = partdefinition.addOrReplaceChild("hindLeftLeg", CubeListBuilder.create().texOffs(42, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 19.0F, 5.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(PitDevilEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 3.1F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
//        limbSwing = time;
//        limbSwingAmount = 1;
        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        head.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle twitches
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 220F;
            if(idle1 < 2F * 2F) rotateX(rightEar, 5F, 2F, 0, 0, idle1, 1, Easing.inOutSine, false);
            float idle2 = (time + (hash >> 8 & 255)) % 220F;
            if(idle2 < 2F * 2F) rotateX(leftEar, 5F, 2F, 0, 0, idle2, 1, Easing.inOutSine, false);
            float idle3 = (time + (hash >> 16 & 255)) % 310F;
            if(idle3 < 90F) {
                float p = Easing.inOutSine.apply(idle3 < 6F ? (idle3 / 6F) : (idle3 >= 84F ? (1F - (idle3 - 84F) / 6F) : 1F));
                rightEar.yRot += MathUtil.toRadians(10) * p;
                head.yRot += MathUtil.toRadians(10) * p;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 310F;
            if(idle4 < 90F) {
                float p = Easing.inOutSine.apply(idle4 < 6F ? (idle4 / 6F) : (idle4 >= 84F ? (1F - (idle4 - 84F) / 6F) : 1F));
                leftEar.yRot += MathUtil.toRadians(-10) * p;
                head.yRot += MathUtil.toRadians(-10) * p;
            }
        }
        //Walk
        rotateX(body, 2, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        for(AnimatedModelPart part : new AnimatedModelPart[] {body, frontRightLeg, frontLeftLeg, hindRightLeg, hindLeftLeg}) {
            translateY(part, -0.5F, 1 * speed, MathUtil.PI/2, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        }
        tail.xRot += MathUtil.toRadians(20F) * limbSwingAmount;
        rotateX(tail, 5, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        frontRightLeg.xRot += MathUtil.toRadians(-10F) * limbSwingAmount;
        hindRightLeg.xRot += MathUtil.toRadians(10F) * limbSwingAmount;
        frontLeftLeg.xRot += MathUtil.toRadians(-10F) * limbSwingAmount;
        hindLeftLeg.xRot += MathUtil.toRadians(10F) * limbSwingAmount;
        rotateX(frontRightLeg, 70, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(hindRightLeg, 70, 1 * speed, MathUtil.PI * 7 / 8, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(frontLeftLeg, 70, 1 * speed, MathUtil.PI * 15 / 16, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        rotateX(hindLeftLeg, 70, 1 * speed, MathUtil.PI * 15 / 16 + MathUtil.PI * 7 / 8, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frontRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
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
    public void animateStun(int frame, int duration, int dir, float mag, PitDevilEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
