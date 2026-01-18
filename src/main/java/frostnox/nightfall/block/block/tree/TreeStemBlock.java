package frostnox.nightfall.block.block.tree;

import frostnox.nightfall.block.BlockStatePropertiesNF;
import frostnox.nightfall.block.IBurnable;
import frostnox.nightfall.block.ITree;
import frostnox.nightfall.world.generation.tree.TreeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;
import java.util.Locale;

public class TreeStemBlock extends RotatedPillarBlock implements IBurnable {
    public enum Type implements StringRepresentable {
        END, TOP, BOTTOM, ROTATED_TOP, ROTATED_BOTTOM, FAKE_END;

        private final String name;

        Type() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Type> TYPE = EnumProperty.create("stem_type", Type.class);
    public static final BooleanProperty CHARRED = BlockStatePropertiesNF.CHARRED;
    public final ITree type;

    public TreeStemBlock(ITree type, Properties pProperties) {
        super(pProperties);
        this.type = type;
        registerDefaultState(defaultBlockState().setValue(TYPE, Type.TOP).setValue(CHARRED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(TYPE, CHARRED);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        if(!TreeTrunkBlockEntity.updating && !pNewState.is(this)) {
            TreeGenerator gen = type.getGenerator();
            int minX = pos.getX() - gen.maxDistXZ, maxX = pos.getX() + gen.maxDistXZ;
            int minZ = pos.getZ() - gen.maxDistXZ, maxZ = pos.getZ() + gen.maxDistXZ;
            List<TreeTrunkBlockEntity> nearbyTrunks = TreeTrunkBlockEntity.getNearbyTrunks(level, type, pos, minX, maxX, minZ, maxZ);
            for(TreeTrunkBlockEntity nearbyTrunk : nearbyTrunks) {
                var simulatedData = gen.getTree((WorldGenLevel) level, nearbyTrunk, true);
                if(simulatedData.hasTrunkWood(pos) || simulatedData.otherWood.contains(pos)) {
                    nearbyTrunk.updateBlocks(pos, simulatedData, false);
                }
            }
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(CHARRED) ? 0 : 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(CHARRED) ? 0 : 5;
    }

    @Override
    public BlockState getBurnedState(BlockState state) {
        return state.setValue(CHARRED, true);
    }
}
