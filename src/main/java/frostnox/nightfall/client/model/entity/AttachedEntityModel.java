package frostnox.nightfall.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.entity.EntityPart;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class AttachedEntityModel extends Model {
    public final ModelPart parent, child;
    public final EntityPart attachedPart;

    public AttachedEntityModel(ModelPart root, EntityPart attachedPart) {
        super(RenderType::entityCutoutNoCull);
        this.parent = root.getChild("parent");
        this.child = parent.getChild("child");
        this.attachedPart = attachedPart;
    }

    public void copyFrom(ModelPart part) {
        parent.copyFrom(part);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        parent.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    public static LayerDefinition createBackpackLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition parent = partdefinition.addOrReplaceChild("parent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition child = parent.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, 2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createPouchLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition parent = partdefinition.addOrReplaceChild("parent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition child = parent.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r1 = child.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -0.5F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition cube_r2 = child.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createMaskLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition parent = partdefinition.addOrReplaceChild("parent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition child = parent.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -1.5F, -4.5F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 16);
    }

    public static LayerDefinition createNecklaceLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition parent = partdefinition.addOrReplaceChild("parent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition child = parent.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    public static LayerDefinition createLanternLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition parent = partdefinition.addOrReplaceChild("parent", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition child = parent.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -0.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r1 = child.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -0.5F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition cube_r2 = child.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -1.5F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}
