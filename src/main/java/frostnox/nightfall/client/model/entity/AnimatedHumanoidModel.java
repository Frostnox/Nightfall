package frostnox.nightfall.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

public abstract class AnimatedHumanoidModel<T extends LivingEntity> extends AnimatedModel<T> implements ArmedModel, HeadedModel, IHumanoidModel {
    public AnimatedModelPart neck;
    public AnimatedModelPart head;
    public AnimatedModelPart body;
    public AnimatedModelPart rightArm;
    public AnimatedModelPart rightHand;
    public AnimatedModelPart leftArm;
    public AnimatedModelPart leftHand;
    public AnimatedModelPart rightLeg;
    public AnimatedModelPart leftLeg;
    private final EnumMap<EntityPart, AnimatedModelPart> partMap = new EnumMap<>(EntityPart.class);
    private final List<AnimatedModelPart> noStunParts;

    public AnimatedHumanoidModel(ModelPart model) {
        this(model, RenderType::entityCutoutNoCull);
    }
    
    public AnimatedHumanoidModel(ModelPart model, Function<ResourceLocation, RenderType> renderFunc) {
        super(model, renderFunc);
        this.body = (AnimatedModelPart) model.getChild("body");
        this.rightLeg = (AnimatedModelPart) model.getChild("right_leg");
        this.leftLeg = (AnimatedModelPart) model.getChild("left_leg");
        this.neck = (AnimatedModelPart) body.getChild("neck");
        this.head = (AnimatedModelPart) neck.getChild("head");
        this.rightArm = (AnimatedModelPart) body.getChild("right_arm");
        this.rightHand = (AnimatedModelPart) rightArm.getChild("right_hand");
        this.leftArm = (AnimatedModelPart) body.getChild("left_arm");
        this.leftHand = (AnimatedModelPart) leftArm.getChild("left_hand");
        partMap.put(EntityPart.NECK, neck);
        partMap.put(EntityPart.HEAD, head);
        partMap.put(EntityPart.BODY, body);
        partMap.put(EntityPart.ARM_RIGHT, rightArm);
        partMap.put(EntityPart.HAND_RIGHT, rightHand);
        partMap.put(EntityPart.ARM_LEFT, leftArm);
        partMap.put(EntityPart.HAND_LEFT, leftHand);
        partMap.put(EntityPart.LEG_RIGHT, rightLeg);
        partMap.put(EntityPart.LEG_LEFT, leftLeg);
        noStunParts = List.of(rightLeg, leftLeg);
    }

    protected Iterable<ModelPart> rootParts() {
        return ImmutableList.of(this.body, this.rightLeg, this.leftLeg);
    }

    public void setAllVisible(boolean visibility) {
        this.neck.visible = visibility;
        this.head.visible = visibility;
        this.body.visible = visibility;
        this.rightArm.visible = visibility;
        this.rightHand.visible = visibility;
        this.leftArm.visible = visibility;
        this.leftHand.visible = visibility;
        this.rightLeg.visible = visibility;
        this.leftLeg.visible = visibility;
    }

    @Override
    public void copyPropertiesTo(EntityModel<T> model) {
        model.attackTime = this.attackTime;
        model.riding = this.riding;
        model.young = this.young;
        if(model instanceof AnimatedHumanoidModel<T> modelA) {
            modelA.neck.copyFrom(neck);
            modelA.head.copyFrom(head);
            modelA.body.copyFrom(body);
            modelA.rightArm.copyFrom(rightArm);
            modelA.leftArm.copyFrom(leftArm);
            modelA.rightHand.copyFrom(rightHand);
            modelA.leftHand.copyFrom(leftHand);
            modelA.leftLeg.copyFrom(leftLeg);
            modelA.rightLeg.copyFrom(rightLeg);
        }
    }

    public void copyPropertiesTo(ArmorModel model) {
        model.attackTime = this.attackTime;
        model.riding = this.riding;
        model.young = this.young;

        model.head.copyFrom(head);
        model.neck.copyFrom(neck);
        model.bodyHeadJoint.copyFrom(body);
        model.body.copyFrom(body);
        model.innerBody.copyFrom(body);
        model.bodyRightArmJoint.copyFrom(body);
        model.rightArm.copyFrom(rightArm);
        model.bodyLeftArmJoint.copyFrom(body);
        model.leftArm.copyFrom(leftArm);
        model.rightHand.copyFrom(rightHand);
        model.leftHand.copyFrom(leftHand);
        model.rightLeg.copyFrom(rightLeg);
        model.rightLeg.z -= 0.001F;
        model.leftLeg.copyFrom(leftLeg);
        model.rightFoot.copyFrom(model.rightLeg);
        model.leftFoot.copyFrom(model.leftLeg);
        model.rightSkirt.copyFrom(model.rightLeg);
        model.leftSkirt.copyFrom(model.leftLeg);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.rootParts().forEach((part) -> {
            part.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        });
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack stack) {
        body.translateAndRotate(stack);
        this.getArm(side).translateAndRotate(stack);
        this.getHand(side).translateAndRotate(stack);
    }

    @Override
    public ModelPart getHead() {
        return head;
    }

    public ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? leftArm : rightArm;
    }

    public ModelPart getHand(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? leftHand : rightHand;
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HAND_RIGHT, rightHand.animationData);
        map.put(EntityPart.ARM_RIGHT, rightArm.animationData);
        map.put(EntityPart.HAND_LEFT, leftHand.animationData);
        map.put(EntityPart.ARM_LEFT, leftArm.animationData);
        map.put(EntityPart.LEG_RIGHT, rightLeg.animationData);
        map.put(EntityPart.LEG_LEFT, leftLeg.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.NECK, neck.animationData);
        map.put(EntityPart.BODY, body.animationData);
        return map;
    }

    @Override
    public ModelPart getModelPart(EntityPart part) {
        return partMap.get(part);
    }

    @Override
    protected void applyMatrixRotation(Vector3f rotations, PoseStack matrix) {
        super.applyMatrixRotation(rotations, matrix);
        head.xRot -= MathUtil.toRadians(rotations.x());
        head.yRot -= MathUtil.toRadians(rotations.y());
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, T user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        super.animateStun(frame, duration, dir, mag, user, mCalc, mVec, partialTicks);
        AnimationUtil.stunPartToDefaultWithPause(head, head.animationData, frame, duration, partialTicks, -12F * mag, 1);
        AnimationUtil.stunPartToDefaultWithPause(neck, neck.animationData, frame, duration, partialTicks, 0, 1);
        AnimationUtil.stunPartToDefaultWithPause(body, body.animationData, frame, duration, partialTicks, -12F * mag, 1);
    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return noStunParts;
    }
}
