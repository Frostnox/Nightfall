package frostnox.nightfall.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Random;

public class MovingBlockRenderer extends EntityRenderer<MovingBlockEntity> {
    public MovingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(MovingBlockEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        BlockState state = pEntity.getBlockState();
        if(state.getRenderShape() == RenderShape.MODEL) {
            Level level = pEntity.getLevel();
            if(pEntity.canRender) {
                pMatrixStack.pushPose();
                BlockPos pos = new BlockPos(pEntity.getX(), pEntity.getBoundingBox().maxY + (pEntity.isSliding() ? 0.2 : 0), pEntity.getZ());
                pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
                BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
                for(RenderType type : RenderType.chunkBufferLayers()) {
                    if(ItemBlockRenderTypes.canRenderInLayer(state, type)) {
                        //Workaround for Forge bug that causes translucent blocks to not render
                        if(type == RenderType.translucent()) type = RenderType.cutout();
                        ForgeHooksClient.setRenderType(type);
                        renderer.getModelRenderer().tesselateBlock(level, renderer.getBlockModel(state), state, pos, pMatrixStack, pBuffer.getBuffer(type), false, new Random(), state.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY);
                    }
                }
                ForgeHooksClient.setRenderType(null);
                pMatrixStack.popPose();
                super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
            }
            //Render inside of block for one frame to compensate for chunk builder lag
            //This only works consistently at 60+ FPS and not at all below 30 FPS
            if(pEntity.canRender && !pEntity.isSliding() && pEntity.noPhysics) {
                pEntity.canRender = state != level.getBlockState(new BlockPos(pEntity.getEyePosition(pPartialTicks)));
            }
        }
    }
    
    public ResourceLocation getTextureLocation(MovingBlockEntity pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
