package frostnox.nightfall.client.render.entity.layer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.client.model.entity.AnimatedHumanoidModel;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.ArmorModel;
import frostnox.nightfall.client.model.entity.PlayerModelNF;
import frostnox.nightfall.item.ITieredArmorMaterial;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import javax.annotation.Nullable;
import java.util.Map;

public class ArmorLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final Map<ITieredArmorMaterial, ResourceLocation> TEXTURES = Maps.newHashMap();
    private static final Map<ITieredArmorMaterial, ResourceLocation> OVERLAY_TEXTURES = Maps.newHashMap();
    private static final Map<String, ResourceLocation> VANILLA_TEXTURES = Maps.newHashMap();
    private final Map<ITieredArmorMaterial, ArmorModel> MODEL_MAP = Maps.newHashMap();
    private final ArmorModel innerModel, outerModel; //Vanilla models

    public ArmorLayer(RenderLayerParent<T, M> parentLayer, EntityRendererProvider.Context renderer, ImmutableMap<String, Vec3f> scaleMap) {
        super(parentLayer);
        innerModel = new ArmorModel(renderer.bakeLayer(ModelRegistryNF.INNER_ARMOR), scaleMap);
        outerModel = new ArmorModel(renderer.bakeLayer(ModelRegistryNF.OUTER_ARMOR), scaleMap);
        for(var material : RegistriesNF.getTieredArmorMaterials()) {
            boolean skip = false;
            for(ITieredArmorMaterial key : MODEL_MAP.keySet()) {
                if(key.getStyledArmorName().equals(material.value.getStyledArmorName())) {
                    MODEL_MAP.put(material.value, MODEL_MAP.get(key));
                    skip = true;
                    break;
                }
            }
            if(!skip) MODEL_MAP.put(material.value, new ArmorModel(renderer.bakeLayer(ModelRegistryNF.getArmor(material.value)), scaleMap));
        }
    }

    public ArmorLayer(RenderLayerParent<T, M> parentLayer, EntityRendererProvider.Context renderer) {
        this(parentLayer, renderer, ImmutableMap.of());
    }

    @Override
    public void render(PoseStack p_225628_1_, MultiBufferSource p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        renderArmorPiece(p_225628_1_, p_225628_2_, p_225628_4_, EquipmentSlot.CHEST, p_225628_3_);
        renderArmorPiece(p_225628_1_, p_225628_2_, p_225628_4_, EquipmentSlot.LEGS, p_225628_3_);
        renderArmorPiece(p_225628_1_, p_225628_2_, p_225628_4_, EquipmentSlot.FEET, p_225628_3_);
        renderArmorPiece(p_225628_1_, p_225628_2_, p_225628_4_, EquipmentSlot.HEAD, p_225628_3_);
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot slot, int light) {
        ItemStack itemStack = entity.getItemBySlot(slot);
        EquipmentSlot armorSlot = null;
        ArmorModel model = null;
        ResourceLocation loc = null;
        boolean vanilla = false;
        if(itemStack.getItem() instanceof TieredArmorItem armor) {
            armorSlot = armor.slot;
            model = getArmorModel(entity, armor, armorSlot);
            loc = getArmorResource(itemStack);
        }
        else if(itemStack.getItem() instanceof ArmorItem armor) {
            armorSlot = armor.getSlot();
            model = getVanillaArmorModel(entity, armor, armorSlot);
            loc = getVanillaArmorResource(entity, itemStack, slot, null);
            vanilla = true;
        }
        if(armorSlot == slot) {
            if(getParentModel() instanceof PlayerModelNF parentModel) parentModel.copyPropertiesTo(model);
            else if(getParentModel() instanceof AnimatedHumanoidModel parentModel) parentModel.copyPropertiesTo(model);
            setPartVisibility(entity, model, slot, vanilla);
            boolean foil = itemStack.hasFoil();
            if(itemStack.getItem() instanceof DyeableLeatherItem dyeable) {
                int color = dyeable.getColor(itemStack);
                //color = DyeType.values()[(Minecraft.getInstance().player.tickCount / 40) % DyeType.values().length].getColor();
                //color = 0x844545;
                float r = (float)(color >> 16 & 255) / 255F;
                float g = (float)(color >> 8 & 255) / 255F;
                float b = (float)(color & 255) / 255F;
                if(vanilla) {
                    renderModel(poseStack, bufferSource, light, foil, model, r, g, b, loc);
                    renderModel(poseStack, bufferSource, light, foil, model, 1F, 1F, 1F, getVanillaArmorResource(entity, itemStack, slot, "overlay"));
                    return;
                }
                renderModel(poseStack, bufferSource, light, foil, model, r, g, b, getArmorOverlayResource(itemStack));
            }
            renderModel(poseStack, bufferSource, light, foil, model, 1F, 1F, 1F, loc);
        }
    }

    protected ArmorModel getArmorModel(Entity entity, TieredArmorItem armor, EquipmentSlot slot) {
        return MODEL_MAP.get(armor.material);
    }

    protected ArmorModel getVanillaArmorModel(Entity entity, ArmorItem armor, EquipmentSlot slot) {
        return usesInnerModel(slot) ? innerModel : outerModel;
    }

    private void renderModel(PoseStack p_117107_, MultiBufferSource p_117108_, int light, boolean p_117111_, ArmorModel p_117112_, float p_117114_, float p_117115_, float p_117116_, ResourceLocation armorResource) {
        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(p_117108_, RenderType.armorCutoutNoCull(armorResource), false, p_117111_);
        p_117112_.renderToBuffer(p_117107_, vertexconsumer, light, OverlayTexture.NO_OVERLAY, p_117114_, p_117115_, p_117116_, 1F);
    }

    private boolean usesInnerModel(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS;
    }

    protected void setPartVisibility(T entity, ArmorModel model, EquipmentSlot slot, boolean vanilla) {
        model.setAllVisible(false);
        switch(slot) {
            case HEAD:
                model.bodyHeadJoint.visible = true;
                model.neck.visible = true;
                model.head.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.innerBody.visible = true;
                model.bodyRightArmJoint.visible = true;
                model.rightArm.visible = true;
                model.rightHand.visible = true;
                model.bodyLeftArmJoint.visible = true;
                model.leftArm.visible = true;
                model.leftHand.visible = true;
                model.rightSkirt.visible = true;
                model.leftSkirt.visible = true;
                break;
            case LEGS:
                model.body.visible = vanilla;
                model.innerBody.visible = entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            case FEET:
                if(vanilla) {
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                }
                else {
                    model.rightFoot.visible = true;
                    model.leftFoot.visible = true;
                }
        }
    }

    protected ResourceLocation getArmorResource(ItemStack stack) {
        TieredArmorItem item = (TieredArmorItem) stack.getItem();
        ITieredArmorMaterial material = item.material;
        ResourceLocation location = TEXTURES.get(material);

        if(location == null) {
            String path = material.getName();
            String domain = item.getRegistryName().getNamespace();
            int idx = path.indexOf(':');
            if(idx != -1) {
                domain = path.substring(0, idx);
                path = path.substring(idx + 1);
            }

            String string = String.format("%s:textures/models/armor/%s.png", domain, path);

            location = ResourceLocation.parse(string);
            TEXTURES.put(material, location);
        }

        return location;
    }

    protected ResourceLocation getArmorOverlayResource(ItemStack stack) {
        TieredArmorItem item = (TieredArmorItem) stack.getItem();
        ITieredArmorMaterial material = item.material;
        ResourceLocation location = OVERLAY_TEXTURES.get(material);

        if(location == null) {
            String path = material.getStyledArmorName();
            String domain = item.getRegistryName().getNamespace();
            int idx = path.indexOf(':');
            if(idx != -1) {
                domain = path.substring(0, idx);
                path = path.substring(idx + 1);
            }

            String string = String.format("%s:textures/models/armor/%s_overlay.png", domain, path);

            location = ResourceLocation.parse(string);
            OVERLAY_TEXTURES.put(material, location);
        }

        return location;
    }

    /**
     * @param entity Entity wearing the armor
     * @param stack ItemStack for the armor
     * @param slot Slot ID that the item is in
     * @param type Subtype, can be null or "overlay"
     * @return ResourceLocation pointing at the armor's texture
     */
    protected ResourceLocation getVanillaArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem)stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (usesInnerModel(slot) ? 2 : 1), type == null ? "" : String.format("_%s", type));

        s1 = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
        ResourceLocation resourcelocation = VANILLA_TEXTURES.get(s1);

        if (resourcelocation == null) {
            resourcelocation = ResourceLocation.parse(s1);
            VANILLA_TEXTURES.put(s1, resourcelocation);
        }

        return resourcelocation;
    }
}
