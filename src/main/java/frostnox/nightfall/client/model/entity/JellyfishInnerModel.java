package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.client.model.AnimatedModel;
import frostnox.nightfall.client.model.AnimatedModelPart;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.entity.ambient.JellyfishEntity;
import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.EnumMap;
import java.util.List;

public class JellyfishInnerModel extends AnimatedModel<JellyfishEntity> {
    private final AnimatedModelPart gut, arms;

    public JellyfishInnerModel(ModelPart root) {
        super(root);
        this.gut = (AnimatedModelPart) root.getChild("gut");
        this.arms = (AnimatedModelPart) root.getChild("arms");
    }

    public static LayerDefinition createInnerLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition gut = partdefinition.addOrReplaceChild("gut", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition arms = partdefinition.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition cube_r1 = arms.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(12, 13).addBox(0.0F, -4.5F, -3.0F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r2 = arms.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 13).addBox(0.0F, -4.5F, -3.0F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(JellyfishEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        arms.visible = entity.isInWaterOrBubble();
        float speed = 3.3F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        //limbSwing = time;
        //limbSwingAmount = 1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gut.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        arms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, gut.animationData);
        map.put(EntityPart.BODY_2, arms.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, JellyfishEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {

    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return List.of();
    }
}
