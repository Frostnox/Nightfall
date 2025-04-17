package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.CockatriceEntity;
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

public class CockatriceModel extends AnimatedModel<CockatriceEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart neck;
    private final AnimatedModelPart head;
    private final AnimatedModelPart tail;
    private final AnimatedModelPart wingRight;
    private final AnimatedModelPart wingLeft;
    private final AnimatedModelPart legRight;
    private final AnimatedModelPart legLeft;
    private final List<AnimatedModelPart> noStunParts;

    public CockatriceModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.neck = (AnimatedModelPart) body.getChild("neck");
        this.head = (AnimatedModelPart) neck.getChild("head");
        this.tail = (AnimatedModelPart) body.getChild("tail");
        this.wingRight = (AnimatedModelPart) body.getChild("wing_right");
        this.wingLeft = (AnimatedModelPart) body.getChild("wing_left");
        this.legRight = (AnimatedModelPart) root.getChild("leg_right");
        this.legLeft = (AnimatedModelPart) root.getChild("leg_left");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 0).addBox(-3.5F, -4.5F, -5.5F, 7.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(52, 20).addBox(-1.5F, -9.0F, -1.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -5.0F, 0.6109F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -4.0F, -3.5F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(12, 0).addBox(-1.0F, -3.0F, -6.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(12, 3).addBox(0.0F, 0.0F, -4.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 15).addBox(0.0F, -6.0F, -2.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.6109F, 0.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 15).addBox(0.0F, -2.5F, -3.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -2.5F, 1.0F, 0.0F, -0.3229F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 15).addBox(0.0F, -2.5F, -3.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -2.5F, 1.0F, 0.0F, 0.3229F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 37).addBox(-1.0F, -0.5F, 7.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 5.5F));

        PartDefinition wing_right = body.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(-12, 50).addBox(-13.0F, 0.0F, -1.5F, 13.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -3.5F, -4.0F, 0.2618F, 0.0F, 0.8727F));

        PartDefinition wing_left = body.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(-12, 50).mirror().addBox(0.0F, 0.0F, -1.5F, 13.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.5F, -3.5F, -4.0F, 0.2618F, 0.0F, -0.8727F));

        PartDefinition leg_right = partdefinition.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(28, 27).addBox(-1.0F, 8.5F, -8.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offset(-3.5F, 14.5F, 3.0F));

        PartDefinition cube_r3 = leg_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, -0.6F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition leg_left = partdefinition.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(28, 27).mirror().addBox(-1.0F, 8.5F, -8.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(3.5F, 14.5F, 3.0F));

        PartDefinition cube_r4 = leg_left.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1.0F, -0.6F, -1.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(CockatriceEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 4.25F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        float trackAmount = AnimationUtil.applyEasing(Mth.lerp(ClientEngine.get().getPartialTick(), entity.targetTimeLast, entity.targetTime) / 9F, Easing.inOutSine);
        //trackAmount = 1F;
        //limbSwing = time/3F;
        //limbSwingAmount = 0.65F;
        //Look
        head.xRot += MathUtil.toRadians(headPitch);
        if(netHeadYaw > 60F || netHeadYaw < -60F) {
            float yaw = Mth.clamp(netHeadYaw, -60F, 60F);
            head.yRot += MathUtil.toRadians(netHeadYaw - yaw);
            neck.yRot += MathUtil.toRadians(yaw);
        }
        else neck.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle
        if(!entity.isDeadOrDying()) {
            float idleAmount = 1F - trackAmount;
            rotateX(tail, 4F, 30F, 0, 0, time, idleAmount, Easing.inOutSine, false);
            int hash = entity.getUUID().hashCode();
            float idle1 = (time + (hash & 255)) % 210F;
            if(idle1 < 3F * 2F) rotateX(wingRight, 5F, 3F, 0, 0, idle1, idleAmount, Easing.inOutSine, false);
            float idle2 = (time + (hash >> 8 & 255)) % 210F;
            if(idle2 < 3F * 2F) rotateX(wingLeft, 5F, 3F, 0, 0, idle2, idleAmount, Easing.inOutSine, false);
            float idle3 = (time + (hash >> 16 & 255)) % 320F;
            if(idle3 < 90F) {
                float p = Easing.inOutCubic.apply(idle3 < 4F ? (idle3 / 4F) : (idle3 >= 86F ? (1F - (idle3 - 86F) / 4F) : 1F));
                head.yRot += MathUtil.toRadians(10) * p * idleAmount;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 320F;
            if(idle4 < 90F) {
                float p = Easing.inOutCubic.apply(idle4 < 4F ? (idle4 / 4F) : (idle4 >= 86F ? (1F - (idle4 - 86F) / 4F) : 1F));
                head.yRot += MathUtil.toRadians(-10) * p * idleAmount;
            }
            translateY(neck, 0.1F, 30F, 0, 0, time, 1F, Easing.inOutSine, true);
            translateY(body, -0.1F, 30F, 0, 0, time, 1F, Easing.inOutSine, true);
        }
        //Hostile
        neck.xRot += MathUtil.toRadians(45) * trackAmount;
        head.z += -1F * trackAmount;
        head.xRot += MathUtil.toRadians(-45) * trackAmount;
        wingLeft.yRot += MathUtil.toRadians(-50) * trackAmount;
        wingLeft.zRot += MathUtil.toRadians(125) * trackAmount;
        wingRight.yRot += MathUtil.toRadians(50) * trackAmount;
        wingRight.zRot += MathUtil.toRadians(-125) * trackAmount;
        //Walk
        translateY(neck, 0.5F, speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, true);
        translateY(body, -0.5F, 1 * speed, 0, 0, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateY(legLeft, -1F, speed, MathUtil.PI - MathUtil.PI/8F, -0.5F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateY(legRight, -1F, speed, -MathUtil.PI/8F, -0.5F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateZ(legLeft, -1F, speed, MathUtil.PI, -1F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateZ(legRight, -1F, speed, 0, -1F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(legLeft, 125, 1 * speed, MathUtil.PI, MathUtil.toRadians(-25), limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(legRight, 125, 1 * speed, 0, MathUtil.toRadians(-25), limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateZ(wingLeft, 20, 1 * speed, MathUtil.PI, 0, limbSwing, limbSwingAmount * (1F - trackAmount), Easing.inOutSine, false);
        rotateZ(wingRight, 20, 1 * speed, 0, 0, limbSwing, limbSwingAmount * (1F - trackAmount), Easing.inOutSine, false);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.LEG_RIGHT, legRight.animationData);
        map.put(EntityPart.LEG_LEFT, legLeft.animationData);
        map.put(EntityPart.WING_RIGHT, wingRight.animationData);
        map.put(EntityPart.WING_LEFT, wingLeft.animationData);
        map.put(EntityPart.NECK, neck.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.BODY, body.animationData);
        map.put(EntityPart.TAIL, tail.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, CockatriceEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
