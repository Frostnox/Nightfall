package frostnox.nightfall.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.cauldron.CauldronBlockEntity;
import frostnox.nightfall.block.block.cauldron.CauldronBlockNF;
import frostnox.nightfall.block.block.cauldron.Task;
import frostnox.nightfall.client.ClientEngine;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.RenderUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Map;

public class CauldronRenderer<T extends CauldronBlockEntity> implements BlockEntityRenderer<T> {
    private final ModelPart LID;
    private static final Map<Block, Material> MATERIALS = new Object2ObjectArrayMap<>(1);
    private static final Map<Item, ResourceLocation> TEXTURES = new Object2ObjectArrayMap<>(1);
    private static final float FREQ = 0.25F;

    public CauldronRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelRegistryNF.CAULDRON);
        LID = root.getChild("lid");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 21).addBox(-5.0F, -0.5F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(22, 0).addBox(-2.0F, -1.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public int getViewDistance() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
        return true;
    }

    @Override
    public void render(T cauldron, float pPartialTick, PoseStack stack, MultiBufferSource buffers, int pPackedLight, int pPackedOverlay) {
        BlockState state = cauldron.getBlockState();
        if(state.getBlock() instanceof CauldronBlockNF cauldronBlock) {
            if(state.getValue(CauldronBlockNF.TASK) == Task.DONE) {
                if(!cauldron.hasMeal()) return;
                stack.pushPose();
                Item meal = cauldron.meal.getItem();
                ResourceLocation loc = TEXTURES.get(meal);
                if(loc == null) {
                    ResourceLocation itemLoc = meal.getRegistryName();
                    loc = ResourceLocation.fromNamespaceAndPath(itemLoc.getNamespace(), "block/" + itemLoc.getPath());
                    TEXTURES.put(meal, loc);
                }
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(loc);
                VertexConsumer builder = buffers.getBuffer(RenderType.entitySolid(sprite.atlas().location()));
                Vec2 UV = new Vec2(sprite.getU0(), sprite.getV0());
                switch((int) state.getSeed(cauldron.getBlockPos()) % 4) {
                    case 0 -> {
                        stack.mulPose(Vector3f.YP.rotationDegrees(90));
                        stack.translate(-1, 0, 0);
                    }
                    case 1 -> {
                        stack.mulPose(Vector3f.YP.rotationDegrees(180));
                        stack.translate(-1, 0, -1);
                    }
                    case 3 -> {
                        stack.mulPose(Vector3f.YP.rotationDegrees(270));
                        stack.translate(0, 0, -1);
                    }
                }
                RenderUtil.drawFace(Direction.UP, stack.last().pose(), stack.last().normal(), builder, Color.WHITE,
                        new Vec3(8D/16D, 2D/16D + 1D/16D * cauldron.meal.getCount(), 8D/16D), 8F/16F, 8F/16F, UV,
                        8F/ClientEngine.get().atlasWidth, 8F/ClientEngine.get().atlasHeight, pPackedLight);
                stack.popPose();
                return;
            }
            //Visual lag when updating states on low FPS (< 30), but this is consistent for all blocks in the game
            //This happens only when using the "threaded" chunk builder (Sodium forces this option on; increasing threads does not mitigate the issue)
            if(state.getValue(CauldronBlockNF.TASK) != Task.COOK) {
                if(cauldron.forceEntityRenderTick == -1 && cauldron.hasLevel()) cauldron.forceEntityRenderTick = cauldron.animTick;
                else return;
            }
            else if(cauldron.forceEntityRenderTick < cauldron.animTick) cauldron.forceEntityRenderTick = -1;
            stack.pushPose();
            stack.translate(0.5D, 1.5D, 0.5D);
            stack.mulPose(Vector3f.XP.rotationDegrees(180));
            if(state.getValue(CauldronBlockNF.AXIS) == Direction.Axis.X) stack.mulPose(Vector3f.YP.rotationDegrees(-90));
            Material material = MATERIALS.get(cauldronBlock);
            if(material == null) {
                material = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath (cauldronBlock.getRegistryName().getNamespace(),
                        "block/" + cauldronBlock.getRegistryName().getPath()));
                MATERIALS.put(cauldronBlock, material);
            }
            //Shake
            float tick = cauldron.animTick + pPartialTick;
            if(tick % (MathUtil.PI * 8) < MathUtil.PI * 4) LID.xRot = 0;
            else LID.xRot = Mth.sin(tick * FREQ) * Mth.sin(tick * FREQ * 1.5F) * Mth.sin(tick * FREQ * 2.2F) * 0.1F;
            LID.render(stack, material.buffer(buffers, RenderType::entitySolid), pPackedLight, pPackedOverlay);
            stack.popPose();
        }
    }
}
