package frostnox.nightfall.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.ClientEngine;

import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.math.Easing;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import static frostnox.nightfall.util.AnimationUtil.*;

/**
 * Similar to vanilla PlayerModel. Suppresses some arm rotations for combat animations and adds "hand" parts that
 * mimic the original arms so those can be used as joints.
 */
public class PlayerModelNF<T extends Player> extends PlayerModel<T> implements IHumanoidModel {
    private final List<ModelPart> cubes = Lists.newArrayList();
    //Copies of the vanilla arms so the originals can be used for x rotation to achieve xzy rotation since renderer does zyx
    public final ModelPart leftHand;
    public final ModelPart rightHand;
    //Replace vanilla sleeves/cloak since they are inaccessible
    public final ModelPart leftSleeveN;
    public final ModelPart rightSleeveN;
    public final ModelPart cloakN;

    public final boolean slim;

    //Data to make animating easier, only updated and used when Actions are played
    public AnimationData leftArmData = new AnimationData();
    public AnimationData rightArmData = new AnimationData();
    public AnimationData leftHandData = new AnimationData();
    public AnimationData rightHandData = new AnimationData();
    public AnimationData leftLegData = new AnimationData();
    public AnimationData rightLegData = new AnimationData();

    private final EnumMap<EntityPart, ModelPart> partMap = new EnumMap<>(EntityPart.class);

    public PlayerModelNF(ModelPart model, boolean slim) {
        super(model, slim);
        this.slim = slim;
        leftHand = model.getChild("left_arm").getChild("left_hand");
        rightHand = model.getChild("right_arm").getChild("right_hand");
        cloakN = model.getChild("cloakN");
        leftSleeveN = model.getChild("left_arm").getChild("left_sleeveN");
        rightSleeveN = model.getChild("right_arm").getChild("right_sleeveN");
        //Arrows won't appear properly in arms; layer class needs to be changed to work with child parts so arms are excluded for now
        cubes.add(head);
        cubes.add(body);
        cubes.add(leftLeg);
        cubes.add(rightLeg);
        partMap.put(EntityPart.NECK, body);
        partMap.put(EntityPart.HEAD, head);
        partMap.put(EntityPart.BODY, body);
        partMap.put(EntityPart.ARM_RIGHT, rightArm);
        partMap.put(EntityPart.HAND_RIGHT, rightHand);
        partMap.put(EntityPart.ARM_LEFT, leftArm);
        partMap.put(EntityPart.HAND_LEFT, leftHand);
        partMap.put(EntityPart.LEG_RIGHT, rightLeg);
        partMap.put(EntityPart.LEG_LEFT, leftLeg);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDef, boolean slim) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition base = meshdefinition.getRoot();
        base.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDef), PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDef.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(0.0F, 12.0F, 0.0F));
        base.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, cubeDef), PartPose.ZERO);
        base.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, cubeDef, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("cloakN", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, cubeDef, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition rightArm, leftArm;
        if(slim) {
            base.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0),
                    PartPose.offset(5.0F + 0.5F, 2.5F, 0.0F));
            base.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0),
                    PartPose.offset(-5.0F - 0.5F, 2.5F, 0.0F));
            rightArm = base.getChild("right_arm");
            leftArm = base.getChild("left_arm");
            leftArm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 48)
                            .addBox(-1.0F - 0.5F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef),
                    PartPose.offset(0F, 0F, 0F));
            rightArm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 16)
                            .addBox(-2.0F + 0.5F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef),
                    PartPose.offset(0F, 0F, 0F));
            leftArm.addOrReplaceChild("left_sleeveN", CubeListBuilder.create().texOffs(48, 48)
                    .addBox(-1.0F - 0.5F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)),
                    PartPose.offset(5.0F + 0.5F, 2.5F, 0.0F));
            rightArm.addOrReplaceChild("right_sleeveN", CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-2.0F + 0.5F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)),
                    PartPose.offset(-5.0F - 0.5F, 2.5F, 0.0F));
            //Vanilla
            base.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            base.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
        }
        else {
            base.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0),
                    PartPose.offset(5.0F + 1.0F, 2.0F, 0.0F));
            base.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0),
                    PartPose.offset(-5.0F - 1.0F, 2.0F, 0.0F));
            rightArm = base.getChild("right_arm");
            leftArm = base.getChild("left_arm");
            leftArm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 48)
                            .addBox(-1.0F - 1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef),
                    PartPose.offset(0F, 0F, 0F));
            rightArm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 16)
                            .addBox(-3.0F + 1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef),
                    PartPose.offset(0F, 0F, 0F));
            leftArm.addOrReplaceChild("left_sleeveN", CubeListBuilder.create().texOffs(48, 48)
                    .addBox(-1.0F - 1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)),
                    PartPose.offset(5.0F + 1.0F, 2.0F, 0.0F));
            rightArm.addOrReplaceChild("right_sleeveN", CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-3.0F + 1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)),
                    PartPose.offset(-5.0F - 1.0F, 2.0F, 0.0F));
            //Vanilla
            base.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
            base.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        }

        base.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(2F, 12.0F, 0.0F));
        base.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.248F)), PartPose.offset(2F, 12.0F, 0.0F));
        base.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef), PartPose.offset(-2F, 12.0F, 0.0F));
        base.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDef.extend(0.249F)), PartPose.offset(-2F, 12.0F, 0.0F));
        base.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDef.extend(0.25F)), PartPose.offset(0, 12.0F, 0));
        return meshdefinition;
    }

    @Override
    public ModelPart getModelPart(EntityPart part) {
        return partMap.get(part);
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = player.getFallFlyingTicks() > 4;
        boolean flag1 = player.isVisuallySwimming();
        float partial = ClientEngine.get().getPartialTick();
        head.yRot = netHeadYaw * (MathUtil.PI / 180F);
        if (flag) {
            head.xRot = (-MathUtil.PI / 4F);
        } else if (swimAmount > 0.0F) {
            if (flag1) {
                head.xRot = rotlerpRad(swimAmount, head.xRot, (-MathUtil.PI / 4F));
            } else {
                head.xRot = rotlerpRad(swimAmount, head.xRot, headPitch * (MathUtil.PI / 180F));
            }
        } else {
            head.xRot = headPitch * (MathUtil.PI / 180F);
        }

        leftHand.x = 0;
        leftHand.y = 0;
        leftHand.z = 0;
        rightHand.x = 0;
        rightHand.y = 0;
        rightHand.z = 0;
        leftHand.xRot = 0.0F;
        leftHand.yRot = 0.0F;
        leftHand.zRot = 0.0F;
        rightHand.xRot = 0.0F;
        rightHand.yRot = 0.0F;
        rightHand.zRot = 0.0F;
        body.yRot = 0.0F;
        rightArm.z = 0.0F;
        rightArm.x = -5.0F;
        leftArm.z = 0.0F;
        leftArm.x = 5.0F;
        head.z = 0;
        float f = 1.0F;
        if (flag) {
            f = (float)player.getDeltaMovement().lengthSqr();
            f = f / 0.2F;
            f = f * f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }
        //Arm swing from walking
        rightArm.xRot = Mth.cos(limbSwing * 0.6662F + MathUtil.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;

        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + MathUtil.PI) * 1.4F * limbSwingAmount / f;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        rightLeg.zRot = 0.0F;
        leftLeg.zRot = 0.0F;
        if (riding) {
            rightArm.xRot += (-MathUtil.PI / 5F);
            leftArm.xRot += (-MathUtil.PI / 5F);
            rightLeg.xRot = -1.4137167F;
            rightLeg.yRot = (MathUtil.PI / 10F);
            rightLeg.zRot = 0.07853982F;
            leftLeg.xRot = -1.4137167F;
            leftLeg.yRot = (-MathUtil.PI / 10F);
            leftLeg.zRot = -0.07853982F;
        }

        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        boolean flag2 = player.getMainArm() == HumanoidArm.RIGHT;
        boolean flag3 = flag2 ? leftArmPose.isTwoHanded() : rightArmPose.isTwoHanded();
        if (flag2 != flag3) {
            poseLeftArm(player);
            poseRightArm(player);
        } else {
            poseRightArm(player);
            poseLeftArm(player);
        }
        float crouchProgress = AnimationUtil.getCrouchProgress(player, partial);
        if(attackTime > 0.0F) {
            HumanoidArm arm = getSwingingArm(player);
            ModelPart model = getArm(arm);
            float progress = attackTime;
            float armSwing = Mth.sin(Mth.sqrt(progress) * (MathUtil.PI * 2F)) * 0.2F;
            body.yRot = -Mth.sin(progress * MathUtil.PI) * (0.3F - 0.15F * crouchProgress);
            if(arm == HumanoidArm.LEFT) {
                body.yRot *= -1.0F;
                armSwing = -armSwing;
            }
            rightArm.z = Mth.sin(armSwing) * 5F;
            rightArm.x *= Mth.cos(armSwing);
            leftArm.z = -Mth.sin(armSwing) * 5F;
            leftArm.x *= Mth.cos(armSwing);
            rightArm.yRot += armSwing;
            leftArm.yRot += armSwing;
            progress = 1.0F - attackTime;
            progress = 1.0F - progress * progress * progress;
            model.xRot -= Mth.sin(progress * MathUtil.PI) * 1.2F + Mth.sin(attackTime * MathUtil.PI) * -(head.xRot - 0.7F) * 0.75F;
            model.yRot += armSwing * 2.0F;
            model.zRot += Mth.sin(attackTime * MathUtil.PI) * -0.4F;
        }

        //Climb amount for positioning arms/body
        IPlayerData capP = PlayerData.get(player);
        double climbYAmount = capP.getClimbYAmount();
        /*if(player.onClimbable()) {
            if(AnimationUtil.isPlayerFacingClimbable(player)) {
                climbYAmount = -1D; //Use -1 as flag for last climb being a vanilla climbing block
                capP.setClimbYAmount(climbYAmount);
            }
            else {
                climbYAmount = -2D;
                capP.setClimbYAmount(-2D);
            }
        }
        else*/ if(capP.isClimbing()) {
            Vector3d hitCoords = capP.getClimbPosition();
            climbYAmount = Mth.clamp((hitCoords.y - player.getPosition(partial).y) / 1.85D, 0D, 1D);
            capP.setClimbYAmount(climbYAmount);
            if(LevelUtil.isPositionFullyClimbable(player, hitCoords)) {
                climbYAmount = -1D;
                capP.setClimbYAmount(climbYAmount);
            }
        }
        body.xRot = 0.0F;
        rightLeg.x = -2F;
        rightLeg.z = 0.0F;
        leftLeg.x = 2F;
        leftLeg.z = 0.0F;
        rightLeg.y = 12.0F;
        leftLeg.y = 12.0F;
        head.y = 0.0F;
        body.y = 12.0F;
        body.z = 0F;
        leftArm.y = 2.0F;
        rightArm.y = 2.0F;
        ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
        if(chestItem.isEmpty()) {
            cloakN.z = 0.0F;
            cloakN.y = 0.0F;
        }
        else {
            cloakN.z = -1.1F;
            cloakN.y = -0.85F;
        }
        if(player.isCrouching()) {
            cloakN.xRot = MathUtil.toRadians(25);
        }
        else cloakN.xRot = 0;
        if(chestItem.is(ItemsNF.BACKPACK.get())) cloakN.visible = false;
        float climbProgress = AnimationUtil.getClimbProgress(player, partial);
        if((crouchProgress > 0.0F || (1D - climbYAmount > 0.0D && climbYAmount > -1D && capP.isClimbing()) || (climbProgress > 0.0F && climbYAmount > -1D)) && !player.isVisuallyCrawling()) {
            if(climbYAmount > -1D && climbProgress > 0F) {
                float climbAmountProgress = (1F - (float) climbYAmount) * 0.6F;
                climbAmountProgress *= climbProgress;
                if(crouchProgress < climbAmountProgress) crouchProgress = climbAmountProgress;
            }
            if(crouchProgress < 0F) crouchProgress = 0F;
            body.xRot += 0.5F * crouchProgress;
            rightArm.xRot += 0.4F * crouchProgress;
            leftArm.xRot += 0.4F * crouchProgress;
            rightLeg.x += 0.1F * crouchProgress;
            rightLeg.z += 4F * crouchProgress;
            leftLeg.x += -0.1F * crouchProgress;
            leftLeg.z += 4F * crouchProgress;
            rightLeg.y += 0.2F * crouchProgress;
            leftLeg.y += 0.2F * crouchProgress;
            head.y += 4.2F * crouchProgress;
            body.y += 1.8F * crouchProgress;
            body.z += 5.8F * crouchProgress;
            leftArm.y += 3.2F * crouchProgress;
            rightArm.y += 3.2F * crouchProgress;
            cloakN.xRot += MathUtil.toRadians(-25) * crouchProgress;
            cloakN.z += 1.4F * crouchProgress;
            cloakN.y += 1.85F * crouchProgress;
        }
        //Arm bob
        if(rightArmPose != HumanoidModel.ArmPose.SPYGLASS && !player.isVisuallyCrawling()) {
            AnimationUtils.bobModelPart(rightArm, ageInTicks, 1.0F);
        }
        if(leftArmPose != HumanoidModel.ArmPose.SPYGLASS && !player.isVisuallyCrawling()) {
            AnimationUtils.bobModelPart(leftArm, ageInTicks, -1.0F);
        }
        //Arm tilt in air
        float tilt = getAirborneProgress(player, partial);
        if(tilt > 0.0F) {
            //tilt = AnimationUtil.applyEasing(tilt, Easing.inOutSine);
            rightArm.zRot += MathUtil.toRadians(3F) * tilt;
            leftArm.zRot += MathUtil.toRadians(-3F) * tilt;
        }
        //Climbing
        if(climbProgress > 0.0F) {
            climbProgress = AnimationUtil.applyEasing(climbProgress, capP.isClimbing() ? Easing.outCubic : Easing.inOutSine);
            float ladderProgress = (float) player.getPosition(partial).y;
            if(climbYAmount <= -1D) {
                rightArm.xRot = Mth.lerp(climbProgress, rightArm.xRot, MathUtil.toRadians(-100F) + Mth.cos(ladderProgress * 2.2F + MathUtil.PI) * 0.5F / f);
                leftArm.xRot = Mth.lerp(climbProgress, leftArm.xRot, MathUtil.toRadians(-100F) + Mth.cos(ladderProgress * 2.2F) * 0.5F / f);
            }
            else {
                rightArm.xRot = Mth.lerp(climbProgress, rightArm.xRot, MathUtil.toRadians(-125F * (float) climbYAmount - 10F));
                leftArm.xRot = Mth.lerp(climbProgress, leftArm.xRot, MathUtil.toRadians(-125F * (float) climbYAmount - 10F));
                rightArm.yRot += Mth.lerp(climbProgress, rightArm.yRot, Mth.cos(limbSwing * 0.6662F + (float) Math.PI * 0.6F) * 2.0F * limbSwingAmount * 0.5F / f);
                leftArm.yRot += Mth.lerp(climbProgress, leftArm.yRot, Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f);
            }
            rightArm.zRot = Mth.lerp(climbProgress, rightArm.zRot, 0);
            leftArm.zRot = Mth.lerp(climbProgress, leftArm.zRot, 0);
            rightLeg.xRot = Mth.lerp(climbProgress, rightLeg.xRot, Mth.cos(limbSwing * 0.5F + ladderProgress * 2.2F + MathUtil.PI) * 0.5F / f);
            leftLeg.xRot = Mth.lerp(climbProgress, leftLeg.xRot, Mth.cos(limbSwing * 0.5F + ladderProgress * 2.2F) * 0.5F / f);
        }

        if(swimAmount > 0.0F && !player.isFallFlying()) {
            HumanoidArm handside = getSwingingArm(player);
            float f2 = handside == HumanoidArm.RIGHT && attackTime > 0.0F ? 0.0F : swimAmount;
            float f3 = handside == HumanoidArm.LEFT && attackTime > 0.0F ? 0.0F : swimAmount;
            if(!player.isVisuallyCrawling()) { //Swimming
                float f1 = limbSwing % 26.0F;
                rightArm.yRot = Mth.lerp(f2, rightArm.yRot, 0);
                leftArm.yRot = Mth.lerp(f3, leftArm.yRot, 0);
                if(f1 < 14.0F) {
                    rightArm.xRot = Mth.lerp(f2, rightArm.xRot, MathUtil.toRadians(-180F));
                    leftArm.xRot = Mth.lerp(f3, leftArm.xRot, MathUtil.toRadians(-180F));
                    rightArm.zRot = Mth.lerp(f2, rightArm.zRot, MathUtil.toRadians(-90F) * f1 / 14F);
                    leftArm.zRot = Mth.lerp(f3, leftArm.zRot, MathUtil.toRadians(90F) * f1 / 14F);
                }
                else if(f1 >= 14.0F && f1 < 22.0F) {
                    float f6 = (f1 - 14.0F) / 8.0F;
                    rightArm.xRot = Mth.lerp(f2, rightArm.xRot, MathUtil.toRadians(-180F) + MathUtil.toRadians(90F) * f6);
                    leftArm.xRot = Mth.lerp(f3, leftArm.xRot, MathUtil.toRadians(-180F) + MathUtil.toRadians(90F) * f6);
                    rightArm.zRot = Mth.lerp(f2, rightArm.zRot, MathUtil.toRadians(-90F) + MathUtil.toRadians(90F) * f6);
                    leftArm.zRot = Mth.lerp(f3, leftArm.zRot, MathUtil.toRadians(90F) + MathUtil.toRadians(-90F) * f6);
                }
                else if(f1 >= 22.0F && f1 < 26.0F) {
                    float f4 = (f1 - 22.0F) / 4.0F;
                    rightArm.xRot = Mth.lerp(f2, rightArm.xRot, MathUtil.toRadians(-90F) + MathUtil.toRadians(-90F) * f4);
                    leftArm.xRot = Mth.lerp(f3, leftArm.xRot, MathUtil.toRadians(-90F) + MathUtil.toRadians(-90F) * f4);
                    rightArm.zRot = Mth.lerp(f2, rightArm.zRot, 0);
                    leftArm.zRot = Mth.lerp(f3, leftArm.zRot, 0);
                }
                leftLeg.xRot = Mth.lerp(swimAmount, leftLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + (float) Math.PI));
                rightLeg.xRot = Mth.lerp(swimAmount, rightLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));
            }
            else { //Crawling
                body.xRot = Mth.lerp(swimAmount, body.xRot, -(float) Math.PI / 16F);
                head.z = Mth.lerp(swimAmount, head.z, 1.8F);

                leftArm.xRot = Mth.lerp(swimAmount, 0, -(float) Math.PI * 0.9F);
                rightArm.xRot = Mth.lerp(swimAmount, 0, -(float) Math.PI * 0.9F);
                leftArm.xRot += Mth.lerp(swimAmount, 0, 0.025F * Mth.cos((limbSwing * 0.65F)));
                rightArm.xRot += Mth.lerp(swimAmount, 0, 0.025F * Mth.cos((limbSwing * 0.65F + (float) Math.PI)));
                leftArm.zRot += Mth.lerp(f3, 0, 0.08F * Mth.cos((limbSwing * 0.65F + (float) Math.PI * 0F)) - 0.1F);
                rightArm.zRot += Mth.lerp(f2, 0, 0.08F * Mth.cos((limbSwing * 0.65F + (float) Math.PI * 0.5F)) + 0.1F);
                leftArm.z = Mth.lerp(swimAmount, leftArm.z, 1.8F);
                rightArm.z = Mth.lerp(swimAmount, rightArm.z, 1.8F);

                leftLeg.xRot = Mth.lerp(swimAmount, leftLeg.xRot, 0F);
                rightLeg.xRot = Mth.lerp(swimAmount, rightLeg.xRot, 0F);
                leftLeg.zRot = Mth.lerp(swimAmount, leftLeg.zRot, -0.175F);
                rightLeg.zRot = Mth.lerp(swimAmount, rightLeg.zRot, 0.175F);
                leftLeg.zRot += Mth.lerp(swimAmount, leftLeg.zRot, 0.14F * Mth.cos((limbSwing * 0.75F + (float) Math.PI)));
                rightLeg.zRot += Mth.lerp(swimAmount, rightLeg.zRot, 0.14F * Mth.cos((limbSwing * 0.75F + (float) Math.PI)));
                leftLeg.y += Mth.lerp(swimAmount, 0, 1.2F * Mth.cos(limbSwing * 0.75F)) - 1.2F;
                rightLeg.y += Mth.lerp(swimAmount, 0, 1.2F * Mth.cos(limbSwing * 0.75F + (float) Math.PI)) - 1.2F;
            }
        }
    }

    /**
     * @return y rotation in radians performed on the player matrix
     */
    public float doCombatAnimations(Player player, PoseStack matrix) {
        if(!player.isAlive()) return 0;
        IActionTracker capA = ActionTracker.get(player);
        if(capA.isInactive()) return 0;
        float partialTicks = capA.modifyPartialTick(ClientEngine.get().getPartialTick());
        AnimationCalculator mCalc = new AnimationCalculator(capA.getDuration(), capA.getFrame(), partialTicks);
        //Update data from model
        readDataFromModel(capA, partialTicks);
        //Transform data
        Action action = capA.getAction();
        action.transformModel(capA.getState(), capA.getFrame(), capA.getDuration(), action.getChargeProgress(capA.getCharge(), capA.getChargePartial()), action.getPitch(player, partialTicks), player, getDataFromModel(), mCalc);
        //Update matrix stack
        Vector3f rotVec = mCalc.getTransformations();
        matrix.mulPose(Vector3f.XP.rotationDegrees(rotVec.x()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(rotVec.y()));
        matrix.mulPose(Vector3f.ZP.rotationDegrees(rotVec.z()));
        //Re-align head
        head.yRot -= MathUtil.toRadians(rotVec.y());
        //Update model with animations from data
        writeDataToModel();
        //Stun
        if(capA.isStunned()) {
            partialTicks = ClientEngine.get().getPartialTick();
            mCalc = new AnimationCalculator(capA.getStunDuration(), capA.getStunFrame(), partialTicks);
            animateStun(capA.getStunFrame(), capA.getStunDuration(), player, mCalc, rotVec, ClientEngine.get().getPartialTick());
            //Update matrix stack
            rotVec = mCalc.getTransformations();
            matrix.mulPose(Vector3f.XP.rotationDegrees(rotVec.x()));
            matrix.mulPose(Vector3f.YP.rotationDegrees(rotVec.y()));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(rotVec.z()));
            //Re-align head
            head.yRot -= MathUtil.toRadians(rotVec.y());
        }
        return MathUtil.toRadians(rotVec.y());
    }

    public void animateStun(int frame, int duration, Player user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        //This assumes frame gets updated; if it doesn't this value will change every tick
        int dir = (user.tickCount - frame) % 2 == 0 ? -1 : 1;
        float mag = Mth.clamp(duration / 10F, 0.5F, 1F);
        stunPart(head, frame, duration, partialTicks, -12F * mag, 1);
        if(!ActionTracker.get(user).getAction().isEmpty()) {
            stunPartToDefault(rightHand, rightHandData, frame, duration, partialTicks);
            stunPartToDefault(rightArm, rightArmData, frame, duration, partialTicks);
            stunPartToDefault(leftHand, leftHandData, frame, duration, partialTicks);
            stunPartToDefault(leftArm, leftArmData, frame, duration, partialTicks);
            stunPartToDefault(rightLeg, rightLegData, frame, duration, partialTicks);
            stunPartToDefault(leftLeg, leftLegData, frame, duration, partialTicks);
        }
        else {
            stunPartToDefaultWithPause(rightHand, rightHandData, frame, duration, partialTicks, 35 * mag, -dir);
            stunPartToDefaultWithPause(leftHand, leftHandData, frame, duration, partialTicks, 35 * mag, dir);
            stunPartToDefaultWithPause(rightLeg, rightLegData, frame, duration, partialTicks, 40 * mag, dir);
            stunPartToDefaultWithPause(leftLeg, leftLegData, frame, duration, partialTicks, 40 * mag, -dir);
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

    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HAND_RIGHT, rightHandData);
        map.put(EntityPart.ARM_RIGHT, rightArmData);
        map.put(EntityPart.HAND_LEFT, leftHandData);
        map.put(EntityPart.ARM_LEFT, leftArmData);
        map.put(EntityPart.LEG_RIGHT, rightLegData);
        map.put(EntityPart.LEG_LEFT, leftLegData);
        return map;
    }

        /**
         * Updates all of the animation data from corresponding model parts
         * Rotation is converted from radians to degrees
         */
    public void readDataFromModel(IActionTracker capC, float partialTicks) {
        leftArmData.readFromModelPart(leftArm);
        leftArmData.update(capC.getFrame(), capC.getDuration(), partialTicks);

        rightArmData.readFromModelPart(rightArm);
        rightArmData.update(capC.getFrame(), capC.getDuration(), partialTicks);

        leftHandData.readFromModelPart(leftHand);
        leftHandData.update(capC.getFrame(), capC.getDuration(), partialTicks);

        rightHandData.readFromModelPart(rightHand);
        rightHandData.update(capC.getFrame(), capC.getDuration(), partialTicks);

        leftLegData.readFromModelPart(leftLeg);
        leftLegData.update(capC.getFrame(), capC.getDuration(), partialTicks);

        rightLegData.readFromModelPart(rightLeg);
        rightLegData.update(capC.getFrame(), capC.getDuration(), partialTicks);
    }

    /**
     * Overwrites model part data with corresponding animation data
     * Expects rotation to be in degrees
     */
    public void writeDataToModel() {
        leftArmData.writeToModelPart(leftArm);
        rightArmData.writeToModelPart(rightArm);
        leftHandData.writeToModelPart(leftHand);
        rightHandData.writeToModelPart(rightHand);
        leftLegData.writeToModelPart(leftLeg);
        rightLegData.writeToModelPart(rightLeg);
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack stack) {
        ModelPart arm = getArm(side);
        ModelPart hand = getHand(side);
        if(slim) {
            float f = 0.5F * (float)(side == HumanoidArm.RIGHT ? 1 : -1);
            arm.x += f;
            arm.translateAndRotate(stack);
            hand.translateAndRotate(stack);
            arm.x -= f;
        } else {
            arm.translateAndRotate(stack);
            hand.translateAndRotate(stack);
        }
    }

    @Override
    public ModelPart getRandomModelPart(Random p_228288_1_) {
        return cubes.get(p_228288_1_.nextInt(cubes.size()));
    }

    public ModelPart getHand(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? leftHand : rightHand;
    }

    @Override
    public ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? leftArm : rightArm;
    }

    @Override
    public void setAllVisible(boolean p_178719_1_) {
        super.setAllVisible(p_178719_1_);
        leftHand.visible = p_178719_1_;
        rightHand.visible = p_178719_1_;
        leftSleeveN.visible = p_178719_1_;
        rightSleeveN.visible = p_178719_1_;
        cloakN.visible = p_178719_1_;
    }

    public void copyPropertiesTo(ArmorModel model) {
        model.attackTime = attackTime;
        model.riding = riding;
        model.young = young;

        model.head.copyFrom(head);
        model.body.copyFrom(body);
        model.innerBody.copyFrom(body);
        model.rightArm.copyFrom(rightArm);
        model.rightHand.copyFrom(rightHand);
        model.leftArm.copyFrom(leftArm);
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
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, hat,
                leftPants, rightPants, jacket);
    }


    @Override
    public void renderCloak(PoseStack p_228289_1_, VertexConsumer p_228289_2_, int p_228289_3_, int p_228289_4_) {
        cloakN.render(p_228289_1_, p_228289_2_, p_228289_3_, p_228289_4_);
    }

    protected void poseRightArm(T player) {
        switch(rightArmPose) {
            case EMPTY:
                rightArm.xRot *= 0.85F;
                rightArm.yRot = 0.0F;
                break;
            case BLOCK:
                rightArm.xRot = rightArm.xRot * 0.5F - 0.9424779F;
                rightArm.yRot = (-MathUtil.PI / 6F);
                break;
            case ITEM:
                rightArm.xRot = rightArm.xRot * 0.45F - (MathUtil.PI / 10F);
                rightArm.yRot = 0.0F;
                break;
            case THROW_SPEAR:
                rightArm.xRot = rightArm.xRot * 0.5F - MathUtil.PI;
                rightArm.yRot = 0.0F;
                break;
            case BOW_AND_ARROW:
                rightArm.yRot = -0.1F + head.yRot;
                leftArm.yRot = 0.1F + head.yRot + 0.4F;
                rightArm.xRot = (-MathUtil.PI / 2F) + head.xRot;
                leftArm.xRot = (-MathUtil.PI / 2F) + head.xRot;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(rightArm, leftArm, player, true);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, true);
                break;
            case SPYGLASS:
                rightArm.xRot = Mth.clamp(head.xRot - 1.9198622F - (player.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
                rightArm.yRot = head.yRot - 0.2617994F;
        }

    }

    protected void poseLeftArm(T player) {
        switch (leftArmPose) {
            case EMPTY:
                leftArm.xRot *= 0.85F;
                leftArm.yRot = 0.0F;
                break;
            case BLOCK:
                leftArm.xRot = leftArm.xRot * 0.45F - 0.9424779F;
                leftArm.yRot = ((float) Math.PI / 6F);
                break;
            case ITEM:
                leftArm.xRot = leftArm.xRot * 0.5F - ((float) Math.PI / 10F);
                leftArm.yRot = 0.0F;
                break;
            case THROW_SPEAR:
                leftArm.xRot = leftArm.xRot * 0.5F - (float) Math.PI;
                leftArm.yRot = 0.0F;
                break;
            case BOW_AND_ARROW:
                rightArm.yRot = -0.1F + head.yRot - 0.4F;
                leftArm.yRot = 0.1F + head.yRot;
                rightArm.xRot = (-(float) Math.PI / 2F) + head.xRot;
                leftArm.xRot = (-(float) Math.PI / 2F) + head.xRot;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(rightArm, leftArm, player, false);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, false);
                break;
            case SPYGLASS:
                leftArm.xRot = Mth.clamp(head.xRot - 1.9198622F - (player.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
                leftArm.yRot = head.yRot + 0.2617994F;
        }
    }

    protected HumanoidArm getSwingingArm(T entity) {
        HumanoidArm humanoidarm = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
    }
}
