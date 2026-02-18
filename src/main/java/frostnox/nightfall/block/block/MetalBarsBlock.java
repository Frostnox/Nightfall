package frostnox.nightfall.block.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalBarsBlock extends CrossCollisionBlockNF {
   public MetalBarsBlock(Properties properties) {
      super(1.0F, 1.0F, 16.0F, 16.0F, 16.0F, properties);
       registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.FALSE).setValue(EAST, Boolean.FALSE).setValue(SOUTH, Boolean.FALSE)
               .setValue(WEST, Boolean.FALSE).setValue(WATER_LEVEL, 0).setValue(WATERLOG_TYPE, WaterlogType.FRESH));
   }

   @Override
   public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
   }

   @Override
   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      if(pAdjacentBlockState.getBlock() instanceof MetalBarsBlock) {
         if(!pSide.getAxis().isHorizontal()) return true;
         if(pState.getValue(PROPERTY_BY_DIRECTION.get(pSide)) && pAdjacentBlockState.getValue(PROPERTY_BY_DIRECTION.get(pSide.getOpposite()))) {
            return true;
         }
      }
      return super.skipRendering(pState, pAdjacentBlockState, pSide);
   }

    @Override
    public boolean connectsTo(BlockState state, boolean pIsSideSolid, Direction pDirection) {
        return pIsSideSolid || state.getBlock() instanceof MetalBarsBlock;
    }
}