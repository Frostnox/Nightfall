package frostnox.nightfall.block.block.fuel;

import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BurningFuelBlockEntity extends BlockEntity {
    public int cookTicks, burnTicks;
    public float temperature, structureTempBonus;

    public BurningFuelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesNF.FUEL.get(), pos, state);
        burnTicks = ((BurningFuelBlock) state.getBlock()).burnTicks;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BurningFuelBlockEntity entity) {
        BurningFuelBlock block = (BurningFuelBlock) state.getBlock();
        if(level.getGameTime() % 66L == 0L) {
            float baseTemp = block.burnTemp;
            if(LevelUtil.getNearbySmelterTier(level, pos) >= TieredHeat.fromTemp(baseTemp).getTier()) entity.structureTempBonus = 200F;
            else entity.structureTempBonus = 0F;
        }
        if(block.cookChecker != null && block.cookChecker.apply(level, pos) != null) entity.cookTicks++;
        entity.burnTicks--;
        if(entity.burnTicks <= 0) {
            ((BurningFuelBlock) entity.getBlockState().getBlock()).spreadHeat(level, pos, TieredHeat.NONE);
            if(entity.cookTicks >= block.burnTicks * 0.9F && block.cookChecker != null) {
                Block cookBlock = block.cookChecker.apply(level, pos);
                if(cookBlock != null) {
                    level.setBlock(pos, cookBlock.defaultBlockState(), 19); //Suppress neighbor updates so new fuel doesn't cook immediately
                }
                else level.setBlock(pos, BlocksNF.ASH.get().defaultBlockState(), 3);
            }
            else level.setBlock(pos, BlocksNF.ASH.get().defaultBlockState(), 3);
        }
        else {
            float oldTemp = entity.temperature;
            float targetTemp = block.getTargetTemperature(level, state, pos);
            if(entity.temperature < targetTemp) entity.temperature += 1F;
            else if(entity.temperature > targetTemp) entity.temperature -= 1F;
            if(entity.temperature != oldTemp) {
                TieredHeat heat = block.getHeat(level, pos, state);
                if(heat != TieredHeat.fromTemp(entity.temperature)) {
                    //TODO: extra particles explosion?
                    if(entity.temperature < oldTemp) block.decreaseHeat(level, state, pos);
                    else block.increaseHeat(level, state, pos);
                }
                entity.setChanged();
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cookTicks = tag.getInt("cookTicks");
        burnTicks = tag.getInt("burnTicks");
        temperature = tag.getFloat("temperature");
        structureTempBonus = tag.getFloat("structureTempBonus");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("cookTicks", cookTicks);
        tag.putInt("burnTicks", burnTicks);
        tag.putFloat("temperature", temperature);
        tag.putFloat("structureTempBonus", structureTempBonus);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
}
