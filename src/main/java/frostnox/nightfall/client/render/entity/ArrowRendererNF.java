package frostnox.nightfall.client.render.entity;

import frostnox.nightfall.entity.entity.projectile.ArrowEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ArrowRendererNF extends ArrowRenderer<ArrowEntity> {
    private static final Map<Item, ResourceLocation> TEXTURES = new Object2ObjectArrayMap<>(6);

    public ArrowRendererNF(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(ArrowEntity arrow) {
        Item item = arrow.getItem();
        ResourceLocation texture = TEXTURES.get(item);
        if(texture == null) {
            ResourceLocation id = item.getRegistryName();
            texture = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/entity/arrow/" + id.getPath() + ".png");
            TEXTURES.put(item, texture);
        }
        return texture;
    }
}