package frostnox.nightfall.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.client.render.entity.layer.ArmorLayer;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;

/**
 * Should be used exclusively for armor in conjunction with {@link ArmorLayer}
 */
public class ArmorModel extends AnimatedModel<LivingEntity> {
    public final AnimatedModelPart body;
    public final AnimatedModelPart innerBody;
    public final AnimatedModelPart bodyHeadJoint;
    public final AnimatedModelPart neck;
    public final AnimatedModelPart head;
    public final AnimatedModelPart bodyLeftArmJoint;
    public final AnimatedModelPart leftArm;
    public final AnimatedModelPart leftHand;
    public final AnimatedModelPart bodyRightArmJoint;
    public final AnimatedModelPart rightArm;
    public final AnimatedModelPart rightHand;
    public final AnimatedModelPart leftLeg;
    public final AnimatedModelPart rightLeg;
    public final AnimatedModelPart leftFoot;
    public final AnimatedModelPart rightFoot;
    public final AnimatedModelPart leftSkirt;
    public final AnimatedModelPart rightSkirt;

    public ArmorModel(ModelPart model, ImmutableMap<String, Vec3f> scaleMap) {
        super(model, RenderType::entityCutoutNoCull);
        body = (AnimatedModelPart) model.getChild("body");
        body.setScale(scaleMap.get("body"));
        innerBody = (AnimatedModelPart) model.getChild("inner_body");
        innerBody.setScale(scaleMap.get("body"));
        bodyHeadJoint = (AnimatedModelPart) model.getChild("body_head_joint");
        neck = (AnimatedModelPart) bodyHeadJoint.getChild("neck");
        head = (AnimatedModelPart) neck.getChild("head");
        head.setScale(scaleMap.get("head"));
        bodyLeftArmJoint = (AnimatedModelPart) model.getChild("body_left_arm_joint");
        leftArm = (AnimatedModelPart) bodyLeftArmJoint.getChild("left_arm_joint");
        leftHand = (AnimatedModelPart) leftArm.getChild("left_arm");
        leftHand.setScale(scaleMap.get("arm"));
        bodyRightArmJoint = (AnimatedModelPart) model.getChild("body_right_arm_joint");
        rightArm = (AnimatedModelPart) bodyRightArmJoint.getChild("right_arm_joint");
        rightHand = (AnimatedModelPart) rightArm.getChild("right_arm");
        rightHand.setScale(scaleMap.get("arm"));
        leftLeg = (AnimatedModelPart) model.getChild("left_leg");
        leftLeg.setScale(scaleMap.get("leg"));
        rightLeg = (AnimatedModelPart) model.getChild("right_leg");
        rightLeg.setScale(scaleMap.get("leg"));
        leftFoot = (AnimatedModelPart) model.getChild("left_foot");
        leftFoot.setScale(scaleMap.get("leg"));
        rightFoot = (AnimatedModelPart) model.getChild("right_foot");
        rightFoot.setScale(scaleMap.get("leg"));
        leftSkirt = (AnimatedModelPart) model.getChild("left_skirt");
        leftSkirt.setScale(scaleMap.get("leg"));
        rightSkirt = (AnimatedModelPart) model.getChild("right_skirt");
        rightSkirt.setScale(scaleMap.get("leg"));
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        return new EnumMap<>(EntityPart.class);
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, LivingEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {

    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return List.of();
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //Must be rendered like this, rendering individual parts will nullify RGB colors, not sure why
        this.headParts().forEach((p_102061_) -> {
            p_102061_.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        });
        this.bodyParts().forEach((p_102051_) -> {
            p_102051_.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        });
    }

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.bodyHeadJoint);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.innerBody, this.bodyRightArmJoint, this.bodyLeftArmJoint, this.rightLeg, this.leftLeg, this.rightFoot, this.leftFoot, this.rightSkirt, this.leftSkirt);
    }

    public void setAllVisible(boolean visible) {
        body.visible = visible;
        innerBody.visible = visible;
        bodyHeadJoint.visible = visible;
        neck.visible = visible;
        head.visible = visible;
        bodyLeftArmJoint.visible = visible;
        leftArm.visible = visible;
        leftHand.visible = visible;
        bodyRightArmJoint.visible = visible;
        rightArm.visible = visible;
        rightHand.visible = visible;
        leftLeg.visible = visible;
        rightLeg.visible = visible;
        leftFoot.visible = visible;
        rightFoot.visible = visible;
        leftSkirt.visible= visible;
        rightSkirt.visible = visible;
    }

    public static MeshDefinition createVanillaMesh(CubeDeformation cubeDef) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition inner_body = partdefinition.addOrReplaceChild("inner_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_foot = partdefinition.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_foot = partdefinition.addOrReplaceChild("right_foot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_head_joint = partdefinition.addOrReplaceChild("body_head_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck = body_head_joint.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDef)
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDef.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition body_right_arm_joint = partdefinition.addOrReplaceChild("body_right_arm_joint", CubeListBuilder.create(), PartPose.offset(0, 0F, 0.0F));

        PartDefinition right_arm_joint = body_right_arm_joint.addOrReplaceChild("right_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = right_arm_joint.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_left_arm_joint = partdefinition.addOrReplaceChild("body_left_arm_joint", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0.0F));

        PartDefinition left_arm_joint = body_left_arm_joint.addOrReplaceChild("left_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = left_arm_joint.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition right_skirt = partdefinition.addOrReplaceChild("right_skirt", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition left_skirt = partdefinition.addOrReplaceChild("left_skirt", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.01F));

        return meshdefinition;
    }

    public static MeshDefinition createFlatMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body_head_joint = partdefinition.addOrReplaceChild("body_head_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition neck = body_head_joint.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 42).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 24).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -13.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition inner_body = partdefinition.addOrReplaceChild("inner_body", CubeListBuilder.create().texOffs(33, 13).addBox(-4.5F, -4.0F, -2.5F, 9.0F, 4.0F, 5.0F, new CubeDeformation(0.02F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition body_right_arm_joint = partdefinition.addOrReplaceChild("body_right_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm_joint = body_right_arm_joint.addOrReplaceChild("right_arm_joint", CubeListBuilder.create(), PartPose.offset(-6.0F, 2.0F, 0.0F));

        PartDefinition right_arm = right_arm_joint.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(64, 42).mirror().addBox(-2.5F, -3.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_left_arm_joint = partdefinition.addOrReplaceChild("body_left_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm_joint = body_left_arm_joint.addOrReplaceChild("left_arm_joint", CubeListBuilder.create(), PartPose.offset(6.0F, 2.0F, 0.0F));

        PartDefinition left_arm = left_arm_joint.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 42).addBox(-2.5F, -3.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(64, 42).addBox(-2.5F, -3.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(44, 27).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.99F, 12.0F, 0.0F));

        PartDefinition right_foot = partdefinition.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(0, 28).mirror().addBox(-3.01F, 6.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-1.99F, 12.0F, 0.0F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(44, 27).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.99F, 12.0F, 0.01F));

        PartDefinition left_foot = partdefinition.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 28).addBox(-2.99F, 6.0F, -3.01F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.99F, 12.0F, 0.01F));

        PartDefinition right_skirt = partdefinition.addOrReplaceChild("right_skirt", CubeListBuilder.create(), PartPose.offset(-1.99F, 12.0F, 0.0F));

        PartDefinition cube_r1 = right_skirt.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 0).addBox(-2.8F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.2F, -1.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition left_skirt = partdefinition.addOrReplaceChild("left_skirt", CubeListBuilder.create(), PartPose.offset(1.99F, 12.0F, 0.0F));

        PartDefinition cube_r2 = left_skirt.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.2F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(-0.03F)).mirror(false), PartPose.offsetAndRotation(-0.2F, -1.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        return meshdefinition;
    }

    public static MeshDefinition createTaperedMesh() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -13.0F, -3.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition inner_body = partdefinition.addOrReplaceChild("inner_body", CubeListBuilder.create().texOffs(33, 13).addBox(-4.5F, -4.0F, -2.5F, 9.0F, 4.0F, 5.0F, new CubeDeformation(0.03F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition right_skirt = partdefinition.addOrReplaceChild("right_skirt", CubeListBuilder.create(), PartPose.offset(-1.99F, 12.0F, 0.0F));

        PartDefinition cube_r1 = right_skirt.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(33, 1).addBox(-2.53F, 0.0F, -2.5F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.2F, -1.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition left_skirt = partdefinition.addOrReplaceChild("left_skirt", CubeListBuilder.create(), PartPose.offset(1.99F, 12.0F, 0.0F));

        PartDefinition cube_r2 = left_skirt.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(33, 1).mirror().addBox(-3.46F, 0.0F, -2.5F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.02F)).mirror(false), PartPose.offsetAndRotation(-0.2F, -1.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        return meshdefinition;
    }

    public static LayerDefinition createFlatLayer() {
        return LayerDefinition.create(createFlatMesh(), 128, 64);
    }

    public static LayerDefinition createTaperedLayer() {
        return LayerDefinition.create(createTaperedMesh(), 128, 64);
    }

    public static LayerDefinition createSlimLayer() {
        MeshDefinition meshdefinition = createTaperedMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(2, 1).addBox(-4.5F, -13.0F, -2.5F, 9.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition inner_body = partdefinition.addOrReplaceChild("inner_body", CubeListBuilder.create().texOffs(33, 15).addBox(-4.5F, -2.0F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createPlateSurvivorLayer() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -13.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(-0.01F))
                .texOffs(106, 0).addBox(-5.0F, -12.0F, -4.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createPlateExplorerLayer() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.getChild("body_head_joint").getChild("neck").addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 42).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(80, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(64, 24).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createChainmailExplorerLayer() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.getChild("body_head_joint").getChild("neck").addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 42).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 24).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F))
                .texOffs(80, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createPlateSlayerLayer() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.getChild("body_head_joint").getChild("neck").addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 42).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 24).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F))
                .texOffs(104, 7).addBox(-1.0F, -10.0F, -6.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(122, 21).addBox(-0.5F, -11.0F, 4.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(124, 25).addBox(-0.5F, -10.0F, 6.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(108, 22).addBox(-0.5F, -9.0F, 5.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -13.0F, -3.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(-0.01F))
                .texOffs(118, 0).addBox(1.0F, -11.0F, -4.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(102, 0).addBox(-5.0F, -11.0F, -4.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(112, 0).addBox(-1.0F, -11.0F, -4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createScaleSlayerLayer() {
        MeshDefinition meshdefinition = createTaperedMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body_right_arm_joint = partdefinition.addOrReplaceChild("body_right_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm_joint = body_right_arm_joint.addOrReplaceChild("right_arm_joint", CubeListBuilder.create(), PartPose.offset(-6.0F, 2.0F, 0.0F));

        PartDefinition right_arm = right_arm_joint.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 42).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(104, 0).addBox(-3.5F, -5.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(64, 42).mirror().addBox(-2.5F, -3.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_left_arm_joint = partdefinition.addOrReplaceChild("body_left_arm_joint", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm_joint = body_left_arm_joint.addOrReplaceChild("left_arm_joint", CubeListBuilder.create(), PartPose.offset(6.0F, 2.0F, 0.0F));

        PartDefinition left_arm = left_arm_joint.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 42).addBox(-2.5F, -3.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(104, 0).mirror().addBox(-2.5F, -5.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(64, 42).addBox(-2.5F, -3.0F, -2.5F, 5.0F, 14.0F, 5.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createChainmailSlayerLayer() {
        MeshDefinition meshdefinition = createFlatMesh();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.getChild("body_head_joint").getChild("neck").addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 42).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(64, 24).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.01F))
                .texOffs(86, 50).addBox(-6.0F, -3.0F, -3.0F, 12.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
