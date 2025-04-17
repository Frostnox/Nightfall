package frostnox.nightfall.block.block.strangesoil;

import frostnox.nightfall.registry.forge.BlockEntitiesNF;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class StrangeSoilBlockEntity extends BlockEntity {
    public @Nullable ResourceLocation lootTableLoc;
    public long lootTableSeed;

    public StrangeSoilBlockEntity(BlockPos pos, BlockState pBlockState) {
        super(BlockEntitiesNF.STRANGE_SOIL.get(), pos, pBlockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("lootTable", CompoundTag.TAG_STRING)) {
            lootTableLoc = ResourceLocation.parse(tag.getString("lootTable"));
            lootTableSeed = tag.getLong("lootTableSeed");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(lootTableLoc != null) {
            tag.putString("lootTable", lootTableLoc.toString());
            tag.putLong("lootTableSeed", lootTableSeed);
        }
    }
}
