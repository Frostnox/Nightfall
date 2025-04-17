package frostnox.nightfall.block.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Modified copy of FenceBlock that replaces the vanilla waterlogging
 */
public class FenceBlockNF extends CrossCollisionBlockNF {
    private final VoxelShape[] occlusionByIndex;

    public FenceBlockNF(Properties properties) {
        super(2.0F, 2.0F, 16.0F, 16.0F, 24.0F, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.FALSE).setValue(EAST, Boolean.FALSE).setValue(SOUTH, Boolean.FALSE)
                .setValue(WEST, Boolean.FALSE).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
        this.occlusionByIndex = this.makeShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
    }

    @Override
    public boolean connectsTo(BlockState state, boolean pIsSideSolid, Direction pDirection) {
        Block block = state.getBlock();
        boolean flag = this.isSameFence(state);
        boolean flag1 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(state, pDirection);
        return !isExceptionForConnection(state) && pIsSideSolid || flag || flag1;
    }

    private boolean isSameFence(BlockState state) {
        return state.is(BlockTags.FENCES) && state.is(BlockTags.WOODEN_FENCES) == this.defaultBlockState().is(BlockTags.WOODEN_FENCES);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.occlusionByIndex[this.getAABBIndex(state)];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(level.isClientSide) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            return itemstack.is(Items.LEAD) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        else return LeadItem.bindPlayerMobs(pPlayer, level, pos);
    }
}
