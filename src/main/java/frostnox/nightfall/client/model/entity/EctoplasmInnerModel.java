package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.monster.EctoplasmEntity;
import frostnox.nightfall.util.AnimationUtil;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.EnumMap;
import java.util.List;

public class EctoplasmInnerModel extends AnimatedModel<EctoplasmEntity> {
    private static float DIST = 0.85F, FREQ = 0.008F;
    private static float[] Y_DIST = new float[] {DIST * 0.7F, DIST * 0.9F, DIST * 0.75F, DIST * 0.85F, DIST * 0.72F, DIST * 1.1F, DIST * 0.93F, DIST * 1.04F, DIST * 0.88F, DIST * 1F};
    private static float[] Y_OFF = new float[] {0, MathUtil.PI * 0.5F, MathUtil.PI * 1.5F, MathUtil.PI * 0.75F, MathUtil.PI * 1.75F, MathUtil.PI * 0.25F, MathUtil.PI * 1.25F, MathUtil.PI * 2F, MathUtil.PI * 0.375F, MathUtil.PI * 1.625F};
    private static float[] Y_FREQ = new float[] {FREQ * 1F, FREQ * 0.7F, FREQ * 0.8F, FREQ * 0.95F, FREQ * 1.05F, FREQ * 0.83F, FREQ * 0.98F, FREQ * 1.1F, FREQ * 0.76F, FREQ * 0.9F};
    private static boolean[] DO_X_ROT = new boolean[] {false, true, false, true, false, true, true, false, false, false};
    private static float[] XZ_FREQ = new float[] {FREQ * 0.8F, FREQ * 0.9F, FREQ * 0.85F, FREQ * 0.7F, FREQ * 0.8F, FREQ * 0.92F, FREQ * 0.72F, FREQ * 0.78F, FREQ * 1.08F, FREQ * 0.87F};
    private final AnimatedModelPart bones;
    private final AnimatedModelPart skull;
    private final AnimatedModelPart clubCentered;
    private final AnimatedModelPart club;
    private final AnimatedModelPart bone1;
    private final AnimatedModelPart bone2;
    private final AnimatedModelPart bone3;
    private final AnimatedModelPart bone4;
    private final AnimatedModelPart bone5;
    private final AnimatedModelPart bone6;
    private final AnimatedModelPart bone7;
    private final AnimatedModelPart bone8;
    private final AnimatedModelPart bone9;
    private AnimatedModelPart[] animParts;
    private List<AnimatedModelPart> noStunParts;

    public EctoplasmInnerModel(ModelPart root) {
        super(root);
        this.bones = (AnimatedModelPart) root.getChild("bones");
        this.skull = (AnimatedModelPart) this.bones.getChild("skull");
        this.clubCentered = (AnimatedModelPart) this.bones.getChild("clubCentered");
        this.club = (AnimatedModelPart) this.clubCentered.getChild("club");
        this.bone1 = (AnimatedModelPart) this.bones.getChild("bone1");
        this.bone2 = (AnimatedModelPart) this.bones.getChild("bone2");
        this.bone3 = (AnimatedModelPart) this.bones.getChild("bone3");
        this.bone4 = (AnimatedModelPart) this.bones.getChild("bone4");
        this.bone5 = (AnimatedModelPart) this.bones.getChild("bone5");
        this.bone6 = (AnimatedModelPart) this.bones.getChild("bone6");
        this.bone7 = (AnimatedModelPart) this.bones.getChild("bone7");
        this.bone8 = (AnimatedModelPart) this.bones.getChild("bone8");
        this.bone9 = (AnimatedModelPart) this.bones.getChild("bone9");
        animParts = new AnimatedModelPart[] {skull, bone1, bone2, bone3, bone4, bone5, bone6, bone7, bone8, bone9};
        noStunParts = List.of(bones);
    }

    public static LayerDefinition createLargeLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bones = partdefinition.addOrReplaceChild("bones", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition skull = bones.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.5236F, 0.3491F));

        PartDefinition clubCentered = bones.addOrReplaceChild("clubCentered", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition club = clubCentered.addOrReplaceChild("club", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(13, 0).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -8.5F, 0.0F, 0, 0.0F));

        PartDefinition bone1 = bones.addOrReplaceChild("bone1", CubeListBuilder.create().texOffs(21, 9).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 2.0F, 7.0F, -0.7854F, -0.4363F, 0.0F));

        PartDefinition bone2 = bones.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(21, 9).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 9.5F, -5.0F, 0.0F, 0.5236F, -1.2217F));

        PartDefinition bone3 = bones.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(2, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.5F, 8.0F, -6.0F, -0.6981F, 1.0472F, 0.0F));

        PartDefinition bone4 = bones.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(5, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5F, 4.0F, -6.0F, 0.0F, -1.309F, -0.7854F));

        PartDefinition bone5 = bones.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(1, 17).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 7.5F, 9.0F, -1.3963F, -0.3491F, 0.0F));

        PartDefinition bone6 = bones.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(21, 9).addBox(-1.0F, -3.5F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 8.0F, 7.0F, -0.7854F, 1.7453F, 0.0F));

        PartDefinition bone7 = bones.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(8, 17).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -10.5F, -10.5F, 0.0F, -1.3963F, -0.8727F));

        PartDefinition bone8 = bones.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(4, 18).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -4.0F, -11.0F, 0.0F, -1.309F, 1.9199F));

        PartDefinition bone9 = bones.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(6, 20).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -10.5F, 11.5F, 0.0F, 1.2217F, 1.309F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createMediumLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bones = partdefinition.addOrReplaceChild("bones", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition skull = bones.addOrReplaceChild("skull", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, -6.0F, 0.0F, 0.0F, 0.5236F, 0.3491F));

        PartDefinition clubCentered = bones.addOrReplaceChild("clubCentered", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition club = clubCentered.addOrReplaceChild("club", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(13, 0).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 8.0F, 0.0F, 0.0F, 0, 0.0F));

        PartDefinition bone1 = bones.addOrReplaceChild("bone1", CubeListBuilder.create().texOffs(21, 9).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 1.0F, 4.5F, -0.7854F, -0.4363F, 0.0F));

        PartDefinition bone2 = bones.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5F, 4.5F, -5.0F, 0.0F, 0.5236F, -1.2217F));

        PartDefinition bone3 = bones.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(2, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 5.5F, -5.0F, -0.6981F, 1.0472F, 0.0F));

        PartDefinition bone4 = bones.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(5, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.5F, -6.0F, 0.0F, -1.309F, -0.7854F));

        PartDefinition bone5 = bones.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(1, 17).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, 5.0F, 5.0F, -1.3963F, -0.3491F, 0.0F));

        PartDefinition bone6 = bones.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 3.0F, 7.0F, -0.7854F, 1.7453F, 0.0F));

        PartDefinition bone7 = bones.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(-11.5F, -15.5F, -10.5F, 0.0F, -1.3963F, -0.8727F));

        PartDefinition bone8 = bones.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(4, 18).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.5F, -5.5F, 0.0F, -1.309F, 1.9199F));

        PartDefinition bone9 = bones.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, -16.5F, 12.5F, 0.0F, 1.2217F, 1.309F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    public static LayerDefinition createSmallLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bones = partdefinition.addOrReplaceChild("bones", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));

        PartDefinition skull = bones.addOrReplaceChild("skull", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, -11.0F, 0.0F, 0.0F, 0.5236F, 0.3491F));

        PartDefinition clubCentered = bones.addOrReplaceChild("clubCentered", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 3.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition club = clubCentered.addOrReplaceChild("club", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0, 0.0F));

        PartDefinition bone1 = bones.addOrReplaceChild("bone1", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, -4.0F, 4.5F, -0.7854F, -0.4363F, 0.0F));

        PartDefinition bone2 = bones.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5F, -0.5F, -5.0F, 0.0F, 0.5236F, -1.2217F));

        PartDefinition bone3 = bones.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.5F, -5.0F, -0.6981F, 1.0472F, 0.0F));

        PartDefinition bone4 = bones.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(5, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.5F, -2.0F, 0.0F, -1.309F, -0.7854F));

        PartDefinition bone5 = bones.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(1, 17).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.5F, 1.0F, -1.3963F, -0.3491F, 0.0F));

        PartDefinition bone6 = bones.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, -2.0F, 7.0F, -0.7854F, 1.7453F, 0.0F));

        PartDefinition bone7 = bones.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(-11.5F, -20.5F, -10.5F, 0.0F, -1.3963F, -0.8727F));

        PartDefinition bone8 = bones.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, -2.5F, -5.5F, 0.0F, -1.309F, 1.9199F));

        PartDefinition bone9 = bones.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, -21.5F, 12.5F, 0.0F, 1.2217F, 1.309F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(EctoplasmEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        clubCentered.yRot += MathUtil.toRadians(180 + entity.getViewYRot(ClientEngine.get().getPartialTick()));
        float speed = 60F;
        for(int i = 0; i < animParts.length; i++) {
            AnimatedModelPart part = animParts[i];
            translateY(part, Y_DIST[i], speed, Y_OFF[i], 0, ageInTicks, 1, Easing.inOutSine, true);
            part.yRot += (ageInTicks * Y_FREQ[i]) % (MathUtil.PI * 2);
            if(DO_X_ROT[i]) part.xRot += (ageInTicks * XZ_FREQ[i]) % (MathUtil.PI * 2);
            else part.zRot += (ageInTicks * XZ_FREQ[i]) % (MathUtil.PI * 2);
        }
        translateY(club, 0.8F, speed, MathUtil.PI * 1.125F, 0, ageInTicks, 1, Easing.inOutSine, true);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bones.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY_2, bones.animationData);
        map.put(EntityPart.ARM_RIGHT, clubCentered.animationData);
        map.put(EntityPart.HAND_RIGHT, club.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, EctoplasmEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {
        super.animateStun(frame, duration, dir, mag, user, mCalc, mVec, partialTicks);
        float progress = AnimationUtil.getStunProgress(frame, duration, partialTicks);
        if(frame < duration / 2) progress = Easing.outQuart.apply(progress);
        else progress = Easing.inOutSine.apply(progress);
        bones.y += switch(user.size) {
            case LARGE -> 3;
            case MEDIUM -> 1.5F;
            case SMALL -> 1;
        } * progress;
    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return noStunParts;
    }
}
