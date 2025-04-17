package frostnox.nightfall.world;

import frostnox.nightfall.block.IStone;
import frostnox.nightfall.block.Stone;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.world.level.block.state.BlockState;

//Be careful not to access this class before BlockStates are set up since static fields depend on them
public class StoneGroup {
    public static final StoneGroup GRANITE = new StoneGroup(Stone.GRANITE, Stone.SUNSCHIST, Stone.SANDSTONE);
    public static final StoneGroup STYGFEL = new StoneGroup(Stone.STYGFEL, Stone.AURGROT, Stone.NIXWACKE);
    public static final StoneGroup DEEPSLATE = new StoneGroup(Stone.DEEPSLATE, Stone.SLATE, Stone.SHALE);
    public static final StoneGroup BASALT = new StoneGroup(Stone.BASALT, Stone.MARBLE, Stone.LIMESTONE);

    public final IStone igneousType, metamorphicType, sedimentaryType;
    public final BlockState igneousStone, metamorphicStone, sedimentaryStone;

    public StoneGroup(Stone igneousType, Stone metamorphicType, Stone sedimentaryType) {
        this(igneousType, metamorphicType, sedimentaryType, BlocksNF.STONE_BLOCKS.get(igneousType).get().defaultBlockState(),
                BlocksNF.STONE_BLOCKS.get(metamorphicType).get().defaultBlockState(), BlocksNF.STONE_BLOCKS.get(sedimentaryType).get().defaultBlockState());
    }

    public StoneGroup(IStone igneousType, IStone metamorphicType, IStone sedimentaryType, BlockState igneousStone, BlockState metamorphicStone, BlockState sedimentaryStone) {
        this.igneousType = igneousType;
        this.metamorphicType = metamorphicType;
        this.sedimentaryType = sedimentaryType;
        this.igneousStone = igneousStone;
        this.metamorphicStone = metamorphicStone;
        this.sedimentaryStone = sedimentaryStone;
    }

    public boolean contains(BlockState block) {
        return block == igneousStone || block == metamorphicStone || block == sedimentaryStone;
    }
}
