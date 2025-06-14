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
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.EnumMap;
import java.util.List;

public class JellyfishOuterModel extends AnimatedModel<JellyfishEntity> implements HeadedModel {
    private final AnimatedModelPart bell;

    public JellyfishOuterModel(ModelPart root) {
        super(root);
        this.bell = (AnimatedModelPart) root.getChild("bell");
    }

    public static LayerDefinition createOuterLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bell = partdefinition.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(JellyfishEntity entity, float limbSwing, float limbSwingAmount, float time, float netHeadYaw, float headPitch) {
        resetPose();
        float speed = 3.3F;
        limbSwingAmount = Math.min(1F, limbSwingAmount * 2);
        //limbSwing = time;
        //limbSwingAmount = 1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bell.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public EnumMap<EntityPart, AnimationData> getDataFromModel() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.HEAD, bell.animationData);
        return map;
    }

    @Override
    public void animateStun(int frame, int duration, int dir, float mag, JellyfishEntity user, AnimationCalculator mCalc, Vector3f mVec, float partialTicks) {

    }

    @Override
    protected List<AnimatedModelPart> getNoStunParts() {
        return List.of();
    }

    @Override
    public ModelPart getHead() {
        return bell;
    }
}
