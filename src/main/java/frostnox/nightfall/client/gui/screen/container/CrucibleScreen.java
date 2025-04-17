package frostnox.nightfall.client.gui.screen.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.Nightfall;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.crucible.CrucibleBlock;
import frostnox.nightfall.block.block.crucible.CrucibleContainer;
import frostnox.nightfall.block.fluid.MetalFluid;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.gui.screen.PartialInventoryScreen;
import frostnox.nightfall.registry.forge.FluidsNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidStack;

public class CrucibleScreen extends PartialInventoryScreen<CrucibleContainer> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightfall.MODID, "textures/gui/screen/crucible.png");
    public static final TranslatableComponent SOLID = new TranslatableComponent(Nightfall.MODID + ".crucibleMenu.solid");
    public static final TranslatableComponent MOLTEN = new TranslatableComponent(Nightfall.MODID + ".crucibleMenu.molten");

    public CrucibleScreen(CrucibleContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation getInventoryTexture() {
        return INVENTORY_TERRACOTTA_TEXTURE;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partial) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partial);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = leftPos + 61;
        int y = topPos + 26;
        stack.pushPose();
        RenderSystem.disableDepthTest();
        for(int i = 0; i < 4; i++) {
            int cookTicks = menu.entity.cookTicks.getInt(i);
            if(cookTicks > 0) {
                int slotX = i % 2 == 0 ? (x + 2) : (x + 2 + 18);
                int slotY = i / 2 == 0 ? (y + 2) : (y + 2 + 18);
                int progress = Mth.clamp(cookTicks * 12 / menu.entity.cookDurations.getInt(i), 1, 12);
                blit(stack, slotX, slotY + 12 - progress, 0, 166 + 12 - progress, 12, progress);
            }
        }
        RenderSystem.enableDepthTest();
        stack.popPose();
        x = leftPos + 106;
        y = topPos + 26;
        int width = ClientEngine.get().atlasWidth;
        int height = ClientEngine.get().atlasHeight;
        int maxUnits = menu.entity.getFluidCapacity(menu.entity.getBlockState());
        float temperature = TieredHeat.fromTier(menu.entity.getBlockState().getValue(CrucibleBlock.HEAT)).getBaseTemp();
        for(FluidStack fluid : menu.entity.fluids) {
            boolean molten = fluid.getFluid() instanceof MetalFluid metalFluid && temperature >= metalFluid.metal.getMeltTemp();
            int color = fluid.getFluid().getAttributes().getColor();
            RenderSystem.setShaderColor(((color & 0x00FF0000) >> 16) / 255F, ((color & 0x0000FF00) >> 8) / 255F, (color & 0x000000FF) / 255F, ((color & 0xFF000000) >> 24) / 255F);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(molten ? fluid.getFluid().getAttributes().getStillTexture() : FluidsNF.METAL_SOLID);
            RenderSystem.setShaderTexture(0, sprite.atlas().location());
            RenderSystem.disableBlend(); //Durability bar on items enables this when dragged which causes the fluid texture to be invisible
            int amount = Math.max(1, Math.round(34F * fluid.getAmount() / maxUnits));
            int yAmount = Math.min(amount, 16);
            blit(stack, x, y + 34 - yAmount, sprite.getU0() * width, sprite.getV0() * height + 16 - yAmount, 9, yAmount, width, height);
            if(amount > 16) {
                yAmount = Math.min(amount, 32) - 16;
                blit(stack, x, y + 18 - yAmount, sprite.getU0() * width, sprite.getV0() * height + 16 - yAmount, 9, yAmount, width, height);
            }
            if(amount > 32) {
                yAmount = Math.min(amount, 34) - 32;
                blit(stack, x, y + 2 - yAmount, sprite.getU0() * width, sprite.getV0() * height + 16 - yAmount, 9, yAmount, width, height);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if(mouseX >= x && mouseX < x + 9 && mouseY >= y + 34 - amount && mouseY < y + 34) {
                renderTooltip(stack, new TextComponent(fluid.getAmount() + " " + (molten ? MOLTEN.getString() : SOLID.getString()) + " ").append(fluid.getDisplayName()), Mth.floor(mouseX), Mth.floor(mouseY));
            }
            y -= amount;
        }
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mouseX, int mouseY) {
        super.renderBg(stack, partial, mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
