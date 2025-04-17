package frostnox.nightfall.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityAsItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final Minecraft mc;
    private final BlockEntityRenderDispatcher renderer;
    //private final Map<ITreeType, ChestBlockEntityNF> chests;

    public BlockEntityAsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        mc = Minecraft.getInstance();
        renderer = mc.getBlockEntityRenderDispatcher();
        /*ImmutableMap.Builder<ITreeType, ChestBlockEntityNF> chestBuilder = new ImmutableMap.Builder<>();
        for(ITreeType type : TreeType.ALL_VALUES) chestBuilder.put(type, new ChestBlockEntityNF(BlockPos.ZERO, type.getDefaultChestState().setValue(ChestBlock.FACING, Direction.SOUTH)));
        chests = chestBuilder.build();*/
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        Item item = itemStack.getItem();
        if(item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();
            BlockEntity entity;
            if(true) return;
            /*if(state.getBlock() instanceof ChestBlockNF chestBlock) entity = chests.get(chestBlock.getType());
            else return;*/
            this.renderer.renderItem(entity, stack, buffer, light, overlay);
        }
    }
}
