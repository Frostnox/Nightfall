package frostnox.nightfall.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.block.block.sign.StandingSignBlockNF;
import frostnox.nightfall.block.block.sign.WallSignBlockNF;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.client.render.blockentity.SignRendererNF;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignEditScreenNF extends SignEditScreen {
    public SignEditScreenNF(SignBlockEntity pSign, boolean pIsTextFilteringEnabled) {
        super(pSign, pIsTextFilteringEnabled);
    }

    @Override
    protected void init() {
        super.init();
        signModel = new SignRenderer.SignModel(minecraft.getEntityModels().bakeLayer(ModelRegistryNF.SIGN));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Lighting.setupForFlatItems();
        this.renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 40, 16777215);
        pPoseStack.pushPose();
        pPoseStack.translate((double)(this.width / 2), 0.0D, 50.0D);
        float f = 93.75F;
        pPoseStack.scale(93.75F, -93.75F, 93.75F);
        pPoseStack.translate(0.0D, -1.3125D, 0.0D);
        BlockState blockstate = this.sign.getBlockState();
        ITree type;
        boolean flag = blockstate.getBlock() instanceof StandingSignBlockNF;
        if(blockstate.getBlock() instanceof StandingSignBlockNF standingSign) {
            type = standingSign.type;
        }
        else {
            WallSignBlockNF wallSign = (WallSignBlockNF) blockstate.getBlock();
            type = wallSign.type;
            pPoseStack.translate(0.0D, -0.3125D, 0.0D);
        }

        boolean flag1 = this.frame / 6 % 2 == 0;
        float f1 = 0.6666667F;
        pPoseStack.pushPose();
        pPoseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        Material material = new Material(Sheets.SIGN_SHEET, SignRendererNF.getTexture(type));
        VertexConsumer vertexconsumer = material.buffer(multibuffersource$buffersource, this.signModel::renderType);
        this.signModel.stick.visible = flag;
        this.signModel.root.render(pPoseStack, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY);
        pPoseStack.popPose();
        float f2 = 0.010416667F;
        pPoseStack.translate(0.0D, (double)0.33333334F, (double)0.046666667F);
        pPoseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int i = this.sign.getColor().getTextColor();
        int j = this.signField.getCursorPos();
        int k = this.signField.getSelectionPos();
        int l = this.line * 10 - this.messages.length * 5;
        Matrix4f matrix4f = pPoseStack.last().pose();

        for(int i1 = 0; i1 < this.messages.length; ++i1) {
            String s = this.messages[i1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                float f3 = (float)(-this.minecraft.font.width(s) / 2);
                this.minecraft.font.drawInBatch(s, f3, (float)(i1 * 10 - this.messages.length * 5), i, false, matrix4f, multibuffersource$buffersource, false, 0, 15728880, false);
                if (i1 == this.line && j >= 0 && flag1) {
                    int j1 = this.minecraft.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
                    int k1 = j1 - this.minecraft.font.width(s) / 2;
                    if (j >= s.length()) {
                        this.minecraft.font.drawInBatch("_", (float)k1, (float)l, i, false, matrix4f, multibuffersource$buffersource, false, 0, 15728880, false);
                    }
                }
            }
        }

        multibuffersource$buffersource.endBatch();

        for(int i3 = 0; i3 < this.messages.length; ++i3) {
            String s1 = this.messages[i3];
            if (s1 != null && i3 == this.line && j >= 0) {
                int j3 = this.minecraft.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int k3 = j3 - this.minecraft.font.width(s1) / 2;
                if (flag1 && j < s1.length()) {
                    fill(pPoseStack, k3, l - 1, k3 + 1, l + 9, -16777216 | i);
                }

                if (k != j) {
                    int l3 = Math.min(j, k);
                    int l1 = Math.max(j, k);
                    int i2 = this.minecraft.font.width(s1.substring(0, l3)) - this.minecraft.font.width(s1) / 2;
                    int j2 = this.minecraft.font.width(s1.substring(0, l1)) - this.minecraft.font.width(s1) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.enableColorLogicOp();
                    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                    bufferbuilder.vertex(matrix4f, (float)k2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)l2, (float)(l + 9), 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)l2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.vertex(matrix4f, (float)k2, (float)l, 0.0F).color(0, 0, 255, 255).endVertex();
                    bufferbuilder.end();
                    BufferUploader.end(bufferbuilder);
                    RenderSystem.disableColorLogicOp();
                    RenderSystem.enableTexture();
                }
            }
        }

        pPoseStack.popPose();
        Lighting.setupFor3DItems();
    }
}
