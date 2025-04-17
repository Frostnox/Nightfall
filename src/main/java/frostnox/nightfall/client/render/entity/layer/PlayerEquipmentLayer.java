package frostnox.nightfall.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.model.entity.AttachedEntityModel;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.world.inventory.AccessoryInventory;
import frostnox.nightfall.world.inventory.AccessorySlot;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class PlayerEquipmentLayer<T extends Player, M extends EntityModel<T> & HeadedModel> extends EquipmentLayer<T, M> {
    private final Map<Item, AttachedEntityModel> MODEL_MAP = new Object2ObjectOpenHashMap<>();

    public PlayerEquipmentLayer(RenderLayerParent<T, M> parent, EntityRendererProvider.Context renderer) {
        super(parent, renderer);
        for(Item item : ForgeRegistries.ITEMS.getValues()) {
            Pair<EntityPart, ModelLayerLocation> data = ModelRegistryNF.getAccessory(item);
            if(data != null) MODEL_MAP.put(item, new AttachedEntityModel(renderer.bakeLayer(data.right()), data.left()));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        super.render(poseStack, buffer, light, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        AccessoryInventory accessories = PlayerData.get(player).getAccessoryInventory();
        for(AccessorySlot slot : AccessorySlot.values()) {
            ItemStack itemStack = accessories.getItem(slot);
            if(!itemStack.isEmpty()) renderItem(itemStack, MODEL_MAP.get(itemStack.getItem()), poseStack, buffer, light, player, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
