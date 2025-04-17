package frostnox.nightfall.client.render.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.block.chest.ChestBlockEntityNF;
import frostnox.nightfall.block.block.chest.ChestBlockNF;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.client.model.ModelRegistryNF;
import frostnox.nightfall.registry.RegistriesNF;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.Map;

public class ChestRendererNF<T extends ChestBlockEntityNF> implements BlockEntityRenderer<T> {
    private static Map<ITree, ResourceLocation> SINGLE_TEXTURES, RIGHT_TEXTURES, LEFT_TEXTURES;
    private static final String LID = "lid";
    private static final String LOCK = "lock";
    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightLock;

    public ChestRendererNF(BlockEntityRendererProvider.Context context) {
        //if(calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) xmasTextures = true;
        ModelPart modelpart = context.bakeLayer(ModelRegistryNF.CHEST);
        lid = modelpart.getChild(LID);
        lock = modelpart.getChild(LOCK);
        ModelPart modelpart1 = context.bakeLayer(ModelRegistryNF.DOUBLE_CHEST_LEFT);
        doubleLeftLid = modelpart1.getChild(LID);
        doubleLeftLock = modelpart1.getChild(LOCK);
        ModelPart modelpart2 = context.bakeLayer(ModelRegistryNF.DOUBLE_CHEST_RIGHT);
        doubleRightLid = modelpart2.getChild(LID);
        doubleRightLock = modelpart2.getChild(LOCK);
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyRightLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createDoubleBodyLeftLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(LID, CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
        partdefinition.addOrReplaceChild(LOCK, CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 15.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
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
    public void render(T chest, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockState = chest.getBlockState();
        if(blockState.getBlock() instanceof ChestBlockNF chestBlock) {
            Level level = chest.getLevel();
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combiner;
            if(level != null) combiner = chestBlock.combine(blockState, level, chest.getBlockPos(), true);
            else combiner = DoubleBlockCombiner.Combiner::acceptNone;

            float lidAngle = combiner.apply(ChestBlock.opennessCombiner(chest)).get(pPartialTick);
            boolean open = chest.getOpenNess(pPartialTick) > 0F;
            //Visual lag when updating states on low FPS (< 30), but this is consistent for all blocks in the game
            //This happens only when using the "threaded" chunk builder (Sodium forces this option on; increasing threads does not mitigate the issue)
            if(blockState.getValue(ChestBlockNF.OPEN) != open) {
                if(!open && chest.forceEntityRenderTick < chest.tickCount) chest.forceEntityRenderTick = chest.tickCount;
                level.setBlock(chest.getBlockPos(), blockState.setValue(ChestBlockNF.OPEN, open), 2 | 16);
            }
            else if(chest.forceEntityRenderTick < chest.tickCount) chest.forceEntityRenderTick = -1;
            if(lidAngle == 0F && chest.forceEntityRenderTick == -1) return;
            lidAngle = 1.0F - lidAngle;
            lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
            ChestType type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
            boolean doubled = type != ChestType.SINGLE;
            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-blockState.getValue(ChestBlock.FACING).toYRot()));
            pPoseStack.translate(-0.5D, -0.5D, -0.5D);
            int i = combiner.apply(new BrightnessCombiner<>()).applyAsInt(pPackedLight);
            Material material = new Material(Sheets.CHEST_SHEET, chooseLocationByType(type, chestBlock.getType()));
            VertexConsumer buffer = material.buffer(pBufferSource, RenderType::entitySolid);
            if(doubled) {
                if(type == ChestType.LEFT) render(pPoseStack, buffer, doubleLeftLid, doubleLeftLock, lidAngle, i, pPackedOverlay);
                else render(pPoseStack, buffer, doubleRightLid, doubleRightLock, lidAngle, i, pPackedOverlay);
            }
            else render(pPoseStack, buffer, lid, lock, lidAngle, i, pPackedOverlay);

            pPoseStack.popPose();
        }
    }

    private void render(PoseStack pPoseStack, VertexConsumer pConsumer, ModelPart pLidPart, ModelPart pLockPart, float pLidAngle, int pPackedLight, int pPackedOverlay) {
        pLidPart.xRot = -(pLidAngle * (MathUtil.PI / 2F));
        pLockPart.xRot = pLidPart.xRot;
        pLidPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pLockPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
    }

    protected ResourceLocation chooseLocationByType(ChestType chestType, ITree materialType) {
        if(chestType == ChestType.RIGHT) return RIGHT_TEXTURES.get(materialType);
        else if(chestType == ChestType.LEFT) return LEFT_TEXTURES.get(materialType);
        return SINGLE_TEXTURES.get(materialType);
    }

    public static void stitchChestTextures(TextureStitchEvent.Pre event) {
        if(!event.getAtlas().location().equals(Sheets.CHEST_SHEET)) return;
        ImmutableMap.Builder<ITree, ResourceLocation> singleBuilder = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<ITree, ResourceLocation> rightBuilder = new ImmutableMap.Builder<>();
        ImmutableMap.Builder<ITree, ResourceLocation> leftBuilder = new ImmutableMap.Builder<>();
        for(var type : RegistriesNF.getTrees().getValues()) {
            singleBuilder.put(type.value, ResourceLocation.fromNamespaceAndPath(type.getRegistryName().getNamespace(),
                    "entity/chest/" + type.getRegistryName().getPath() + "_chest_single"));
            rightBuilder.put(type.value, ResourceLocation.fromNamespaceAndPath(type.getRegistryName().getNamespace(),
                    "entity/chest/" + type.getRegistryName().getPath() + "_chest_right"));
            leftBuilder.put(type.value, ResourceLocation.fromNamespaceAndPath(type.getRegistryName().getNamespace(),
                    "entity/chest/" + type.getRegistryName().getPath() + "_chest_left"));
        }
        SINGLE_TEXTURES = singleBuilder.build();
        RIGHT_TEXTURES = rightBuilder.build();
        LEFT_TEXTURES = leftBuilder.build();
        for(var type : RegistriesNF.getTrees()) {
            event.addSprite(SINGLE_TEXTURES.get(type.value));
            event.addSprite(RIGHT_TEXTURES.get(type.value));
            event.addSprite(LEFT_TEXTURES.get(type.value));
        }
    }
}
