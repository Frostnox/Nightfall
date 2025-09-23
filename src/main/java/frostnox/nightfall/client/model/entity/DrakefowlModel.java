package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.animal.DrakefowlEntity;
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

public class DrakefowlModel extends AnimatedModel<DrakefowlEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart neck;
    private final AnimatedModelPart head;
    private final AnimatedModelPart wingRight;
    private final AnimatedModelPart wingLeft;
    private final AnimatedModelPart legRight;
    private final AnimatedModelPart legLeft;
    private final List<AnimatedModelPart> noStunParts;

    public DrakefowlModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.neck = (AnimatedModelPart) body.getChild("neck");
        this.head = (AnimatedModelPart) neck.getChild("head");
        this.wingRight = (AnimatedModelPart) body.getChild("rightWing");
        this.wingLeft = (AnimatedModelPart) body.getChild("leftWing");
        this.legRight = (AnimatedModelPart) root.getChild("rightLeg");
        this.legLeft = (AnimatedModelPart) root.getChild("leftLeg");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.5F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(20, 13).addBox(0.0F, -5.5F, 2.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.5F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -3.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(9, 12).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.01F))
                .texOffs(19, 12).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 10).addBox(0.0F, 0.5F, -3.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 12).addBox(0.0F, -3.0F, -1.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 12).mirror().addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.5F, -1.5F, 0.5F, 0.0F, -0.2618F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 12).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -1.5F, 0.5F, 0.0F, 0.2618F, 0.0F));

        PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(-7, 20).addBox(0.0F, 0.0F, -1.0F, 4.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -2.0F, 0.0F, 0.0F, 1.309F));

        PartDefinition rightWing = body.addOrReplaceChild("rightWing", CubeListBuilder.create().texOffs(-7, 20).mirror().addBox(-4.0F, 0.0F, -1.0F, 4.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -2.5F, -2.0F, 0.0F, 0.0F, -1.309F));

        PartDefinition leftLeg = partdefinition.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(23, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 0).addBox(-1.0F, 4.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 20.0F, -0.5F));

        PartDefinition rightLeg = partdefinition.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(23, 0).mirror().addBox(-0.5F, -1.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(26, 0).mirror().addBox(-1.0F, 4.0F, -1.5F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 20.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(DrakefowlEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 4F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
//        limbSwing = time/3F;
//        limbSwingAmount = 0.65F;
        //Look
        if(headPitch > 45F) {
            head.xRot += MathUtil.toRadians(45);
            neck.xRot += MathUtil.toRadians(headPitch - 45);
        }
        else head.xRot += MathUtil.toRadians(headPitch);
        neck.yRot += MathUtil.toRadians(netHeadYaw);
        //Idle
        if(!entity.isDeadOrDying()) {
            float idleAmount = 1F;
            int hash = entity.getUUID().hashCode();
            float idle3 = (time + (hash >> 16 & 255)) % 210F;
            if(idle3 < 90F) {
                float p = Easing.inOutCubic.apply(idle3 < 4F ? (idle3 / 4F) : (idle3 >= 86F ? (1F - (idle3 - 86F) / 4F) : 1F));
                neck.yRot += MathUtil.toRadians(12) * p * idleAmount;
            }
            float idle4 = (time + (hash >>> 24 & 255)) % 210F;
            if(idle4 < 90F) {
                float p = Easing.inOutCubic.apply(idle4 < 4F ? (idle4 / 4F) : (idle4 >= 86F ? (1F - (idle4 - 86F) / 4F) : 1F));
                neck.yRot += MathUtil.toRadians(-12) * p * idleAmount;
            }
        }
        //Walk
        translateZ(neck, -1.25F, speed / 2, MathUtil.PI, 0, limbSwing, limbSwingAmount, Easing.inOutCubic, false);
        translateY(legLeft, -0.5F, speed, MathUtil.PI - MathUtil.PI/8F, -0.5F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateY(legRight, -0.5F, speed, -MathUtil.PI/8F, -0.5F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateZ(legLeft, 1.5F, speed, MathUtil.PI, -1F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        translateZ(legRight, 1.5F, speed, 0, -1F, limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(legLeft, 115, 1 * speed, MathUtil.PI, MathUtil.toRadians(-30), limbSwing, limbSwingAmount, Easing.inOutSine, false);
        rotateX(legRight, 115, 1 * speed, 0, MathUtil.toRadians(-30), limbSwing, limbSwingAmount, Easing.inOutSine, false);
        //Flap
        float flap = (Mth.sin(Mth.lerp(ClientEngine.get().getPartialTick(), entity.oFlap, entity.flap)) + 1.0F)
                * Mth.lerp(ClientEngine.get().getPartialTick(), entity.oFlapSpeed, entity.flapSpeed);
        wingRight.zRot += flap;
        wingLeft.zRot += -flap;
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
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, DrakefowlEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
