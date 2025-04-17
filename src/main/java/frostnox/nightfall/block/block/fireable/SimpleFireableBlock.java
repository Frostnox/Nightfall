package frostnox.nightfall.block.block.fireable;

import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.registries.RegistryObject;

public class SimpleFireableBlock extends FireableBlock {
    public final RegistryObject<? extends Block> firedBlock;

    public SimpleFireableBlock(int cookTicks, TieredHeat cookHeat, RegistryObject<? extends Block> firedBlock, Properties properties) {
        super(cookTicks, cookHeat, properties);
        this.firedBlock = firedBlock;
    }

    @Override
    public boolean isStructureValid(Level level, BlockPos pos, BlockState state) {
        if(cookHeat.getTier() <= 1) return true;
        else return Math.max(LevelUtil.getNearbyKilnTier(level, pos), LevelUtil.getNearbyFurnaceTier(level, pos)) >= cookHeat.getTier() - 1;
    }

    @Override
    public BlockState getFiredBlock(Level level, BlockPos pos, BlockState state) {
        return firedBlock.get().defaultBlockState();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
        return false;
    }
}
