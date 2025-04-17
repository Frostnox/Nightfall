package frostnox.nightfall.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IIgnitable {
    /**
     * @param stack fire-starting item (can be empty)
     * @return true if attempt succeeds
     */
    boolean tryToIgnite(Level level, BlockPos pos, BlockState state, ItemStack stack, TieredHeat heat);

    boolean isIgnited(BlockState state);
}
