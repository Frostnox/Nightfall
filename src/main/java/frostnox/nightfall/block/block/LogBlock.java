package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IBurnable;
import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.world.ToolActionsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class LogBlock extends RotatedPillarBlock implements IBurnable {
    public final RegistryObject<? extends RotatedPillarBlock> strippedBlock;
    public final @Nullable RegistryObject<? extends TieredAnvilBlock> anvilBlock;

    public LogBlock(RegistryObject<? extends RotatedPillarBlock> strippedBlock, @Nullable RegistryObject<? extends TieredAnvilBlock> anvilBlock, Properties pProperties) {
        super(pProperties);
        this.strippedBlock = strippedBlock;
        this.anvilBlock = anvilBlock;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(toolAction == ToolActionsNF.STRIP) return strippedBlock.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        else if(toolAction == ToolActionsNF.REFINE) {
            if(anvilBlock != null) return anvilBlock.get().getStateForPlacement(new BlockPlaceContext(context));
        }
        return null;
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
