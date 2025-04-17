package frostnox.nightfall.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AnimatedModel<T extends Entity> extends EntityModel<T> {
    public final ImmutableList<AnimatedModelPart> parts;

    public AnimatedModel(ModelPart model) {
        this(model, RenderType::entityCutoutNoCull);
    }

    public AnimatedModel(ModelPart root, Function<ResourceLocation, RenderType> renderFunc) {
        super(renderFunc);
        convertChildren(root);
        List<ModelPart> originalParts = root.getAllParts().collect(Collectors.toList());
        originalParts.remove(0); //Discard root
        ImmutableList.Builder<AnimatedModelPart> builder = ImmutableList.builder();
        for(ModelPart part : originalParts) builder.add((AnimatedModelPart) part);
        parts = builder.build();
        setDefaultPose();
    }

    private static void convertChildren(ModelPart part) {
        for(Map.Entry<String, ModelPart> entry : part.children.entrySet()) {
            convertChildren(entry.getValue());
            part.children.put(entry.getKey(), new AnimatedModelPart(entry.getValue()));
        }
    }

    public void setDefaultPose() {
        for(AnimatedModelPart part : parts) part.setDefaultPose();
    }

    public void resetPose() {
        for(AnimatedModelPart part : parts) part.resetPose();
    }

    public void readDataFromModel() {
        for(AnimatedModelPart part : parts) part.readAnimation();
    }

    public void writeDataToModel() {
        for(AnimatedModelPart part : parts) part.writeAnimation();
    }

    public void updateData(int frame, int length, float partialTicks, Easing easing) {
        for(AnimatedModelPart part : parts) part.animationData.update(frame, length, partialTicks, easing);
    }

    /**
     * @return AnimationData from model (not copied)
     */
    public abstract EnumMap<EntityPart, AnimationData> getDataFromModel();

    public void doCombatAnimations(T entity, PoseStack matrix) {
        if(!(entity instanceof LivingEntity livingEntity)) return;
        float partialTicks = ClientEngine.get().getPartialTick();
        if(entity instanceof ActionableEntity animEntity) {
            IActionTracker capA = animEntity.getActionTracker();
            if(!capA.isInactive()) {
                partialTicks = capA.modifyPartialTick(partialTicks);
                Action action = capA.getAction();
                AnimationCalculator mCalc = new AnimationCalculator(capA.getDuration(), capA.getFrame(), partialTicks);
                readDataFromModel();
                updateData(capA.getFrame(), capA.getDuration(), partialTicks, Easing.inOutSine);
                EnumMap<EntityPart, AnimationData> data = getDataFromModel();
                action.transformModel(capA.getState(), capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), action.getPitch(livingEntity, partialTicks), livingEntity, data, mCalc);
                Vector3f rotVec = mCalc.getTransformations();
                writeDataToModel();
                applyMatrixRotation(mCalc.getTransformations(), matrix);
                //Stun
                if(capA.isStunned()) {
                    partialTicks = ClientEngine.get().getPartialTick();
                    mCalc = new AnimationCalculator(capA.getStunDuration(), capA.getStunFrame(), partialTicks);
                    //This assumes frame gets updated; if it doesn't this value will change every tick
                    int dir = (entity.tickCount - capA.getStunFrame()) % 2 == 0 ? -1 : 1;
                    float mag = Mth.clamp(capA.getStunDuration() / 7F, 0.5F, 1F);
                    animateStun(capA.getStunFrame(), capA.getStunDuration(), dir, mag, entity, mCalc, rotVec, partialTicks);
                    //Update matrix stack
                    rotVec = mCalc.getTransformations();
                    matrix.mulPose(Vector3f.XP.rotationDegrees(rotVec.x()));
                    matrix.mulPose(Vector3f.YP.rotationDegrees(rotVec.y()));
                    matrix.mulPose(Vector3f.ZP.rotationDegrees(rotVec.z()));
                }
            }
        }
    }

    public void animateStun(int frame, int duration, int dir, float mag, T user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        for(AnimatedModelPart part : parts) {
            if(!getNoStunParts().contains(part)) AnimationUtil.stunPartToDefault(part, part.animationData, frame, duration, partialTicks);
        }
        int offset = duration / 2;
        mCalc.length = offset;
        mCalc.setEasing(Easing.outQuart);
        mCalc.add(0, -7.5F * mag * dir, 0);
        if(frame > offset) {
            mCalc.setEasing(Easing.inOutSine);
            mCalc.offset = offset;
            mCalc.length = duration;
            mVec.mul(-1);
            mCalc.extend(mVec);
        }
    }

    protected abstract List<AnimatedModelPart> getNoStunParts();

    protected void applyMatrixRotation(Vector3f rotations, PoseStack matrix) {
        matrix.mulPose(Vector3f.XP.rotationDegrees(rotations.x()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(rotations.y()));
        matrix.mulPose(Vector3f.ZP.rotationDegrees(rotations.z()));
    }

    //Utility functions for animation
    public void rotateX(ModelPart part, float degree, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.xRot += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * Math.toRadians(degree) * limbSwingAmount + positionOffset * limbSwingAmount;
    }

    public void rotateY(ModelPart part, float degree, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.yRot += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * Math.toRadians(degree) * limbSwingAmount + positionOffset * limbSwingAmount;
    }

    public void rotateZ(ModelPart part, float degree, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.zRot += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * Math.toRadians(degree) * limbSwingAmount + positionOffset * limbSwingAmount;
    }

    public void translateX(ModelPart part, float distance, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.x += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * distance * limbSwingAmount + positionOffset * limbSwingAmount;
    }

    public void translateY(ModelPart part, float distance, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.y += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * distance * limbSwingAmount + positionOffset * limbSwingAmount;
    }

    public void translateZ(ModelPart part, float distance, float slow, float swingOffset, float positionOffset, float limbSwing, float limbSwingAmount, Easing easing, boolean symmetric) {
        part.z += AnimationUtil.getProgress(limbSwing + swingOffset * slow, slow, easing, symmetric) * distance * limbSwingAmount + positionOffset * limbSwingAmount;
    }
}
