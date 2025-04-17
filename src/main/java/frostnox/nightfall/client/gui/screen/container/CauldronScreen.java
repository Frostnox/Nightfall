package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.block.cauldron.CauldronBlockEntity;
import frostnox.nightfall.world.inventory.StorageContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class CauldronScreen extends SimpleContainerScreen<StorageContainer> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/cauldron.png");

    public CauldronScreen(StorageContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void innerRender(PoseStack stack, int mouseX, int mouseY, float partial) {
        CauldronBlockEntity cauldron = (CauldronBlockEntity) menu.entity;
        int cookTicks = cauldron.getCookTicks();
        if(cookTicks > 0) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            int progress = Mth.clamp(1 + cookTicks * 12 / cauldron.getCookTicksTotal(), 2, 13);
            blit(stack, leftPos + 82, topPos + 58 + 13 - progress, 0, 166 + 13 - progress, 12, progress);
        }
    }

    @Override
    public ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected ResourceLocation getInventoryTexture() {
        return INVENTORY_TERRACOTTA_TEXTURE;
    }
}
