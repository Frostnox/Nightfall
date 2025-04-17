package frostnox.nightfall.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.client.model.entity.IHumanoidModel;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.item.client.IAnimatedEquipment;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.util.LevelUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class EquipmentLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends CustomHeadLayer<T, M> {
    private static final Map<Item, ResourceLocation> TEXTURES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, ResourceLocation> DYED_OVERLAY_TEXTURES = new Object2ObjectOpenHashMap<>();
    private final Map<Item, AttachedEntityModel> MODEL_MAP = new Object2ObjectOpenHashMap<>();

    public EquipmentLayer(RenderLayerParent<T, M> parent, EntityRendererProvider.Context renderer) {
        super(parent, renderer.getModelSet());
        for(Item item : ForgeRegistries.ITEMS.getValues()) {
            Pair<EntityPart, ModelLayerLocation> data = ModelRegistryNF.getEquipment(item);
            if(data != null) MODEL_MAP.put(item, new AttachedEntityModel(renderer.bakeLayer(data.right()), data.left()));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        for(EquipmentSlot slot : LevelUtil.ARMOR_SLOTS) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if(!itemStack.isEmpty() && !(itemStack.getItem() instanceof TieredArmorItem)) {
                Item item = itemStack.getItem();
                if(!(item instanceof TieredArmorItem)) {
                    if(slot == EquipmentSlot.HEAD && !MODEL_MAP.containsKey(item)) {
                        super.render(poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                    }
                    else renderItem(itemStack, MODEL_MAP.get(item), poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                }
            }
        }
    }

    protected void renderItem(ItemStack itemStack, AttachedEntityModel model, PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if(model == null) return;
        poseStack.pushPose();
        Item item = itemStack.getItem();
        if(getParentModel() instanceof IHumanoidModel parentModel) model.copyFrom(parentModel.getModelPart(model.attachedPart));
        if(item instanceof IAnimatedEquipment animated) animated.animate(model, itemStack, poseStack, buffer, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        boolean foil = itemStack.hasFoil();
        if(item instanceof DyeableLeatherItem dyeable) {
            int color = dyeable.getColor(itemStack);
            //color = DyeType.values()[(Minecraft.getInstance().player.tickCount / 40) % DyeType.values().length].getColor();
            float r = (float)(color >> 16 & 255) / 255F;
            float g = (float)(color >> 8 & 255) / 255F;
            float b = (float)(color & 255) / 255F;
            renderModel(poseStack, buffer, light, foil, model, r, g, b, getResource(item, true));
        }
        renderModel(poseStack, buffer, light, foil, model, 1F, 1F, 1F, getResource(item, false));
        poseStack.popPose();
    }

    protected void renderModel(PoseStack stack, MultiBufferSource buffer, int light, boolean foil, AttachedEntityModel model, float r, float g, float b, ResourceLocation loc) {
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, model.renderType(loc), false, foil);
        model.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1F);
    }

    protected static ResourceLocation getResource(Item item, boolean overlay) {
        ResourceLocation location = (overlay ? DYED_OVERLAY_TEXTURES : TEXTURES).get(item);
        if(location == null) {
            ResourceLocation name = item.getRegistryName();
            String path = overlay ? (name.getPath() + "_overlay") : name.getPath();
            location = ResourceLocation.parse(String.format("%s:textures/models/equipment/%s.png", name.getNamespace(), path));
            (overlay ? DYED_OVERLAY_TEXTURES : TEXTURES).put(item, location);
        }
        return location;
    }
}
