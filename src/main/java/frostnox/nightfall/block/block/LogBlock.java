package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IBurnable;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class LogBlock extends RotatedPillarBlock implements IBurnable {
    public final RegistryObject<? extends RotatedPillarBlock> strippedBlock;

    public LogBlock(RegistryObject<? extends RotatedPillarBlock> strippedBlock, Properties pProperties) {
        super(pProperties);
        this.strippedBlock = strippedBlock;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public BlockState getBurnedState(BlockState state) {
        return BlocksNF.CHARRED_LOG.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }
}
