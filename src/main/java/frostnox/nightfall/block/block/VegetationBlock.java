package frostnox.nightfall.block.block;

import frostnox.nightfall.block.INaturalVegetation;
import frostnox.nightfall.block.ISoil;
import frostnox.nightfall.block.SoilCover;
import frostnox.nightfall.data.TagsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VegetationBlock extends BushBlockNF implements INaturalVegetation {
    private final int weight;
    private final float tempMin, tempMax, humidityMin, humidityMax;
    private final boolean clustered;

    public VegetationBlock(VoxelShape shape, int weight, float tempMin, float tempMax, float humidityMin, float humidityMax, boolean clustered, Properties pProperties) {
        super(shape, pProperties);
        this.weight = weight;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
        this.clustered = clustered;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(TagsNF.TILLABLE_SOIL);
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public BlockState getGrowthBlock() {
        return defaultBlockState();
    }

    @Override
    public boolean canGrowAt(ServerLevel level, BlockPos pos, ISoil soil, SoilCover cover, int skyLight, float temperature, float humidity) {
        return skyLight >= 6 && temperature >= tempMin && temperature <= tempMax && humidity >= humidityMin && humidity <= humidityMax;
    }

    @Override
    public boolean hasClusteredGrowth() {
        return clustered;
    }
}
