package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.CreeperEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.List;

public class CreeperModel extends AnimatedModel<CreeperEntity> implements HeadedModel {
    private final AnimatedModelPart body;
    private final AnimatedModelPart head;
    private final AnimatedModelPart legBackRight;
    private final AnimatedModelPart legBackLeft;
    private final AnimatedModelPart legFrontRight;
    private final AnimatedModelPart legFrontLeft;
    private final List<AnimatedModelPart> noStunParts;

    public CreeperModel(ModelPart root) {
        super(root);
        this.body = (AnimatedModelPart) root.getChild("body");
        this.head = (AnimatedModelPart) body.getChild("head");
        this.legBackRight = (AnimatedModelPart) root.getChild("legBackRight");
        this.legBackLeft = (AnimatedModelPart) root.getChild("legBackLeft");
        this.legFrontRight = (AnimatedModelPart) root.getChild("legFrontRight");
        this.legFrontLeft = (AnimatedModelPart) root.getChild("legFrontLeft");
        noStunParts = List.of(head);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition legBackRight = partdefinition.addOrReplaceChild("legBackRight", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, 2.0F));

        PartDefinition legBackLeft = partdefinition.addOrReplaceChild("legBackLeft", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, 2.0F));

        PartDefinition legFrontRight = partdefinition.addOrReplaceChild("legFrontRight", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 18.0F, -2.0F));

        PartDefinition legFrontLeft = partdefinition.addOrReplaceChild("legFrontLeft", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 18.0F, -2.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(CreeperEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        //Walk
        head.yRot = netHeadYaw * (MathUtil.PI / 180F);
        head.xRot = headPitch * (MathUtil.PI / 180F);
        body.xRot = Mth.cos(limbSwing * 0.6662F) * 0.035F * limbSwingAmount;
        legBackRight.xRot = Mth.cos(limbSwing * 0.6662F) * 0.65F * limbSwingAmount;
        legBackLeft.xRot = Mth.cos(limbSwing * 0.6662F + MathUtil.PI) * 0.65F * limbSwingAmount;
        legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F + MathUtil.PI) * 0.65F * limbSwingAmount;
        legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 0.65F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legBackRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legBackLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legFrontRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        legFrontLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.LEG_RIGHT, legFrontRight.animationData);
        map.put(EntityPart.LEG_LEFT, legFrontLeft.animationData);
        map.put(EntityPart.LEG_2_RIGHT, legBackRight.animationData);
        map.put(EntityPart.LEG_2_LEFT, legBackLeft.animationData);
        map.put(EntityPart.HEAD, head.animationData);
        map.put(EntityPart.BODY, body.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, CreeperEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
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
