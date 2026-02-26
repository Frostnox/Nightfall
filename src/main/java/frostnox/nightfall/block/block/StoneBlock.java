package frostnox.nightfall.block.block;

import frostnox.nightfall.block.IFallable;
import frostnox.nightfall.block.IStone;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StoneBlock extends BlockNF implements IFallable {
    public final IStone type;
    public static boolean noDislodging = false;

    public StoneBlock(IStone type, Properties pProperties) {
        super(pProperties);
        this.type = type;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean pIsMoving) {
        if(noDislodging) return; //Stop dislodging from nearby blocks falling
        if(level.getBlockState(pos.below()).getMaterial().blocksMotion()) return;
        BlockPos.MutableBlockPos neighborPos = pos.mutable();
        for(Direction dir : LevelUtil.HORIZONTAL_UP_DIRECTIONS) {
            if(level.getBlockState(neighborPos.setWithOffset(pos, dir)).is(TagsNF.SUPPORT_STONE)) {
                return;
            }
        }
        level.removeBlock(pos, pIsMoving);
        level.playSound(null, pos, SoundsNF.STONE_DISLODGE.get(), SoundSource.BLOCKS, 1F, 1F);
        popResource(level, pos, new ItemStack(asItem()));
    }

    @Override
    public SoundEvent getFallSound(BlockState state) {
        return SoundsNF.STONE_FALL.get();
    }
}
