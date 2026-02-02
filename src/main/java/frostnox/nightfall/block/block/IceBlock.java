package frostnox.nightfall.block.block;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IceBlock extends MeltableBlock {
    public IceBlock(Supplier<? extends Block> meltBlock, float meltTemp, Properties pProperties) {
        super(meltBlock, meltTemp, pProperties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) || super.skipRendering(state, pAdjacentBlockState, pSide);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity pTe, ItemStack pStack) {
        super.playerDestroy(level, player, pos, state, pTe, pStack);
        if(level.dimensionType().ultraWarm()) level.removeBlock(pos, false);
        else level.setBlockAndUpdate(pos, meltBlock.get().defaultBlockState());
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        if(!level.isClientSide && pProjectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && pProjectile.getDeltaMovement().length() >= 2D) {
            if(level.dimensionType().ultraWarm()) level.removeBlock(pHit.getBlockPos(), false);
            else level.setBlockAndUpdate(pHit.getBlockPos(), meltBlock.get().defaultBlockState());
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALL)) {
            //Shatter now before this block is gone
            if(entity instanceof MovingBlockEntity movingBlock && movingBlock.getBlockState().is(TagsNF.SHATTER_ON_FALL)) {
                movingBlock.tryPlacement();
            }
            if(level.dimensionType().ultraWarm()) level.removeBlock(pos, false);
            else level.setBlockAndUpdate(pos, meltBlock.get().defaultBlockState());
        }
    }
}
