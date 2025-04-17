package frostnox.nightfall.mixin;

import com.google.common.collect.ImmutableList;
import frostnox.nightfall.block.ITimeSimulatedBlock;
import frostnox.nightfall.capability.ChunkData;
import frostnox.nightfall.capability.IChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements net.minecraftforge.common.capabilities.ICapabilityProviderImpl<LevelChunk> {
    @Shadow @Final private Level level;

    private LevelChunkMixin(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, long pInhabitedTime, @Nullable LevelChunkSection[] pSections, @Nullable BlendingData pBlendingData) {
        super(pChunkPos, pUpgradeData, pLevelHeightAccessor, pBiomeRegistry, pInhabitedTime, pSections, pBlendingData);
    }

    /**
     * @author Frostnox
     * @reason Allow chunk to record time simulatable block positions from world gen. Can't do this via chunk Load event since chunks may be saved
     * as ProtoChunks which do not load nor have capabilities. Could be saved per level but this data should be saved per chunk and makes use of
     * the postprocessing field which is already saved and loaded correctly.aaaa
     */
    @Overwrite
    public void postProcessGeneration() {
        ChunkPos chunkPos = getPos();
        if(ChunkData.isPresent((LevelChunk) (Object) this)) {
            IChunkData chunkData = ChunkData.get((LevelChunk) (Object) this);
            for(int i = 0; i < postProcessing.length; i++) {
                if(postProcessing[i] != null) {
                    for(Short packedPos : postProcessing[i]) {
                        BlockPos pos = ProtoChunk.unpackOffsetCoordinates(packedPos, getSectionYFromSectionIndex(i), chunkPos);
                        BlockState blockState = getBlockState(pos);
                        Block block = blockState.getBlock();
                        FluidState fluidState = blockState.getFluidState();
                        if(!fluidState.isEmpty()) fluidState.tick(level, pos);
                        if(block instanceof ITimeSimulatedBlock timeSimulatable) {
                            chunkData.addSimulatableBlock(timeSimulatable.getTickPriority(), pos);
                        }
                        else if(!(block instanceof LiquidBlock)) {
                            BlockState updatedBlock = Block.updateFromNeighbourShapes(blockState, level, pos);
                            level.setBlock(pos, updatedBlock, 20);
                        }
                    }
                    postProcessing[i].clear();
                }
            }
        }
        else for(int i = 0; i < postProcessing.length; i++) {
            if(postProcessing[i] != null) {
                for(Short packedPos : postProcessing[i]) {
                    BlockPos pos = ProtoChunk.unpackOffsetCoordinates(packedPos, getSectionYFromSectionIndex(i), chunkPos);
                    BlockState blockState = getBlockState(pos);
                    Block block = blockState.getBlock();
                    FluidState fluidState = blockState.getFluidState();
                    if(!fluidState.isEmpty()) fluidState.tick(level, pos);
                    if(!(block instanceof LiquidBlock)) {
                        BlockState updatedBlock = Block.updateFromNeighbourShapes(blockState, level, pos);
                        level.setBlock(pos, updatedBlock, 20);
                    }
                }
                postProcessing[i].clear();
            }
        }
        for(BlockPos pos : ImmutableList.copyOf(pendingBlockEntities.keySet())) {
            getBlockEntity(pos);
        }
        pendingBlockEntities.clear();
        upgradeData.upgrade((LevelChunk) (Object) this);
    }
}
