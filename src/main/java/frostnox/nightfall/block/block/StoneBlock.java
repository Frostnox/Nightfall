package frostnox.nightfall.block.block;

import frostnox.nightfall.block.block.anvil.TieredAnvilBlock;
import frostnox.nightfall.world.ToolActionsNF;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class StoneBlock extends BlockNF {
    public final @Nullable RegistryObject<? extends TieredAnvilBlock> anvilBlock;

    public StoneBlock(@Nullable RegistryObject<? extends TieredAnvilBlock> anvilBlock, Properties pProperties) {
        super(pProperties);
        this.anvilBlock = anvilBlock;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if(toolAction == ToolActionsNF.REFINE) {
            if(anvilBlock != null) return anvilBlock.get().getStateForPlacement(new BlockPlaceContext(context));
        }
        return null;
    }
}
