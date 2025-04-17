package frostnox.nightfall.block.fluid;

import frostnox.nightfall.block.Stone;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class LavaFluidNF extends SizedFluid {
    protected LavaFluidNF(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(Level level, BlockPos pos, FluidState state, Random random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            int i = random.nextInt(3);
            if (i > 0) {
                BlockPos blockpos = pos;

                for(int j = 0; j < i; ++j) {
                    blockpos = blockpos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                    if (!level.isLoaded(blockpos)) {
                        return;
                    }

                    BlockState blockstate = level.getBlockState(blockpos);
                    if (blockstate.isAir()) {
                        if (this.hasFlammableNeighbours(level, blockpos)) {
                            level.setBlockAndUpdate(blockpos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(level, blockpos, pos, Blocks.FIRE.defaultBlockState()));
                            return;
                        }
                    } else if (blockstate.getMaterial().blocksMotion()) {
                        return;
                    }
                }
            } else {
                for(int k = 0; k < 3; ++k) {
                    BlockPos blockpos1 = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!level.isLoaded(blockpos1)) {
                        return;
                    }

                    if (level.isEmptyBlock(blockpos1.above()) && this.isFlammable(level, blockpos1, Direction.UP)) {
                        level.setBlockAndUpdate(blockpos1.above(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(level, blockpos1.above(), pos, Blocks.FIRE.defaultBlockState()));
                    }
                }
            }

        }
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        super.tick(level, pos, state);
        //Spread heat here to avoid scheduling duplicate tick for block
        for(Direction dir : Direction.values()) {
            BlockPos spreadPos = pos.relative(dir);
            BlockState spreadState = level.getBlockState(spreadPos);
            LevelUtil.spreadHeat(level, spreadPos, spreadState, TieredHeat.RED, dir.getOpposite());
        }
    }

    private boolean hasFlammableNeighbours(LevelReader level, BlockPos pos) {
        for(Direction direction : Direction.values()) {
            if (this.isFlammable(level, pos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    private boolean isFlammable(LevelReader level, BlockPos pos, Direction face) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() && !level.hasChunkAt(pos) ? false : level.getBlockState(pos).isFlammable(level, pos, face);
    }

    private void fizz(LevelAccessor level, BlockPos pos) {
        level.levelEvent(1501, pos, 0);
    }

    @Override
    public void animateTick(Level level, BlockPos pos, FluidState state, Random random) {
        BlockPos blockpos = pos.above();
        if (level.getBlockState(blockpos).isAir() && !level.getBlockState(blockpos).isSolidRender(level, blockpos)) {
            if (random.nextInt(100) == 0) {
                double d0 = pos.getX() + random.nextDouble();
                double d1 = pos.getY() + 1.0D;
                double d2 = pos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }

            if (random.nextInt(200) == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }

    }

    @Nullable
    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypesNF.DRIPPING_LAVA.get();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction) {
        return state.getHeight(level, pos) >= 0.44444445F && fluidIn.is(FluidTags.WATER);
    }

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState pBlockState, Direction pDirection, FluidState pFluidState) {
        if (pDirection == Direction.DOWN) {
            FluidState fluidstate = level.getFluidState(pos);
            if (this.is(FluidTags.LAVA) && fluidstate.is(FluidTags.WATER)) {
                if (pBlockState.getBlock() instanceof LiquidBlock) {
                    level.setBlock(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, BlocksNF.STONE_BLOCKS.get(Stone.PUMICE).get().defaultBlockState()), 3);
                }

                this.fizz(level, pos);
                return;
            }
        }

        super.spreadTo(level, pos, pBlockState, pDirection, pFluidState);
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        this.fizz(level, pos);
    }

    public static class Flowing extends LavaFluidNF {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 3));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends LavaFluidNF {
        public Source(Properties properties) {
            super(properties);
        }

        public int getAmount(FluidState state) {
            return 4;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
