package frostnox.nightfall.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ArmorStandDummyModel extends AnimatedHumanoidModel<ArmorStandDummyEntity> {
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;
    
    public ArmorStandDummyModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.rightBodyStick = body.getChild("right_body_stick");
        this.leftBodyStick = body.getChild("left_body_stick");
        this.shoulderStick = body.getChild("shoulder_stick");
        this.basePlate = root.getChild("base_plate");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, -12F, -1.5F, 12.0F, 3.0F, 3.0F), PartPose.offset(0F, 12F, 0F));
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0F, 0F, 0.0F));
        neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, -12F, 0.0F));
        body.addOrReplaceChild("right_body_stick", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0F, -12F, 0F));
        body.addOrReplaceChild("left_body_stick", CubeListBuilder.create().texOffs(48, 16).addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0F, -12F, 0F));
        body.addOrReplaceChild("shoulder_stick", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.offset(0F, -12F, 0F));
        PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 0), PartPose.offset(-5.0F - 1F, 2.0F - 12F, 0.0F));
        rightArm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.ZERO);
        PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).mirror(), PartPose.offset(5.0F + 1F, 2.0F - 12F, 0.0F));
        leftArm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(-2F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(2F, 12.0F, 0.0F));
        root.addOrReplaceChild("base_plate", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void prepareMobModel(ArmorStandDummyEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
        this.basePlate.xRot = 0.0F;
        this.basePlate.yRot = ((float)Math.PI / 180F) * -Mth.rotLerp(pPartialTick, pEntity.yRotO, pEntity.getYRot());
        this.basePlate.zRot = 0.0F;
    }

    @Override
    public void setupAnim(ArmorStandDummyEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.head.xRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getX();
        this.head.yRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getY();
        this.head.zRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getZ();
        this.body.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
        this.body.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
        this.body.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
        this.leftArm.xRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getX();
        this.leftArm.yRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getY();
        this.leftArm.zRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getZ();
        this.rightArm.xRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getX();
        this.rightArm.yRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getY();
        this.rightArm.zRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getZ();
        this.leftLeg.xRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getX();
        this.leftLeg.yRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getY();
        this.leftLeg.zRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getZ();
        this.rightLeg.xRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getX();
        this.rightLeg.yRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getY();
        this.rightLeg.zRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getZ();
        this.leftArm.visible = pEntity.isShowArms();
        this.rightArm.visible = pEntity.isShowArms();
        this.basePlate.visible = !pEntity.isNoBasePlate();
        this.rightBodyStick.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
        this.rightBodyStick.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
        this.rightBodyStick.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
        this.leftBodyStick.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
        this.leftBodyStick.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
        this.leftBodyStick.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
        this.shoulderStick.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
        this.shoulderStick.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
        this.shoulderStick.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
    }

    @Override
    protected Iterable<ModelPart> rootParts() {
        return Iterables.concat(super.rootParts(), ImmutableList.of(this.basePlate));
    }

    @Override
    public void translateToHand(HumanoidArm pSide, PoseStack stack) {
        ModelPart modelpart = this.getArm(pSide);
        boolean flag = modelpart.visible;
        modelpart.visible = true;
        super.translateToHand(pSide, stack);
        modelpart.visible = flag;
        stack.scale(0.999F, 1F, 1F);
        stack.translate(pSide == HumanoidArm.RIGHT ? (-1D/16D - 0.00005D) : (1D/16D + 0.00005D), 0.625D, -0.125D);
    }
}
