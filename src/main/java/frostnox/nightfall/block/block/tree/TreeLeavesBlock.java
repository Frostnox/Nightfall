package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.ITree;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.world.Season;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.common.IForgeShearable;

import java.util.Random;

public class TreeLeavesBlock extends TreeBranchesBlock implements IForgeShearable {
    protected final IBlockRenderProperties renderProperties = new IBlockRenderProperties() {
        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            boolean hasParticle = type.getParticle() != null;
            if(hasParticle && level.getGameTime() % 10L == 0) {
                Vec3 pos = target.getLocation();
                level.addParticle(new BlockParticleOption(type.getParticle().get(), state), pos.x, pos.y, pos.z, 0, 0, 0);
            }
            return hasParticle;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
            boolean hasParticle = type.getParticle() != null;
            if(hasParticle) {
                for(int i = 0; i < 3 + Math.abs(level.random.nextInt() % 4); i++) {
                    double x = pos.getX() + level.random.nextDouble();
                    double y = pos.getY() + level.random.nextDouble();
                    double z = pos.getZ() + level.random.nextDouble();
                    level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
                }
            }
            return hasParticle;
        }
    };

    public TreeLeavesBlock(ITree type, Properties properties) {
        super(type, properties);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if(level.isRainingAt(pos.above())) {
            if(random.nextInt() % 15 == 0) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);
                if(!blockstate.canOcclude() || !blockstate.isFaceSturdy(level, blockpos, Direction.UP)) {
                    double d0 = (double)pos.getX() + random.nextDouble();
                    double d1 = (double)pos.getY() - 0.05D;
                    double d2 = (double)pos.getZ() + random.nextDouble();
                    level.addParticle(ParticleTypesNF.DRIPPING_WATER.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        if(type.getParticle() != null) {
            int tickMod;
            switch(Season.get(level)) {
                case SPRING: tickMod = 2400; break;
                case SUMMER: tickMod = 1600; break;
                case FALL: tickMod = 550; break;
                default: return;
            }
            if(random.nextInt() % tickMod == 0) {
                double x = (double) pos.getX() + random.nextDouble();
                double y = pos.getY();
                double z = (double) pos.getZ() + random.nextDouble();
                level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if(entity.causeFallDamage(Math.max(0, fallDistance - 1.5F), 1.0F, DamageSource.FALL)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if(!level.isClientSide() || type.getParticle() == null) return;
        Vec3 velocity = entity.getDeltaMovement();
        if(level.getGameTime() % 12L == 0L && !entity.isCrouching() && velocity.lengthSqr() > 0) {
            double x = pos.getX() + level.random.nextDouble();
            double y = pos.getY() + level.random.nextDouble();
            double z = pos.getZ() + level.random.nextDouble();
            level.addParticle(new BlockParticleOption(type.getParticle().get(), state), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }
}
