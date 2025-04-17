package frostnox.nightfall.block.block.fuel;

import frostnox.nightfall.block.IIgnitable;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.block.block.BlockNF;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class FuelBlock extends BlockNF implements IIgnitable {
    public final Supplier<? extends BurningFuelBlock> burningBlock;

    public FuelBlock(Supplier<? extends BurningFuelBlock> burningBlock, Properties properties) {
        super(properties);
        this.burningBlock = burningBlock;
    }

    @Override
    public boolean tryToIgnite(Level level, BlockPos pos, BlockState state, ItemStack stack, TieredHeat heat) {
        if(level.isClientSide()) return false;
        burningBlock.get().createAt(state, pos, level);
        return true;
    }

    @Override
    public boolean isIgnited(BlockState state) {
        return false;
    }
}
