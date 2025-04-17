package frostnox.nightfall.client.model.entity;

import frostnox.nightfall.entity.EntityPart;
import net.minecraft.client.model.geom.ModelPart;

public interface IHumanoidModel {
    ModelPart getModelPart(EntityPart part);
}
