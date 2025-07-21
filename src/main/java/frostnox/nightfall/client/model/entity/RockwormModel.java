package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.RockwormEntity;
import frostnox.nightfall.registry.ActionsNF;
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

public class RockwormModel extends AnimatedModel<RockwormEntity> implements HeadedModel {
    private final AnimatedModelPart upperBody, upperBodyCubes;
    private final AnimatedModelPart lowerBody, lowerBodyCubes;
    private final AnimatedModelPart head;
    private final List<AnimatedModelPart> noStunParts;

    public RockwormModel(ModelPart root) {
        super(root);
        this.lowerBody = (AnimatedModelPart) root.getChild("lower_body");
        this.lowerBodyCubes = (AnimatedModelPart) this.lowerBody.getChild("lower_body_cubes");
        this.upperBody = (AnimatedModelPart) this.lowerBody.getChild("upper_body");
        this.upperBodyCubes = (AnimatedModelPart) this.upperBody.getChild("upper_body_cubes");
        this.head = (AnimatedModelPart) this.upperBody.getChild("head");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lower_body = partdefinition.addOrReplaceChild("lower_body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition lower_body_cubes = lower_body.addOrReplaceChild("lower_body_cubes", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 15.0F, 6.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_body = lower_body.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition upper_body_cubes = upper_body.addOrReplaceChild("upper_body_cubes", CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 15.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = upper_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 22).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 1.309F, 0.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 27).mirror().addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -2.5F, 1.5F, -0.3491F, 0.7854F, -0.5236F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(34, 27).mirror().addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -2.5F, -2.0F, 0.5236F, -0.7854F, -0.6109F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 27).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, -2.0F, 0.5236F, 0.7854F, 0.6109F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 27).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.5F, 1.5F, -0.3491F, -0.7854F, 0.5236F));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(43, 27).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 2.5F, -0.4189F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(RockwormEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        //Idle
        if(!entity.isDeadOrDying()) {
            int hash = entity.getUUID().hashCode();
            float idle = (time + (hash & 255));
            rotateZ(lowerBody, 0.6F, 60F, 0, 0, idle, 1, Easing.inOutSine, true);
            rotateZ(upperBody, 1.2F, 60F, MathUtil.PI * 0.4F, 0, idle, 1, Easing.inOutSine, true);
            IActionTracker capA = entity.getActionTracker();
            if(capA.getActionID().equals(ActionsNF.ROCKWORM_RETREAT.getId())) {
                float progress = capA.getProgress(ClientEngine.get().getPartialTick());
                lowerBodyCubes.visible = progress < 0.5F;
                upperBodyCubes.visible = progress < 0.8F;
            }
            else if(capA.getActionID().equals(ActionsNF.ROCKWORM_EMERGE.getId())) {
                float progress = capA.getProgress(ClientEngine.get().getPartialTick());
                lowerBodyCubes.visible = progress > 0.5F;
                upperBodyCubes.visible = progress > 0.2F;
            }
            else {
                lowerBodyCubes.visible = true;
                upperBodyCubes.visible = true;
            }
        }
        //Death
        if(entity.deathTime > 0) {
            float anim = ((float) entity.deathTime + ClientEngine.get().getPartialTick() - 1.0F) / 20.0F * 1.6F;
            anim = Math.min(1, Mth.sqrt(anim));
            int dir = entity.getSynchedRandom() % 2 == 0 ? 1 : -1;
            lowerBody.x += -5 * dir * anim;
            lowerBody.z += 5 * anim;
            lowerBody.zRot += MathUtil.toRadians(90) * dir * anim;
            upperBody.xRot += MathUtil.toRadians(90) * anim;
            head.xRot += MathUtil.toRadians(30) * anim;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        lowerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.BODY_2, upperBody.animationData);
        map.put(EntityPart.BODY, lowerBody.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, RockwormEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
