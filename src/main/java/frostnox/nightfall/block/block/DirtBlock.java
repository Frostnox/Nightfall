package frostnox.nightfall.block.block;

import frostnox.nightfall.block.SoilCover;
import frostnox.nightfall.capability.IChunkData;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class DirtBlock extends SoilBlock {
    public DirtBlock(Supplier<SoundEvent> slideSound, Map<SoilCover, RegistryObject<? extends CoveredSoilBlock>> map, RegistryObject<? extends TilledSoilBlock> tilledSoilBlock, Properties properties) {
        super(slideSound, map, tilledSoilBlock, properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if(aboveState.getFluidState().is(FluidTags.WATER)) {
            if(random.nextBoolean()) level.setBlockAndUpdate(pos, BlocksNF.MUD.get().defaultBlockState());
        }
        else {
            int light = level.getBrightness(LightLayer.SKY, abovePos);
            if(light > 0 && LevelUtil.isDay(level) && random.nextInt(10) == 0) tryGrowCover(state, level, pos, abovePos, aboveState, light);
        }
    }

    @Override
    public void simulateTime(ServerLevel level, LevelChunk chunk, IChunkData chunkData, BlockPos pos, BlockState state, long elapsedTime, long gameTime, long dayTime, long seasonTime, float seasonalTemp, double randomTickChance, Random random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = chunk.getBlockState(abovePos);
        if(aboveState.getFluidState().is(FluidTags.WATER)) {
            if(MathUtil.getRandomSuccesses(randomTickChance / 2, elapsedTime, 1, random) >= 1) {
                level.setBlockAndUpdate(pos, BlocksNF.MUD.get().defaultBlockState());
            }
        }
        else {
            int light = level.getBrightness(LightLayer.SKY, abovePos);
            if(light > 0 && MathUtil.getRandomSuccesses(randomTickChance / 10, LevelUtil.getDayTimePassed(level, dayTime, elapsedTime), 1, random) >= 1) {
                tryGrowCover(state, level, pos, abovePos, aboveState, light);
            }
        }
    }
}
