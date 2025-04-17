package frostnox.nightfall.block.block;

import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.entity.MovingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;

public class GlassBlockNF extends BlockNF {
    public GlassBlockNF(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(fallDistance, 1.0F, DamageSource.FALL)) {
            //Shatter now before this block is gone
            if(entity instanceof MovingBlockEntity movingBlock && movingBlock.getBlockState().is(TagsNF.SHATTER_ON_FALL)) {
                movingBlock.tryPlacement();
            }
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile pProjectile) {
        if(!level.isClientSide && pProjectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && pProjectile.getDeltaMovement().length() >= 1.4D) {
            level.destroyBlock(pHit.getBlockPos(), true);
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState pAdjacentBlockState, Direction pSide) {
        if(pAdjacentBlockState.is(Tags.Blocks.GLASS_COLORLESS)) {
            VoxelShape adjShape = pAdjacentBlockState.getShape(null, null);
            if(adjShape == Shapes.block()) return true;
            adjShape = adjShape.getFaceShape(pSide.getOpposite());
            if(adjShape.isEmpty()) return false;
            VoxelShape shape = state.getShape(null, null).getFaceShape(pSide);
            if(shape.isEmpty()) return false;
            AABB box = shape.bounds(), adjBox = adjShape.bounds();
            return box.minX >= adjBox.minX && box.maxX <= adjBox.maxX && box.minY >= adjBox.minY && box.maxY <= adjBox.maxY
                    && box.minZ >= adjBox.minZ && box.maxZ <= adjBox.maxZ ;
        }
        else return false;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter pReader, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }
}
