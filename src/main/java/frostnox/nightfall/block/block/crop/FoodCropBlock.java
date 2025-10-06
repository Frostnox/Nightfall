package frostnox.nightfall.block.block.crop;

import frostnox.nightfall.block.Fertility;
import frostnox.nightfall.block.IFoodBlock;
import frostnox.nightfall.entity.entity.Diet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class FoodCropBlock extends CropBlockNF implements IFoodBlock {
    public FoodCropBlock(Fertility minFertility, float minHumidity, float maxHumidity, float minTemp, float maxTemp, int minSunlight, int ticksToGrow, Supplier<? extends Item> seedItem, boolean dieAsPlant, Properties pProperties) {
        super(minFertility, minHumidity, maxHumidity, minTemp, maxTemp, minSunlight, ticksToGrow, seedItem, dieAsPlant, pProperties);
    }

    @Override
    public boolean isEatable(BlockState state, Diet diet) {
        return state.getValue(STAGE) == 8 && diet != Diet.CARNIVORE;
    }

    @Override
    public void eat(Entity eater, Level level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(STAGE, 1), 2);
    }
}
