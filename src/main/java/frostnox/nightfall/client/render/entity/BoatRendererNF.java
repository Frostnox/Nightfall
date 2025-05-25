package frostnox.nightfall.client.render.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.entity.entity.BoatEntity;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.Map;

public class BoatRendererNF extends BoatRenderer {
    protected final BoatModel model;
    private static final Map<String, Pair<ResourceLocation, BoatModel>> TEXTURE_CACHE = Maps.newHashMap();

    public BoatRendererNF(EntityRendererProvider.Context pContext) {
        super(pContext);
        model = new BoatModel(pContext.bakeLayer(ModelRegistryNF.BOAT));
    }

    @Override
    public Pair<ResourceLocation, BoatModel> getModelWithLocation(Boat boat) {
        String key = ((BoatEntity) boat).getMaterial();
        Pair<ResourceLocation, BoatModel> pair = TEXTURE_CACHE.get(key);
        if(pair == null) {
            int split = key.indexOf(":");
            String nameSpace = key.substring(0, split);
            String name = key.substring(split + 1);
            pair = Pair.of(ResourceLocation.fromNamespaceAndPath(nameSpace, "textures/entity/boat/" + name + ".png"), model);
            TEXTURE_CACHE.put(key, pair);
        }
        return pair;
    }

    public static LayerDefinition createModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-14.0F, -8.0F, 0.0F, 28.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition front = partdefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-10.0F, -3.0F, -1.0F, 20.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(15.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition back = partdefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-10.0F, -3.0F, -1.0F, 20.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-15.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition right = partdefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-14.0F, -3.0F, -1.0F, 28.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition left = partdefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-14.0F, -3.0F, -1.0F, 28.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 9.0F));

        PartDefinition paddle_left = partdefinition.addOrReplaceChild("left_paddle", CubeListBuilder.create().texOffs(67, 4).mirror().addBox(-0.5F, -1.0F, -5.5F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(62, 0).mirror().addBox(-0.51F, -3.0F, 8.5F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -4.0F, 9.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition paddle_right = partdefinition.addOrReplaceChild("right_paddle", CubeListBuilder.create().texOffs(67, 4).mirror().addBox(-0.5F, -1.0F, -5.5F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(62, 0).mirror().addBox(-0.49F, -3.0F, 8.5F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.5F, -4.0F, -9.0F, -0.5236F, 3.1416F, 0.0F));

        partdefinition.addOrReplaceChild("water_patch", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
