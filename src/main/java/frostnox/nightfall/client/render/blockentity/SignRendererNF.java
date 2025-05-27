package frostnox.nightfall.client.render.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.block.block.sign.SignBlockEntityNF;
import frostnox.nightfall.block.block.sign.StandingSignBlockNF;
import frostnox.nightfall.block.block.sign.WallSignBlockNF;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SignRendererNF implements BlockEntityRenderer<SignBlockEntityNF> {
   public static final int MAX_LINE_WIDTH = 90;
   private static final int LINE_HEIGHT = 10;
   private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
   private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
   private static Map<ITree, ResourceLocation> TEXTURES;
   private final Font font;
   private final SignRenderer.SignModel standingModel, wallModel;

   public SignRendererNF(BlockEntityRendererProvider.Context pContext) {
      this.font = pContext.getFont();
      this.standingModel = new SignRenderer.SignModel(pContext.bakeLayer(ModelRegistryNF.SIGN));
      this.wallModel = new SignRenderer.SignModel(pContext.bakeLayer(ModelRegistryNF.SIGN));
   }

   @Override
   public void render(SignBlockEntityNF pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
      BlockState blockstate = pBlockEntity.getBlockState();
      pPoseStack.pushPose();
      SignRenderer.SignModel model;
      Material material;
      if (blockstate.getBlock() instanceof StandingSignBlockNF standingSign) {
         model = standingModel;
         material = new Material(Sheets.SIGN_SHEET, TEXTURES.get(standingSign.type));
         pPoseStack.translate(0.5D, 0.5D, 0.5D);
         float f1 = -((float)(blockstate.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
         pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f1));
         model.stick.visible = true;
      }
      else {
         WallSignBlockNF wallSign = (WallSignBlockNF) blockstate.getBlock();
         model = wallModel;
         material = new Material(Sheets.SIGN_SHEET, TEXTURES.get(wallSign.type));
         pPoseStack.translate(0.5D, 0.5D, 0.5D);
         float f4 = -blockstate.getValue(WallSignBlock.FACING).toYRot();
         pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f4));
         pPoseStack.translate(0.0D, -0.3125D, -0.4375D);
         model.stick.visible = false;
      }

      pPoseStack.pushPose();
      pPoseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      VertexConsumer vertexconsumer = material.buffer(pBufferSource, model::renderType);
      model.root.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
      pPoseStack.popPose();
      pPoseStack.translate(0.0D, 0.33333334F, 0.046666667F);
      pPoseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
      int i = getDarkColor(pBlockEntity);
      FormattedCharSequence[] aformattedcharsequence = pBlockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (p_173653_) -> {
         List<FormattedCharSequence> list = this.font.split(p_173653_, MAX_LINE_WIDTH);
         return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
      });
      int k;
      boolean flag;
      int l;
      if (pBlockEntity.hasGlowingText()) {
         k = pBlockEntity.getColor().getTextColor();
         flag = isOutlineVisible(pBlockEntity, k);
         l = 15728880;
      } else {
         k = i;
         flag = false;
         l = pPackedLight;
      }

      for(int i1 = 0; i1 < 4; ++i1) {
         FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
         float f3 = (float)(-this.font.width(formattedcharsequence) / 2);
         if (flag) {
            this.font.drawInBatch8xOutline(formattedcharsequence, f3, (float)(i1 * LINE_HEIGHT - LINE_HEIGHT * 2), k, i, pPoseStack.last().pose(), pBufferSource, l);
         } else {
            this.font.drawInBatch(formattedcharsequence, f3, (float)(i1 * LINE_HEIGHT - LINE_HEIGHT * 2), k, false, pPoseStack.last().pose(), pBufferSource, false, 0, l);
         }
      }

      pPoseStack.popPose();
   }

   private static boolean isOutlineVisible(SignBlockEntityNF pBlockEntity, int pTextColor) {
      if (pTextColor == DyeColor.BLACK.getTextColor()) {
         return true;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
            return true;
         } else {
            Entity entity = minecraft.getCameraEntity();
            return entity != null && entity.distanceToSqr(Vec3.atCenterOf(pBlockEntity.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
         }
      }
   }

   private static int getDarkColor(SignBlockEntityNF pBlockEntity) {
      int i = pBlockEntity.getColor().getTextColor();
      int j = (int)((double)NativeImage.getR(i) * 0.4D);
      int k = (int)((double)NativeImage.getG(i) * 0.4D);
      int l = (int)((double)NativeImage.getB(i) * 0.4D);
      return i == DyeColor.BLACK.getTextColor() && pBlockEntity.hasGlowingText() ? BLACK_TEXT_OUTLINE_COLOR : NativeImage.combine(0, l, k, j);
   }

   public static void stitchSignTextures(TextureStitchEvent.Pre event) {
      if(!event.getAtlas().location().equals(Sheets.SIGN_SHEET)) return;
      ImmutableMap.Builder<ITree, ResourceLocation> builder = new ImmutableMap.Builder<>();
      for(var type : RegistriesNF.getTrees().getValues()) {
         builder.put(type.value, ResourceLocation.fromNamespaceAndPath(type.getRegistryName().getNamespace(),
                 "entity/sign/" + type.getRegistryName().getPath() + "_plank"));
      }
      TEXTURES = builder.build();
      for(var type : RegistriesNF.getTrees()) event.addSprite(TEXTURES.get(type.value));
   }

   public static ResourceLocation getTexture(ITree type) {
      return TEXTURES.get(type);
   }
}