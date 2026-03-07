package frostnox.nightfall.block.block.meltedmetal;

import frostnox.nightfall.block.Metal;
import frostnox.nightfall.block.TieredHeat;
import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MeltedMetalBlockEntity extends BlockEntity {
    public float temperature = TieredHeat.YELLOW.getBaseTemp() - 1;
    public BlockState originalState = BlocksNF.METAL_BLOCKS.get(Metal.COPPER).get().defaultBlockState();
    public int units = 400;
    public boolean hasSlag;

    public MeltedMetalBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesNF.MELTED_METAL.get(), pPos, pBlockState);
    }

    protected MeltedMetalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeltedMetalBlockEntity entity) {

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        temperature = tag.getFloat("temperature");
        originalState = MeltedMetalBlock.stateById(tag.getInt("originalState"));
        units = tag.getInt("units");
        hasSlag = tag.getBoolean("hasSlag");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("temperature", temperature);
        tag.putInt("originalState", MeltedMetalBlock.getId(originalState));
        tag.putInt("units", units);
        tag.putBoolean("hasSlag", hasSlag);
    }
}
