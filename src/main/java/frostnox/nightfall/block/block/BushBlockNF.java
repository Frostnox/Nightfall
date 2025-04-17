package frostnox.nightfall.block.block;

import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.common.Tags;

public class BushBlockNF extends BushBlock {
    public final VoxelShape shape;

    public BushBlockNF(VoxelShape shape, Properties pProperties) {
        super(pProperties);
        this.shape = shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TagsNF.TILLABLE_SOIL) || state.is(Tags.Blocks.SAND);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return null;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }
}
