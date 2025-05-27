package frostnox.nightfall.block.block.sign;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignBlockEntityNF extends SignBlockEntity {
   public SignBlockEntityNF(BlockPos pPos, BlockState pBlockState) {
      this(BlockEntitiesNF.SIGN.get(), pPos, pBlockState);
   }

   protected SignBlockEntityNF(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(pPos, pBlockState);
      this.type = pType;
   }
}