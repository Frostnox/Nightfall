package frostnox.nightfall.block.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SoftFallBlock extends BlockNF {
    public final float cushionDist;
    public final float multiplier;

    public SoftFallBlock(float cushionDist, float multiplier, Properties pProperties) {
        super(pProperties);
        this.cushionDist = cushionDist;
        this.multiplier = multiplier;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(Math.max(0, fallDistance - cushionDist), multiplier, DamageSource.FALL);
    }
}
